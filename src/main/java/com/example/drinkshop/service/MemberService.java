package com.example.drinkshop.service;

import com.example.drinkshop.dto.MemberRequests.*;
import com.example.drinkshop.dto.RouteDtos.DeliveryRoute;
import com.example.drinkshop.entity.Member;
import com.example.drinkshop.exception.ApiException;
import com.example.drinkshop.repository.MemberRepository;
import com.example.drinkshop.repository.PosRepository;
import jakarta.persistence.EntityManager;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;
import static com.example.drinkshop.dto.MemberRequests.PHONE_PATTERN;

@Service
public class MemberService {
    private final MemberRepository members;
    private final PosRepository pos;
    private final OrderService orders;
    private final EntityManager em;
    private final HereService here;

    public MemberService(MemberRepository members, PosRepository pos, OrderService orders, EntityManager em,
            HereService here) {
        this.members = members;
        this.pos = pos;
        this.orders = orders;
        this.em = em;
        this.here = here;
    }

    @Transactional(readOnly = true)
    public List<Response> search(String keyword) {
        String k = keyword == null ? "" : keyword.trim();
        var found = k.isEmpty() ? members.findTop50ByOrderByCreatedAtDesc()
                : members.findTop50ByPhoneContainingOrNameContainingOrderByPhoneAsc(k, k);
        return found.stream().map(Response::from).toList();
    }

    @Transactional(readOnly = true)
    public Response get(String phone) {
        return Response.from(find(phone));
    }

    @Transactional
    public Response create(Create request, String username) {
        orders.actor(username);
        String phone = request.phone().trim();
        if (members.existsById(phone))
            throw ApiException.conflict("此電話已經是會員");
        checkLocation(request.lat(), request.lng());
        var member = new Member(phone, request.name().trim());
        apply(member, request.address(), request.lat(), request.lng(), request.note());
        em.persist(member);
        em.flush();
        em.refresh(member); // 取回資料庫自動填入的 created_at / updated_at
        return Response.from(member);
    }

    @Transactional
    public Response update(String phone, Update request, String username) {
        orders.actor(username);
        var member = find(phone);
        checkLocation(request.lat(), request.lng());
        boolean addressChanged = !Objects.equals(member.getAddress(), blankToNull(request.address()));
        // BigDecimal 要用 compareTo 比數值：資料庫回傳 25.0330000，前端送回 25.033，equals 會因小數位數不同判成不相等
        boolean sameCoordinates = sameValue(member.getLat(), request.lat()) && sameValue(member.getLng(), request.lng());
        member.setName(request.name().trim());
        if (addressChanged && sameCoordinates) {
            // 地址改了、座標卻是舊的 → 舊座標作廢，下次點開會用新地址重新 geocode
            apply(member, request.address(), null, null, request.note());
        } else if (sameCoordinates) {
            var source = member.getGeoSource(); // 座標沒動 → 來源也不動
            apply(member, request.address(), request.lat(), request.lng(), request.note());
            member.setGeoSource(source);
        } else {
            apply(member, request.address(), request.lat(), request.lng(), request.note());
        }
        em.flush();
        em.refresh(member);
        return Response.from(member);
    }

    @Transactional
    public void delete(String phone, String username) {
        var staff = orders.actor(username);
        if (!Set.of("MANAGER", "HQ").contains(staff.level()))
            throw new ApiException(HttpStatus.FORBIDDEN, "只有店長或總部可以刪除會員");
        var member = find(phone);
        long orderCount = PosRepository.number(
                pos.one("SELECT COUNT(*) AS c FROM pos_order WHERE member_phone=?", member.getPhone()), "c");
        if (orderCount > 0)
            throw ApiException.conflict("此會員已有訂單紀錄，不能刪除");
        members.delete(member);
    }

    /**
     * 會員外送路線。沒有座標時用地址向 HERE 計算並存回（GEOCODE，每日凌晨清除）；
     * 已有座標（當天算過的 GEOCODE 或店員設定的 MANUAL）就直接使用。
     */
    @Transactional
    public DeliveryRoute route(String phone) {
        var member = find(phone);
        boolean needsReview = false;
        String label = member.getAddress();
        if (member.getLat() == null) {
            if (member.getAddress() == null)
                throw ApiException.bad("此會員沒有地址，請先補上地址或在地圖上設定位置");
            var geo = here.geocode(member.getAddress());
            member.setLat(geo.lat());
            member.setLng(geo.lng());
            member.setGeoSource(Member.GeoSource.GEOCODE);
            em.flush();
            needsReview = geo.needsReview();
            label = geo.label();
        }
        var route = here.route(member.getLat(), member.getLng());
        return new DeliveryRoute(member.getLat(), member.getLng(), member.getGeoSource(), needsReview,
            label, route.distanceM(), route.polylines());
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> orders(String phone) {
        var member = find(phone);
        return pos.rows(
                """
                        SELECT order_id,order_no,business_date,source,payment,payment_status,paid_amount,refunded_amount,total,status,created_at
                        FROM pos_order WHERE member_phone=? ORDER BY order_id DESC LIMIT 50
                        """,
                member.getPhone());
    }

    private Member find(String phone) {
        String p = phone == null ? "" : phone.trim();
        if (!p.matches(PHONE_PATTERN))
            throw ApiException.bad("電話格式不正確");
        return members.findById(p).orElseThrow(() -> ApiException.missing("找不到此會員"));
    }

    private static boolean sameValue(BigDecimal a, BigDecimal b) {
        return a == null ? b == null : b != null && a.compareTo(b) == 0;
    }

    private static void checkLocation(BigDecimal lat, BigDecimal lng) {
        if ((lat == null) != (lng == null))
            throw ApiException.bad("經緯度要同時填寫或同時留空");
    }

    private static void apply(Member member, String address, BigDecimal lat, BigDecimal lng, String note) {
        member.setAddress(blankToNull(address));
        member.setLat(lat);
        member.setLng(lng);
        // 透過會員 API 填入的座標一律視為店員手動設定；HERE 自動計算的座標由 geocode 流程另外寫入 GEOCODE。
        member.setGeoSource(lat == null ? null : Member.GeoSource.MANUAL);
        member.setNote(blankToNull(note));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
