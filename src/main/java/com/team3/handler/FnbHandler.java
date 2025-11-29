package com.team3.handler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.team3.model.FnbItem;
import com.team3.service.FnbService;
import com.team3.util.HttpRequestHelper;
import com.team3.util.HttpResponseHelper;
import com.team3.util.LocalDateTimeAdapter;

public class FnbHandler implements HttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(FnbHandler.class);
    private final FnbService fnbService;
    private final Gson gson;

    public FnbHandler(FnbService fnbService) {
        this.fnbService = fnbService;
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            if (path.endsWith("/add") && "POST".equals(method)) {
                handleAdd(exchange);
            } else if (path.endsWith("/list") && "GET".equals(method)) {
                handleList(exchange);
            } else if (path.endsWith("/delete") && "POST".equals(method)) {
                handleDelete(exchange);
            } else {
                HttpResponseHelper.sendErrorResponse(exchange, 404, "Not Found");
            }
        } catch (Exception e) {
            logger.error("F&B 처리 중 오류", e);
            HttpResponseHelper.sendErrorResponse(exchange, 500, "Server Error: " + e.getMessage());
        }
    }

    private void handleAdd(HttpExchange exchange) throws IOException {
        try {
            String body = HttpRequestHelper.readRequestBody(exchange);
            FnbItem item = gson.fromJson(body, FnbItem.class);

            if (fnbService.addFnbItem(item)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "주문/예약이 완료되었습니다.");
                HttpResponseHelper.sendJsonResponse(exchange, 200, response);
            } else {
                HttpResponseHelper.sendErrorResponse(exchange, 400, "저장 실패");
            }
        } catch (IllegalArgumentException e) {
            HttpResponseHelper.sendErrorResponse(exchange, 400, e.getMessage()); // 객실 번호 누락 등
        } catch (JsonSyntaxException e) {
            HttpResponseHelper.sendErrorResponse(exchange, 400, "잘못된 JSON 형식");
        }
    }

    private void handleList(HttpExchange exchange) throws IOException {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", fnbService.getAllItems());
        HttpResponseHelper.sendJsonResponse(exchange, 200, response);
    }

    private void handleDelete(HttpExchange exchange) throws IOException {
        String body = HttpRequestHelper.readRequestBody(exchange);
        // {"id": "..."} 형태
        @SuppressWarnings("unchecked")
        Map<String, String> req = gson.fromJson(body, Map.class);
        String id = req.get("id");

        if (id != null && fnbService.deleteItem(id)) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "삭제되었습니다.");
            HttpResponseHelper.sendJsonResponse(exchange, 200, response);
        } else {
            HttpResponseHelper.sendErrorResponse(exchange, 404, "삭제 실패: 항목을 찾을 수 없습니다.");
        }
    }
}