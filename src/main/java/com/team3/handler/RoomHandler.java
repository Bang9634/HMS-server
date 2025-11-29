package com.team3.handler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.team3.model.PriceChangeLog;
import com.team3.model.Room;
import com.team3.model.User;
import com.team3.service.RoomService;
import com.team3.service.TokenService;
import com.team3.util.HttpRequestHelper;
import com.team3.util.HttpResponseHelper;
import com.team3.util.LocalDateTimeAdapter;

/**
 * 객실 관련 HTTP 요청 처리 핸들러
 * 
 * @author bang9634
 * @since 2025-11-27
 */
public class RoomHandler implements HttpHandler {
    /** SLF4J Logger 인스턴스 - 요청 처리 로그를 기록 */
    private static final Logger logger = LoggerFactory.getLogger(UserHandler.class);

    private final Gson gson = new GsonBuilder()
        .setPrettyPrinting()  // 가독성 좋은 JSON 포맷
        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())  // ← 추가!
        .create();

    private final TokenService tokenService;
    private final RoomService roomService;

    /**
     * RoomHandler의 생성자
     * 
     * @param tokenService 토큰 인증을 위한 서비스 객체
     * @param roomService 객실 비즈니스 로직을 위한 서비스 객체
     */
    public RoomHandler(TokenService tokenService, RoomService roomService) {
        if (tokenService == null) {
            throw new IllegalArgumentException("의존성은 null일 수 없습니다.");
        }
        this.tokenService = tokenService;
        this.roomService = roomService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String clientIP = exchange.getRemoteAddress().getAddress().getHostAddress();
        String path = exchange.getRequestURI().getPath();
        
        logger.debug("객실 요청 수신: method={}, clientIP={}", method, clientIP);

        try {
            // 라우팅
            if (path.endsWith("/get-rooms") && "GET".equals(method)) {
                handleGetRooms(exchange);
            } else if (path.endsWith("/add-room") && "POST".equals(method)) {
                handleAddRoom(exchange);
            } else if (path.endsWith("/delete-room") && "POST".equals(method)) {
                handleDeleteRoom(exchange);
            } else if (path.endsWith("/update-room") && "POST".equals(method)) {
                handleUpdateRoom(exchange);
            } else if (path.endsWith("/get-price-change-logs") && "POST".equals(method)) {
                handleGetPriceChangeLogs(exchange);
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
     * 객실 목록 조회 요청을 처리하는 메서드
     * <p>
     * 요청문 헤더에 토큰 유효성을 확인하고,
     * 유효한 토큰이라면 객실 목록 정보를 클라이언트에게 전송한다.
     * </p>
     * 
     * @param exchange HTTP 교환 객체
     * @throws IOException
     */
    private void handleGetRooms(HttpExchange exchange) throws IOException{
        logger.info("객실 목록 조회 시도");
            
        // 인증 토큰 유효성 검증
        logger.debug("인증 토큰 유효성 검증 시도...");
        String token = HttpRequestHelper.extractBearerToken(exchange);
        Optional<User> user = tokenService.validateToken(token);
        if (user.isEmpty()) {
            logger.debug("인증 토큰 유효하지 않음");
            HttpResponseHelper.sendErrorResponse(exchange, 401, "인증 토큰이 유효하지 않습니다.");
            return;
        }

        // response 객체 생성
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "조회 성공");
        response.put("rooms", roomService.getRooms());
        logger.info("모든 객실 조회 성공: userId={}, token={}", user.get().getUserId(), token);
            
        // response를 클라이언트에게 전송
        HttpResponseHelper.sendJsonResponse(exchange, 200, response);
    }

    /**
     * 객실 추가 요청을 처리하는 메서드
     * 
     * @param exchange HTTP 교환 객체
     * @throws IOException
     */
    private void handleAddRoom(HttpExchange exchange) throws IOException {
        logger.info("객실 추가 시도");
        
        // 인증 토큰 유효성 검증
        logger.debug("인증 토큰 유효성 검증 시도...");
        String token = HttpRequestHelper.extractBearerToken(exchange);
        Optional<User> user = tokenService.validateToken(token);
        if (user.isEmpty()) {
            logger.debug("인증 토큰 유효하지 않음");
            HttpResponseHelper.sendErrorResponse(exchange, 401, "인증 토큰이 유효하지 않습니다.");
            return;
        }

        // request의 body에서 로그인 정보 추출
        String requestBody = HttpRequestHelper.readRequestBody(exchange);
        logger.debug("객실 추가 요청 본문 수신: length={}", requestBody.length());

        // User 모델로 변환
        Room roomAdded = new Room(gson.fromJson(requestBody, Room.class));
        if (!roomService.addRoom(roomAdded)) {
            logger.warn("객실 추가 실패: 중복된 객실 존재");
            HttpResponseHelper.sendErrorResponse(exchange, 401, "이미 존재하는 객실입니다..");
            return;
        }

        // response 객체 생성
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "추가 성공");
        response.put("rooms", roomService.getRooms());
        logger.info("객실 추가 성공: token={}", token);
        
        // response를 클라이언트에게 전송
        HttpResponseHelper.sendJsonResponse(exchange, 200, response);
    }

    /**
     * 객실 삭제 요청을 처리하는 메서드
     * 
     * @param exchange HTTP 교환 객체
     */
    public void handleDeleteRoom(HttpExchange exchange) throws IOException {
        logger.info("객실 삭제 시도");
        
        // 인증 토큰 유효성 검증
        logger.debug("인증 토큰 유효성 검증 시도...");
        String token = HttpRequestHelper.extractBearerToken(exchange);
        Optional<User> user = tokenService.validateToken(token);
        if (user.isEmpty()) {
            logger.debug("인증 토큰 유효하지 않음");
            HttpResponseHelper.sendErrorResponse(exchange, 401, "인증 토큰이 유효하지 않습니다.");
            return;
        }

        // request의 body에서 로그인 정보 추출
        String requestBody = HttpRequestHelper.readRequestBody(exchange);
        logger.debug("객실 삭제 요청 본문 수신: length={}", requestBody.length());

        // User 모델로 변환
        Room roomDeleted = gson.fromJson(requestBody, Room.class);
        if (roomService.deleteRoom(roomDeleted.getRoomId()).isEmpty()) {
            logger.warn("객실 삭제 실패: 객실이 존재하지 않음.");
            HttpResponseHelper.sendErrorResponse(exchange, 401, "존재하지 않는 객실입니다.");
            return;
        }

        // response 객체 생성
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "삭제 성공");
        response.put("rooms", roomService.getRooms());
        logger.info("객실 삭제 성공: token={}", token);
        
        // response를 클라이언트에게 전송
        HttpResponseHelper.sendJsonResponse(exchange, 200, response);
    }


    /**
     * 객실 추가 요청을 처리하는 메서드
     * 
     * @param exchange HTTP 교환 객체
     * @throws IOException
     */
    private void handleUpdateRoom(HttpExchange exchange) throws IOException {
        logger.info("객실 수정 시도");
        
        // 인증 토큰 유효성 검증
        logger.debug("인증 토큰 유효성 검증 시도...");
        String token = HttpRequestHelper.extractBearerToken(exchange);
        Optional<User> user = tokenService.validateToken(token);
        if (user.isEmpty()) {
            logger.debug("인증 토큰 유효하지 않음");
            HttpResponseHelper.sendErrorResponse(exchange, 401, "인증 토큰이 유효하지 않습니다.");
            return;
        }

        // request의 body에서 로그인 정보 추출
        String requestBody = HttpRequestHelper.readRequestBody(exchange);
        logger.debug("객실 수정 요청 본문 수신: length={}", requestBody.length());

        JsonObject jsonObj = JsonParser.parseString(requestBody).getAsJsonObject();
        String priceChangeReason = jsonObj.has("reason") ? jsonObj.get("reason").getAsString() : null;
        jsonObj.remove("reason");

        Room roomUpdated = gson.fromJson(requestBody, Room.class);
        if (!roomService.updateRoom(roomUpdated, priceChangeReason)) {
            logger.warn("객실 수정 실패: 객실이 존재하지 않음.");
            HttpResponseHelper.sendErrorResponse(exchange, 401, "존재하지 않는 객실입니다.");
            return;
        }

        // response 객체 생성
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "수정 성공");
        response.put("rooms", roomService.getRooms());
        logger.info("객실 수정 성공: token={}", token);
        
        // response를 클라이언트에게 전송
        HttpResponseHelper.sendJsonResponse(exchange, 200, response);
    }


    /**
     * 객실 금액변경로그 조회 요청을 처리하는 메서드
     * 
     * @param exchange HTTP 교환 객체
     */
    public void handleGetPriceChangeLogs(HttpExchange exchange) throws IOException {
        logger.info("금액변경로그 조회 시도");
        
        // 인증 토큰 유효성 검증
        logger.debug("인증 토큰 유효성 검증 시도...");
        String token = HttpRequestHelper.extractBearerToken(exchange);
        Optional<User> user = tokenService.validateToken(token);
        if (user.isEmpty()) {
            logger.debug("인증 토큰 유효하지 않음");
            HttpResponseHelper.sendErrorResponse(exchange, 401, "인증 토큰이 유효하지 않습니다.");
            return;
        }

        // request의 body에서 로그인 정보 추출
        String requestBody = HttpRequestHelper.readRequestBody(exchange);
        logger.debug("금액변경로그 조회 요청 본문 수신: length={}", requestBody.length());

        // User 모델로 변환
        Room room = gson.fromJson(requestBody, Room.class);
        Optional<List<PriceChangeLog>> priceChangeLogs = roomService.getPriceChangeLog(room.getRoomId());
        if (priceChangeLogs.isEmpty()) {
            logger.warn("금액변경로그 조회 실패: 객실이 존재하지 않음.");
            HttpResponseHelper.sendErrorResponse(exchange, 401, "존재하지 않는 객실입니다.");
            return;
        }

        // response 객체 생성
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "조회 성공");
        response.put("logs", priceChangeLogs.get());
        logger.info("금액변경로그 조회 성공: token={}", token);
        
        // response를 클라이언트에게 전송
        HttpResponseHelper.sendJsonResponse(exchange, 200, response);
    }
}
