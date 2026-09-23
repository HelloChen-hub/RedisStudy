package io.chn.redisstudy.controller;

import io.chn.redisstudy.common.UserContext;
import io.chn.redisstudy.dto.CouponGrabDTO;
import io.chn.redisstudy.service.OrderService;
import io.chn.redisstudy.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/couponOrder")
    public Result<Void> createCouponOrder(@RequestBody CouponGrabDTO couponGrabDTO) throws InterruptedException {
        // 必须在进入 Service 代理前写入用户上下文，PermissionAOP 才能在切面中读到当前用户（真实项目应在登录拦截器中根据 token 解析写入）
        if (couponGrabDTO.getUserId() != null) {
            UserContext.setCurrentUser(String.valueOf(couponGrabDTO.getUserId()));
        }
        try {
            orderService.createCouponOrder(couponGrabDTO);
        } finally {
            // 清理 ThreadLocal，防止 Tomcat 复用线程导致上下文串号
            UserContext.clear();
        }
        return Result.success();
    }

}
