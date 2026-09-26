package io.chn.redisstudy.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import io.chn.redisstudy.common.*;
import io.chn.redisstudy.config.rabbitConfig.RabbitMQConfig;
import io.chn.redisstudy.config.redisConfig.RedisIdConfig;
import io.chn.redisstudy.dto.CouponGrabDTO;
import io.chn.redisstudy.dto.SeckillMessageDTO;
import io.chn.redisstudy.entity.Coupon;
import io.chn.redisstudy.entity.Order;
import io.chn.redisstudy.mapper.CouponMapper;
import io.chn.redisstudy.mapper.OrderMapper;
import io.chn.redisstudy.po.UserVO;
import io.chn.redisstudy.service.OrderService;
import io.chn.redisstudy.service.UserService;
import jakarta.annotation.Resource;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private final StringRedisTemplate stringRedisTemplate;

    private final RedisIdConfig redisIdConfig;

    private final CouponMapper couponMapper;

    private final UserService userService;

    private final RedissonClient redissonClient;

    private final RedisLua lua;

    private final RabbitTemplate rabbitTemplate;

    @Resource
    private RabbitMQConfig rabbitMQConfig;


    public OrderServiceImpl(StringRedisTemplate stringRedisTemplate, RedisIdConfig redisIdConfig, CouponMapper couponMapper, UserService userService, RedissonClient redissonClient, RedisLua lua, RabbitTemplate rabbitTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.redisIdConfig = redisIdConfig;
        this.redissonClient = redissonClient;
        this.userService = userService;
        this.couponMapper = couponMapper;
        this.lua = lua;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Permission("USER")
    @Log("创建优惠券订单")
    @Override
    public void createCouponOrder(CouponGrabDTO couponGrabDTO) throws InterruptedException {
        // 服务器端逻辑
        // 1.判断对象字段是否为空
        if (couponGrabDTO.getUserId() == null || couponGrabDTO.getCouponId() == null) {
            throw new RuntimeException("用户ID或优惠券ID为空");
        }

        // 2.从redis查询用户信息
        Long userId = couponGrabDTO.getUserId();
        Long couponId = couponGrabDTO.getCouponId();

        String userJsonStr = stringRedisTemplate.opsForValue().get(RedisPrefix.LOGIN_USER_KEY + userId);

        LoginUser user = JSONUtil.toBean(userJsonStr, LoginUser.class);
        
        // 3.判断用户是否存在
        if (user == null) {
            // 1.数据库查询用户
            UserVO userById = userService.getUserById(userId);
            // 2.判断用户是否存在
            if (userById == null) {
                throw new RuntimeException("用户不存在");
            }
            // 3.将用户信息写入redis
            stringRedisTemplate.opsForValue()
                    .set(RedisPrefix.LOGIN_USER_KEY + userId, JSONUtil.toJsonStr(userById), RedisPrefix.LOGIN_USER_TTL, TimeUnit.SECONDS);
        }

        // 4.获取优惠券信息（coupon 表由 CouponMapper 操作）
        Coupon coupon = couponMapper.selectById(couponId);
        //5.判断优惠券是否存在
        if (BeanUtil.isEmpty(coupon)) {
            throw new RuntimeException("优惠券不存在");
        }

        // 6.判断优惠券是否开始及结束
        if (coupon.getStartTime().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("优惠券未开始");
        }

        if (coupon.getEndTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("优惠券已结束");
        }

        /*// 6.判断优惠券库存
        if (coupon.getRemainCount() < 1) {
            throw new RuntimeException("优惠券已售罄");
        }
            OrderServiceImpl proxy = (OrderServiceImpl) AopContext.currentProxy();
            proxy.createOrder(couponId);*/

        // lua脚本逻辑
        String path = "seckill.lua";
        // 已购用户集合 key 必须用 couponId 拼装（与 seckill.lua 的 KEYS[2]、消费者补偿逻辑一致），否则会跨券误拦截
        List<String> keys = Arrays.asList(
                RedisPrefix.seckillStockKey(couponId),
                RedisPrefix.seckillUserKey(couponId)
        );

        Object[] args = new Object[]{String.valueOf(userId)};
        Long result = lua.execute(path, Long.class, keys, args);

        // 判断返回值
        if (result == 0) throw new RuntimeException("已售罄");
        if (result == -1) throw new RuntimeException("请勿重复下单");

        // 抢券成功，发送MQ执行异步操作
        SeckillMessageDTO message = new SeckillMessageDTO(userId, couponId);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.SECKILL_EXCHANGE,      // 交换机
                RabbitMQConfig.SECKILL_ORDER_ROUTING_KEY, // 路由键
                message                               // 消息体（会自动通过 Jackson转成 JSON）
        );
    }

    @Transactional
    public void createOrder(Long couponId) throws InterruptedException {
        Long userId = UserContext.getCurrentUser();
        // 创建锁对象
        String lockKey = "order:" + userId + ":" + couponId;
        RLock lock = redissonClient.getLock(lockKey);
        // 使用redisson尝试加锁
        boolean isLocked = lock.tryLock(0, -1, TimeUnit.SECONDS);
        //判断是否成功加锁
        if (!isLocked) {
            throw new RuntimeException("请勿重复下单");
        }

        // 执行业务逻辑
        // 数据库层面兜底：查询是否已经下单优惠券
        try {
            // 一人一单校验：必须查订单表。分布式锁只能拦住“同时刻”的并发，拦不住“锁已释放”之后到达的重复请求，是否下过单必须以数据库为准
            Long count = count(new QueryWrapper<Order>()
                    .eq("user_id", userId)
                    .eq("coupon_id", couponId));
            if (count > 0) {
                throw new RuntimeException("该用户已领取过该优惠券");
            }

            // 扣减库存（coupon 表由 CouponMapper 操作）：WHERE remain_count>=1 防止超卖，SET remain_count=remain_count-1 由数据库原子扣减
            boolean success = couponMapper.update(null, new UpdateWrapper<Coupon>()
                    .eq("id", couponId)
                    .ge("remain_count", 1)
                    .setSql("remain_count = remain_count - 1")) > 0;

            if (!success) {
                throw new RuntimeException("优惠券使用失败");
            }

            // 创建订单
            // 1.生成订单号
            Long orderId = redisIdConfig.createRedisId(RedisPrefix.ORDER_KEY);
            // 2.创建订单
            Order order = new Order(orderId, userId, couponId, 0, LocalDateTime.now(), null);
            //3.写入数据库
            save(order);
        } finally {
            lock.unlock();
        }
        /*// 获取分布式锁
        RedisCreateLock redisCreateLock = new RedisCreateLock(stringRedisTemplate, "order:" + userId + ":" + couponId);
        boolean isLock = redisCreateLock.setLock(100);

        if (!isLock) {
            throw new RuntimeException("一人只有一个优惠券");
        }


*/
    }
}
