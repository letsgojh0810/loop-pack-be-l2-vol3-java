package com.loopers.domain.like;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ProductLikeServiceTest {

    private ProductLikeService productLikeService;
    private FakeProductLikeRepository fakeProductLikeRepository;

    private static final Long USER_ID = 1L;
    private static final Long PRODUCT_ID = 100L;

    @BeforeEach
    void setUp() {
        fakeProductLikeRepository = new FakeProductLikeRepository();
        productLikeService = new ProductLikeService(fakeProductLikeRepository);
    }

    @DisplayName("좋아요 등록 시,")
    @Nested
    class Like {

        @DisplayName("좋아요가 정상적으로 등록된다.")
        @Test
        void likes_successfully() {
            // arrange & act
            productLikeService.like(USER_ID, PRODUCT_ID);

            // assert
            assertThat(productLikeService.isLiked(USER_ID, PRODUCT_ID)).isTrue();
        }

        @DisplayName("이미 좋아요한 상품에 다시 좋아요를 눌러도 예외가 발생하지 않는다.")
        @Test
        void doesNotThrow_whenAlreadyLiked() {
            // arrange
            productLikeService.like(USER_ID, PRODUCT_ID);

            // act & assert
            assertDoesNotThrow(() -> productLikeService.like(USER_ID, PRODUCT_ID));
        }

        @DisplayName("이미 좋아요한 상품에 다시 좋아요를 눌러도 좋아요 수는 1이다.")
        @Test
        void likeCount_remainsOne_whenLikedAgain() {
            // arrange
            productLikeService.like(USER_ID, PRODUCT_ID);
            productLikeService.like(USER_ID, PRODUCT_ID);

            // assert
            assertThat(productLikeService.getLikeCount(PRODUCT_ID)).isEqualTo(1L);
        }
    }

    @DisplayName("좋아요 취소 시,")
    @Nested
    class Unlike {

        @DisplayName("좋아요가 정상적으로 취소된다.")
        @Test
        void unlikes_successfully() {
            // arrange
            productLikeService.like(USER_ID, PRODUCT_ID);

            // act
            productLikeService.unlike(USER_ID, PRODUCT_ID);

            // assert
            assertThat(productLikeService.isLiked(USER_ID, PRODUCT_ID)).isFalse();
        }

        @DisplayName("좋아요하지 않은 상품에 좋아요 취소를 해도 예외가 발생하지 않는다.")
        @Test
        void doesNotThrow_whenNotLiked() {
            // arrange & act & assert
            assertDoesNotThrow(() -> productLikeService.unlike(USER_ID, PRODUCT_ID));
        }
    }

    @DisplayName("좋아요 수 조회 시,")
    @Nested
    class GetLikeCount {

        @DisplayName("여러 사용자가 좋아요한 경우 올바른 좋아요 수를 반환한다.")
        @Test
        void returnsCorrectCount() {
            // arrange
            productLikeService.like(1L, PRODUCT_ID);
            productLikeService.like(2L, PRODUCT_ID);
            productLikeService.like(3L, PRODUCT_ID);

            // act
            long count = productLikeService.getLikeCount(PRODUCT_ID);

            // assert
            assertThat(count).isEqualTo(3L);
        }

        @DisplayName("좋아요가 없으면 0을 반환한다.")
        @Test
        void returnsZero_whenNoLikes() {
            // arrange & act
            long count = productLikeService.getLikeCount(PRODUCT_ID);

            // assert
            assertThat(count).isEqualTo(0L);
        }
    }

    @DisplayName("좋아요 여부 조회 시,")
    @Nested
    class IsLiked {

        @DisplayName("좋아요한 경우 true를 반환한다.")
        @Test
        void returnsTrue_whenLiked() {
            // arrange
            productLikeService.like(USER_ID, PRODUCT_ID);

            // act & assert
            assertThat(productLikeService.isLiked(USER_ID, PRODUCT_ID)).isTrue();
        }

        @DisplayName("좋아요하지 않은 경우 false를 반환한다.")
        @Test
        void returnsFalse_whenNotLiked() {
            // arrange & act & assert
            assertThat(productLikeService.isLiked(USER_ID, PRODUCT_ID)).isFalse();
        }
    }
}
