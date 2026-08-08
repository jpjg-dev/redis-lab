package com.jipi.redis_lab.modeling.service;

import com.jipi.redis_lab.modeling.model.ProductSnapshot;
import com.jipi.redis_lab.modeling.model.RankedProduct;
import com.jipi.redis_lab.modeling.support.ProductRedisKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Service;

import java.util.*;

// 12강: 데이터 특성에 맞는 Redis 자료구조를 조합하여 Product를 모델링한다.
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductRedisService {
    private static final String NAME_FIELD = "name";
    private static final String PRICE_FIELD = "price";

    private final StringRedisTemplate stringRedisTemplate;

    // 12강: 상품의 여러 속성은 하나의 Hash로 저장한다.
    public void saveProduct(long productId, String name, long price) {
        String key = ProductRedisKey.product(productId);
        hashOperations().putAll(key, Map.of(NAME_FIELD, name, PRICE_FIELD, String.valueOf(price)));

        log.info(
                "Redis product saved. key={}, productId={}, name={}, price={}",
                key,
                productId,
                name,
                price
        );
    }

    // 12강: 단순 숫자 카운터인 조회수는 String의 INCR을 사용한다.
    public long increaseViewCount(long productId) {
        String key = ProductRedisKey.viewCount(productId);
        Long viewCount = valueOperations().increment(key);
        Long result = viewCount == null ? 0 : viewCount;
        log.info(
                "Redis product view count increased. key={}, productId={}, viewCount={}",
                key,
                productId,
                result
        );
        return result;
    }

    // 12강: 좋아요 사용자는 중복을 허용하지 않으므로 Set에 저장한다.
    // 새 좋아요일 때만 Sorted Set의 랭킹 점수를 증가시킨다.
    public boolean addLike(long productId, String userId) {
        String likedUsersKey = ProductRedisKey.likeUsers(productId);
        Long addedCount = setOperations().add(likedUsersKey, userId);
        boolean added = addedCount != null && addedCount > 0;

        if (!added) {
            log.info(
                    "Redis product like already exists. key={}, productId={}, userId={}",
                    likedUsersKey,
                    productId,
                    userId
            );
            return false;
        }

        Double likeScore = zSetOperations()
                .incrementScore(ProductRedisKey.likeRanking(), String.valueOf(productId), 1);

        log.info(
                "Redis product like added. productId={}, userId={}, likeScore={}",
                productId,
                userId,
                likeScore
        );

        return true;
    }

    // 12강: Hash, String, Set에 나뉜 데이터를 하나의 상품 정보로 조립한다.
    public Optional<ProductSnapshot> findProduct(long productId) {
        Map<String, String> product = hashOperations().entries(ProductRedisKey.product(productId));

        if (product.isEmpty()) {
            return Optional.empty();
        }

        String viewCountValue = valueOperations().get(ProductRedisKey.viewCount(productId));
        Long likeCountValue = setOperations().size(ProductRedisKey.likeUsers(productId));
        long viewCount = viewCountValue == null ? 0 : Long.parseLong(viewCountValue);
        long likeCount = likeCountValue == null ? 0 : likeCountValue;

        return Optional.of(
                new ProductSnapshot(
                        productId,
                        product.get(NAME_FIELD),
                        Long.parseLong(product.get(PRICE_FIELD)),
                        viewCount,
                        likeCount
                )
        );
    }

    // 12강: Sorted Set을 이용해 좋아요가 많은 상품부터 조회한다.
    public List<RankedProduct> findLikeRanking(int limit) {
        if (limit <= 0) return List.of();

        Set<ZSetOperations.TypedTuple<String>> tuples =
                zSetOperations().reverseRangeByScoreWithScores(
                        ProductRedisKey.likeRanking(), 0, limit - 1
                );
        if (tuples == null || tuples.isEmpty()) return List.of();

        List<RankedProduct> rankings = new ArrayList<>();

        long rank = 1;

        for (ZSetOperations.TypedTuple<String> tuple : tuples) {
            String productId = tuple.getValue();
            Double score = tuple.getScore();
            if (productId == null || score == null) continue;
            rankings.add(new RankedProduct(
                    rank,
                    Long.parseLong(productId),
                    score.longValue()
            ));

            rank++;
        }
        return rankings;
    }

    private HashOperations<String, String, String>
    hashOperations() {
        return stringRedisTemplate.opsForHash();
    }

    private ValueOperations<String, String>
    valueOperations() {
        return stringRedisTemplate.opsForValue();
    }

    private SetOperations<String, String>
    setOperations() {
        return stringRedisTemplate.opsForSet();
    }

    private ZSetOperations<String, String>
    zSetOperations() {
        return stringRedisTemplate.opsForZSet();
    }
}
