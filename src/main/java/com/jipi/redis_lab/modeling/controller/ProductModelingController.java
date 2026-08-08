package com.jipi.redis_lab.modeling.controller;

import com.jipi.redis_lab.modeling.dto.*;
import com.jipi.redis_lab.modeling.model.ProductSnapshot;
import com.jipi.redis_lab.modeling.service.ProductRedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 12강: 자료구조 선택과 Key 모델링 실습 API를 제공한다.
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/modeling")
public class ProductModelingController {

    private final ProductRedisService productRedisService;

    @PostMapping("/products/{productId}")
    public ResponseEntity<ProductResponse> saveProduct(
            @PathVariable("productId") long productId,
            @RequestBody ProductSaveRequest request
    ) {
        productRedisService.saveProduct(
                productId,
                request.name(),
                request.price()
        );

        return productRedisService
                .findProduct(productId)
                .map(product ->
                        ResponseEntity.ok(
                                toResponse(product)
                        )
                )
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }

    @PostMapping("/products/{productId}/views")
    public ResponseEntity<ProductResponse> increaseViewCount(
            @PathVariable("productId") long productId
    ) {
        if (productRedisService
                .findProduct(productId)
                .isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        productRedisService
                .increaseViewCount(productId);

        return productRedisService
                .findProduct(productId)
                .map(product ->
                        ResponseEntity.ok(
                                toResponse(product)
                        )
                )
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }

    @PostMapping("/products/{productId}/likes")
    public ResponseEntity<ProductResponse> addLike(
            @PathVariable("productId") long productId,
            @RequestBody ProductLikeRequest request
    ) {
        if (productRedisService
                .findProduct(productId)
                .isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        productRedisService.addLike(
                productId,
                request.userId()
        );

        return productRedisService
                .findProduct(productId)
                .map(product ->
                        ResponseEntity.ok(
                                toResponse(product)
                        )
                )
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<ProductResponse> findProduct(
            @PathVariable("productId") long productId
    ) {
        return productRedisService
                .findProduct(productId)
                .map(product ->
                        ResponseEntity.ok(
                                toResponse(product)
                        )
                )
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }

    @GetMapping("/rankings/products/likes")
    public ResponseEntity<ProductRankingResponse>
    findLikeRanking(
            @RequestParam(
                    name = "limit",
                    defaultValue = "10"
            ) int limit
    ) {
        List<ProductRankingEntryResponse> rankings =
                productRedisService
                        .findLikeRanking(limit)
                        .stream()
                        .map(product ->
                                new ProductRankingEntryResponse(
                                        product.rank(),
                                        product.productId(),
                                        product.likeCount()
                                )
                        )
                        .toList();

        return ResponseEntity.ok(
                new ProductRankingResponse(
                        rankings,
                        rankings.size()
                )
        );
    }

    private ProductResponse toResponse(
            ProductSnapshot product
    ) {
        return new ProductResponse(
                product.productId(),
                product.name(),
                product.price(),
                product.viewCount(),
                product.likeCount()
        );
    }
}