package io.chn.redisstudy.service.impl;

import io.chn.redisstudy.common.RedisPrefix;
import io.chn.redisstudy.entity.Coupon;
import io.chn.redisstudy.service.PreheatService;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class PreheatServiceImpl implements PreheatService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    // 预热：把数据库库存写入 Redis，seckill.lua 基于该 key 原子扣减
    @Async
    @Override
    public void preheatAsync(Coupon coupon) {
        String stockKey = RedisPrefix.seckillStockKey(coupon.getId());
        // 双重幂等：库存 key 已存在说明已预热过，跳过避免覆盖已被扣减的库存
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(stockKey))) {
            return;
        }
        // TTL 为活动结束后 1 小时，到期自动清理
        Duration ttl = Duration.between(LocalDateTime.now(), coupon.getEndTime()).plusHours(1);
        if (ttl.isNegative() || ttl.isZero()) {
            return;
        }
        stringRedisTemplate.opsForValue().set(stockKey, String.valueOf(coupon.getRemainCount()), ttl);
    }
}
