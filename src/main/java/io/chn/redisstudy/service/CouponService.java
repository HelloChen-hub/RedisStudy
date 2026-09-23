package io.chn.redisstudy.service;

import com.baomidou.mybatisplus.spring.service.IService;
import io.chn.redisstudy.dto.CouponGrabDTO;
import io.chn.redisstudy.entity.Coupon;
import org.springframework.stereotype.Service;

@Service
public interface CouponService extends IService<Coupon> {
    void useCoupon(CouponGrabDTO couponGrabDTO);
}
