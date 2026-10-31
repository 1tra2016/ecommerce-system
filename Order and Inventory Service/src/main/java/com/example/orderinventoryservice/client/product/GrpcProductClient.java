package com.example.orderinventoryservice.client.product;

import com.example.product.grpc.GetVariantsRequest;
import com.example.product.grpc.GetVariantsResponse;
import com.example.product.grpc.ProductServiceGrpc;
import com.example.product.grpc.ProductVariantInfo;
import com.example.orderinventoryservice.exception.exceptions.BusinessException;
import com.example.orderinventoryservice.exception.exceptions.ServiceUnavailableException;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class GrpcProductClient implements ProductGrpcClient {

    @GrpcClient("product-service")
    private ProductServiceGrpc.ProductServiceBlockingStub productService;

    @Override
    public Map<Long, ProductInfo> getVariants(Collection<Long> variantIds) {
        if (variantIds == null || variantIds.isEmpty()) {
            return Map.of();
        }

        try {
            GetVariantsResponse response = productService.getVariants(
                    GetVariantsRequest.newBuilder()
                            .addAllVariantIds(variantIds)
                            .build());
            System.out.println("danh sách variantIds: " +variantIds);

            return response.getVariantsList().stream()
                    .map(this::toProductInfo)
                    .collect(Collectors.toUnmodifiableMap(
                            ProductInfo::variantId,
                            Function.identity()));
        } catch (StatusRuntimeException exception) {
            if (exception.getStatus().getCode() == Status.Code.NOT_FOUND) {
                throw new BusinessException("One or more product variants do not exist");
            }
            if (exception.getStatus().getCode() == Status.Code.INVALID_ARGUMENT) {
                throw new BusinessException("Product Service rejected the variant IDs");
            }
            throw new ServiceUnavailableException("Product Service is unavailable");
        } catch (NumberFormatException exception) {
            throw new ServiceUnavailableException("Product Service returned an invalid price");
        }
    }

    private ProductInfo toProductInfo(ProductVariantInfo variant) {
        return new ProductInfo(
                variant.getProductId(),
                variant.getVariantId(),
                variant.getSku(),
                variant.getProductName(),
                new BigDecimal(variant.getPrice()),
                variant.getActive(),
                variant.getActive()
        );
    }
}
