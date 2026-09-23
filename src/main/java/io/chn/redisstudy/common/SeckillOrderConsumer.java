package io.chn.redisstudy.common;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.rabbitmq.client.Channel;
import io.chn.redisstudy.config.rabbitConfig.RabbitMQConfig;
import io.chn.redisstudy.dto.SeckillMessageDTO;
import io.chn.redisstudy.entity.Coupon;
import io.chn.redisstudy.entity.Order;
import io.chn.redisstudy.mapper.CouponMapper;
import io.chn.redisstudy.mapper.OrderMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@Component
public class SeckillOrderConsumer {
    @Resource
    private OrderMapper orderMapper;
    @Resource
    private CouponMapper couponMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @RabbitListener(queues = RabbitMQConfig.SECKILL_ORDER_QUEUE)
    public void handleSeckillOrder(SeckillMessageDTO message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        log.info("收到秒杀消息: 用户={}, 券={}", message.getUserId(), message.getCouponId());

        Long userId = message.getUserId();
        Long couponId = message.getCouponId();

        try {
            // 1. 数据库层扣减库存（防超卖）
            boolean success = couponMapper.update(null, new UpdateWrapper<Coupon>()
                    .eq("id", couponId)
                    .ge("remain_count", 1)
                    .setSql("remain_count = remain_count - 1")) > 0;
            if (!success) {
                throw new RuntimeException("数据库库存不足");
            }

            // 2. 写入订单（依赖数据库唯一索引兜底防重复）
            Long orderId = IdWorker.getId();
            Order order = new Order(orderId, userId, couponId, 0, LocalDateTime.now(), null);
            orderMapper.insert(order);

            log.info("订单创建成功：{}", orderId);

            // 3. 手动 ACK：告诉 MQ 消息已成功消费，可以从队列删除
            channel.basicAck(deliveryTag, false);

        } catch (DuplicateKeyException e) {
            // 唯一索引冲突（说明这个用户已经下过单了），这就是兜底防线
            log.warn("用户 {} 重复下单券 {}，已拦截", userId, couponId);
            // 这种情况也算消费成功，直接 ACK，否则消息会无限重试
            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("订单落库失败，开始回滚 Redis", e);

            // 4. 补偿逻辑：MySQL 落库失败，把 Redis 预扣的库存和用户记录加回去
            stringRedisTemplate.opsForValue().increment(RedisPrefix.seckillStockKey(couponId));
            stringRedisTemplate.opsForSet().remove(RedisPrefix.seckillUserKey(couponId), String.valueOf(userId));

            // 5. 拒绝消息，并重新入队（或者放入死信队列，根据你的策略）
            // requeue = false 表示不重新入队，避免死循环；通常建议投递到死信队列人工干预
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
