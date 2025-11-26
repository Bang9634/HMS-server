package com.team3.service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.team3.model.AuthToken;
import com.team3.model.User;
import com.team3.repository.UserRepository;

public class TokenService {
    private static final Logger logger = LoggerFactory.getLogger(TokenService.class);

    private final Map<String, AuthToken> tokenStore = new ConcurrentHashMap<>();
    /** 토큰 유효시간 60분으로 설정 */
    private static final int TOKEN_VALID_MINUTES = 60;

    private final UserRepository userRepository;

    public TokenService(UserRepository userRepository) {
        this.userRepository = userRepository;
    } 

    public AuthToken generateToken(String userId) {
        AuthToken token = new AuthToken(userId, TOKEN_VALID_MINUTES);
        tokenStore.put(token.getToken(), token);
        return token;
    }

    public AuthToken removeToken(String token) {
        return tokenStore.remove(token);
    }
    
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
