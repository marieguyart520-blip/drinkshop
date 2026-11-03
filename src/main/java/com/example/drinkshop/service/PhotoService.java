package com.example.drinkshop.service;

import com.example.drinkshop.exception.ApiException;
import com.example.drinkshop.repository.PosRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import javax.imageio.ImageIO;
import java.nio.file.*;
import java.io.IOException;
import java.util.*;

/** 損耗照片存成檔案，資料庫只記檔名；每筆已損耗明細目前限一張。 */
@Service
public class PhotoService {
    private final Path directory;
    private final PosRepository repo;
    private final OrderService orders;
    public PhotoService(PosRepository repo,OrderService orders,@Value("${pos.photo-directory}") String directory) {
        this.repo=repo; this.orders=orders; this.directory=Path.of(directory).toAbsolutePath().normalize();
    }
    @Transactional
    public Map<String,Object> upload(long orderId,long itemId,MultipartFile file,String username) {
        orders.actor(username);
        var item=repo.one("SELECT * FROM order_item WHERE order_id=? AND item_id=? FOR UPDATE",orderId,itemId);
        if(!"WASTED".equals(item.get("status"))) throw ApiException.conflict("只有已授權損耗的明細可上傳照片");
        if(item.get("photo_path")!=null) throw ApiException.conflict("此損耗已有一張照片，不能重複上傳");
        if(file.isEmpty() || file.getSize()>5L*1024*1024) throw ApiException.bad("請選擇 5 MB 以內的 JPEG 或 PNG 圖片");
        String filename=UUID.randomUUID()+".png";
        Path target=directory.resolve(filename);
        // 不只看副檔名或前端 MIME：實際解碼確認 JPEG／PNG，並限制 5 MB、1200 萬像素。
        try(var input=ImageIO.createImageInputStream(file.getInputStream())) {
            if(input==null) throw ApiException.bad("無法讀取圖片");
            var readers=ImageIO.getImageReaders(input);
            if(!readers.hasNext()) throw ApiException.bad("僅支援 JPEG 與 PNG 圖片");
            var reader=readers.next();
            try {
                String format=reader.getFormatName().toLowerCase(Locale.ROOT);
                if(!Set.of("jpeg","jpg","png").contains(format)) throw ApiException.bad("僅支援 JPEG 與 PNG 圖片");
                reader.setInput(input);
                long pixels=(long)reader.getWidth(0)*reader.getHeight(0);
                if(pixels>12000000) throw ApiException.bad("圖片不可超過 1200 萬像素，請先縮小");
                var image=reader.read(0);
                Files.createDirectories(directory);
                // 後端產生隨機檔名並重編碼成 PNG，不沿用使用者檔名或圖片附帶資訊。
                try(var output=Files.newOutputStream(target,StandardOpenOption.CREATE_NEW)) {
                    if(!ImageIO.write(image,"png",output)) throw new IOException("PNG encoder unavailable");
                }
            } finally { reader.dispose(); }
            // 檔案不受資料庫交易控制；若資料庫回滾，要另外刪掉剛存的檔案。
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCompletion(int status) {
                    if(status!=STATUS_COMMITTED) remove(target);
                }
            });
            repo.update("UPDATE order_item SET photo_path=? WHERE item_id=?",filename,itemId);
        } catch(IOException ex) {
            remove(target);
            throw ApiException.bad("圖片無法處理或儲存，請重試");
        }
        return Map.of("photo_url","/api/orders/"+orderId+"/items/"+itemId+"/photo");
    }
    // 只允許讀取指定目錄中的合法檔名，避免任意路徑讀檔。
    public Resource read(long orderId,long itemId) {
        var row=repo.one("SELECT photo_path FROM order_item WHERE order_id=? AND item_id=?",orderId,itemId);
        Object name=row.get("photo_path");
        if(name==null || !name.toString().matches("[a-f0-9-]{36}\\.png")) throw ApiException.missing("尚無可讀取的照片");
        Path file=directory.resolve(name.toString()).normalize();
        if(!file.startsWith(directory) || !Files.isRegularFile(file)) throw ApiException.missing("照片檔案不存在");
        return new FileSystemResource(file);
    }
    private void remove(Path file) {
        try { Files.deleteIfExists(file); }
        catch(IOException ex) { /* Orphan is inaccessible without a committed DB path. */ }
    }
}
