package com.example.productservice.grpc;
import com.example.product.grpc.GetVariantsRequest;
import com.example.product.grpc.GetVariantsResponse;
import com.example.product.grpc.ProductServiceGrpc;
import com.example.product.grpc.ProductVariantInfo;
import com.example.productservice.service.ProductVariantService;
import com.example.productservice.entity.ProductVariant;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@GrpcService
@RequiredArgsConstructor
public class ProductGrpcService extends ProductServiceGrpc.ProductServiceImplBase {

    private final ProductVariantService productVariantService;

    @Override
    public void getVariants(GetVariantsRequest request, StreamObserver<GetVariantsResponse> observer) {
        try {
            Set<Long> variantIds = request.getVariantIdsList()
                    .stream()
                    .collect(Collectors.toCollection(HashSet::new));

            if (variantIds.stream().anyMatch(id -> id <= 0)) {
                observer.onError(Status.INVALID_ARGUMENT
                        .withDescription("variant_ids must contain positive IDs")
                        .asRuntimeException());
                return;
            }

            if (variantIds.isEmpty()) {
                observer.onNext(
                        GetVariantsResponse.newBuilder().build()
                );
                observer.onCompleted();
                return;
            }

            List<ProductVariant> variants = productVariantService.getVariantsByIds(variantIds);

            Set<Long> foundIds = variants.stream()
                    .map(ProductVariant::getId)
                    .collect(Collectors.toSet());
            Set<Long> missingIds = new HashSet<>(variantIds);
            missingIds.removeAll(foundIds);
            if (!missingIds.isEmpty()) {
                observer.onError(Status.NOT_FOUND
                        .withDescription("Product variants not found: " + missingIds)
                        .asRuntimeException());
                return;
            }

            GetVariantsResponse.Builder response =
                    GetVariantsResponse.newBuilder();

            for (ProductVariant variant : variants) {
                ProductVariantInfo info =
                        ProductVariantInfo.newBuilder()
                                .setVariantId(variant.getId())
                                .setProductId(variant.getProduct().getId())
                                .setProductName(
                                        variant.getProduct().getName()
                                )
                                .setSku(variant.getSku())
                                .setAttributes(
                                        variant.getAttributes() == null
                                                ? ""
                                                : variant.getAttributes()
                                )
                                .setPrice(
                                        variant.getPrice().toPlainString()
                                )
                                .setActive(
                                        Boolean.TRUE.equals(
                                                variant.getActive()
                                        )
                                )
                                .build();

                response.addVariants(info);
            }

            observer.onNext(response.build());
            observer.onCompleted();

        } catch (Exception exception) {
            observer.onError(
                    Status.INTERNAL.withDescription("Unable to retrieve product variants")
                            .withCause(exception)
                            .asRuntimeException()
            );
        }
    }
}
