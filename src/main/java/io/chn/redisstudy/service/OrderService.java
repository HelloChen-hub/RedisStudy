package io.chn.redisstudy.service;

import com.baomidou.mybatisplus.spring.service.IService;
import io.chn.redisstudy.dto.CouponGrabDTO;
import io.chn.redisstudy.entity.Order;
import org.springframework.stereotype.Service;

@Service
public interface OrderService extends IService<Order> {
    void createCouponOrder(CouponGrabDTO couponGrabDTO) throws InterruptedException;

}
