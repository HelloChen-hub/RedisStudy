package io.chn.redisstudy.common;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.chn.redisstudy.entity.Coupon;
import io.chn.redisstudy.mapper.CouponMapper;
import io.chn.redisstudy.service.PreheatService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class SeckillPreheatTask {

    @Resource
    private CouponMapper couponMapper;
    @Resource
    private PreheatService preheatService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    // 定时任务：每分钟扫描一次，发现未来五分钟有活动就开始预热缓存
    @Scheduled(cron = "0 */1 * * * ?")
    public void scanAndPreheat() {
        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime soonStart = now.plusMinutes(5);

        // 查询未来五分钟内开始的优惠券
        List<Coupon> coupons = couponMapper.selectList(new QueryWrapper<Coupon>()
                .le("start_time", soonStart)
                .ge("end_time", now));

        for (Coupon coupon : coupons) {
            // 幂等性校验：库存 key 已存在说明已预热过，直接跳过（同时避免覆盖运行中已被扣减的库存）
            String stockKey = RedisPrefix.seckillStockKey(coupon.getId());
            if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(stockKey))) {
                continue;
            }

            //异步执行分批预热
            preheatService.preheatAsync(coupon);
        }
    }
}
