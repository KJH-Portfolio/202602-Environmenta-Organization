package com.kh.spring.util;

import java.io.File;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import net.coobird.thumbnailator.Thumbnails;

@Component
public class ThumbnailGenerator {

    @Async
    public void generateThumbnail(File originalFile, String savePath, String fileName) {
        try {
            String thumbnailName = "s_" + fileName;
            File thumbnailFile = new File(savePath + thumbnailName);
            
            Thumbnails.of(originalFile)
                .size(300, 300)
                .toFile(thumbnailFile);
                
        } catch (Exception e) {
            e.printStackTrace();
            // 썸네일 생성 실패는 원본 저장에 영향을 주지 않으므로 로그만 남김
        }
    }
}
