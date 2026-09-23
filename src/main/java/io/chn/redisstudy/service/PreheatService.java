package io.chn.redisstudy.service;

import io.chn.redisstudy.entity.Coupon;
import org.springframework.stereotype.Service;

@Service
public interface PreheatService {
    void preheatAsync(Coupon coupon);

}
