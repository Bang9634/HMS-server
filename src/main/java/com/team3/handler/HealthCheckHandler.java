package com.team3.handler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.team3.model.User;
import com.team3.model.User.Role;
import com.team3.service.TokenService;
import com.team3.util.HttpRequestHelper;
import com.team3.util.HttpResponseHelper;

/**
 * 서버 헬스 체크를 위한 HTTP 핸들러
 * <p>
 * 서버의 상태, 버전, 실행 시간 등의 정보를 JSON 형태로 제공하는 헬스 체크 엔드포인트를 처리한다.
 * 로드 밸런서, 모니터링 시스템, 운영팀에서 서버의 정상 작동 여부를 확인하는데 사용된다.
 * </p>
 * 
 * <h3>주요 기능:</h3>
 * <ul>
 *   <li>서버 상태 정보 제공 (상태, 타임스탬프, 서비스명, 버전)</li>
 *   <li>JSON 형태의 구조화된 응답</li>
 *   <li>CORS 헤더 자동 설정으로 브라우저 호환성 보장</li>
 *   <li>빠른 응답 시간으로 헬스 체크 최적화</li>
 * </ul>
 * 
 * <h3>API 엔드포인트:</h3>
 * <pre>{@code
 * GET /health
 * 
 * 응답 예시:
 * {
 *   "status": "UP",
 *   "timestamp": "2025-11-11T15:30:45.123",
 *   "service": "HMS-Server",
 *   "version": "1.0.0",
 *   "uptime": "2 hours 15 minutes"
 * }
 * }</pre>
 * 
 * @author bang9634
 * @since 2025-11-10
 * 
 * @see com.sun.net.httpserver.HttpHandler
 * @see com.HmsServer.server.PlanPServer
 * 
 * @implNote 이 핸들러는 가능한 한 빠르게 응답해야 하므로 복잡한 로직이나 외부 의존성을 피해야 함
 */
public class HealthCheckHandler implements HttpHandler {

    /** SLF4J 로거 인스턴스 - 헬스 체크 요청을 로깅 */
    private static final Logger logger = LoggerFactory.getLogger(HealthCheckHandler.class);

    private final TokenService tokenService;
    
    /** 서버 시작 시간 (업타임 계산용) */
    private static final long SERVER_START_TIME = System.currentTimeMillis();

    /** 날짜/시간 포맷터 (ISO 8601 형식) */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");


    public HealthCheckHandler(TokenService tokenService) {
        if (tokenService == null) {
            throw new IllegalArgumentException("의존성은 null일 수 없습니다.");
        }
        this.tokenService = tokenService;
    }
    /**
     * 헬스 체크 요청을 처리하는 메인 메서드
     * <p>
     * GET 요청만 허용하며, 서버의 현재 상태 정보를 JSON 형태로 반환한다.
     * 빠른 응답을 위해 최소한의 처리만 수행한다.
     * </p>
     * 
     * <h4>처리 과정:</h4>
     * <ol>
     *   <li>HTTP 메서드 검증 (GET만 허용)</li>
     *   <li>CORS 헤더 설정</li>
     *   <li>헬스 상태 정보 수집</li>
     *   <li>JSON 응답 생성 및 전송</li>
     * </ol>
     * 
     * <h4>응답 정보:</h4>
     * <ul>
     *   <li><strong>status:</strong> "UP" (서버 정상 작동)</li>
     *   <li><strong>timestamp:</strong> 현재 시간 (ISO 8601 형식)</li>
     *   <li><strong>service:</strong> 서비스명</li>
     *   <li><strong>version:</strong> 현재 버전</li>
     *   <li><strong>uptime:</strong> 서버 가동 시간 (human-readable)</li>
     * </ul>
     * 
     * @param exchange HTTP 요청/응답 교환 객체
     * @throws IOException HTTP 처리 중 I/O 오류가 발생한 경우
     * 
     * @apiNote 이 메서드는 헬스 체크 목적으로 최대한 빠르게 응답해야 함
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String clientIP = exchange.getRemoteAddress().getAddress().getHostAddress();
        
        logger.debug("헬스 체크 요청 수신: method={}, clientIP={}", method, clientIP);
        
        // HTTP 메서드 검증 - GET만 허용
        if (!"GET".equals(method)) {
            logger.warn("헬스 체크에 잘못된 HTTP 메서드 사용: method={}, clientIP={}", method, clientIP);
            HttpResponseHelper.sendErrorResponse(exchange, 405, "Method Not Allowed - GET 요청만 지원됩니다");
            return;
        }
        String token = HttpRequestHelper.extractBearerToken(exchange);
        Optional<User> user = tokenService.validateToken(token);
        logger.debug("클라이언트에서 전송한 토큰 검증: token = {}", token);
        if (user.isEmpty()) {
            logger.debug("유효하지 않은 토큰: userId = {}", user.get().getUserId());
            HttpResponseHelper.sendErrorResponse(exchange, 401, "인증 토큰이 유효하지 않습니다.");
            return;
        } else if (user.get().getRole() != Role.ADMIN) {
            logger.debug("접근 권한 부족: role = {}", user.get().getRole());
            HttpResponseHelper.sendErrorResponse(exchange, 401, "접근 권한이 없습니다.");
            return;
        }
        logger.debug("토큰 검증 성공");
        try {
            // 헬스 상태 정보 수집
            Map<String, Object> healthStatus = createHealthStatusInfo();
            logger.debug("헬스 체크 응답 생성 완료: status={}", healthStatus.get("status"));
            
            // 성공 응답 전송
            HttpResponseHelper.sendJsonResponse(exchange, 200,healthStatus);
            
        } catch (IOException e) {
            logger.error("헬스 체크 처리 오류: clientIP={}", clientIP, e);
            HttpResponseHelper.sendErrorResponse(exchange, 500, "Internal server error");
        }
    }

    /**
     * 서버의 헬스 상태 정보를 수집하는 헬퍼 메서드
     * <p>
     * 현재 시간, 서버 버전, 가동 시간 등의 정보를 Map 형태로 수집한다.
     * 향후 데이터베이스 연결 상태, 메모리 사용량 등을 추가할 수 있다.
     * </p>
     * 
     * @return 헬스 상태 정보를 담은 Map 객체
     * 
     * @implNote 이 메서드는 빠른 응답을 위해 복잡한 검사를 피하고 기본 정보만 수집
     */
    private Map<String, Object> createHealthStatusInfo() {
        Map<String, Object> healthStatus = new HashMap<>();
        
        // 기본 상태 정보
        healthStatus.put("status", "UP");
        healthStatus.put("timestamp", LocalDateTime.now().format(DATE_TIME_FORMATTER));
        healthStatus.put("service", "HMS-Server");
        healthStatus.put("version", "1.0.0");
        
        // 서버 가동 시간 추가
        healthStatus.put("uptime", calculateUptime());
        
        // JVM 기본 정보 (선택사항)
        healthStatus.put("javaVersion", System.getProperty("java.version"));
        
        // 추가 가능한 정보들 (향후 확장)
        // healthStatus.put("dbStatus", checkDatabaseConnection());
        // healthStatus.put("memoryUsage", getMemoryUsage());
        // healthStatus.put("activeConnections", getActiveConnectionCount());
        
        return healthStatus;
    }

    /**
     * 서버 가동 시간을 계산하여 사람이 읽기 쉬운 형태로 반환하는 헬퍼 메서드
     * <p>
     * 서버 시작 시간부터 현재까지의 경과 시간을 계산하여
     * "X days, Y hours, Z minutes" 형태의 문자열로 변환한다.
     * </p>
     * 
     * @return 사람이 읽기 쉬운 형태의 가동 시간 문자열
     */
    private String calculateUptime() {
        long uptimeMillis = System.currentTimeMillis() - SERVER_START_TIME;
        long seconds = uptimeMillis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return String.format("%d days, %d hours, %d minutes", 
                days, hours % 24, minutes % 60);
        } else if (hours > 0) {
            return String.format("%d hours, %d minutes", 
                hours, minutes % 60);
        } else if (minutes > 0) {
            return String.format("%d minutes, %d seconds", 
                minutes, seconds % 60);
        } else {
            return String.format("%d seconds", seconds);
        }
    }
}