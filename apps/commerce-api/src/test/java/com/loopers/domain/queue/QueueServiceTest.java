package com.loopers.domain.queue;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class QueueServiceTest {

    private QueueService queueService;
    private FakeQueueRepository fakeQueueRepository;

    @BeforeEach
    void setUp() {
        fakeQueueRepository = new FakeQueueRepository();
        queueService = new QueueService(fakeQueueRepository);
    }

    @DisplayName("토큰 만료 테스트")
    @Nested
    class TokenExpiry {

        @DisplayName("TTL이 만료된 토큰으로 검증하면 FORBIDDEN 예외가 발생한다.")
        @Test
        void throwsForbidden_whenTokenExpired() {
            // arrange - 토큰 발급 후 만료 처리
            Long userId = 1L;
            queueService.issueTokens(0); // 빈 큐에서 발급 시도 (no-op)
            fakeQueueRepository.saveToken(userId, "test-token", 300);
            fakeQueueRepository.saveHardToken(userId, 600);
            fakeQueueRepository.expireToken(userId); // Hard TTL 만료

            // act & assert
            CoreException exception = assertThrows(CoreException.class,
                () -> queueService.validateToken(userId, "test-token")
            );
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.FORBIDDEN);
        }

        @DisplayName("유효한 토큰으로 검증하면 예외가 발생하지 않는다.")
        @Test
        void doesNotThrow_whenTokenValid() {
            // arrange
            Long userId = 1L;
            fakeQueueRepository.saveToken(userId, "valid-token", 300);
            fakeQueueRepository.saveHardToken(userId, 600);

            // act & assert - 예외 없이 통과
            queueService.validateToken(userId, "valid-token");
        }

        @DisplayName("잘못된 토큰 값으로 검증하면 FORBIDDEN 예외가 발생한다.")
        @Test
        void throwsForbidden_whenTokenMismatch() {
            // arrange
            Long userId = 1L;
            fakeQueueRepository.saveToken(userId, "correct-token", 300);
            fakeQueueRepository.saveHardToken(userId, 600);

            // act & assert
            CoreException exception = assertThrows(CoreException.class,
                () -> queueService.validateToken(userId, "wrong-token")
            );
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.FORBIDDEN);
        }

        @DisplayName("대기열이 비활성화 상태면 토큰 없이도 통과한다.")
        @Test
        void doesNotThrow_whenQueueDisabled() {
            // arrange
            fakeQueueRepository.setEnabled(false);

            // act & assert - 토큰 없어도 통과
            queueService.validateToken(99L, null);
        }
    }

    @DisplayName("처리량 초과 테스트")
    @Nested
    class BatchSize {

        @DisplayName("대기 인원이 1000명 이상이면 배치 크기가 200이다.")
        @Test
        void returnsBatchSize200_whenWaitingOver1000() {
            // arrange - 1000명 대기
            for (long i = 1; i <= 1000; i++) {
                fakeQueueRepository.enter(i, i);
            }

            // act
            int batchSize = queueService.getBatchSize();

            // assert
            assertThat(batchSize).isEqualTo(200);
        }

        @DisplayName("대기 인원이 1000명 미만이면 배치 크기가 50이다.")
        @Test
        void returnsBatchSize50_whenWaitingUnder1000() {
            // arrange - 500명 대기
            for (long i = 1; i <= 500; i++) {
                fakeQueueRepository.enter(i, i);
            }

            // act
            int batchSize = queueService.getBatchSize();

            // assert
            assertThat(batchSize).isEqualTo(50);
        }

        @DisplayName("배치 크기만큼만 토큰이 발급되고 대기열에서 제거된다.")
        @Test
        void issuesTokens_upToBatchSize() {
            // arrange - 100명 대기
            for (long i = 1; i <= 100; i++) {
                fakeQueueRepository.enter(i, i);
            }

            // act - 50명 발급
            List<Long> issued = queueService.issueTokens(50);

            // assert
            assertThat(issued).hasSize(50);
            assertThat(queueService.getWaitingCount()).isEqualTo(50);
        }

        @DisplayName("대기열보다 큰 배치 크기를 요청해도 대기 인원만큼만 발급된다.")
        @Test
        void issuesOnlyAvailable_whenBatchExceedsQueue() {
            // arrange - 10명 대기
            for (long i = 1; i <= 10; i++) {
                fakeQueueRepository.enter(i, i);
            }

            // act - 200명 요청하지만 10명만 있음
            List<Long> issued = queueService.issueTokens(200);

            // assert
            assertThat(issued).hasSize(10);
            assertThat(queueService.getWaitingCount()).isEqualTo(0);
        }
    }

    @DisplayName("대기열 진입 테스트")
    @Nested
    class QueueEntry {

        @DisplayName("같은 유저가 중복 진입해도 순번이 변하지 않는다.")
        @Test
        void returnsSamePosition_whenDuplicateEntry() {
            // arrange
            Long userId = 1L;
            long firstPosition = queueService.enter(userId);

            // act - 중복 진입
            long secondPosition = queueService.enter(userId);

            // assert
            assertThat(firstPosition).isEqualTo(secondPosition);
            assertThat(queueService.getWaitingCount()).isEqualTo(1);
        }

        @DisplayName("순서대로 진입한 유저들은 모두 대기열에 등록된다.")
        @Test
        void registersAllUsers_inOrder() throws InterruptedException {
            // arrange & act - 각 진입 사이에 충분한 간격
            long pos1 = queueService.enter(1L);
            Thread.sleep(2);
            long pos2 = queueService.enter(2L);
            Thread.sleep(2);
            long pos3 = queueService.enter(3L);

            // assert - 3명 모두 등록, 순번은 1~3 범위
            assertThat(queueService.getWaitingCount()).isEqualTo(3);
            assertThat(pos1).isLessThan(pos2);
            assertThat(pos2).isLessThan(pos3);
        }
    }
}
