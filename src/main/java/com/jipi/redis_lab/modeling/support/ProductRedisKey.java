package com.jipi.redis_lab.modeling.support;

// 12강: Product 도메인의 Redis Key 규칙을 한 곳에서 관리한다.
public class ProductRedisKey {

    private static final String PRODUCT_PREFIX = "product:";
    private static final String VIEW_COUNT_SUFFIX = ":view-count";
    private static final String LIKED_USERS_SUFFIX = ":liked-users";
    private static final String LIKE_RANKING_KEY = "ranking:products:likes";

    private ProductRedisKey() {
    }

    public static String product(long productId) {
        return PRODUCT_PREFIX + productId;
    }

    public static String viewCount(long productId) {
        return PRODUCT_PREFIX + productId + VIEW_COUNT_SUFFIX;
    }

    public static String likeUsers(long productId) {
        return PRODUCT_PREFIX + productId + LIKED_USERS_SUFFIX;
    }

    public static String likeRanking() {
        return LIKE_RANKING_KEY;
    }

}
