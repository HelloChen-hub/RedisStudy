package io.chn.redisstudy.controller;

import io.chn.redisstudy.dto.CouponGrabDTO;
import io.chn.redisstudy.service.CouponService;
import io.chn.redisstudy.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/coupon")
public class CouponController {
    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @PutMapping("/useCoupon")
    public Result<Void> useCoupon(CouponGrabDTO couponGrabDTO) {
        couponService.useCoupon(couponGrabDTO);
        return Result.success();
    }
}
