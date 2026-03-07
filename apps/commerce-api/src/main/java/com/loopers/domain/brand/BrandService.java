package com.loopers.domain.brand;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Component
public class BrandService {

    private final BrandRepository brandRepository;

    @Transactional
    public Brand register(String name, String description, String imageUrl) {
        if (brandRepository.existsByName(name)) {
            throw new CoreException(ErrorType.CONFLICT, "이미 존재하는 브랜드 이름입니다.");
        }

        Brand brand = new Brand(name, description, imageUrl);
        return brandRepository.save(brand);
    }

    @Transactional(readOnly = true)
    public Brand getBrand(Long id) {
        return brandRepository.findById(id)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "브랜드를 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public List<Brand> getAllBrands() {
        return brandRepository.findAll();
    }

    @Transactional
    public Brand update(Long id, String name, String description, String imageUrl) {
        Brand brand = brandRepository.findById(id)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "브랜드를 찾을 수 없습니다."));

        brandRepository.findByName(name).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new CoreException(ErrorType.CONFLICT, "이미 존재하는 브랜드 이름입니다.");
            }
        });

        brand.update(name, description, imageUrl);
        return brand;
    }

    @Transactional
    public void delete(Long id) {
        Brand brand = brandRepository.findById(id)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "브랜드를 찾을 수 없습니다."));

        brand.delete();
    }
}
