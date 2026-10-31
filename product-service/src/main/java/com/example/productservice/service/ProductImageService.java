package com.example.productservice.service;

import com.example.productservice.dto.productimage.ProductImageRequest;
import com.example.productservice.entity.Product;
import com.example.productservice.entity.ProductImage;
import com.example.productservice.exception.exceptions.BusinessException;
import com.example.productservice.exception.exceptions.ResourceNotFoundException;
import com.example.productservice.repository.ProductImageRepository;
import com.example.productservice.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductImageService {

    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;

    @Transactional
    public ProductImage create(
            Long productId,
            ProductImageRequest request) {

        Product product = findProductById(productId);

        boolean isFirstImage = !productImageRepository.existsByProductId(productId);

        ProductImage image = ProductImage.builder()
                .product(product)
                .imageUrl(request.getImageUrl().trim())
                .sortOrder(
                        request.getSortOrder() != null
                                ? request.getSortOrder()
                                : 0
                )
                .isPrimary(isFirstImage)
                .build();

        return productImageRepository.save(image);
    }

    @Transactional(readOnly = true)
    public List<ProductImage> getAllByProductId(Long productId) {
        findProductById(productId);

        return productImageRepository.findByProductIdOrderBySortOrderAscIdAsc(productId);
    }

    @Transactional(readOnly = true)
    public Page<ProductImage> getPageByProductId(Long productId, Pageable pageable) {
        findProductById(productId);
        return productImageRepository.findByProductId(productId, pageable);
    }

    @Transactional(readOnly = true)
    public ProductImage getById(Long productId, Long imageId) {
        findProductById(productId);
        return findImageByIdAndProductId(imageId, productId);
    }

    @Transactional
    public ProductImage update(Long productId, Long imageId, ProductImageRequest request) {

        findProductById(productId);

        ProductImage image = findImageByIdAndProductId(imageId, productId);

        if (request.getImageUrl() != null) {
            image.setImageUrl(request.getImageUrl().trim());
        }

        if (request.getSortOrder() != null) {
            image.setSortOrder(request.getSortOrder());
        }

        return productImageRepository.save(image);
    }

    @Transactional
    public ProductImage setPrimary(Long productId, Long imageId) {
        findProductById(productId);
        ProductImage selected = findImageByIdAndProductId(imageId, productId);

        List<ProductImage> images = productImageRepository
                .findByProductIdOrderBySortOrderAscIdAsc(productId);

        for (ProductImage image : images) {
            image.setIsPrimary(image.getId().equals(selected.getId()));
        }

        productImageRepository.saveAll(images);
        return selected;
    }

    @Transactional
    public void delete(Long productId, Long imageId) {
        findProductById(productId);
        ProductImage image = findImageByIdAndProductId(imageId, productId);
        if (Boolean.TRUE.equals(image.getIsPrimary())) {
            throw new BusinessException("Không thể xóa ảnh chính của sản phẩm");
        }
        productImageRepository.delete(image);
    }

    private Product findProductById(Long productId) {
        return productRepository.findById(productId).orElseThrow(() ->
                new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: "+ productId));
    }

    private ProductImage findImageByIdAndProductId(Long imageId, Long productId) {

        return productImageRepository
                .findByIdAndProductId(imageId, productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy hình ảnh ID: " + imageId
                        + " thuộc sản phẩm ID: " + productId
                ));
    }
}