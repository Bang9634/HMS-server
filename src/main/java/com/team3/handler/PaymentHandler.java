package com.team3.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.google.gson.Gson;
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
 * 결제 관련 HTTP 요청을 처리하는 핸들러
 * @author 김현준
 */
public class PaymentHandler implements HttpHandler {

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

        if ("POST".equalsIgnoreCase(method) && path.endsWith("/process")) {
            handleProcessPayment(exchange);
        } else if ("GET".equalsIgnoreCase(method) && path.endsWith("/history")) {
            handleGetHistory(exchange);
        } else if ("DELETE".equalsIgnoreCase(method) && path.endsWith("/history")) {
            handleDeleteHistory(exchange);
        } else {
            sendResponse(exchange, 405, "Method Not Allowed");
        }
    }

    private void handleProcessPayment(HttpExchange exchange) throws IOException {
        try {
            InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            
            // PaymentRequest 제거 -> Payment 클래스로 바로 변환 - 수정
            Payment payment = gson.fromJson(reader, Payment.class);
            
            paymentService.processPayment(payment);
            
            String response = gson.toJson("결제 승인 완료");
            sendResponse(exchange, 200, response);
            logger.info("결제 처리 완료: {}", payment.getGuestName());

        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, e.getMessage());
        } catch (Exception e) {
            logger.error("결제 처리 중 오류", e);
            sendResponse(exchange, 500, "Internal Server Error");
        }
    }

    private void handleGetHistory(HttpExchange exchange) throws IOException {
        try {
            List<Payment> history = paymentService.getAllPayments();
            String response = gson.toJson(history);
            sendResponse(exchange, 200, response);
        } catch (Exception e) {
            logger.error("조회 오류", e);
            sendResponse(exchange, 500, "Error retrieving history");
        }
    }

    private void handleDeleteHistory(HttpExchange exchange) throws IOException {
        try {
            String query = exchange.getRequestURI().getQuery();
            
            // receiptId(영수증번호)로 삭제 요청 처리
            if (query != null && query.contains("receiptId=")) {
                String receiptId = query.split("=")[1];
                paymentService.deleteByReceiptId(receiptId);
                
                String response = gson.toJson("영수증(" + receiptId + ") 내역 삭제 완료");
                sendResponse(exchange, 200, response);
                logger.info("개별 삭제 완료: {}", receiptId);
            } else {
                paymentService.clearAllHistory();
                String response = gson.toJson("전체 내역 초기화 완료");
                sendResponse(exchange, 200, response);
                logger.info("전체 초기화 완료");
            }
        } catch (Exception e) {
            logger.error("삭제 오류", e);
            sendResponse(exchange, 500, e.getMessage());
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