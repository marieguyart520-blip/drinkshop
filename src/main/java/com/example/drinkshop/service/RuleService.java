package com.example.drinkshop.service;

import com.example.drinkshop.dto.OrderRequests.Item;
import com.example.drinkshop.exception.ApiException;
import com.example.drinkshop.repository.PosRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/** 後端點餐防呆：前端會先調整選項，但送單時仍以這裡的驗證為準。 */
@Service
public class RuleService {
    private final PosRepository repo;
    private final ObjectMapper json;
    public RuleService(PosRepository repo,ObjectMapper json) { this.repo=repo; this.json=json; }
    // 相容資料庫驅動回傳的 JSON 字串／位元組，以及外層又包一層字串的舊資料。
    public JsonNode parse(Object value) {
        try {
            String text=value instanceof byte[] bytes ? new String(bytes, StandardCharsets.UTF_8) : value.toString();
            JsonNode node=json.readTree(text);
            return node.isTextual()?json.readTree(node.asText()):node;
        } catch (Exception ex) { throw ApiException.conflict("資料庫中的配方或規則格式錯誤"); }
    }
    public String write(Object value) {
        try { return json.writeValueAsString(value); }
        catch (Exception ex) { throw new IllegalStateException("Cannot encode JSON",ex); }
    }
    public void validate(Item item,Map<String,Object> product) {
        if (!PosRepository.flag(product,"on_sale")) throw ApiException.conflict("飲料已下架");
        if (item.temperature()!=com.example.drinkshop.dto.OrderRequests.Temperature.COLD) {
            if (!PosRepository.flag(product,"hot_allowed")) throw ApiException.bad("此飲料不提供溫熱飲");
            if (item.ice()!=com.example.drinkshop.dto.OrderRequests.Ice.NONE) throw ApiException.bad("溫熱飲不可加冰");
        }
        if (item.toppingIds().stream().distinct().count()!=item.toppingIds().size()) throw ApiException.bad("同一種額外加料不可重複");
        // condition_json 決定何時套用；action_json 決定禁止、固定、上限或互斥的內容。
        // AUTO_ADJUST 在後端是檢查固定值是否正確，不會偷偷替前端改掉送單內容。
        for (var row:repo.rules()) {
            var condition=parse(row.get("condition_json"));
            if (!matches(condition,item,product)) continue;
            var action=parse(row.get("action_json"));
            String message=row.get("message").toString();
            switch (row.get("rule_type").toString()) {
                case "FORBID" -> {
                    if (!action.path("forbidToppingIds").isArray()) throw ApiException.conflict("禁用規則缺少加料清單");
                    for (var id:action.get("forbidToppingIds")) if (item.toppingIds().contains(id.asLong())) throw ApiException.bad(message);
                }
                case "AUTO_ADJUST" -> {
                    if (!action.path("set").isObject()) throw ApiException.conflict("自動調整規則缺少固定值");
                    var fields=action.get("set").fields();
                    while(fields.hasNext()) {
                        var field=fields.next();
                        if (!value(field.getKey(),item).equals(field.getValue().asText())) throw ApiException.bad(message);
                    }
                }
                case "MAX_COUNT" -> {
                    if (!"toppingIds".equals(action.path("field").asText()) || !action.path("maxDistinct").canConvertToInt())
                        throw ApiException.conflict("加料上限規則格式錯誤");
                    if (item.toppingIds().size()>action.get("maxDistinct").asInt()) throw ApiException.bad(message);
                }
                case "MUTEX" -> {
                    if (!action.path("toppingIds").isArray()) throw ApiException.conflict("互斥規則缺少加料群組");
                    int count=0;
                    for (var id:action.get("toppingIds")) if(item.toppingIds().contains(id.asLong())) count++;
                    if(count>1) throw ApiException.bad(message);
                }
                default -> throw ApiException.conflict("不支援的衝突規則");
            }
        }
    }
    // 同一個條件物件內的條件必須全部符合，才會啟用該條規則。
    private boolean matches(JsonNode condition,Item item,Map<String,Object> product) {
        if (!condition.isObject() || condition.isEmpty()) throw ApiException.conflict("規則條件不可空白");
        boolean matches=true;
        var fields=condition.fields();
        while(fields.hasNext()) {
            var field=fields.next();
            switch(field.getKey()) {
                case "always" -> matches &= field.getValue().asBoolean();
                case "category" -> matches &= field.getValue().asText().equals(product.get("category"));
                case "temperatureIn" -> {
                    if(!field.getValue().isArray()) throw ApiException.conflict("溫度規則格式錯誤");
                    boolean found=false;
                    for(var v:field.getValue()) found |= v.asText().equals(item.temperature().name());
                    matches &= found;
                }
                default -> throw ApiException.conflict("不支援的規則條件："+field.getKey());
            }
        }
        return matches;
    }
    private String value(String field,Item item) {
        return switch(field) {
            case "temperature" -> item.temperature().name();
            case "sugar" -> item.sugar().name();
            case "ice" -> item.ice().name();
            default -> throw ApiException.conflict("不支援的固定選項："+field);
        };
    }
}
