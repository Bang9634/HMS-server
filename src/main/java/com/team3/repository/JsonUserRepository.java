package com.team3.repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.reflect.TypeToken;
import com.team3.model.User;
import com.team3.util.JsonFileManager;

/**
 * JSON 파일 기반 사용자 저장소 구현체
 * <p>
 * JsonFileManager를 사용하여 users.json 파일에 사용자 데이터를 저장한다.
 * </p>
 * 
 * @author bang9634
 * @since 2025-11-23
 */
public class JsonUserRepository implements UserRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(JsonUserRepository.class);
    private static final String DATA_FILE = "data/users.json";
    
    private final JsonFileManager<User> fileManager;
    
    public JsonUserRepository() {
        this.fileManager = new JsonFileManager<>(
            DATA_FILE, 
            new TypeToken<List<User>>() {}
        );
        logger.info("JsonUserRepository 초기화: {}", DATA_FILE);
    }
    
    @Override
    public void save(User user) {
        logger.info("사용자 저장: {}", user.getUserId());
        
        List<User> users = fileManager.readAll();
        
        // 기존 사용자 업데이트 또는 새 사용자 추가
        boolean updated = false;
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUserId().equals(user.getUserId())) {
                users.set(i, user);
                updated = true;
                logger.debug("기존 사용자 업데이트: {}", user.getUserId());
                break;
            }
        }
        
        if (!updated) {
            users.add(user);
            logger.debug("새 사용자 추가: {}", user.getUserId());
        }
        
        fileManager.writeAll(users);
    }
    
    @Override
    public Optional<User> findById(String userId) {
        logger.debug("사용자 조회: userId={}", userId);
        
        return fileManager.readAll().stream()
            .filter(user -> user.getUserId().equals(userId))
            .findFirst();
    }
    
    @Override
    public List<User> findAll() {
        logger.debug("전체 사용자 조회");
        return fileManager.readAll();
    }
    
    @Override
    public boolean deleteById(String userId) {
        logger.info("사용자 삭제: {}", userId);
        
        List<User> users = fileManager.readAll();
        int originalSize = users.size();
        
        List<User> filtered = users.stream()
            .filter(user -> !user.getUserId().equals(userId))
            .collect(Collectors.toList());
        
        if (filtered.size() < originalSize) {
            fileManager.writeAll(filtered);
            logger.info("사용자 삭제 완료: {}", userId);
            return true;
        }
        
        logger.warn("삭제할 사용자 없음: {}", userId);
        return false;
    }
    
    @Override
    public boolean existsById(String userId) {
        return findById(userId).isPresent();
    }

    @Override
    public Optional<User> findAdmin() {
        logger.debug("관리자 사용자 조회");
        
        return fileManager.readAll().stream()
            .filter(User::isAdmin)
            .findFirst();
    }

    @Override
    public long countAdmins() {
        long count = fileManager.readAll().stream()
            .filter(User::isAdmin)
            .count();
        
        logger.debug("관리자 사용자 수: {}", count);
        return count;
    }

}
