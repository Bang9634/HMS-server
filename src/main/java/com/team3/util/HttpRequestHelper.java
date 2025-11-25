package com.team3.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.HttpExchange;

/**
 * HTTP 요청 처리 유틸리티
 * 
 * @author bang9634
 * @since 2025-11-23
 */
public class HttpRequestHelper {
    
    private HttpRequestHelper() {
        throw new AssertionError("유틸리티 클래스는 인스턴스화할 수 없습니다");
    }
    
    /**
     * HTTP 요청 본문 읽기
     */
    public static String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
    
    /**
     * Authorization 헤더에서 Bearer 토큰 추출
     */
    public static String extractBearerToken(HttpExchange exchange) {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        
        return null;
    }
    
    /**
     * 쿼리 파라미터 추출
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