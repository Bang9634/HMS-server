package com.team3.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * JSON 파일 읽기/쓰기를 담당하는 유틸리티 클래스
 * <p>
 * 파일 시스템에 JSON 형식으로 데이터를 저장하고 읽어오는 기능을 제공한다.
 * 스레드 안전성을 보장하기 위해 synchronized를 사용한다.
 * </p>
 * 
 * @param <T> 저장/로드할 데이터의 타입
 * 
 * @author bang9634
 * @since 2025-11-23
 */
public class JsonFileManager<T> {
    
    private static final Logger logger = LoggerFactory.getLogger(JsonFileManager.class);
    private final Gson gson;
    private final String filePath;
    private final TypeToken<List<T>> typeToken;
    
    /**
     * JsonFileManager 생성자
     * 
     * @param filePath JSON 파일 경로 (예: "data/users.json")
     * @param typeToken 리스트 타입 정보
     */
    public JsonFileManager(String filePath, TypeToken<List<T>> typeToken) {
        this.filePath = filePath;
        this.typeToken = typeToken;
        
        // LocalDateTime 어댑터 등록
        this.gson = new GsonBuilder()
            .setPrettyPrinting()  // 가독성 좋은 JSON 포맷
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())  // ← 추가!
            .create();
        
        ensureFileExists();
    }
    
    /**
     * 파일이 존재하지 않으면 생성한다
     */
    private void ensureFileExists() {
        try {
            Path path = Paths.get(filePath);
            File file = path.toFile();
            
            // 디렉토리 생성
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                if (parentDir.mkdirs()) {
                    logger.info("데이터 디렉토리 생성: {}", parentDir.getAbsolutePath());
                }
            }
            
            // 파일 생성 (빈 배열로 초기화)
            if (!file.exists()) {
                writeToFile(new ArrayList<>());
                logger.info("JSON 파일 생성: {}", filePath);
            }
            
        } catch (IOException e) {
            logger.error("파일 초기화 실패: {}", filePath, e);
            throw new RuntimeException("파일 초기화 실패", e);
        }
    }
    
    /**
     * JSON 파일에서 모든 데이터를 읽어온다
     * 
     * @return 데이터가 담긴 ArrayList
     */
    public synchronized List<T> readAll() {
        logger.debug("JSON 파일 읽기: {}", filePath);
        
        try (FileReader reader = new FileReader(filePath, StandardCharsets.UTF_8)) {
            List<T> data = gson.fromJson(reader, typeToken.getType());
            
            if (data == null) {
                logger.warn("JSON 파일이 비어있음, 빈 리스트 반환: {}", filePath);
                return new ArrayList<>();
            }
            
            logger.debug("데이터 로드 완료: {} 건", data.size());
            return data;
            
        } catch (IOException e) {
            logger.error("JSON 파일 읽기 실패: {}", filePath, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 데이터를 JSON 파일에 저장한다
     * 
     * @param data 저장할 데이터 리스트
     */
    public synchronized void writeAll(List<T> data) {
        logger.debug("JSON 파일 쓰기: {} ({}건)", filePath, data.size());
        
        try {
            writeToFile(data);
            logger.info("데이터 저장 완료: {} 건", data.size());
            
        } catch (IOException e) {
            logger.error("JSON 파일 쓰기 실패: {}", filePath, e);
            throw new RuntimeException("데이터 저장 실패", e);
        }
    }
    
    /**
     * 실제 파일에 쓰기 (내부 메서드)
     * 
     * @param data 실제 파일에 쓸 데이터 리스트
     */
    private void writeToFile(List<T> data) throws IOException {
        String json = gson.toJson(data);
        
        try (FileWriter writer = new FileWriter(filePath, StandardCharsets.UTF_8)) {
            writer.write(json);
        }
    }
    
    /**
     * 파일 경로를 반환한다.
     * @return 파일 경로
     */
    public String getFilePath() {
        return filePath;
    }
}

