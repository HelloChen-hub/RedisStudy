package io.chn.redisstudy.common;

public class RedisPrefix {

    public static final long LOGIN_USER_TTL = 300L;
    public static String LOGIN_USER_KEY = "login_user:";
    public static String ORDER_KEY = "order:";
    public static String LOCK_KEY = "lock:";
    // 秒杀库存 key，格式必须与 seckill.lua 的 KEYS[1] 一致；{coupon:ID} 是 Redis Cluster 的 hash tag，保证两个 key 落在同一个槽位
    public static String seckillStockKey(Long couponId) {
        return "seckill:stock:{coupon:" + couponId + "}";
    }

    // 秒杀已购用户集合 key，格式必须与 seckill.lua 的 KEYS[2] 一致
    public static String seckillUserKey(Long couponId) {
        return "seckill:users:{coupon:" + couponId + "}";
    }

}
