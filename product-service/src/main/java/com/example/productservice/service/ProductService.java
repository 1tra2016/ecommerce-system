package com.example.productservice.service;

import com.example.productservice.dto.product.ProductRequest;
import com.example.productservice.entity.Brand;
import com.example.productservice.entity.Category;
import com.example.productservice.entity.Product;
import com.example.productservice.exception.exceptions.DuplicateResourceException;
import com.example.productservice.exception.exceptions.ResourceNotFoundException;
import com.example.productservice.mapper.ProductMapper;
import com.example.productservice.repository.BrandRepository;
import com.example.productservice.repository.CategoryRepository;
import com.example.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import com.example.productservice.enums.ProductStatus;

import java.math.BigDecimal;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public Product create(ProductRequest request) {
        if (productRepository.existsByNameIgnoreCase(request.getName().trim())) {
            throw new DuplicateResourceException("Tên sản phẩm đã tồn tại");
        }

        if (productRepository.existsBySlugIgnoreCase(request.getSlug().trim())) {
            throw new DuplicateResourceException("Slug sản phẩm đã tồn tại");
        }

        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Không tìm thấy Brand"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Không tìm thấy Category"));

        Product product = ProductMapper.toEntity(request, brand, category);
        return productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public Page<Product> getAll(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Product> search(String keyword, Long categoryId, Long brandId,
                                ProductStatus status, BigDecimal minPrice,
                                BigDecimal maxPrice, Pageable pageable) {
        String keywordPattern = keyword == null || keyword.isBlank()
                ? "%" : "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%";
        return productRepository.search(keywordPattern, categoryId, brandId, status, minPrice, maxPrice, pageable);
    }

    @Transactional(readOnly = true)
    public Product getById(Long id) {
        return findProductById(id);
    }

    @Transactional
    public Product update(Long id, ProductRequest request) {
        Product product = findProductById(id);

        if (productRepository.existsByNameIgnoreCaseAndIdNot(
                request.getName().trim(), id)) {
            throw new DuplicateResourceException("Tên sản phẩm đã tồn tại");
        }

        if (productRepository.existsBySlugIgnoreCaseAndIdNot(
                request.getSlug().trim(), id)) {
            throw new DuplicateResourceException("Slug sản phẩm đã tồn tại");
        }

        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Không tìm thấy Brand"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Không tìm thấy Category"));

        product.setName(request.getName().trim());
        product.setSlug(request.getSlug().trim());
        product.setDescription(request.getDescription());
        product.setBasePrice(request.getBasePrice());

        if (request.getStatus() != null) {
            product.setStatus(request.getStatus());
        }

        product.setBrand(brand);
        product.setCategory(category);

        return productRepository.save(product);
    }

    @Transactional
    public void delete(Long id) {
        Product product = findProductById(id);
        productRepository.delete(product);
    }

    private Product findProductById(Long id) {
        return productRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + id));
    }
}
