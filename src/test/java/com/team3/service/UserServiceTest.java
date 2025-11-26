package com.team3.service;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.team3.model.AuthToken;
import com.team3.model.User;
import com.team3.model.User.Role;
import com.team3.repository.UserRepository;
import com.team3.util.PasswordUtil;

@SuppressWarnings("unused")
@DisplayName("UserService 테스트")
class UserServiceTest {

    private UserRepository userRepository;
    private UserService userService;

    private User testUser;
    private String testUserId;
    private String plainPassword;
    private String hashedPassword;

    @BeforeEach
    void setUp() {
        // Mock 객체 직접 생성
        userRepository = mock(UserRepository.class);
        
        // 테스트 데이터 준비
        testUserId = "testUser";
        plainPassword = "password123!";
        hashedPassword = PasswordUtil.hash(plainPassword);
        
        testUser = new User(testUserId, hashedPassword, "테스트유저", Role.CSR);
        
        // Mock 기본 설정 (관리자 1명 존재)
        when(userRepository.countAdmins()).thenReturn(1L);
        when(userRepository.findAdmin()).thenReturn(Optional.of(
            new User("admin", PasswordUtil.hash("admin"), "admin", Role.ADMIN)
        ));
        
        // UserService 생성
        userService = new UserService(userRepository);
    }

    @Nested
    @DisplayName("생성자 테스트")
    class ConstructorTests {

        @Test
        @DisplayName("UserRepository가 null이면 IllegalArgumentException 발생")
        void constructor_withNullRepository_throwsException() {
            // when & then
            assertThatThrownBy(() -> new UserService(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("UserRepository는 null일 수 없습니다.");
        }

        @Test
        @DisplayName("관리자 계정이 없으면 자동 생성")
        void constructor_noAdminExists_createsDefaultAdmin() {
            // given
            UserRepository mockRepo = mock(UserRepository.class);
            when(mockRepo.countAdmins()).thenReturn(0L);

            // when
            UserService service = new UserService(mockRepo);

            // then
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(mockRepo).save(userCaptor.capture());
            
            User savedAdmin = userCaptor.getValue();
            assertThat(savedAdmin.getUserId()).isEqualTo("admin");
            assertThat(savedAdmin.getRole()).isEqualTo(Role.ADMIN);
            assertThat(PasswordUtil.verify("admin", savedAdmin.getPassword())).isTrue();
        }

        @Test
        @DisplayName("관리자 계정이 1개 존재하면 생성하지 않음")
        void constructor_oneAdminExists_doesNotCreateAdmin() {
            // given
            UserRepository mockRepo = mock(UserRepository.class);
            User existingAdmin = new User("admin", "hashedPassword", "admin", Role.ADMIN);
            when(mockRepo.countAdmins()).thenReturn(1L);
            when(mockRepo.findAdmin()).thenReturn(Optional.of(existingAdmin));

            // when
            UserService service = new UserService(mockRepo);

            // then
            verify(mockRepo, never()).save(any(User.class));
        }

        @Test
        @DisplayName("관리자 계정이 2개 이상 존재하면 생성하지 않음")
        void constructor_multipleAdminsExist_doesNotCreateAdmin() {
            // given
            UserRepository mockRepo = mock(UserRepository.class);
            when(mockRepo.countAdmins()).thenReturn(2L);

            // when
            UserService service = new UserService(mockRepo);

            // then
            verify(mockRepo, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("login() 테스트")
    class LoginTests {

        @Test
        @DisplayName("유효한 사용자 ID와 비밀번호로 로그인 성공")
        void login_withValidCredentials_success() {
            // given
            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

            // when
            AuthToken token = userService.login(testUserId, plainPassword);

            // then
            assertThat(token).isNotNull();
            assertThat(token.getUserId()).isEqualTo(testUserId);
            assertThat(token.getToken()).isNotEmpty();
            assertThat(token.isExpired()).isFalse();
        }

        @Test
        @DisplayName("존재하지 않는 사용자 ID로 로그인 시 예외 발생")
        void login_withNonExistentUserId_throwsException() {
            // given
            when(userRepository.findById("nonexistent")).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.login("nonexistent", "password"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 사용자입니다");
        }

        @Test
        @DisplayName("잘못된 비밀번호로 로그인 시 예외 발생")
        void login_withWrongPassword_throwsException() {
            // given
            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

            // when & then
            assertThatThrownBy(() -> userService.login(testUserId, "wrongPassword"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("비밀번호가 일치하지 않습니다");
        }

        @Test
        @DisplayName("userId가 null이면 IllegalArgumentException 발생")
        void login_withNullUserId_throwsException() {
            // when & then
            assertThatThrownBy(() -> userService.login(null, plainPassword))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("사용자 아이디 또는 비밀번호가 null입니다.");
        }

        @Test
        @DisplayName("password가 null이면 IllegalArgumentException 발생")
        void login_withNullPassword_throwsException() {
            // when & then
            assertThatThrownBy(() -> userService.login(testUserId, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("사용자 아이디 또는 비밀번호가 null입니다.");
        }

        @Test
        @DisplayName("연속된 로그인 시도로 여러 토큰 발급 가능")
        void login_multipleTimes_issuesMultipleTokens() {
            // given
            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

            // when
            AuthToken token1 = userService.login(testUserId, plainPassword);
            AuthToken token2 = userService.login(testUserId, plainPassword);

            // then
            assertThat(token1.getToken()).isNotEqualTo(token2.getToken());
            assertThat(token1.getUserId()).isEqualTo(token2.getUserId());
        }

        @Test
        @DisplayName("로그인 성공 시 토큰이 tokenStore에 저장됨")
        void login_success_storesTokenInStore() {
            // given
            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

            // when
            AuthToken token = userService.login(testUserId, plainPassword);

            // then
            Optional<User> validatedUser = userService.validateToken(token.getToken());
            assertThat(validatedUser).isPresent();
            assertThat(validatedUser.get().getUserId()).isEqualTo(testUserId);
        }

        @Test
        @DisplayName("관리자 계정으로 로그인 성공")
        void login_withAdminAccount_success() {
            // given
            User admin = new User("admin", PasswordUtil.hash("admin"), "admin", Role.ADMIN);
            when(userRepository.findById("admin")).thenReturn(Optional.of(admin));

            // when
            AuthToken token = userService.login("admin", "admin");

            // then
            assertThat(token).isNotNull();
            assertThat(token.getUserId()).isEqualTo("admin");
        }
    }

    @Nested
    @DisplayName("logout() 테스트")
    class LogoutTests {

        @Test
        @DisplayName("유효한 토큰으로 로그아웃 성공")
        void logout_withValidToken_success() {
            // given
            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            AuthToken token = userService.login(testUserId, plainPassword);

            // when
            userService.logout(token.getToken());

            // then
            Optional<User> validatedUser = userService.validateToken(token.getToken());
            assertThat(validatedUser).isEmpty();
        }

        @Test
        @DisplayName("존재하지 않는 토큰으로 로그아웃 시도")
        void logout_withNonExistentToken_doesNotThrowException() {
            // when & then
            assertThatCode(() -> userService.logout("nonexistent-token"))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("null 토큰으로 로그아웃 시도")
        void logout_withNullToken_doesNotThrowException() {
            // when & then
            assertThatCode(() -> userService.logout(null))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("이미 로그아웃된 토큰으로 재시도")
        void logout_alreadyLoggedOut_doesNotThrowException() {
            // given
            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            AuthToken token = userService.login(testUserId, plainPassword);
            userService.logout(token.getToken());

            // when & then
            assertThatCode(() -> userService.logout(token.getToken()))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("빈 문자열 토큰으로 로그아웃")
        void logout_withEmptyToken_doesNotThrowException() {
            // when & then
            assertThatCode(() -> userService.logout(""))
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("validateToken() 테스트")
    class ValidateTokenTests {

        @Test
        @DisplayName("유효한 토큰 검증 성공")
        void validateToken_withValidToken_returnsUser() {
            // given
            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            AuthToken token = userService.login(testUserId, plainPassword);

            // when
            Optional<User> result = userService.validateToken(token.getToken());

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getUserId()).isEqualTo(testUserId);
        }

        @Test
        @DisplayName("존재하지 않는 토큰 검증 실패")
        void validateToken_withNonExistentToken_returnsEmpty() {
            // when
            Optional<User> result = userService.validateToken("nonexistent-token");

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("null 토큰 검증 실패")
        void validateToken_withNullToken_returnsEmpty() {
            // when
            Optional<User> result = userService.validateToken(null);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("로그아웃된 토큰 검증 실패")
        void validateToken_afterLogout_returnsEmpty() {
            // given
            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            AuthToken token = userService.login(testUserId, plainPassword);
            userService.logout(token.getToken());

            // when
            Optional<User> result = userService.validateToken(token.getToken());

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("만료된 토큰 검증 실패")
        void validateToken_withExpiredToken_returnsEmpty() throws Exception {
            // given
            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
            
            // 0분 유효기간으로 즉시 만료되는 토큰 생성
            AuthToken expiredToken = new AuthToken(testUserId, 0);
            
            // 리플렉션으로 tokenStore에 직접 접근
            java.lang.reflect.Field tokenStoreField = UserService.class.getDeclaredField("tokenStore");
            tokenStoreField.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.Map<String, AuthToken> tokenStore = 
                (java.util.Map<String, AuthToken>) tokenStoreField.get(userService);
            tokenStore.put(expiredToken.getToken(), expiredToken);
            
            // 약간 대기하여 만료 보장
            Thread.sleep(100);

            // when
            Optional<User> result = userService.validateToken(expiredToken.getToken());

            // then
            assertThat(result).isEmpty();
            assertThat(tokenStore).doesNotContainKey(expiredToken.getToken());
        }

        @Test
        @DisplayName("빈 문자열 토큰 검증 실패")
        void validateToken_withEmptyToken_returnsEmpty() {
            // when
            Optional<User> result = userService.validateToken("");

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("isUserIdAvailable() 테스트")
    class IsUserIdAvailableTests {

        @Test
        @DisplayName("존재하지 않는 ID는 사용 가능")
        void isUserIdAvailable_withNonExistentId_returnsTrue() {
            // given
            when(userRepository.existsById("newUser")).thenReturn(false);

            // when
            boolean result = userService.isUserIdAvailable("newUser");

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("이미 존재하는 ID는 사용 불가")
        void isUserIdAvailable_withExistingId_returnsFalse() {
            // given
            when(userRepository.existsById(testUserId)).thenReturn(true);

            // when
            boolean result = userService.isUserIdAvailable(testUserId);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("null ID 확인")
        void isUserIdAvailable_withNullId_callsRepository() {
            // given
            when(userRepository.existsById(null)).thenReturn(false);

            // when
            boolean result = userService.isUserIdAvailable(null);

            // then
            assertThat(result).isTrue();
            verify(userRepository).existsById(null);
        }

        @Test
        @DisplayName("빈 문자열 ID 확인")
        void isUserIdAvailable_withEmptyString_callsRepository() {
            // given
            when(userRepository.existsById("")).thenReturn(false);

            // when
            boolean result = userService.isUserIdAvailable("");

            // then
            assertThat(result).isTrue();
            verify(userRepository).existsById("");
        }

        @Test
        @DisplayName("관리자 ID는 사용 불가")
        void isUserIdAvailable_withAdminId_returnsFalse() {
            // given
            when(userRepository.existsById("admin")).thenReturn(true);

            // when
            boolean result = userService.isUserIdAvailable("admin");

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("통합 시나리오 테스트")
    class IntegrationScenarioTests {

        @Test
        @DisplayName("로그인 -> 토큰 검증 -> 로그아웃 -> 토큰 검증 실패 시나리오")
        void fullLoginLogoutScenario() {
            // given
            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

            // when - 로그인
            AuthToken token = userService.login(testUserId, plainPassword);
            
            // then - 토큰 검증 성공
            Optional<User> user1 = userService.validateToken(token.getToken());
            assertThat(user1).isPresent();
            assertThat(user1.get().getUserId()).isEqualTo(testUserId);

            // when - 로그아웃
            userService.logout(token.getToken());

            // then - 토큰 검증 실패
            Optional<User> user2 = userService.validateToken(token.getToken());
            assertThat(user2).isEmpty();
        }

        @Test
        @DisplayName("동일 사용자가 여러 기기에서 로그인 시나리오")
        void multipleDeviceLoginScenario() {
            // given
            when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

            // when - 첫 번째 기기에서 로그인
            AuthToken token1 = userService.login(testUserId, plainPassword);
            
            // when - 두 번째 기기에서 로그인
            AuthToken token2 = userService.login(testUserId, plainPassword);

            // then - 두 토큰 모두 유효
            assertThat(userService.validateToken(token1.getToken())).isPresent();
            assertThat(userService.validateToken(token2.getToken())).isPresent();

            // when - 첫 번째 기기에서 로그아웃
            userService.logout(token1.getToken());

            // then - 첫 번째 토큰만 무효화, 두 번째는 여전히 유효
            assertThat(userService.validateToken(token1.getToken())).isEmpty();
            assertThat(userService.validateToken(token2.getToken())).isPresent();
        }

        @Test
        @DisplayName("ID 중복 확인 -> 로그인 실패 -> 새 사용자 등록 -> 로그인 성공")
        void userRegistrationAndLoginScenario() {
            // given
            String newUserId = "newUser";
            String newPassword = "newPassword123!";

            // when - ID 중복 확인 (사용 가능)
            when(userRepository.existsById(newUserId)).thenReturn(false);
            boolean available = userService.isUserIdAvailable(newUserId);

            // then
            assertThat(available).isTrue();

            // when - 아직 등록되지 않아 로그인 실패
            when(userRepository.findById(newUserId)).thenReturn(Optional.empty());
            
            // then
            assertThatThrownBy(() -> userService.login(newUserId, newPassword))
                .isInstanceOf(IllegalArgumentException.class);

            // when - 사용자 등록 후 로그인 성공
            User newUser = new User(newUserId, PasswordUtil.hash(newPassword), "새유저", Role.CSR);
            when(userRepository.findById(newUserId)).thenReturn(Optional.of(newUser));
            AuthToken token = userService.login(newUserId, newPassword);

            // then
            assertThat(token).isNotNull();
            assertThat(token.getUserId()).isEqualTo(newUserId);
        }
    }
}