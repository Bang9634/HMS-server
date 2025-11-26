package com.team3.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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

import com.team3.model.User;
import com.team3.model.User.Role;
import com.team3.util.JsonFileManager;


@SuppressWarnings({"unused", "unchecked"})
@DisplayName("JsonUserRepository 테스트")
class JsonUserRepositoryTest {

    private JsonFileManager<User> mockFileManager;
    private JsonUserRepository repository;

    private User testUser;
    private User adminUser;
    private User csrUser;

    @BeforeEach
    void setUp() {
        // Mock 객체 생성
        mockFileManager = (JsonFileManager<User>)mock(JsonFileManager.class);
        repository = new JsonUserRepository(mockFileManager);

        // 테스트 데이터 준비
        testUser = new User("testUser", "hashedPassword", "테스트유저", Role.CSR);
        adminUser = new User("admin", "adminHash", "관리자", Role.ADMIN);
        csrUser = new User("csr1", "csrHash", "상담원1", Role.CSR);
    }

    @Nested
    @DisplayName("save() 테스트")
    class SaveTests {

        @Test
        @DisplayName("새 사용자 저장 - 파일에 추가")
        void save_newUser_addsToList() {
            // given
            when(mockFileManager.readAll()).thenReturn(new ArrayList<>());

            // when
            repository.save(testUser);

            // then
            ArgumentCaptor<List<User>> captor = ArgumentCaptor.forClass(List.class);
            verify(mockFileManager).writeAll(captor.capture());

            List<User> savedUsers = captor.getValue();
            assertThat(savedUsers).hasSize(1);
            assertThat(savedUsers.get(0).getUserId()).isEqualTo("testUser");
            assertThat(savedUsers.get(0).getUserName()).isEqualTo("테스트유저");
        }

        @Test
        @DisplayName("기존 사용자 저장 - 업데이트")
        void save_existingUser_updates() {
            // given
            User existingUser = new User("testUser", "oldHash", "구이름", Role.CSR);
            when(mockFileManager.readAll()).thenReturn(new ArrayList<>(List.of(existingUser)));

            User updatedUser = new User("testUser", "newHash", "새이름", Role.ADMIN);

            // when
            repository.save(updatedUser);

            // then
            ArgumentCaptor<List<User>> captor = ArgumentCaptor.forClass(List.class);
            verify(mockFileManager).writeAll(captor.capture());

            List<User> savedUsers = captor.getValue();
            assertThat(savedUsers).hasSize(1);
            assertThat(savedUsers.get(0).getPassword()).isEqualTo("newHash");
            assertThat(savedUsers.get(0).getUserName()).isEqualTo("새이름");
            assertThat(savedUsers.get(0).getRole()).isEqualTo(Role.ADMIN);
        }

        @Test
        @DisplayName("여러 사용자 중 특정 사용자만 업데이트")
        void save_existingUserAmongMany_updatesOnlyOne() {
            // given
            User user1 = new User("user1", "hash1", "이름1", Role.CSR);
            User user2 = new User("user2", "hash2", "이름2", Role.CSR);
            User user3 = new User("user3", "hash3", "이름3", Role.ADMIN);

            when(mockFileManager.readAll()).thenReturn(
                new ArrayList<>(List.of(user1, user2, user3))
            );

            User updatedUser2 = new User("user2", "newHash2", "새이름2", Role.ADMIN);

            // when
            repository.save(updatedUser2);

            // then
            ArgumentCaptor<List<User>> captor = ArgumentCaptor.forClass(List.class);
            verify(mockFileManager).writeAll(captor.capture());

            List<User> savedUsers = captor.getValue();
            assertThat(savedUsers).hasSize(3);
            
            // user1, user3는 변경 없음
            assertThat(savedUsers.get(0).getPassword()).isEqualTo("hash1");
            assertThat(savedUsers.get(2).getPassword()).isEqualTo("hash3");
            
            // user2만 업데이트됨
            assertThat(savedUsers.get(1).getPassword()).isEqualTo("newHash2");
            assertThat(savedUsers.get(1).getUserName()).isEqualTo("새이름2");
            assertThat(savedUsers.get(1).getRole()).isEqualTo(Role.ADMIN);
        }

        @Test
        @DisplayName("여러 사용자가 있는 상태에서 새 사용자 추가")
        void save_newUserAmongMany_addsToEnd() {
            // given
            when(mockFileManager.readAll()).thenReturn(
                new ArrayList<>(List.of(adminUser, csrUser))
            );

            User newUser = new User("newUser", "newHash", "새유저", Role.CSR);

            // when
            repository.save(newUser);

            // then
            ArgumentCaptor<List<User>> captor = ArgumentCaptor.forClass(List.class);
            verify(mockFileManager).writeAll(captor.capture());

            List<User> savedUsers = captor.getValue();
            assertThat(savedUsers).hasSize(3);
            assertThat(savedUsers.get(2).getUserId()).isEqualTo("newUser");
        }
    }

    @Nested
    @DisplayName("findById() 테스트")
    class FindByIdTests {

        @Test
        @DisplayName("존재하는 사용자 조회 성공")
        void findById_existingUser_returnsUser() {
            // given
            when(mockFileManager.readAll()).thenReturn(List.of(testUser));

            // when
            Optional<User> result = repository.findById("testUser");

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getUserId()).isEqualTo("testUser");
            assertThat(result.get().getUserName()).isEqualTo("테스트유저");
        }

        @Test
        @DisplayName("존재하지 않는 사용자 조회 시 빈 Optional 반환")
        void findById_nonExistingUser_returnsEmpty() {
            // given
            when(mockFileManager.readAll()).thenReturn(List.of(testUser));

            // when
            Optional<User> result = repository.findById("nonexistent");

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("빈 파일에서 조회 시 빈 Optional 반환")
        void findById_emptyFile_returnsEmpty() {
            // given
            when(mockFileManager.readAll()).thenReturn(new ArrayList<>());

            // when
            Optional<User> result = repository.findById("testUser");

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("여러 사용자 중 특정 사용자 조회")
        void findById_amongManyUsers_returnsCorrectUser() {
            // given
            when(mockFileManager.readAll()).thenReturn(
                List.of(adminUser, csrUser, testUser)
            );

            // when
            Optional<User> result = repository.findById("csr1");

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getUserId()).isEqualTo("csr1");
            assertThat(result.get().getUserName()).isEqualTo("상담원1");
        }
    }

    @Nested
    @DisplayName("findAll() 테스트")
    class FindAllTests {

        @Test
        @DisplayName("전체 사용자 조회 - 빈 리스트")
        void findAll_emptyFile_returnsEmptyList() {
            // given
            when(mockFileManager.readAll()).thenReturn(new ArrayList<>());

            // when
            List<User> users = repository.findAll();

            // then
            assertThat(users).isEmpty();
        }

        @Test
        @DisplayName("전체 사용자 조회 - 단일 사용자")
        void findAll_singleUser_returnsList() {
            // given
            when(mockFileManager.readAll()).thenReturn(List.of(testUser));

            // when
            List<User> users = repository.findAll();

            // then
            assertThat(users).hasSize(1);
            assertThat(users.get(0).getUserId()).isEqualTo("testUser");
        }

        @Test
        @DisplayName("전체 사용자 조회 - 여러 사용자")
        void findAll_multipleUsers_returnsList() {
            // given
            when(mockFileManager.readAll()).thenReturn(
                List.of(adminUser, csrUser, testUser)
            );

            // when
            List<User> users = repository.findAll();

            // then
            assertThat(users).hasSize(3);
            assertThat(users.get(0).getUserId()).isEqualTo("admin");
            assertThat(users.get(1).getUserId()).isEqualTo("csr1");
            assertThat(users.get(2).getUserId()).isEqualTo("testUser");
        }
    }

    @Nested
    @DisplayName("deleteById() 테스트")
    class DeleteByIdTests {

        @Test
        @DisplayName("존재하는 사용자 삭제 성공")
        void deleteById_existingUser_returnsTrue() {
            // given
            when(mockFileManager.readAll()).thenReturn(
                new ArrayList<>(List.of(adminUser, csrUser, testUser))
            );

            // when
            boolean result = repository.deleteById("csr1");

            // then
            assertThat(result).isTrue();
            
            ArgumentCaptor<List<User>> captor = ArgumentCaptor.forClass(List.class);
            verify(mockFileManager).writeAll(captor.capture());

            List<User> savedUsers = captor.getValue();
            assertThat(savedUsers).hasSize(2);
            assertThat(savedUsers).extracting(User::getUserId)
                .containsExactly("admin", "testUser");
        }

        @Test
        @DisplayName("존재하지 않는 사용자 삭제 시도 - 실패")
        void deleteById_nonExistingUser_returnsFalse() {
            // given
            when(mockFileManager.readAll()).thenReturn(
                new ArrayList<>(List.of(testUser))
            );

            // when
            boolean result = repository.deleteById("nonexistent");

            // then
            assertThat(result).isFalse();
            verify(mockFileManager, never()).writeAll(any());
        }

        @Test
        @DisplayName("빈 파일에서 삭제 시도 - 실패")
        void deleteById_emptyFile_returnsFalse() {
            // given
            when(mockFileManager.readAll()).thenReturn(new ArrayList<>());

            // when
            boolean result = repository.deleteById("testUser");

            // then
            assertThat(result).isFalse();
            verify(mockFileManager, never()).writeAll(any());
        }

        @Test
        @DisplayName("마지막 사용자 삭제")
        void deleteById_lastUser_leavesEmpty() {
            // given
            when(mockFileManager.readAll()).thenReturn(
                new ArrayList<>(List.of(testUser))
            );

            // when
            boolean result = repository.deleteById("testUser");

            // then
            assertThat(result).isTrue();
            
            ArgumentCaptor<List<User>> captor = ArgumentCaptor.forClass(List.class);
            verify(mockFileManager).writeAll(captor.capture());

            List<User> savedUsers = captor.getValue();
            assertThat(savedUsers).isEmpty();
        }
    }

    @Nested
    @DisplayName("existsById() 테스트")
    class ExistsByIdTests {

        @Test
        @DisplayName("존재하는 사용자 확인 - true")
        void existsById_existingUser_returnsTrue() {
            // given
            when(mockFileManager.readAll()).thenReturn(List.of(testUser));

            // when
            boolean result = repository.existsById("testUser");

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 사용자 확인 - false")
        void existsById_nonExistingUser_returnsFalse() {
            // given
            when(mockFileManager.readAll()).thenReturn(List.of(testUser));

            // when
            boolean result = repository.existsById("nonexistent");

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("빈 파일에서 확인 - false")
        void existsById_emptyFile_returnsFalse() {
            // given
            when(mockFileManager.readAll()).thenReturn(new ArrayList<>());

            // when
            boolean result = repository.existsById("testUser");

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("findAdmin() 테스트")
    class FindAdminTests {

        @Test
        @DisplayName("관리자 사용자 조회 성공")
        void findAdmin_existingAdmin_returnsAdmin() {
            // given
            when(mockFileManager.readAll()).thenReturn(     
                List.of(csrUser, adminUser, testUser)        
            );
      
            // when
            Optional<User> result = repository.findAdmin();

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getUserId()).isEqualTo("admin");
            assertThat(result.get().getRole()).isEqualTo(Role.ADMIN);
        }

        @Test
        @DisplayName("관리자 없을 때 빈 Optional 반환")
        void findAdmin_noAdmin_returnsEmpty() {
            // given
            when(mockFileManager.readAll()).thenReturn(
                List.of(csrUser, testUser)
            );

            // when
            Optional<User> result = repository.findAdmin();

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("여러 관리자 중 첫 번째 반환")
        void findAdmin_multipleAdmins_returnsFirst() {
            // given
            User admin1 = new User("admin1", "hash1", "관리자1", Role.ADMIN);
            User admin2 = new User("admin2", "hash2", "관리자2", Role.ADMIN);
            
            when(mockFileManager.readAll()).thenReturn(
                List.of(csrUser, admin1, admin2)
            );

            // when
            Optional<User> result = repository.findAdmin();

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getUserId()).isEqualTo("admin1");
        }

        @Test
        @DisplayName("빈 파일에서 관리자 조회 - 빈 Optional")
        void findAdmin_emptyFile_returnsEmpty() {
            // given
            when(mockFileManager.readAll()).thenReturn(new ArrayList<>());

            // when
            Optional<User> result = repository.findAdmin();

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("countAdmins() 테스트")
    class CountAdminsTests {

        @Test
        @DisplayName("관리자 수 정확히 카운트")
        void countAdmins_returnsCorrectCount() {
            // given
            User admin2 = new User("admin2", "hash2", "관리자2", Role.ADMIN);
            
            when(mockFileManager.readAll()).thenReturn(
                List.of(adminUser, csrUser, admin2, testUser)
            );

            // when
            long count = repository.countAdmins();

            // then
            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("관리자 없을 때 0 반환")
        void countAdmins_noAdmins_returnsZero() {
            // given
            when(mockFileManager.readAll()).thenReturn(
                List.of(csrUser, testUser)
            );

            // when
            long count = repository.countAdmins();

            // then
            assertThat(count).isEqualTo(0);
        }

        @Test
        @DisplayName("모든 사용자가 관리자일 때")
        void countAdmins_allAdmins_returnsTotal() {
            // given
            User admin2 = new User("admin2", "hash2", "관리자2", Role.ADMIN);
            User admin3 = new User("admin3", "hash3", "관리자3", Role.ADMIN);
            
            when(mockFileManager.readAll()).thenReturn(
                List.of(adminUser, admin2, admin3)
            );

            // when
            long count = repository.countAdmins();

            // then
            assertThat(count).isEqualTo(3);
        }

        @Test
        @DisplayName("빈 파일에서 관리자 카운트 - 0")
        void countAdmins_emptyFile_returnsZero() {
            // given
            when(mockFileManager.readAll()).thenReturn(new ArrayList<>());

            // when
            long count = repository.countAdmins();

            // then
            assertThat(count).isEqualTo(0);
        }

        @Test
        @DisplayName("단일 관리자만 존재")
        void countAdmins_singleAdmin_returnsOne() {
            // given
            when(mockFileManager.readAll()).thenReturn(List.of(adminUser));

            // when
            long count = repository.countAdmins();

            // then
            assertThat(count).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("통합 시나리오 테스트")
    class IntegrationScenarioTests {

        @Test
        @DisplayName("사용자 저장 → 조회 → 업데이트 → 삭제 시나리오")
        void fullCrudScenario() {
            // given - 초기 빈 상태
            List<User> users = new ArrayList<>();
            when(mockFileManager.readAll()).thenReturn(users);

            // 1. 새 사용자 저장
            repository.save(testUser);
            users.add(testUser);

            // 2. 조회 확인
            when(mockFileManager.readAll()).thenReturn(users);
            Optional<User> found = repository.findById("testUser");
            assertThat(found).isPresent();

            // 3. 업데이트
            User updatedUser = new User("testUser", "newHash", "새이름", Role.ADMIN);
            when(mockFileManager.readAll()).thenReturn(new ArrayList<>(users));
            repository.save(updatedUser);

            // 4. 삭제
            when(mockFileManager.readAll()).thenReturn(new ArrayList<>(List.of(updatedUser)));
            boolean deleted = repository.deleteById("testUser");
            assertThat(deleted).isTrue();
        }

        @Test
        @DisplayName("여러 사용자 관리 시나리오")
        void multipleUsersScenario() {
            // given
            List<User> users = new ArrayList<>();
            when(mockFileManager.readAll()).thenReturn(new ArrayList<>(users));

            // 1. 관리자 추가
            repository.save(adminUser);
            users.add(adminUser);

            // 2. 일반 사용자 추가

            repository.save(csrUser);
            users.add(csrUser);

            // 3. 관리자 수 확인
            when(mockFileManager.readAll()).thenReturn(users);
            long adminCount = repository.countAdmins();
            assertThat(adminCount).isEqualTo(1);

            // 4. 전체 사용자 조회
            List<User> allUsers = repository.findAll();
            assertThat(allUsers).hasSize(2);

            // 5. 관리자 조회
            Optional<User> admin = repository.findAdmin();
            assertThat(admin).isPresent();
            assertThat(admin.get().getUserId()).isEqualTo("admin");
        }
    }
}