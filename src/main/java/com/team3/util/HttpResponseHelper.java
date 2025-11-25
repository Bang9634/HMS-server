package com.team3.util;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;

/**
 * HTTP 응답을 표준화된 형식으로 전송하는 유틸리티 클래스
 * <p>
 * 모든 컨트롤러에서 공통으로 사용하는 응답 전송 로직을 제공한다.
 * JSON 형식의 성공/실패 응답을 일관된 구조로 전송한다.
 * </p>
 * 
 * @author bang9634
 * @since 2025-11-23
 */
public class HttpResponseHelper {
    
    private static final Logger logger = LoggerFactory.getLogger(HttpResponseHelper.class);
    private static final Gson gson = new Gson();
    
    /**
     * private 생성자 - 유틸리티 클래스는 인스턴스화 불가
     */
    private HttpResponseHelper() {
        throw new AssertionError("유틸리티 클래스는 인스턴스화할 수 없습니다");
    }
    
    /**
     * JSON 성공 응답 전송
     * 
     * @param exchange HTTP 교환 객체
     * @param statusCode HTTP 상태 코드
     * @param data 응답 데이터 (Map, 객체 등)
     * @throws IOException 응답 전송 실패 시
     */
    public static void sendJsonResponse(HttpExchange exchange, int statusCode, Object data) 
            throws IOException {
        String jsonResponse = gson.toJson(data);
        byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
        
        logger.debug("JSON 응답 전송: statusCode={}, length={} bytes", statusCode, responseBytes.length);
    }
    
    /**
     * 표준 오류 응답 전송
     * 
     * @param exchange HTTP 교환 객체
     * @param statusCode HTTP 상태 코드
     * @param message 오류 메시지
     * @throws IOException 응답 전송 실패 시
     */
    public static void sendErrorResponse(HttpExchange exchange, int statusCode, String message) 
            throws IOException {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", message);
        errorResponse.put("timestamp", System.currentTimeMillis());
        
        logger.warn("오류 응답 전송: statusCode={}, message={}", statusCode, message);
        
        sendJsonResponse(exchange, statusCode, errorResponse);
    }
    
    /**
     * 표준 성공 응답 전송
     * 
     * @param exchange HTTP 교환 객체
     * @param data 응답 데이터
     * @throws IOException 응답 전송 실패 시
     */
    public static void sendSuccessResponse(HttpExchange exchange, Object data) throws IOException {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        response.put("timestamp", System.currentTimeMillis());
        
        sendJsonResponse(exchange, 200, response);
    }
}