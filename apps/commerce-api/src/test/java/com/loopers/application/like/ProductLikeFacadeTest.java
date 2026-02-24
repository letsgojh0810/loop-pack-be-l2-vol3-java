package com.loopers.application.like;

import com.loopers.domain.like.FakeProductLikeRepository;
import com.loopers.domain.like.ProductLikeService;
import com.loopers.domain.product.FakeProductRepository;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductLikeFacadeTest {

    private ProductLikeFacade productLikeFacade;
    private FakeProductRepository fakeProductRepository;
    private FakeProductLikeRepository fakeProductLikeRepository;

    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        fakeProductRepository = new FakeProductRepository();
        fakeProductLikeRepository = new FakeProductLikeRepository();
        ProductService productService = new ProductService(fakeProductRepository);
        ProductLikeService productLikeService = new ProductLikeService(fakeProductLikeRepository);
        productLikeFacade = new ProductLikeFacade(productService, productLikeService);
    }

    @DisplayName("좋아요 등록 시,")
    @Nested
    class Like {

        @DisplayName("상품이 존재하면 좋아요가 정상적으로 등록된다.")
        @Test
        void likes_whenProductExists() {
            // arrange
            Product product = fakeProductRepository.save(new Product(1L, "상품명", "설명", 1000, 10, "http://image.url"));
            Long productId = product.getId();

            // act
            productLikeFacade.like(USER_ID, productId);

            // assert
            ProductLikeInfo info = productLikeFacade.getLikeInfo(USER_ID, productId);
            assertAll(
                () -> assertThat(info.liked()).isTrue(),
                () -> assertThat(info.likeCount()).isEqualTo(1L)
            );
        }

        @DisplayName("존재하지 않는 상품에 좋아요를 누르면 NOT_FOUND 예외가 발생한다.")
        @Test
        void throwsNotFound_whenProductDoesNotExist() {
            // arrange
            Long nonExistentProductId = 999L;

            // act
            CoreException result = assertThrows(CoreException.class, () ->
                productLikeFacade.like(USER_ID, nonExistentProductId)
            );

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        }
    }

    @DisplayName("좋아요 정보 조회 시,")
    @Nested
    class GetLikeInfo {

        @DisplayName("좋아요 수와 좋아요 여부를 올바르게 반환한다.")
        @Test
        void returnsCorrectLikeInfo() {
            // arrange
            Product product = fakeProductRepository.save(new Product(1L, "상품명", "설명", 1000, 10, "http://image.url"));
            Long productId = product.getId();
            productLikeFacade.like(USER_ID, productId);
            productLikeFacade.like(2L, productId);

            // act
            ProductLikeInfo info = productLikeFacade.getLikeInfo(USER_ID, productId);

            // assert
            assertAll(
                () -> assertThat(info.productId()).isEqualTo(productId),
                () -> assertThat(info.likeCount()).isEqualTo(2L),
                () -> assertThat(info.liked()).isTrue()
            );
        }

        @DisplayName("좋아요하지 않은 사용자에 대해 liked가 false로 반환된다.")
        @Test
        void returnsLikedFalse_whenUserHasNotLiked() {
            // arrange
            Product product = fakeProductRepository.save(new Product(1L, "상품명", "설명", 1000, 10, "http://image.url"));
            Long productId = product.getId();
            productLikeFacade.like(2L, productId);

            // act
            ProductLikeInfo info = productLikeFacade.getLikeInfo(USER_ID, productId);

            // assert
            assertAll(
                () -> assertThat(info.productId()).isEqualTo(productId),
                () -> assertThat(info.likeCount()).isEqualTo(1L),
                () -> assertThat(info.liked()).isFalse()
            );
        }
    }
}
