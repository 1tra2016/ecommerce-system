package com.example.productservice.service;

import com.example.productservice.dto.productvariant.ProductVariantRequest;
import com.example.productservice.entity.Product;
import com.example.productservice.entity.ProductVariant;
import com.example.productservice.exception.exceptions.BusinessException;
import com.example.productservice.exception.exceptions.ConflictException;
import com.example.productservice.exception.exceptions.ResourceNotFoundException;
import com.example.productservice.mapper.ProductVariantMapper;
import com.example.productservice.repository.ProductRepository;
import com.example.productservice.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.math.BigDecimal;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ProductVariantService {

    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository;

    @Transactional
    public ProductVariant create(Long productId, ProductVariantRequest request) {
        Product product = findProductById(productId);
        String sku = request.getSku().trim();

        if (productVariantRepository.existsBySku(sku)) {
            throw new ConflictException("SKU đã tồn tại: " + sku);
        }
        ProductVariant variant = ProductVariantMapper.toEntity(request, product);
        variant.setSku(sku);

        return productVariantRepository.save(variant);
    }

    @Transactional(readOnly = true)
    public Page<ProductVariant> getAllByProductId(Long productId, Pageable pageable) {
        findProductById(productId);
        return productVariantRepository.findByProductId(productId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ProductVariant> searchByProductId(Long productId, String keyword,
                                                   Boolean active, BigDecimal minPrice,
                                                   BigDecimal maxPrice, Pageable pageable) {
        findProductById(productId);
        String keywordPattern = keyword == null || keyword.isBlank()
                ? "%" : "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%";
        return productVariantRepository.searchByProductId(productId,
                keywordPattern, active, minPrice, maxPrice, pageable);
    }

    @Transactional(readOnly = true)
    public ProductVariant getById(
            Long productId,
            Long variantId
    ) {
        findProductById(productId);

        return findVariantByIdAndProductId(variantId, productId);
    }

    @Transactional
    public ProductVariant update(
            Long productId,
            Long variantId,
            ProductVariantRequest request
    ) {
        findProductById(productId);

        ProductVariant variant =
                findVariantByIdAndProductId(variantId, productId);

        if (request.getSku() != null) {
            String sku = request.getSku().trim();

            if (productVariantRepository.existsBySkuAndIdNot(sku, variantId)) {
                throw new ConflictException("SKU đã tồn tại: " + sku);
            }

            variant.setSku(sku);
        }

        if (request.getAttributes() != null) {
            variant.setAttributes(request.getAttributes());
        }

        if (request.getPrice() != null) {
            variant.setPrice(request.getPrice());
        }

        if (request.getActive() != null) {
            variant.setActive(request.getActive());
        }

        return productVariantRepository.save(variant);
    }

    @Transactional
    public void delete(Long productId, Long variantId) {
        findProductById(productId);

        ProductVariant variant =
                findVariantByIdAndProductId(variantId, productId);

        productVariantRepository.delete(variant);
    }

    private Product findProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy sản phẩm với ID: " + productId
                ));
    }

    private ProductVariant findVariantByIdAndProductId(
            Long variantId,
            Long productId
    ) {
        return productVariantRepository
                .findByIdAndProductId(variantId, productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy variant ID: " + variantId
                                + " thuộc sản phẩm ID: " + productId
                ));
    }

    @Transactional(readOnly = true)
    public List<ProductVariant> getVariantsByIds(Collection<Long> variantIds) {
        return productVariantRepository.findByIdIn(variantIds);
    }
}
