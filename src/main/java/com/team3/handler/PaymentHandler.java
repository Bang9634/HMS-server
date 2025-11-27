package com.team3.handler;

/*
* HTTP 요청을 받아서 서비스를 호출하고 결과를 응답
*/

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

public class PaymentHandler implements HttpHandler {

    private final PaymentService paymentService;
    private final TokenService tokenService; // 로그인 체크용 (필요시)
    private final Gson gson = new Gson();

    public PaymentHandler(PaymentService paymentService, TokenService tokenService) {
        this.paymentService = paymentService;
        this.tokenService = tokenService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        // 1. POST: 결제 진행 (/api/payments/process)
        if ("POST".equalsIgnoreCase(method) && path.endsWith("/process")) {
            handleProcessPayment(exchange);
        } 
        // 2. GET: 결제 내역 조회 (/api/payments/history)
        else if ("GET".equalsIgnoreCase(method) && path.endsWith("/history")) {
            handleGetHistory(exchange);
        } 
        else {
            sendResponse(exchange, 405, "Method Not Allowed");
        }
    }

    // 결제 처리 핸들러
    private void handleProcessPayment(HttpExchange exchange) throws IOException {
        try {
            // Body에서 JSON 읽기
            InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            PaymentRequest request = gson.fromJson(reader, PaymentRequest.class);

            // 서비스 호출
            paymentService.processPayment(request);

            // 성공 응답
            String response = gson.toJson("결제가 성공적으로 처리되었습니다.");
            sendResponse(exchange, 200, response);
            
        } catch (IllegalArgumentException e) {
            // 카드 번호 오류 등 비즈니스 로직 에러
            sendResponse(exchange, 400, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "Internal Server Error");
        }
    }

    // 내역 조회 핸들러
    private void handleGetHistory(HttpExchange exchange) throws IOException {
        List<Payment> history = paymentService.getAllPayments();
        String response = gson.toJson(history);
        sendResponse(exchange, 200, response);
    }

    // 응답 전송 헬퍼 메서드
    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}