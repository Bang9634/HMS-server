package com.team3.handler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.team3.dto.request.LoginRequest;
import com.team3.model.AuthToken;
import com.team3.model.User;
import com.team3.model.User.Role;
import com.team3.service.TokenService;
import com.team3.service.UserService;
import com.team3.util.HttpRequestHelper;
import com.team3.util.HttpResponseHelper;
import com.team3.util.LocalDateTimeAdapter;

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
 * @author bang9634
 * @since 2025-11-10
 */
public class UserHandler implements HttpHandler {

    /** SLF4J Logger 인스턴스 - 요청 처리 로그를 기록 */
    private static final Logger logger = LoggerFactory.getLogger(UserHandler.class);
    
    /** 사용자 비즈니스 로직 처리 서비스 */
    private final UserService userService;
    private final TokenService tokenService;

    /** JSON을 다루기 위한 Gson 인스턴스 */
    private final Gson gson = new GsonBuilder()
        .setPrettyPrinting()  // 가독성 좋은 JSON 포맷
        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())  // ← 추가!
        .create();
    
    /**
     * UserController 객체를 생성하는 생성자
     * 
     * @param userService 사용자 관련 비즈니스 로직을 처리하는 서비스 객체
     */
    public UserHandler(UserService userService, TokenService tokenService) {
        if (userService == null || tokenService == null) {
            throw new IllegalArgumentException("의존성은 null일 수 없습니다.");
        }
        this.userService = userService;
        this.tokenService = tokenService;
    }
    

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String clientIP = exchange.getRemoteAddress().getAddress().getHostAddress();
        String path = exchange.getRequestURI().getPath();
        
        logger.debug("사용자 요청 수신: method={}, clientIP={}", method, clientIP);

        try {
            // 라우팅
            if (path.endsWith("/login") && "POST".equals(method)) {
                handleLogin(exchange);
            } else if (path.endsWith("/get-users") && "GET".equals(method)) {
                handleGetUsers(exchange);
            } else if (path.endsWith("/add-user") && "POST".equals(method)) {
                handleAddUser(exchange);
            } else if (path.endsWith("/delete-user") && "POST".equals(method)) {
                handleDeleteUser(exchange);
            } else {
                logger.warn("잘못된 요청: {} {}", method, path);
                HttpResponseHelper.sendErrorResponse(exchange, 404, "Not Found");
            }
            
        } catch (IOException e) {
            logger.error("요청 처리 중 오류 발생", e);
            HttpResponseHelper.sendErrorResponse(exchange, 500, "Internal Server Error");
        }
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
     * 
     * @apiNote LoginRequest DTO를 사용하여 타입 안정성을 보장함
     */
    private void handleLogin(HttpExchange exchange) throws IOException {
        // request의 body에서 로그인 정보 추출
        String requestBody = HttpRequestHelper.readRequestBody(exchange);
        logger.debug("로그인 요청 본문 수신: length={}", requestBody.length());
        
        // LoginRequest DTO로 변환
        LoginRequest loginRequest = gson.fromJson(requestBody, LoginRequest.class);
        
        if (loginRequest == null) {
            logger.warn("잘못된 JSON");
            HttpResponseHelper.sendErrorResponse(exchange, 400, "Invalid JSON format");
            return;
        }
        
        String userId = loginRequest.getUserId();
        String password = loginRequest.getPassword();

        // 필수 필드 검증
        if (userId == null || userId.trim().isEmpty() || 
            password == null || password.trim().isEmpty()) {
            logger.warn("필수 필드 누락: userId={}", userId);
            HttpResponseHelper.sendErrorResponse(exchange, 400, "userId와 password는 필수 입력 항목입니다.");
            return;
        }

        // UserService를 통한 로그인 검증
        try {
            logger.info("로그인 인증 시도: userId={}", userId);
            AuthToken token = userService.login(userId, password);
            Optional<User> user = tokenService.validateToken(token.getToken());

            if (user.isEmpty()) {
                logger.info("로그인 인증 실패: userId={}", userId);
                HttpResponseHelper.sendErrorResponse(exchange, 400, "인증 토큰이 유효하지 않습니다.");
                throw new NullPointerException("인증 토큰으로 서버 접속 실패");
            }

            // response 객체 생성
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "로그인 성공");
            response.put("token", token.getToken());
            response.put("expiresAt", token.getExpiresAt().toString());
            response.put("userId", user.get().getUserId());
            response.put("userName", user.get().getUserName());
            response.put("role", user.get().getRole());
            logger.info("로그인 성공: userId={}, token={}", userId, token.getToken());
            
            // response를 클라이언트에게 전송
            HttpResponseHelper.sendJsonResponse(exchange, 200, response);
        } catch (IllegalArgumentException e) {
            HttpResponseHelper.sendErrorResponse(exchange, 400, e.getMessage());
        }
        
    }
    
    /**
     * 사용자 조회를 처리하는 메서드
     * <p>
     * 클라이언트에서 전송한 인증 토큰의 유효성과 사용자의 권한을 검사한 후,
     * 사용자 정보 목록을 전송한다.
     * </p>
     * 
     * @param exchange HTTP 요청/응답 처리를 위한 교환 객체
     * @throws IOException 네트워크 I/O 처리 중 오류가 발생한 경우
     */
    private void handleGetUsers(HttpExchange exchange) throws IOException {
        logger.info("사용자 목록 조회 시도");
        
        // 인증 토큰 유효성 검증
        logger.debug("인증 토큰 유효성 검증 시도...");
        String token = HttpRequestHelper.extractBearerToken(exchange);
        Optional<User> user = tokenService.validateToken(token);
        if (user.isEmpty()) {
            logger.debug("인증 토큰 유효하지 않음");
            HttpResponseHelper.sendErrorResponse(exchange, 401, "인증 토큰이 유효하지 않습니다.");
            return;
        }
        if (user.get().getRole() != Role.ADMIN) {   
            logger.debug("접근 권한 부족: role = {}", user.get().getRole());
            HttpResponseHelper.sendErrorResponse(exchange, 401, "접근 권한이 없습니다.");
            return;
        }

        // response 객체 생성
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "조회 성공");
        response.put("users", userService.getUsers());
        logger.info("모든 사용자 조회 성공: userId={}, token={}", user.get().getUserId(), token);
        
        // response를 클라이언트에게 전송
        HttpResponseHelper.sendJsonResponse(exchange, 200, response);
    }

    private void handleAddUser(HttpExchange exchange) throws IOException {
        logger.info("사용자 추가 시도");
        
        // 인증 토큰 유효성 검증
        logger.debug("인증 토큰 유효성 검증 시도...");
        String token = HttpRequestHelper.extractBearerToken(exchange);
        Optional<User> user = tokenService.validateToken(token);
        if (user.isEmpty()) {
            logger.debug("인증 토큰 유효하지 않음");
            HttpResponseHelper.sendErrorResponse(exchange, 401, "인증 토큰이 유효하지 않습니다.");
            return;
        }
        if (user.get().getRole() != Role.ADMIN) {   
            logger.debug("접근 권한 부족: role = {}", user.get().getRole());
            HttpResponseHelper.sendErrorResponse(exchange, 401, "접근 권한이 없습니다.");
            return;
        }
        // request의 body에서 로그인 정보 추출
        String requestBody = HttpRequestHelper.readRequestBody(exchange);
        logger.debug("사용자 추가 요청 본문 수신: length={}", requestBody.length());

        // User 모델로 변환
        User userAdded = gson.fromJson(requestBody, User.class);
        if (!userService.addUser(userAdded)) {
            logger.warn("사용자 추가 실패: 중복된 아이디 존재");
            HttpResponseHelper.sendErrorResponse(exchange, 401, "이미 존재하는 아이디입니다.");
            return;
        }

        // response 객체 생성
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "추가 성공");
        response.put("users", userService.getUsers());
        logger.info("사용자 추가 성공: userId={}, token={}", userAdded.getUserId(), token);
        
        // response를 클라이언트에게 전송
        HttpResponseHelper.sendJsonResponse(exchange, 200, response);
    }


    private void handleDeleteUser(HttpExchange exchange) throws IOException {
        logger.info("사용자 삭제 시도");
        
        // 인증 토큰 유효성 검증
        logger.debug("인증 토큰 유효성 검증 시도...");
        String token = HttpRequestHelper.extractBearerToken(exchange);
        Optional<User> user = tokenService.validateToken(token);
        if (user.isEmpty()) {
            logger.debug("인증 토큰 유효하지 않음");
            HttpResponseHelper.sendErrorResponse(exchange, 401, "인증 토큰이 유효하지 않습니다.");
            return;
        }
        if (user.get().getRole() != Role.ADMIN) {   
            logger.debug("접근 권한 부족: role = {}", user.get().getRole());
            HttpResponseHelper.sendErrorResponse(exchange, 401, "접근 권한이 없습니다.");
            return;
        }

        // request의 body에서 로그인 정보 추출
        String requestBody = HttpRequestHelper.readRequestBody(exchange);
        logger.debug("사용자 삭제 요청 본문 수신: length={}", requestBody.length());

        // User 모델로 변환
        User userDeleted = gson.fromJson(requestBody, User.class);

        // 사용자가 자기 자신을 삭제하는 것을 방지
        if (user.get().getUserId().equals(userDeleted.getUserId())) {   
            logger.debug("사용자 본인 삭제 시도 감지: userId = {}", userDeleted.getUserId());
            HttpResponseHelper.sendErrorResponse(exchange, 401, "사용자 본인을 삭제할 수 없습니다.");
            return;
        }

        if (userService.deleteUser(userDeleted.getUserId()).isEmpty()) {
            logger.warn("사용자 삭제 실패: 아이디 존재가 존재하지 않음={}", userDeleted.getUserId());
            HttpResponseHelper.sendErrorResponse(exchange, 401, "존재하지 않는 아이디입니다.");
            return;
        }

        // response 객체 생성
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "삭제 성공");
        logger.info("사용자 삭제 성공: userId={}, token={}", userDeleted.getUserId(), token);
        
        // response를 클라이언트에게 전송
        HttpResponseHelper.sendJsonResponse(exchange, 200, response);
    }
}