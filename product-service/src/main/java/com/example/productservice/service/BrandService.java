package com.example.productservice.service;

import com.example.productservice.dto.brand.BrandRequest;
import com.example.productservice.dto.brand.BrandResponse;
import com.example.productservice.entity.Brand;
import com.example.productservice.exception.exceptions.DuplicateResourceException;
import com.example.productservice.exception.exceptions.ResourceNotFoundException;
import com.example.productservice.mapper.BrandMapper;
import com.example.productservice.repository.BrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BrandService {

    private final BrandRepository brandRepository;

    @Transactional
    public Brand create(BrandRequest request) {
        String name = request.getName().trim();
        String slug = request.getSlug().trim();

        if (brandRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException("Brand name already exists");
        }

        if (brandRepository.existsBySlugIgnoreCase(slug)) {
            throw new DuplicateResourceException("Brand slug already exists");
        }

        Brand brand = BrandMapper.toEntity(request);
        brand.setName(name);
        brand.setSlug(slug);

        return brandRepository.save(brand);
    }

    @Transactional(readOnly = true)
    public Page<Brand> getAll(Pageable pageable) {
        return brandRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Brand getById(Long id) {
        return brandRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Brand not found"));
    }

    @Transactional
    public Brand update(Long id, BrandRequest request) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Brand not found"));

        String name = request.getName().trim();
        String slug = request.getSlug().trim();

        if (brandRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new DuplicateResourceException("Brand name already exists");
        }

        if (brandRepository.existsBySlugIgnoreCaseAndIdNot(slug, id)) {
            throw new DuplicateResourceException("Brand slug already exists");
        }

        brand.setName(name);
        brand.setSlug(slug);
        brand.setLogoUrl(request.getLogoUrl());

        return brandRepository.save(brand);
    }

    @Transactional
    public void delete(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Brand not found"));

        brandRepository.delete(brand);
    }


}
