package com.jipi.redis_lab.modeling.service;

import com.jipi.redis_lab.modeling.model.ProductSnapshot;
import com.jipi.redis_lab.modeling.model.RankedProduct;
import com.jipi.redis_lab.modeling.support.ProductRedisKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
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

    // 14강: WATCH 충돌 발생 시 무한 반복하지 않도록 재시도 횟수를 제한한다.
    private static final int MAX_TRANSCATIONS_RETRIES = 3;

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
    // 13강: 조회 후 값을 다시 저장하지 않고 INCR 한 명령으로 원자적으로 증가시킨다.
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
    // 13강: SADD와 ZINCRBY는 각각 원자적이지만,
    // 두 명령을 조합한 좋아요 처리 전체는 하나의 원자적 작업이 아니다.
    // 여러 Redis 명령의 원자적 처리는 14~15강에서 다룬다.
    // 14강: WATCH + MULTI + EXEC를 사용해 좋아요 정보와 랭킹 점수를 함께 변경한다.
    public boolean addLike(long productId, String userId) {
        String likedUsersKey = ProductRedisKey.likeUsers(productId);
        String rankingKey = ProductRedisKey.likeRanking();
        for (int attempt = 1; attempt <= MAX_TRANSCATIONS_RETRIES; attempt++) {
            Boolean added = executeLikeTransaction(
                    productId,
                    userId,
                    likedUsersKey,
                    rankingKey);

            if (!added) {
                return added;
            }

            log.info(
                    "Redis product like transaction retry. productId={}, userId={}, attempt={}",
                    productId,
                    userId,
                    attempt
            );
        }

        log.info(
                "Redis product like transaction failed. productId={}, userId={}",
                productId,
                userId
        );

        return false;

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

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Boolean executeLikeTransaction(
            long productId,
            String userId,
            String likedUsersKey,
            String rankingKey
    ) {
        return stringRedisTemplate.execute(
                new SessionCallback<Boolean>() {

                    @Override
                    public Boolean execute(
                            RedisOperations operations
                    ) throws DataAccessException {

                        operations.watch(likedUsersKey);

                        Boolean alreadyLiked =
                                operations
                                        .opsForSet()
                                        .isMember(
                                                likedUsersKey,
                                                userId
                                        );

                        if (Boolean.TRUE.equals(alreadyLiked)) {
                            operations.unwatch();

                            log.info(
                                    "Redis product like already exists. key={}, productId={}, userId={}",
                                    likedUsersKey,
                                    productId,
                                    userId
                            );

                            return false;
                        }

                        try {
                            operations.multi();

                            operations
                                    .opsForSet()
                                    .add(
                                            likedUsersKey,
                                            userId
                                    );

                            operations
                                    .opsForZSet()
                                    .incrementScore(
                                            rankingKey,
                                            String.valueOf(productId),
                                            1
                                    );

                            List<Object> results =
                                    operations.exec();

                            if (results == null) {
                                return null;
                            }

                            log.info(
                                    "Redis product like transaction committed. productId={}, userId={}",
                                    productId,
                                    userId
                            );

                            return true;

                        } catch (RuntimeException exception) {
                            operations.discard();
                            throw exception;
                        }
                    }
                }
        );
    }
}
