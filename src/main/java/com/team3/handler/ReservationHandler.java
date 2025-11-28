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
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.team3.model.Reservation;
import com.team3.model.User;
import com.team3.service.ReservationService;
import com.team3.service.TokenService;
import com.team3.util.HttpRequestHelper;
import com.team3.util.HttpResponseHelper;
import com.team3.util.LocalDateTimeAdapter;

/**
 * 예약 관련 HTTP 요청 처리 핸들러
 * <p>
 * /api/reservation/* 경로로 들어오는 요청을 처리한다.
 * </p>
 * * @author bang9634
 * @since 2025-11-28
 */
public class ReservationHandler implements HttpHandler {

    private static final Logger logger = LoggerFactory.getLogger(ReservationHandler.class);
    
    private final ReservationService reservationService;
    private final TokenService tokenService;
    
    // LocalDateTime 처리를 위한 Gson 설정
    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    public ReservationHandler(ReservationService reservationService, TokenService tokenService) {
        if (reservationService == null || tokenService == null) {
            throw new IllegalArgumentException("의존성은 null일 수 없습니다.");
        }
        this.reservationService = reservationService;
        this.tokenService = tokenService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        logger.debug("예약 요청 수신: {} {}", method, path);

        try {
            // 1. 공통 로직: 인증 토큰 검증
            // (로그인한 사용자만 예약을 하거나 조회할 수 있어야 함)
            String token = HttpRequestHelper.extractBearerToken(exchange);
            Optional<User> user = tokenService.validateToken(token);
            
            if (user.isEmpty()) {
                logger.warn("미인증 요청 거부");
                HttpResponseHelper.sendErrorResponse(exchange, 401, "로그인이 필요합니다.");
                return;
            }

            // 2. 라우팅 (UserHandler와 같은 방식)
            if (path.endsWith("/create") && "POST".equals(method)) {
                handleCreate(exchange, user.get());
            } else if (path.endsWith("/list") && "GET".equals(method)) {
                handleList(exchange, user.get());
            } else if (path.endsWith("/delete") && "POST".equals(method)) {
                handleDelete(exchange, user.get());
            } 
            else if (path.endsWith("/update") && "POST".equals(method)) {
                handleUpdate(exchange, user.get());
            }
            else {
                logger.warn("잘못된 경로 또는 메서드: {} {}", method, path);
                HttpResponseHelper.sendErrorResponse(exchange, 404, "Not Found");
            }

        } catch (Exception e) {
            logger.error("예약 처리 중 서버 오류", e);
            HttpResponseHelper.sendErrorResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }

    /**
     * 예약 생성 (POST /api/reservation/create)
     */
    private void handleCreate(HttpExchange exchange, User user) throws IOException {
        try {
            String body = HttpRequestHelper.readRequestBody(exchange);
            logger.debug("예약 생성 요청 데이터: {}", body);

            Reservation res = gson.fromJson(body, Reservation.class);
            
            // 보안: 요청한 사람의 ID를 예약자 ID로 강제 설정 (다른 사람 명의 예약 방지)
            res.setUserId(user.getUserId());

            if (reservationService.createReservation(res)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "예약이 완료되었습니다.");
                response.put("data", res);
                
                logger.info("예약 성공: {}", res.getId());
                HttpResponseHelper.sendJsonResponse(exchange, 201, response);
            } else {
                HttpResponseHelper.sendErrorResponse(exchange, 400, "예약 생성 실패");
            }
        } catch (IllegalArgumentException e) {
            HttpResponseHelper.sendErrorResponse(exchange, 400, e.getMessage());
        } catch (JsonSyntaxException e) {
            HttpResponseHelper.sendErrorResponse(exchange, 400, "잘못된 JSON 형식");
        }
    }

    /**
     * 예약 목록 조회 (GET /api/reservation/list)
     */
    private void handleList(HttpExchange exchange, User user) throws IOException {
        logger.info("예약 목록 조회 요청: user={}", user.getUserId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        
        // 전체 조회를 할지, 내 예약만 조회할지는 기획에 따라 다름
        // 여기서는 일단 전체 조회를 반환합니다. (UserHandler 로직 참고)
        response.put("reservations", reservationService.getAllReservations());
        
        HttpResponseHelper.sendJsonResponse(exchange, 200, response);
    }

    /**
     * 예약 취소 (POST /api/reservation/delete)
     */
    private void handleDelete(HttpExchange exchange, User user) throws IOException {
        try {
            String body = HttpRequestHelper.readRequestBody(exchange);
            // 클라이언트가 {"id": "예약ID"} 형태로 보낸다고 가정
            Reservation target = gson.fromJson(body, Reservation.class);

            if (target == null || target.getId() == null) {
                HttpResponseHelper.sendErrorResponse(exchange, 400, "예약 ID가 필요합니다.");
                return;
            }

            logger.info("예약 취소 요청: id={}", target.getId());

            if (reservationService.cancelReservation(target.getId())) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "예약이 취소되었습니다.");
                
                HttpResponseHelper.sendJsonResponse(exchange, 200, response);
            } else {
                HttpResponseHelper.sendErrorResponse(exchange, 404, "해당 예약을 찾을 수 없습니다.");
            }
        } catch (JsonSyntaxException e) {
            HttpResponseHelper.sendErrorResponse(exchange, 400, "잘못된 JSON 형식");
        }
    }
    
   /* 예약 수정 처리 (POST /api/reservation/update) */
    private void handleUpdate(HttpExchange exchange, User user) throws IOException {
        try {
            // 1. 클라이언트가 보낸 수정 데이터 읽기
            String body = HttpRequestHelper.readRequestBody(exchange);
            logger.debug("예약 수정 요청 데이터: {}", body);

            // 2. JSON -> Reservation 객체 변환
            Reservation res = gson.fromJson(body, Reservation.class);

            // 3. 서비스 호출 (수정 로직)
            // (보안을 위해 res.setUserId(user.getUserId())를 해서 본인 것만 수정하게 할 수도 있음)
            if (reservationService.updateReservation(res)) {
                // 성공 시 응답
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "예약 정보가 수정되었습니다.");
                
                HttpResponseHelper.sendJsonResponse(exchange, 200, response);
            } else {
                // 실패 시 응답 (DB에 없거나 검증 실패)
                HttpResponseHelper.sendErrorResponse(exchange, 400, "수정 실패: 해당 예약을 찾을 수 없거나 정보가 부족합니다.");
            }

        } catch (IllegalArgumentException e) {
            // 필수 값 누락 등 잘못된 요청
            HttpResponseHelper.sendErrorResponse(exchange, 400, e.getMessage());
        } catch (JsonSyntaxException e) {
            // JSON 형식이 깨진 경우
            HttpResponseHelper.sendErrorResponse(exchange, 400, "잘못된 JSON 형식입니다.");
        } catch (Exception e) {
            // 기타 서버 에러
            logger.error("예약 수정 중 서버 에러", e);
            HttpResponseHelper.sendErrorResponse(exchange, 500, "Internal Server Error");
        }
    }
}