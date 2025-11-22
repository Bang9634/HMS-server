package com.team3.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.team3.dto.request.LoginRequest;
import com.team3.service.UserService;

/**
 * 사용자 관련 HTTP 요청 처리 컨트롤러
 * <p>
 * 사용자 인증 및 관리와 관련된 모든 HTTP 엔드포인트를 담당한다.
 * 회원가입, 로그인, 중복 확인 등의 기능을 제공한다.
 * </p>
 * <p>
 * CORS 처리는 CorsFilter에서 담당하므로 해당 클래스는 비즈니스 로직만 다룬다.
 * </p>
 * 
 * TODO:전체적으로 손봐야함. 
 * 
 * @author bang9634
 * @since 2025-11-10
 */
public class UserController {

    /** SLF4J Logger 인스턴스 - 요청 처리 로그를 기록 */
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    /** 사용자 비즈니스 로직 처리 서비스 */
    private final UserService userService;

    /** JSON을 다루기 위한 Gson 인스턴스 */
    private final Gson gson;
    
    /**
     * UserController 객체를 생성하는 생성자
     * 
     * @param userService 사용자 관련 비즈니스 로직을 처리하는 서비스 객체
     */
    public UserController(UserService userService) {
        this.userService = userService;
        this.gson = new Gson();
    }
    
    /**
     * 로그인 인증 요청을 처리하는 메서드
     * <p>
     * 사용자가 제공한 ID와 비밀번호를 검증하여 인증을 수행한다.
     * 성공 시 사용자 정보를 반환하고, 실패 시 적절한 오류 메시지를 제공한다.
     * </p>
     * 
     * 
     * @param exchange HTTP 요청/응답 처리를 위한 교환 객체
     * @throws IOException 네트워크 I/O 처리 중 오류가 발생한 경우
     * @author wnwoghd
     * @apiNote LoginRequest DTO를 사용하여 타입 안정성을 보장함
     */
    public void handleLogin(HttpExchange exchange) throws IOException {
        String clientIP = exchange.getRemoteAddress().getAddress().getHostAddress();

        // HTTP POST 메서드 방식만 허용
        if (!"POST".equals(exchange.getRequestMethod())) {
            logger.warn("잘못된 HTTP 메서드: method={}, clientIP={}", exchange.getRequestMethod(), clientIP);
            sendErrorResponse(exchange, 405, "Method Not Allowed");
            return;
        }
        
        try {
            // request의 body에서 로그인 정보 추출
            String requestBody = readRequestBody(exchange);
            logger.debug("로그인 요청 본문 수신: length={}", requestBody.length());
            
            // LoginRequest DTO로 변환
            LoginRequest loginRequest = gson.fromJson(requestBody, LoginRequest.class);
            
            if (loginRequest == null) {
                logger.warn("잘못된 JSON: clientIP={}", clientIP);
                sendErrorResponse(exchange, 400, "Invalid JSON format");
                return;
            }
            
            String userId = loginRequest.getUserId();
            String password = loginRequest.getPassword();

            // 필수 필드 검증
            if (userId == null || userId.trim().isEmpty() || 
                password == null || password.trim().isEmpty()) {
                logger.warn("필수 필드 누락: userId={}, clientIP={}", userId, clientIP);
                sendErrorResponse(exchange, 400, "userId와 password는 필수 입력 항목입니다.");
                return;
            }

            logger.info("로그인 인증 시도: userId={}, clientIP={}", userId, clientIP);
            
            // UserService를 통한 로그인 검증
            boolean loginSuccess = userService.login(userId, password);
            
            // response 객체 생성
            // TODO: 나중에는 Map에 넣었다가 다시 Json으로 파싱하는 괴상한 로직 수정하기...
            // 테스트용으로 대충 복붙
            Map<String, Object> response = new HashMap<>();
            response.put("success", loginSuccess);
            response.put("message", loginSuccess ? "로그인 성공" : "아이디 또는 비밀번호가 올바르지 않습니다.");
            
            // 로그인 성공 시 response에 사용자 아이디를 포함
            if (loginSuccess) {
                response.put("userId", userId);
                logger.info("로그인 성공: userId={}, clientIP={}", userId, clientIP);
            } else {
                logger.warn("로그인 실패: userId={}, clientIP={}", userId, clientIP);
            }

            // response를 JSON 형태로 파싱하여 클라이언트에게 전송
            String jsonResponse = gson.toJson(response);
            sendJsonResponse(exchange, loginSuccess ? 200 : 401, jsonResponse);
            
        } catch (JsonSyntaxException e) {
            logger.error("JSON 파싱 오류: clientIP={}", clientIP, e);
            sendErrorResponse(exchange, 400, "Invalid JSON format: " + e.getMessage());
        } catch (IOException | RuntimeException e) {
            logger.error("로그인 처리 오류: clientIP={}", clientIP, e);
            sendErrorResponse(exchange, 500, "Internal server error");
        }
    }
  
    
    /**
     * JSON 형태의 성공 응답을 클라이언트에게 전송하는 헬퍼 메서드
     * <p>
     * HTTP 헤더를 적절히 설정하고 JSON 데이터를 UTF-8 인코딩으로 전송한다.
     * Content-Type은 application/json으로 설정되어 클라이언트가 응답을 올바르게 파싱할 수 있게 한다.
     * </p>
     * 
     * @param exchange HTTP 요청/응답 처리를 위한 교환 객체
     * @param statusCode HTTP 응답 상태 코드 (200, 400, 500 등)
     * @param jsonResponse 클라이언트에게 전송할 JSON 문자열
     * @throws IOException 응답 전송 중 네트워크 오류가 발생한 경우
     */
    private void sendJsonResponse(HttpExchange exchange, int statusCode, String jsonResponse) throws IOException {
        // HTTP 응답 헤더 설정
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        
        // JSON 문자열을 UTF-8 바이트 배열로 변환
        byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);

        // HTTP 응답 헤더 전송 (상태 코드와 Content-Length를 포함)
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        
        // HTTP 응답 body 전송 (try-with-resources로 자동 스트림 닫기)
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
        
        logger.debug("JSON 응답 전송 완료: statusCode={}, length={} bytes", statusCode, responseBytes.length);
    }
    
     /**
     * 표준화된 오류 응답을 JSON 형태로 클라이언트에게 전송하는 헬퍼 메서드
     * <p>
     * 모든 오류 응답은 동일한 구조를 가지도록 표준화되어 있다.
     * 클라이언트는 success 필드를 통해 요청의 성공 여부를 쉽게 판단할 수 있다.
     * </p>
     * 
     * <h4>오류 응답 형식:</h4>
     * <pre>{@code
     * {
     *   "success": false,
     *   "error": "오류 메시지",
     *   "timestamp": 1699123456789
     * }
     * }</pre>
     * 
     * @param exchange HTTP 요청/응답 처리를 위한 교환 객체
     * @param statusCode HTTP 오류 상태 코드 (400, 404, 405, 500 등)
     * @param message 클라이언트에게 전달할 오류 메시지
     * @throws IOException 응답 전송 중 네트워크 오류가 발생한 경우
     */
    private void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        // 표준 오류 reponse 구조 생성
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);                 // 요청 실패 표시
        errorResponse.put("error", message);                        // 사용자에게 전달할 오류 메시지
        errorResponse.put("timestamp", System.currentTimeMillis()); // 오류 발생 시각 (ms)
        
        // 오류 응답을 JSON으로 변환
        String jsonResponse = gson.toJson(errorResponse);

        logger.warn("오류 응답 전송: statusCode={}, message={}", statusCode, message);

        // JSON 응답 전달
        sendJsonResponse(exchange, statusCode, jsonResponse);
    }
    
    /**
     * HTTP 요청 본문에서 텍스트 데이터를 읽어오는 헬퍼 메서드
     * <p>
     * InputStream으로부터 모든 바이트를 읽어와서 UTF-8 문자열로 변환한다.
     * try-with-resources 구문을 사용하여 스트림이 자동으로 해제되도록 보장한다.
     * </p>
     * 
     * @param exchange HTTP 요청/응답 처리를 위한 교환 객체
     * @return 요청 본문의 전체 내용을 담은 UTF-8 문자열
     * @throws IOException 요청 본문 읽기 중 I/O 오류가 발생한 경우
     */
    private String readRequestBody(HttpExchange exchange) throws IOException {
        // try-with-resources로 InputStream 자동 해제
        try (InputStream is = exchange.getRequestBody()) {
            // 모든 바이트를 읽어와서 UTF-8 문자열로 변환
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}