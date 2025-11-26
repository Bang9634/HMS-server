package com.team3.repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final JsonFileManager<User> fileManager;
    
    /**
     * JsonUserRepository 생성자
     * 
     * @param jsonFileManager 의존성 주입용 jsonFileManager 객체
     */
    public JsonUserRepository(JsonFileManager<User> jsonFileManager) {
        logger.info("JsonUserRepository 초기화 시작...");
        this.fileManager = jsonFileManager;
        logger.info("JsonUserRepository 초기화 완료");
    }
    
    /**
     * 사용자 객체를 Json 파일에 저장한다.
     * 
     * @param user 저장할 사용자 객체
     */
    @Override
    public boolean save(User user) {
        logger.info("사용자 저장: {}", user.getUserId());
        
        List<User> users = fileManager.readAll();
        
        // 기존 사용자 업데이트 또는 새 사용자 추가

        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUserId().equals(user.getUserId())) {
                logger.debug("기존 사용자 존재: {}", user.getUserId());
                return false;
            }
        }

        users.add(user);
        logger.debug("새 사용자 추가: {}", user.getUserId());
        countUsers();
        
        fileManager.writeAll(users);
        return true;
    }

    /**
     * 사용자 객체를 Json 파일에 업데이트한다.
     * 
     * @param user 업데이트할 사용자 객체
     */
    @Override
    public boolean update(User user) {
        logger.info("사용자 업데이트: {}", user.getUserId());
        
        List<User> users = fileManager.readAll();
        
        // 기존 사용자 업데이트
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUserId().equals(user.getUserId())) {
                users.set(i, user);
                logger.debug("기존 사용자 업데이트: {}", user.getUserId());
                fileManager.writeAll(users);
                return true;
            }
        }
        logger.debug("기존 사용자 존재하지 않음: {}", user.getUserId());
        return false;
    }
    
    /**
     * 사용자가 존재하는 지 아이디로 조회한다.
     * <p>
     * 사용자 조회에 성공하면, 해당 사용자 객체를 반환하며,
     * 그렇지 않으면 null을 반환한다.
     * </p>
     * 
     * @param userId 조회를 시도할 사용자 아이디
     * @return 조회한 사용자 객체
     */
    @Override
    public Optional<User> findById(String userId) {
        logger.debug("사용자 조회: userId={}", userId);
        
        return fileManager.readAll().stream()
            .filter(user -> user.getUserId().equals(userId))
            .findFirst();
    }
    
    /**
     * 전체 사용자를 조회한다.
     * 
     * @return 전체 사용자 객체를 담은 List<User>
     */
    @Override
    public List<User> findAll() {
        logger.debug("전체 사용자 조회");
        return fileManager.readAll();
    }
    
    /**
     * 사용자 아이디를 조회해 삭제하고, 결과를 반환한다.
     * 
     * @param userId 삭제할 사용자 아이디
     * @return 사용자 삭제 성공 여부
     */
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
    
    /**
     * 사용자 존재 여부를 반환한다.
     * 
     * @param userId 존재 여부를 확인할 사용자 아이디
     * @return 존재하면 true, 그렇지 않으면 false
     */
    @Override
    public boolean existsById(String userId) {
        return findById(userId).isPresent();
    }

    /**
     * 관리자 역할 사용자를 조회하고 반환한다.
     * <p>
     * 만약 관리자 역할의 사용자가 여러 명이 존재하면,
     * 파일에서 가장 처음으로 탐색된 관리자 역할 사용자를 반환한다.
     * 존재하지 않으면 null을 반환한다.
     * </p>
     * 
     * @return 파일에 존재하는 관리자 역할 사용자
     */
    @Override
    public Optional<User> findAdmin() {
        logger.debug("관리자 사용자 조회");
        
        return fileManager.readAll().stream()
            .filter(User::isAdmin)
            .findFirst();
    }

    /**
     * 관리자 역할 사용자의 수를 반환한다.
     * 
     * @return 관리자 역할 사용자의 수
     */
    @Override
    public long countAdmins() {
        long count = fileManager.readAll().stream()
            .filter(User::isAdmin)
            .count();
        
        logger.debug("관리자 사용자 수: {}", count);
        return count;
    }

    /**
     * 관리자 역할 사용자의 수를 반환한다.
     * 
     * @return 관리자 역할 사용자의 수
     */
    @Override
    public long countUsers() {
        long count = fileManager.readAll().stream().count();
        
        logger.debug("사용자 수: {}", count);
        return count;
    }

}
