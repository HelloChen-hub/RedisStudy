package io.chn.redisstudy.service;

import com.baomidou.mybatisplus.spring.service.IService;
import io.chn.redisstudy.entity.Coupon;
import org.springframework.stereotype.Service;

@Service
public interface CouponAdminService extends IService<Coupon> {
    void createSeckillCoupon(Coupon coupon);
}
