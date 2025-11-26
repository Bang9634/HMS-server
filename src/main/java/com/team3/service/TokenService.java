package com.team3.service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.team3.model.AuthToken;
import com.team3.model.User;
import com.team3.repository.UserRepository;

/**
 * 인증 토큰 관련 비즈니스 로직을 처리하는 클래스
 * 
 * @author bang9634
 * @since 2025-11-27
 */
public class TokenService {
    private static final Logger logger = LoggerFactory.getLogger(TokenService.class);

    /** 발급된 토큰들을 저장한다. */
    private final Map<String, AuthToken> tokenStore = new ConcurrentHashMap<>();

    /** 토큰 유효시간 60분으로 설정 */
    private static final int TOKEN_VALID_MINUTES = 60;

    private final UserRepository userRepository;

    /**
     * TokenService의 생성자
     * 
     * @param userRepository 의존성 주입
     */
    public TokenService(UserRepository userRepository) {
        this.userRepository = userRepository;
    } 

    /**
     * 토큰을 발급한다.
     * <p>
     * 새로운 토큰을 생성 후, 메모리에 저장한 다음
     * 이를 반환한다.
     * </p>
     * 
     * @param userId 토큰을 발급할 사용자 아이디
     * @return 발급된 토큰
     */
    public AuthToken generateToken(String userId) {
        AuthToken token = new AuthToken(userId, TOKEN_VALID_MINUTES);
        tokenStore.put(token.getToken(), token);
        return token;
    }

    /**
     * 토큰을 삭제한다.
     * <p>
     * 메모리에서도 삭제되며 더 이상 유효하지 않은 토큰이 된다.
     * </p>
     * 
     * @param token 삭제할 토큰
     * @return 삭제된 토큰
     */
    public AuthToken removeToken(String token) {
        return tokenStore.remove(token);
    }
    
    /**
     * 토큰의 유효성을 검증한다.
     * <p>
     * 토큰의 유효성을 검증하고, 유효하지 않은 토큰이면 빈 Optional<User>를 반환한다.
     * 유효한 토큰이라면 해당 사용자의 정보가 담긴 Optional<User> 객체를 반환한다.
     * </p>
     * @param token 검증할 토큰
     * @return 유효하면 해당 Optional 사용자 객체를, 그렇지 않으면 빈 Optional 사용자 객체를 반환
     */
    public Optional<User> validateToken(String token) {
        logger.debug("토큰 검증: {}", token);

        if (token == null) {
            logger.warn("유효하지 않는 토큰: {}", token);
            return Optional.empty();
        }
        
        AuthToken authToken = tokenStore.get(token);
        
        if (authToken == null) {
            logger.warn("존재하지 않는 토큰: {}", token);
            return Optional.empty();
        }
        
        if (authToken.isExpired()) {
            logger.warn("만료된 토큰: {}", token);
            tokenStore.remove(token);
            return Optional.empty();
        }
        return userRepository.findById(authToken.getUserId());
    }
}
