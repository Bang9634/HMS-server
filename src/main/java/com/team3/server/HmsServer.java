package com.team3.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * PlanP 애플리케이션의 메인 HTTP 서버 클래스
 * <p>
 * Java의 내장 HttpServer를 사용하여 RESTful API 서버를 구성하고 관리한다.
 * 사용자 관리, 헬스 체크 등의 API 엔드포인트를 제공하며, 
 * CORS 정책과 스레드 풀을 통한 동시성 처리를 지원한다.
 * </p>
 * 
 * <h3>주요 기능:</h3>
 * <ul>
 *   <li>HTTP 서버 생성 및 관리</li>
 *   <li>RESTful API 라우팅 설정</li>
 *   <li>CORS 정책 적용</li>
 *   <li>스레드 풀 기반 동시성 처리</li>
 *   <li>graceful 서버 시작/종료</li>
 * </ul>
 * 
 * 
 * <h3>서버 설정:</h3>
 * <ul>
 *   <li><strong>기본 포트:</strong> 8080</li>
 *   <li><strong>스레드 풀:</strong> 고정 크기 10개 스레드</li>
 *   <li><strong>CORS 정책:</strong> localhost 기반 개발 환경 허용</li>
 *   <li><strong>인코딩:</strong> UTF-8</li>
 * </ul>
 * 
 * @author bang9634
 * @since 2025-11-10
 * 
 * @see com.sun.net.httpserver.HttpServer
 * @see com.team3.handler.UserHandler
 * @see com.team3.server.CorsFilter
 * @see com.team3.handler.HealthCheckHandler
 * 
 * @implNote Java 내장 HttpServer 사용
 */
public class HmsServer {
    
    /** SLF4J 로거 인스턴스 - 서버 시작/종료 및 라우팅 설정 로깅 */
    private static final Logger logger = LoggerFactory.getLogger(HmsServer.class);
    
    /** Java 내장 HTTP 서버 인스턴스 */
    private final HttpServer server;
    
    /** 서버 바인딩 호스트 주소 */
    private final String host;
    
    /** 서버 바인딩 포트 번호 */
    private final int port;

    private final Map<String, HttpHandler> handlers;
    
    /** 스레드 풀 크기 (동시 처리 가능한 요청 수) */
    private static final int THREAD_POOL_SIZE = 10;
    
    /** 서버 종료 시 대기 시간 (초) */
    private static final int SHUTDOWN_DELAY_SECONDS = 3;

    /** 허용된 오리진 규칙 리스트 */
    private final List<String>allowedOrigins;
    
    /**
     * HmsServer 생성자
     * <p>
     * 지정된 호스트와 포트에 HTTP 서버를 생성하고, API 라우트를 설정한다.
     * UserService를 주입받아 사용자 관련 API의 비즈니스 로직을 처리한다.
     * </p>
     * 
     * <h4>초기화 과정:</h4>
     * <ol>
     *   <li>HttpServer 인스턴스 생성</li>
     *   <li>UserController 초기화</li>
     *   <li>API 라우트 및 CORS 필터 설정</li>
     *   <li>스레드 풀 설정</li>
     * </ol>
     * 
     * @param builder
     * 
     * @throws IOException 서버 생성 중 네트워크 오류가 발생한 경우
     * @throws IllegalArgumentException 잘못된 호스트나 포트가 제공된 경우
     * 
     * @implNote 서버는 생성만 되고 실제 시작은 start() 메서드 호출 시
     */
    private HmsServer(Builder builder) throws IOException {
        this.host = builder.host;
        this.port = builder.port;
        this.allowedOrigins = builder.allowedOrigins;
        this.handlers = builder.handlers;
        
        logger.info("HMS Server 초기화 시작: {}:{}", host, port);
        
        this.server = HttpServer.create(new InetSocketAddress(host, port), 0);
        
        setupRoutes();
        
        server.setExecutor(Executors.newFixedThreadPool(THREAD_POOL_SIZE));
        
        logger.info("서버 초기화 완료: {}:{}, 스레드풀크기={}", host, port, THREAD_POOL_SIZE);
    }
    
    /**
     * API 엔드포인트 라우팅을 설정하는 메서드
     * <p>
     * 모든 API 엔드포인트에 경로와 핸들러를 매핑하고,
     * 각 엔드포인트에 CORS 필터를 적용한다.
     * RESTful API 설계 원칙을 따라 경로를 구성한다.
     * </p>
     * 
     * <h4>CORS 정책:</h4>
     * <p>
     * 모든 API에 CorsFilter를 적용하여 브라우저의 Same-Origin Policy를 우회하고,
     * 프론트엔드 개발 환경에서 API 호출을 가능하게 한다.
     * </p>
     * 
     * @implNote 새로운 API 추가 시 이 메서드에 라우트 설정을 추가해야 함
     */
    private void setupRoutes() {
        logger.info("API 라우트 설정 시작...");
        
        // CORS 필터 인스턴스 생성 (모든 API에서 재사용)
        CorsFilter corsFilter = new CorsFilter(allowedOrigins);
        
        handlers.forEach((path, handler) -> {
            HttpContext context = server.createContext(path, handler);
            context.getFilters().add(corsFilter);
            logger.debug("라우트 설정: {}", path);
        });

        // 라우트 설정 완료 로그
        logger.info("라우트 설정 완료:");
        logger.info("  ├─ GET  /health                    → HealthCheckHandler (헬스 체크)");
        logger.info("  └─ POST /api/users/login           → UserController::handleLogin (로그인)");
        logger.info("모든 API에 CORS 필터 적용 완료 (localhost 개발 환경 허용)");
    }
    
    /**
     * HTTP 서버를 시작하는 메서드
     * <p>
     * HttpServer를 실제로 시작하여 클라이언트 요청 수신을 시작한다.
     * 서버 시작 후 접속 가능한 URL과 CORS 설정 정보를 로그로 출력한다.
     * </p>
     * 
     * <h4>시작 과정:</h4>
     * <ol>
     *   <li>HttpServer.start() 호출</li>
     *   <li>스레드 풀 활성화</li>
     *   <li>요청 수신 대기 상태로 전환</li>
     *   <li>시작 완료 로그 출력</li>
     * </ol>
     * 
     * @apiNote 이 메서드는 논블로킹이므로 호출 후 즉시 반환됨
     * @see #stop() 서버 종료 메서드
     */
    public void start() {
        server.start();
        
        logger.info("══════════════════════════════════════════════════════════════");
        logger.info("                    HMS Server 시작 완료!                      ");
        logger.info("══════════════════════════════════════════════════════════════");
        logger.info("  서버 주소: http://{}:{}{}", 
            host, port, " ".repeat(Math.max(1, 39 - (host + ":" + port).length())));
        logger.info("  상태 확인: http://{}:{}/health{}", 
            host, port, " ".repeat(Math.max(1, 32 - (host + ":" + port).length())));
        logger.info("                                                              ");
        logger.info("  CORS 설정: localhost:3000, localhost:8080 허용              ");
        logger.info("  스레드 풀: {} 개 스레드로 동시 요청 처리{}", 
            THREAD_POOL_SIZE, " ".repeat(Math.max(1, 36 - String.valueOf(THREAD_POOL_SIZE).length())));
        logger.info("══════════════════════════════════════════════════════════════");
        
        // 개발자를 위한 빠른 테스트 가이드
        logger.info("💡 빠른 테스트:");
        logger.info("   curl http://{}:{}/health", host, port);
        logger.info("   → 브라우저에서 http://{}:{} 접속", host, port);
    }
    
    /**
     * HTTP 서버를 graceful하게 종료하는 메서드
     * <p>
     * 현재 처리 중인 요청들이 완료될 때까지 지정된 시간만큼 대기한 후
     * 서버를 안전하게 종료한다. 강제 종료가 아닌 우아한 종료를 수행한다.
     * </p>
     * 
     * <h4>종료 과정:</h4>
     * <ol>
     *   <li>새로운 요청 수신 중단</li>
     *   <li>처리 중인 요청 완료 대기 (최대 3초)</li>
     *   <li>스레드 풀 종료</li>
     *   <li>서버 리소스 해제</li>
     * </ol>
     * 
     * @apiNote 이 메서드는 블로킹되며, 모든 요청이 완료되거나 타임아웃까지 대기함
     * @see #start() 서버 시작 메서드
     */
    public void stop() {
        logger.info("서버 종료 요청 수신...");
        logger.info("현재 처리 중인 요청 완료 대기 중... (최대 {}초)", SHUTDOWN_DELAY_SECONDS);
        
        // graceful shutdown - 처리 중인 요청들의 완료를 기다림
        server.stop(SHUTDOWN_DELAY_SECONDS);
        
        logger.info("══════════════════════════════════════════════════════════════");
        logger.info("                    HMS Server 서버 종료 완료                       ");
        logger.info("                                                              ");
        logger.info("  모든 요청 처리가 완료되었습니다.                              ");
        logger.info("  서버 리소스가 정리되었습니다.                                 ");
        logger.info("                                                              ");
        logger.info("  서버 주소: http://{}:{}{}║", 
            host, port, " ".repeat(Math.max(1, 39 - (host + ":" + port).length())));
        logger.info("══════════════════════════════════════════════════════════════");
    }
    
    /**
     * 서버 설정 정보를 반환하는 게터 메서드
     * 
     * @return 서버가 바인딩된 호스트 주소
     */
    public String getHost() {
        return host;
    }
    
    /**
     * 서버 포트 정보를 반환하는 게터 메서드
     * 
     * @return 서버가 바인딩된 포트 번호
     */
    public int getPort() {
        return port;
    }
    
    /**
     * 서버 실행 상태를 확인하는 메서드
     * <p>
     * 현재 서버가 시작되어 요청을 처리할 수 있는 상태인지 확인한다.
     * 모니터링이나 헬스 체크에서 사용할 수 있다.
     * </p>
     * 
     * @return 서버가 시작된 상태이면 true, 그렇지 않으면 false
     * 
     * @implNote HttpServer는 직접적인 상태 확인 메서드를 제공하지 않으므로 실제 구현 시 주의 필요
     */
    public boolean isRunning() {
        // HttpServer는 직접적인 상태 확인 방법을 제공하지 않음
        // 실제 구현에서는 별도의 상태 플래그를 관리하거나
        // 포트 바인딩 상태를 확인하는 방식을 사용해야 함
        return server != null; // 간단한 null 체크로 대체
    }
    
    /**
     * 서버 정보를 문자열로 반환하는 메서드
     * 
     * @return 서버 주소와 상태 정보를 포함한 문자열
     */
    @Override
    public String toString() {
        return String.format("HMS_Server{host='%s', port=%d, running=%s}", 
                           host, port, isRunning());
    }

    public static class Builder {
        private String host;
        private int port;
        private List<String> allowedOrigins;
        private final Map<String, HttpHandler> handlers = new HashMap<>();

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder allowedOrigins(List<String> origins) {
            this.allowedOrigins = origins;
            return this;
        }
        
        public Builder addHandler(String path, HttpHandler handler) {
            this.handlers.put(path, handler);
            return this;
        }
        
        public HmsServer build() throws IOException {
            return new HmsServer(this);
        }
    }
}