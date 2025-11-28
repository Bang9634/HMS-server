package com.team3.handler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.team3.model.Customer;
import com.team3.service.CustomerService;
import com.team3.util.HttpRequestHelper;
import com.team3.util.HttpResponseHelper;
import com.team3.util.LocalDateTimeAdapter;

public class CustomerHandler implements HttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(CustomerHandler.class);
    private final CustomerService customerService;
    private final Gson gson;

    public CustomerHandler(CustomerService customerService) {
        this.customerService = customerService;
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
            } else if (path.endsWith("/search") && "GET".equals(method)) {
                handleSearch(exchange); // 검색 (SFR-703)
            } else if (path.endsWith("/delete") && "POST".equals(method)) {
                handleDelete(exchange);
            } else {
                HttpResponseHelper.sendErrorResponse(exchange, 404, "Not Found");
            }
        } catch (Exception e) {
            logger.error("고객 관리 오류", e);
            HttpResponseHelper.sendErrorResponse(exchange, 500, e.getMessage());
        }
    }

    private void handleAdd(HttpExchange exchange) throws IOException {
        String body = HttpRequestHelper.readRequestBody(exchange);
        Customer customer = gson.fromJson(body, Customer.class);
        
        if (customerService.addCustomer(customer)) {
            Map<String, Object> res = new HashMap<>();
            res.put("success", true);
            res.put("message", "고객 정보가 저장되었습니다.");
            HttpResponseHelper.sendJsonResponse(exchange, 200, res);
        } else {
            HttpResponseHelper.sendErrorResponse(exchange, 400, "저장 실패");
        }
    }

    private void handleList(HttpExchange exchange) throws IOException {
        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("data", customerService.getAllCustomers());
        HttpResponseHelper.sendJsonResponse(exchange, 200, res);
    }

    // 검색 핸들러 (Query String 파싱 필요)
    private void handleSearch(HttpExchange exchange) throws IOException {
        // URL 파싱: /api/customer/search?type=NAME&keyword=홍길동
        String query = exchange.getRequestURI().getQuery();
        String type = "NAME";
        String keyword = "";

        if (query != null) {
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair.length == 2) {
                    if (pair[0].equals("type")) type = pair[1];
                    if (pair[0].equals("keyword")) keyword = pair[1];
                }
            }
        }

        List<Customer> results = customerService.searchCustomers(keyword, type);
        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("data", results);
        HttpResponseHelper.sendJsonResponse(exchange, 200, res);
    }

    private void handleDelete(HttpExchange exchange) throws IOException {
        String body = HttpRequestHelper.readRequestBody(exchange);
        @SuppressWarnings("unchecked")
        Map<String, String> map = gson.fromJson(body, Map.class);
        String id = map.get("id");

        if (id != null && customerService.deleteCustomer(id)) {
            Map<String, Object> res = new HashMap<>();
            res.put("success", true);
            res.put("message", "삭제되었습니다.");
            HttpResponseHelper.sendJsonResponse(exchange, 200, res);
        } else {
            HttpResponseHelper.sendErrorResponse(exchange, 404, "삭제 실패");
        }
    }
}