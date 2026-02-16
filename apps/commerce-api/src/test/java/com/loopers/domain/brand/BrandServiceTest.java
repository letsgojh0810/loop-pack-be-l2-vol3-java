package com.loopers.domain.brand;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BrandServiceTest {

    private BrandRepository brandRepository;
    private BrandService brandService;

    private static final String VALID_NAME = "나이키";
    private static final String VALID_DESCRIPTION = "스포츠 브랜드";
    private static final String VALID_IMAGE_URL = "https://example.com/nike.png";

    @BeforeEach
    void setUp() {
        brandRepository = new FakeBrandRepository();
        brandService = new BrandService(brandRepository);
    }

    @DisplayName("브랜드 등록 시,")
    @Nested
    class Register {

        @DisplayName("유효한 정보로 등록하면, 브랜드가 생성된다.")
        @Test
        void createsBrand_whenValidInfoIsProvided() {
            // arrange & act
            Brand result = brandService.register(VALID_NAME, VALID_DESCRIPTION, VALID_IMAGE_URL);

            // assert
            assertAll(
                () -> assertThat(result.getId()).isNotNull(),
                () -> assertThat(result.getName()).isEqualTo(VALID_NAME),
                () -> assertThat(result.getDescription()).isEqualTo(VALID_DESCRIPTION),
                () -> assertThat(result.getImageUrl()).isEqualTo(VALID_IMAGE_URL)
            );
        }

        @DisplayName("이미 존재하는 이름으로 등록하면, CONFLICT 예외가 발생한다.")
        @Test
        void throwsConflict_whenNameAlreadyExists() {
            // arrange
            brandService.register(VALID_NAME, VALID_DESCRIPTION, VALID_IMAGE_URL);

            // act
            CoreException result = assertThrows(CoreException.class, () ->
                brandService.register(VALID_NAME, "다른 설명", "https://other.com/image.png")
            );

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.CONFLICT);
        }
    }

    @DisplayName("브랜드 단건 조회 시,")
    @Nested
    class GetBrand {

        @DisplayName("존재하는 ID로 조회하면, 브랜드 정보를 반환한다.")
        @Test
        void returnsBrand_whenBrandExists() {
            // arrange
            Brand saved = brandService.register(VALID_NAME, VALID_DESCRIPTION, VALID_IMAGE_URL);

            // act
            Brand result = brandService.getBrand(saved.getId());

            // assert
            assertAll(
                () -> assertThat(result.getId()).isEqualTo(saved.getId()),
                () -> assertThat(result.getName()).isEqualTo(VALID_NAME)
            );
        }

        @DisplayName("존재하지 않는 ID로 조회하면, NOT_FOUND 예외가 발생한다.")
        @Test
        void throwsNotFound_whenBrandDoesNotExist() {
            // arrange
            Long nonExistentId = 999L;

            // act
            CoreException result = assertThrows(CoreException.class, () ->
                brandService.getBrand(nonExistentId)
            );

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        }
    }

    @DisplayName("브랜드 목록 조회 시,")
    @Nested
    class GetAllBrands {

        @DisplayName("등록된 브랜드가 있으면, 목록을 반환한다.")
        @Test
        void returnsBrandList_whenBrandsExist() {
            // arrange
            brandService.register(VALID_NAME, VALID_DESCRIPTION, VALID_IMAGE_URL);
            brandService.register("아디다스", "또 다른 스포츠 브랜드", "https://example.com/adidas.png");

            // act
            List<Brand> result = brandService.getAllBrands();

            // assert
            assertThat(result).hasSize(2);
        }
    }

    @DisplayName("브랜드 수정 시,")
    @Nested
    class UpdateBrand {

        @DisplayName("유효한 정보로 수정하면, 브랜드가 수정된다.")
        @Test
        void updatesBrand_whenValidInfoIsProvided() {
            // arrange
            Brand saved = brandService.register(VALID_NAME, VALID_DESCRIPTION, VALID_IMAGE_URL);

            // act
            Brand result = brandService.update(saved.getId(), "아디다스", "새 설명", "https://new.com/image.png");

            // assert
            assertAll(
                () -> assertThat(result.getName()).isEqualTo("아디다스"),
                () -> assertThat(result.getDescription()).isEqualTo("새 설명"),
                () -> assertThat(result.getImageUrl()).isEqualTo("https://new.com/image.png")
            );
        }

        @DisplayName("다른 브랜드가 이미 사용 중인 이름으로 수정하면, CONFLICT 예외가 발생한다.")
        @Test
        void throwsConflict_whenNameIsUsedByAnotherBrand() {
            // arrange
            brandService.register(VALID_NAME, VALID_DESCRIPTION, VALID_IMAGE_URL);
            Brand second = brandService.register("아디다스", "또 다른 브랜드", "https://adidas.com/img.png");

            // act
            CoreException result = assertThrows(CoreException.class, () ->
                brandService.update(second.getId(), VALID_NAME, "새 설명", "https://new.com/img.png")
            );

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.CONFLICT);
        }

        @DisplayName("존재하지 않는 ID로 수정하면, NOT_FOUND 예외가 발생한다.")
        @Test
        void throwsNotFound_whenBrandDoesNotExist() {
            // arrange
            Long nonExistentId = 999L;

            // act
            CoreException result = assertThrows(CoreException.class, () ->
                brandService.update(nonExistentId, VALID_NAME, VALID_DESCRIPTION, VALID_IMAGE_URL)
            );

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        }
    }

    @DisplayName("브랜드 삭제 시,")
    @Nested
    class DeleteBrand {

        @DisplayName("존재하는 ID로 삭제하면, 소프트 삭제된다.")
        @Test
        void softDeletesBrand_whenBrandExists() {
            // arrange
            Brand saved = brandService.register(VALID_NAME, VALID_DESCRIPTION, VALID_IMAGE_URL);

            // act
            brandService.delete(saved.getId());

            // assert
            Brand deleted = brandRepository.findById(saved.getId()).orElseThrow();
            assertThat(deleted.getDeletedAt()).isNotNull();
        }

        @DisplayName("존재하지 않는 ID로 삭제하면, NOT_FOUND 예외가 발생한다.")
        @Test
        void throwsNotFound_whenBrandDoesNotExist() {
            // arrange
            Long nonExistentId = 999L;

            // act
            CoreException result = assertThrows(CoreException.class, () ->
                brandService.delete(nonExistentId)
            );

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        }
    }
}
