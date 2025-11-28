package com.team3.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.team3.model.AuthToken;
import com.team3.model.User;
import com.team3.repository.UserRepository;
import com.team3.util.PasswordUtil;


/**
 * 사용자 관련 비즈니스 로직을 처리하는 서비스 클래스
 * <p>
 * 이 클래스는 사용자 관리의 핵심 비즈니스 로직을 담당한다.
 * </p>
 * 
 * @author bang9634
 * @since 2025-11-10
 */
public class UserService {

    /** SLF4J 로거 인스턴스 - 비즈니스 로직 처리 과정 로깅 */
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final TokenService tokenService;

    // 기본 관리자 계정 설정
    private static final String DEFAULT_ADMIN_ID = "admin";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin";
    private static final String DEFAULT_ADMIN_NAME = "admin";
    
    /**
     * userRepository를 주입받는 생성자
     * <p>
     * 의존성 주입을 통해 userRepository 구현체를 받아 초기화한다.
     * </p>
     * 
     * @param userRepository 의존성 주입할 userRepository 객체
     * 
     * @throws IllegalArgumentException userRepository가 null인 경우
     */
    public UserService(UserRepository userRepository, TokenService tokenService) {
        if (userRepository == null || tokenService == null) {
            throw new IllegalArgumentException("의존성은 null일 수 없습니다.");
        }
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        ensureAdminExists();
    }

    /**
     * 관리자 계정이 존재하는지 확인하고 없으면 생성한다.
     */
    private void ensureAdminExists() {
        logger.info("관리자 계정 확인 중...");
        
        long adminCount = userRepository.countAdmins();
        
        if (adminCount == 0) {
            logger.warn("관리자 계정이 존재하지 않습니다. 기본 관리자 계정을 생성합니다.");
            
            User admin = new User(
                DEFAULT_ADMIN_ID, 
                DEFAULT_ADMIN_PASSWORD, 
                DEFAULT_ADMIN_NAME,
                User.Role.ADMIN
            );
            
            addUser(admin);

            logger.info("기본 관리자 계정이 생성되었습니다");
            logger.info("ID: {}", DEFAULT_ADMIN_ID);
            logger.info("비밀번호: {}", DEFAULT_ADMIN_PASSWORD);
            logger.info("보안을 위해 비밀번호를 변경해주세요!");
            
        } else if (adminCount == 1) {
            Optional<User> admin = userRepository.findAdmin();
            logger.info("관리자 계정 확인됨: {}", admin.get().getUserId());
        } else {
            logger.error("경고: 관리자 계정이 {}개 존재합니다! (정상: 1개)", adminCount);
            System.err.println("경고: 관리자 계정이 " + adminCount + "개 존재합니다!");
        }
    }

    /**
     * 사용자 로그인 인증을 처리하는 메서드
     * <p>
     * 제공된 사용자 ID와 비밀번호를 검증하여 로그인 성공 여부를 반환한다.
     * </p>
     * 
     * 
     * @param userId 로그인할 사용자 ID
     * @param password 클라이언트에서 전송한 평문 비밀번호
     * @return 인증 성공 시 AuthToken 반환
     * 
     * @apiNote 보안을 위해 사용자 존재 여부와 비밀번호 오류를 구분하지 않음
     */
    public AuthToken login(String userId, String password) {
        logger.info("로그인 시도: userId={}", userId);

        if (userId == null || password == null) {
            logger.warn("로그인 시도 - null 파라미터: userId={}, password={}", userId, password != null);
            throw new IllegalArgumentException("사용자 아이디 또는 비밀번호가 null입니다.");
        }

        // 사용자 확인
        logger.debug("사용자 존재 유무 확인 시도: userId={}", userId);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다"));
        
        // 비밀번호 확인
        logger.debug("비밀번호 검사 시도");
        if (!PasswordUtil.verify(password, user.getPassword())) {
            logger.warn("로그인 실패 - 잘못된 비밀번호: userId={}", userId);
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다");
        }

        // 토큰 발급
        logger.debug("인증 성공 토큰 발급 시도");
        AuthToken token = tokenService.generateToken(userId);
        
        logger.info("로그인 성공: userId={}, token={}", userId, token.getToken());
        return token;
    }

    /**
     * 토큰을 무효화하여 로그아웃을 한다.
     * 
     * @param token 무효화할 토큰
     */
    public void logout(String token) {
        logger.info("로그아웃 요청: token={}", token);
        if (token == null) {
            logger.warn("null 토큰으로 로그 아웃 시도");
            return;
        }
        AuthToken removed = tokenService.removeToken(token);
        if (removed != null) {
            logger.info("로그아웃 완료: userId={}", removed.getUserId());
        } else {
            logger.warn("유효하지 않은 토큰으로 로그아웃 시도: {}", token);
        }
    }

    /**
     * ID 사용 가능 여부 확인한다.
     * 
     * @param userId 사용 가능 여부를 확인할 아이디
     * @return 사용 가능 여부를 true, false로 반환한다.
     */
    public boolean isUserIdAvailable(String userId) {
        return !userRepository.existsById(userId);
    }

    /**
     * 모든 사용자 정보를 반환한다.
     * 
     * @return 모든 사용자 정보
     */
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    /**
     * 사용자를 추가한다.
     * 
     * @param user 추가할 사용자 객체
     * @return 성공 여부
     */
    public boolean addUser(User user) {
        user.setPassword(PasswordUtil.hash(user.getPassword()));
        return userRepository.save(user);
    }

    public Optional<User> deleteUser(String userId) {
        return userRepository.deleteUser(userId);
    }
}