package com.team3.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.HttpExchange;

/**
 * HTTP 요청 처리 유틸리티 클래스
 * 
 * @author bang9634
 * @since 2025-11-23
 * 
 * @apiNote 해당 클래스는 모두 정적 메서드로 이루어져 인스턴스를 생성할 필요가 없음.
 */
public class HttpRequestHelper {
    
    /**
     * 해당 클래스의 생성자 호출 시, 예외를 던진다.
     * 
     * @throws AssertionError 생성자를 호출해 인스턴스화할 경우
     */
    private HttpRequestHelper() {
        throw new AssertionError("유틸리티 클래스는 인스턴스화할 수 없습니다");
    }
    
    /**
     * HTTP 요청 본문을 읽는다.
     * 
     * @param exchange HTTP 요청문
     * @return 요청문의 Body 부분
     */
    public static String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
    
    /**
     * Authorization 헤더에서 토큰을 추출한다.
     * 
     * @param exchange 토큰을 추출할 HTTP문
     * @return authHeader에서 토큰을 반환한다.
     */
    public static String extractBearerToken(HttpExchange exchange) {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        
        return null;
    }
    
    /**
     * 쿼리 파라미터를 추출한다.
     * <p>
     * 파라미터가 존재하지 않으면 null을 반환한다.
     * </p>
     * 
     * @param exchange 쿼리 파라미터를 추출할 HTTP 문
     * @param paramName 추출할 파라미터의 이름
     * @return 추출한 파라미터 값
     */
    public static String getQueryParameter(HttpExchange exchange, String paramName) {
        String query = exchange.getRequestURI().getQuery();
        
        if (query == null || query.isEmpty()) {
            return null;
        }
        
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2 && keyValue[0].equals(paramName)) {
                return keyValue[1];
            }
        }
        
        return null;
    }
}