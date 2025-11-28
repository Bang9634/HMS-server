package com.team3.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.google.gson.Gson;
import com.team3.dto.request.PaymentRequest;
import com.team3.model.Payment;
import com.team3.service.PaymentService;
import com.team3.service.TokenService;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 결제 관련 HTTP 요청을 처리하는 핸들러 클래스
 * <p>
 * /api/payments 경로로 들어오는 POST(결제), GET(조회), DELETE(삭제) 요청을 분기하여 처리한다.
 * 예외 발생 시 Logger를 통해 에러를 기록한다.
 * </p>
 * * @author 김현준
 */
public class PaymentHandler implements HttpHandler {

    // 로거 객체 생성 (printStackTrace 대신 사용)
    private static final Logger logger = LoggerFactory.getLogger(PaymentHandler.class);

    private final PaymentService paymentService;
    private final TokenService tokenService;
    private final Gson gson = new Gson();

    public PaymentHandler(PaymentService paymentService, TokenService tokenService) {
        this.paymentService = paymentService;
        this.tokenService = tokenService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        logger.info("요청 수신: {} {}", method, path); // 요청 들어오면 로그 남김

        if ("POST".equalsIgnoreCase(method) && path.endsWith("/process")) {
            handleProcessPayment(exchange);
        } 
        else if ("GET".equalsIgnoreCase(method) && path.endsWith("/history")) {
            handleGetHistory(exchange);
        } 
        else if ("DELETE".equalsIgnoreCase(method) && path.endsWith("/history")) {
            handleDeleteHistory(exchange);
        }
        else {
            logger.warn("지원하지 않는 요청: {} {}", method, path);
            sendResponse(exchange, 405, "Method Not Allowed");
        }
    }

    private void handleProcessPayment(HttpExchange exchange) throws IOException {
        try {
            InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            PaymentRequest request = gson.fromJson(reader, PaymentRequest.class);
            
            paymentService.processPayment(request);
            
            String response = gson.toJson("결제가 성공적으로 처리되었습니다.");
            sendResponse(exchange, 200, response);
            
            logger.info("결제 처리 완료: {}", request.getGuestName());

        } catch (IllegalArgumentException e) {
            logger.warn("결제 요청 데이터 오류: {}", e.getMessage());
            sendResponse(exchange, 400, e.getMessage());
        } catch (Exception e) {
            // [수정] printStackTrace 대신 logger 사용 (에러 내용과 스택트레이스를 로그 파일에 남김)
            logger.error("결제 처리 중 서버 내부 오류 발생", e); 
            sendResponse(exchange, 500, "Internal Server Error");
        }
    }

    private void handleGetHistory(HttpExchange exchange) throws IOException {
        try {
            List<Payment> history = paymentService.getAllPayments();
            String response = gson.toJson(history);
            sendResponse(exchange, 200, response);
            logger.info("결제 내역 조회 완료: {}건", history.size());
        } catch (Exception e) {
            logger.error("내역 조회 중 오류 발생", e);
            sendResponse(exchange, 500, "Error retrieving history");
        }
    }

    private void handleDeleteHistory(HttpExchange exchange) throws IOException {
        try {
            paymentService.clearAllHistory();
            String response = gson.toJson("모든 결제 내역이 초기화되었습니다.");
            sendResponse(exchange, 200, response);
            logger.info("전체 결제 내역 초기화 완료");
        } catch (Exception e) {
            logger.error("내역 삭제 중 오류 발생", e);
            sendResponse(exchange, 500, "Error deleting history");
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
