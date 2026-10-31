package com.example.orderinventoryservice.client.product;

import java.util.Collection;
import java.util.Map;

public interface ProductGrpcClient {

    /**
     * Lấy thông tin các variant trong một lần gọi gRPC.
     * Map được đánh khóa bằng variantId.
     */
    Map<Long, ProductInfo> getVariants(
            Collection<Long> variantIds
    );
}
