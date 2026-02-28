package com.kh.spring.common.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class FileCacheService {

    private final String DATA_DIR = "C:/easyearth_cache/";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public FileCacheService() {
        // 데이터 디렉토리 생성
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 데이터 저장
    public void save(String fileName, Object data) {
        try {
            //객체를 json으로 직렬화 + 저장
            objectMapper.writeValue(new File(DATA_DIR + fileName), data);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("파일 저장 실패: " + fileName);
        }
    }

    // 데이터 불러오기
    public <T> T load(String fileName, Class<T> clazz) {
        File file = new File(DATA_DIR + fileName);
        if (!file.exists()) {
            return null;
        }
        try {
            //json 데이터를 해당 클래스로 역직렬화
            return objectMapper.readValue(file, clazz);
        } catch (IOException e) {
            //예외출력
            e.printStackTrace();
            System.err.println("파일 읽기 실패: " + fileName);
            return null;
        }
    }
    
    // 파일 존재 여부 확인
    public boolean exists(String fileName) {
        return new File(DATA_DIR + fileName).exists();
    }
}
