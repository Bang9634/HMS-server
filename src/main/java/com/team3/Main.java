package com.team3;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.reflect.TypeToken;
import com.team3.config.EnvironmentConfig;
import com.team3.handler.HealthCheckHandler;
import com.team3.handler.PaymentHandler; // 결제 핸들러
import com.team3.handler.UserHandler;
import com.team3.model.Payment;        // 결제 모델
import com.team3.model.User;
import com.team3.repository.JsonPaymentRepository; // 결제 JSON 리포지토리
import com.team3.repository.JsonUserRepository;
import com.team3.repository.PaymentRepository;     // 결제 리포지토리 인터페이스
import com.team3.repository.UserRepository;
import com.team3.server.HmsServer;
import com.team3.service.PaymentService; // 결제 서비스
import com.team3.service.TokenService;
import com.team3.service.UserService;
import com.team3.util.JsonFileManager;

/**
 * HMS 서버 애플리케이션의 메인 엔트리 포인트 클래스
 */
public class Main {
    
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    
    private static final String STARTUP_BANNER = """
             ══════════════════════════════════════════════════════════════
                                      HMS Server                                   
                                  서버 시작 중...                                   
             ══════════════════════════════════════════════════════════════
             """;

    public static void main(String[] args) {
        System.out.println(STARTUP_BANNER);
        System.out.println("HMS Server 초기화 중...\n");

        Dependencies dependencies;
        
        try {
            EnvironmentConfig.printConfig();

            String host = getHostFromArgs(args);
            int port = getPortFromArgs(args);
            
            logger.info("서버 시작: {}:{}", host, port);
            
            System.out.printf("서버 설정:\n");
            System.out.printf("├─ 호스트: %s\n", host);
            System.out.printf("├─ 포트: %d\n", port);
            System.out.printf("└─  허용 오리진: %s\n",
                String.join(", ", EnvironmentConfig.getAllowedOrigins()));

            // 의존성 초기화 (여기서 User와 Payment 관련 객체들이 모두 생성됨)
            dependencies = initializDependencies();

            // HTTP 서버 생성 및 시작
            System.out.println("\nHTTP 서버 생성 중...");
            HmsServer server = new HmsServer.Builder()
                .host(host)
                .port(port)
                .allowedOrigins(Arrays.asList(EnvironmentConfig.getAllowedOrigins()))
                
                // 1. 헬스 체크
                .addHandler("/health", dependencies.healthCheckHandler)
                
                // 2. 회원(User) 관련 API
                .addHandler("/api/users/login", dependencies.userHandler)
                .addHandler("/api/users/get-users", dependencies.userHandler)
                .addHandler("/api/users/add-user", dependencies.userHandler)
                .addHandler("/api/users/delete-user", dependencies.userHandler)
                
                // 3. 결제(Payment) 관련 API 등록
                .addHandler("/api/payments/process", dependencies.paymentHandler) // 결제 요청 (POST)
                .addHandler("/api/payments/history", dependencies.paymentHandler) // 내역 조회 (GET)
                
                .build();

            System.out.println("\nHTTP 서버 생성 완료");

            registerShutdownHook(server);
            
            System.out.printf("\n서버가 http://%s:%d 에서 시작되었습니다\n", host, port);
            System.out.println("Health Check: http://" + host + ":" + port + "/health");
            System.out.println("Payment API: http://" + host + ":" + port + "/api/payments/process");
            System.out.println("종료하려면 Ctrl+C를 누르세요.\n");
            
            server.start();
            
        } catch (Exception e) {
            // 모든 에러를 여기서 한 번에 잡습니다.
            logger.error("서버 실행 중 치명적인 오류 발생", e);
            System.err.println("\n[오류 발생] 서버를 시작할 수 없습니다.");
            System.err.println("메시지: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static Dependencies initializDependencies() {
        System.out.println("\n=== 의존성 초기화 시작 ===\n");
    
        try {
            Dependencies deps = new Dependencies();
            System.out.println("=== 의존성 초기화 완료 ===\n");
            return deps;
            
        } catch (Exception e) {
            logger.error("의존성 초기화 중 오류 발생", e);
            System.err.println("\n의존성 초기화 실패: " + e.getMessage());
            throw new RuntimeException("Dependencies initialization failed", e);
        }
    }
    
    
    private static int getPortFromArgs(String[] args) {
        if (args.length > 0) {
            try {
                int port = Integer.parseInt(args[0]);
                validatePort(port);
                if (port < 1024) System.out.println("시스템 포트 사용: " + port);
                return port;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("잘못된 포트 번호 형식: " + args[0]);
            }
        }
        return EnvironmentConfig.getPort();
    }
        
    private static String getHostFromArgs(String[] args) {
        if (args.length > 1) {
            String host = args[1].trim();
            if (!host.isEmpty()) {
                validateHost(host);
                return host;
            }
        }
        return EnvironmentConfig.getHost();
    }

    private static void validatePort(int port) {
        if (port < 1 || port > 65535) throw new IllegalArgumentException("포트 번호는 1-65535 범위여야 합니다.: " + port);
    }

    private static void validateHost(String host) {
        if (host == null || host.trim().isEmpty()) throw new IllegalArgumentException("호스트 주소가 비어있습니다.");
        if (host.contains(" ")) throw new IllegalArgumentException("호스트 주소에 공백이 포함되어 있습니다.: " + host);
    }

    private static void registerShutdownHook(HmsServer server) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n서버 종료 신호 수신...");
            try {
                server.stop();
                System.out.println("HTTP 서버 종료 완료");
            } catch (Exception e) {
                System.err.println("서버 종료 중 오류: " + e.getMessage());
            }
        }, "shutdown-hook"));
    }

    /**
     * 애플리케이션 의존성 컨테이너
     */
    private static class Dependencies {
        // 데이터 파일 경로 정의
        private static final String USER_DATA_FILE = "data/users.json";
        private static final String PAYMENT_DATA_FILE = "data/payments.json"; // [New] 결제 파일 경로
        
        // User 관련 의존성
        final JsonFileManager<User> userFileManager;
        final UserRepository userRepository;
        final TokenService tokenService;
        final UserService userService;
        final UserHandler userHandler;
        final HealthCheckHandler healthCheckHandler;

        // Payment 관련 의존성 필드 추가
        final JsonFileManager<Payment> paymentFileManager;
        final PaymentRepository paymentRepository;
        final PaymentService paymentService;
        final PaymentHandler paymentHandler;

        private Dependencies() {
            logger.info("=== 의존성 초기화 시작 ===");
            try {
                // 1. User 관련 초기화
                logger.info("User System 초기화...");
                this.userFileManager = new JsonFileManager<>(
                    USER_DATA_FILE, 
                    new TypeToken<List<User>>() {}
                );
                this.userRepository = new JsonUserRepository(userFileManager);
                this.tokenService = new TokenService(userRepository);
                this.userService = new UserService(userRepository, tokenService);
                this.healthCheckHandler = new HealthCheckHandler(tokenService);
                this.userHandler = new UserHandler(userService, tokenService);

                // 2. Payment 관련 초기화
                logger.info("Payment System 초기화...");
                
                // (1) 파일 매니저 생성 (List<Payment> 타입 인식)
                this.paymentFileManager = new JsonFileManager<>(
                    PAYMENT_DATA_FILE,
                    new TypeToken<List<Payment>>() {}
                );

                // (2) 리포지토리 생성 (JsonPaymentRepository 사용)
                this.paymentRepository = new JsonPaymentRepository(paymentFileManager);

                // (3) 서비스 생성
                this.paymentService = new PaymentService(paymentRepository);

                // (4) 핸들러 생성 (TokenService도 주입 - 필요시 인증 체크용)
                this.paymentHandler = new PaymentHandler(paymentService, tokenService);

                logger.info("=== 의존성 초기화 완료 ===\n");
                
            } catch (Exception e) {
                logger.error("의존성 초기화 실패: {}", e.getMessage());
                throw new RuntimeException("Failed to initialize dependencies", e);
            }
        }
    }
}