package io.chn.redisstudy.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import io.chn.redisstudy.common.RedisPrefix;
import io.chn.redisstudy.entity.Coupon;
import io.chn.redisstudy.mapper.CouponMapper;
import io.chn.redisstudy.service.CouponAdminService;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class CouponAdminServiceImpl extends ServiceImpl<CouponMapper, Coupon> implements CouponAdminService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    //增量写入库存
    @Transactional
    @Override
    public void createSeckillCoupon(Coupon coupon) {
        //1.写入mysql
        save(coupon);
        //2.更新redis：key 必须与 seckill.lua 读取的库存 key 一致
        String key = RedisPrefix.seckillStockKey(coupon.getId());
        // TTL 从当前时刻算到活动结束后 1 小时（Duration.between 的参数顺序为 开始、结束）
        Duration ttl = Duration.between(LocalDateTime.now(), coupon.getEndTime()).plusHours(1);
        if (ttl.isNegative() || ttl.isZero()) {
            throw new RuntimeException("优惠券结束时间必须晚于当前时间");
        }
        stringRedisTemplate.opsForValue()
                .set(key, String.valueOf(coupon.getRemainCount()), ttl);
    }
}
