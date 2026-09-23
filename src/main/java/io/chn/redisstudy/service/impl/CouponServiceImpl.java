package io.chn.redisstudy.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import io.chn.redisstudy.dto.CouponGrabDTO;
import io.chn.redisstudy.entity.Coupon;
import io.chn.redisstudy.mapper.CouponMapper;
import io.chn.redisstudy.service.CouponService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CouponServiceImpl extends ServiceImpl<CouponMapper, Coupon> implements CouponService {


    @Override
    public void useCoupon(CouponGrabDTO couponGrabDTO) {

    }
}
