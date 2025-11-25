package com.team3.repository;

import java.util.List;
import java.util.Optional;

import com.team3.model.User;

/**
 * 사용자 데이터 저장소 인터페이스
 * <p>
 * 데이터 저장 방식에 독립적인 추상 인터페이스를 제공한다.
 * JSON, 데이터베이스 등 다양한 구현체를 사용할 수 있다.
 * JSON 파일 저장 방식만을 사용하지만 테스트를 위해서
 * 인터페이스를 유지한다.
 * </p>
 * 
 * @author bang9634
 * @since 2025-11-23
 */
public interface UserRepository {
    
    /**
     * 사용자 저장
     */
    void save(User user);
    
    /**
     * ID로 사용자 찾기
     */
    Optional<User> findById(String userId);
    
    /**
     * 모든 사용자 조회
     */
    List<User> findAll();
    
    /**
     * 사용자 삭제
     */
    boolean deleteById(String userId);
    
    /**
     * ID 존재 여부 확인
     */
    boolean existsById(String userId);
    
    Optional<User> findAdmin();

    long countAdmins();
}