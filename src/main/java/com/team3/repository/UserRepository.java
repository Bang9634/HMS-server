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
     * 
     * @param user 저장할 사용자 객체
     */
    void save(User user);
    
    /**
     * ID로 사용자 찾기
     * 
     * @param userId 탐색할 사용자 아이디
     */
    Optional<User> findById(String userId);
    
    /**
     * 모든 사용자 조회
     * 
     * @return 모든 사용자 객체를 List에 저장해 반환한다.
     */
    List<User> findAll();
    
    /**
     * 사용자 삭제
     * 
     * @param userId 삭제할 사용자 아이디
     * @return 삭제 성공 여부를 true, false로 반환한다.
     */
    boolean deleteById(String userId);
    
    /**
     * ID 존재 여부 확인
     * 
     * @param userId 존재 여부를 확인할 사용자 아이디
     * @return 존재 여부를 true, false로 반환한다.
     */
    boolean existsById(String userId);
    
    /**
     * 관리자 권한 사용자를 탐색한다.
     * <p>
     * 관리자 권한 사용자가 존재하지 않으면, null을 반환할 수 있다.
     * </p>
     * 
     * @return 관리자 권한 사용자 객체를 반환하며, 존재하지 않으면 null을 반환한다.
     */
    Optional<User> findAdmin();

    /**
     * 관리자 권한 사용자의 수를 반환한다.
     * 
     * @return 관리자 권한 사용자가 몇 명인지 반환한다.
     */
    long countAdmins();
}