package com.team3.util;

import static org.assertj.core.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.google.gson.reflect.TypeToken;
import com.team3.model.User;
import com.team3.model.User.Role;

@SuppressWarnings("unused")
@DisplayName("JsonFileManager 테스트")
class JsonFileManagerTest {

    private static final String TEST_FILE = "data/test-users.json";
    private JsonFileManager<User> fileManager;

    @BeforeEach
    void setUp() {
        fileManager = new JsonFileManager<>(TEST_FILE, new TypeToken<List<User>>() {});
    }

    @AfterEach
    void tearDown() throws IOException {
        // 테스트 파일 삭제
        Files.deleteIfExists(Paths.get(TEST_FILE));
        
        // 디렉토리도 비어있으면 삭제
        File dataDir = new File("data");
        if (dataDir.exists() && dataDir.isDirectory() && dataDir.list().length == 0) {
            dataDir.delete();
        }
    }

    @Nested
    @DisplayName("생성자 테스트")
    class ConstructorTests {

        @Test
        @DisplayName("파일이 없으면 자동 생성")
        void constructor_createsFileIfNotExists() {
            // given - setUp에서 생성됨

            // then
            File file = new File(TEST_FILE);
            assertThat(file).exists();
        }

        @Test
        @DisplayName("디렉토리가 없으면 자동 생성")
        void constructor_createsDirectoryIfNotExists() throws IOException {
            // given
            Files.deleteIfExists(Paths.get(TEST_FILE));
            File dataDir = new File("data");
            if (dataDir.exists()) {
                dataDir.delete();
            }

            // when
            JsonFileManager<User> tempManager = new JsonFileManager<>(TEST_FILE, new TypeToken<List<User>>() {});

            // then
            assertThat(dataDir).exists();
        }
    }

    @Nested
    @DisplayName("readAll() 테스트")
    class ReadAllTests {

        @Test
        @DisplayName("빈 파일 읽기 시 빈 리스트 반환")
        void readAll_withEmptyFile_returnsEmptyList() {
            // when
            List<User> users = fileManager.readAll();

            // then
            assertThat(users).isEmpty();
        }

        @Test
        @DisplayName("저장된 데이터 정상 읽기")
        void readAll_withSavedData_returnsData() {
            // given
            List<User> testUsers = new ArrayList<>();
            testUsers.add(new User("user1", "hash1", "name1", Role.CSR));
            testUsers.add(new User("user2", "hash2", "name2", Role.ADMIN));
            fileManager.writeAll(testUsers);

            // when
            List<User> users = fileManager.readAll();

            // then
            assertThat(users).hasSize(2);
            assertThat(users.get(0).getUserId()).isEqualTo("user1");
            assertThat(users.get(1).getUserId()).isEqualTo("user2");
        }
    }

    @Nested
    @DisplayName("writeAll() 테스트")
    class WriteAllTests {

        @Test
        @DisplayName("데이터 저장 후 파일 존재 확인")
        void writeAll_createsFile() {
            // given
            List<User> users = List.of(
                new User("user1", "hash1", "name1", Role.CSR)
            );

            // when
            fileManager.writeAll(users);

            // then
            File file = new File(TEST_FILE);
            assertThat(file).exists();
            assertThat(file.length()).isGreaterThan(0);
        }

        @Test
        @DisplayName("빈 리스트 저장 가능")
        void writeAll_withEmptyList_success() {
            // given
            List<User> emptyList = new ArrayList<>();

            // when
            fileManager.writeAll(emptyList);

            // then
            List<User> users = fileManager.readAll();
            assertThat(users).isEmpty();
        }

        @Test
        @DisplayName("여러 번 쓰기 가능 (덮어쓰기)")
        void writeAll_multipleWrites_overwrite() {
            // given
            List<User> users1 = List.of(new User("user1", "hash1", "name1", Role.CSR));
            List<User> users2 = List.of(new User("user2", "hash2", "name2", Role.ADMIN));

            // when
            fileManager.writeAll(users1);
            fileManager.writeAll(users2);

            // then
            List<User> users = fileManager.readAll();
            assertThat(users).hasSize(1);
            assertThat(users.get(0).getUserId()).isEqualTo("user2");
        }
    }

    @Nested
    @DisplayName("동시성 테스트")
    class ConcurrencyTests {

        @Test
        @DisplayName("동시 읽기 가능")
        void readAll_concurrentReads_success() throws InterruptedException {
            // given
            List<User> users = List.of(new User("user1", "hash1", "name1", Role.CSR));
            fileManager.writeAll(users);

            // when - 10개 스레드가 동시 읽기
            Thread[] threads = new Thread[10];
            for (int i = 0; i < 10; i++) {
                threads[i] = new Thread(() -> {
                    List<User> result = fileManager.readAll();
                    assertThat(result).hasSize(1);
                });
                threads[i].start();
            }

            // then
            for (Thread thread : threads) {
                thread.join();
            }
        }
    }
}