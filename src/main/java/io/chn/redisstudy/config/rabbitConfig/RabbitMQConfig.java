package io.chn.redisstudy.config.rabbitConfig;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    // 定义交换机
    public static final String SECKILL_EXCHANGE = "seckill_exchange";
    // 定义队列
    public static final String SECKILL_ORDER_QUEUE = "seckill_order_queue";
    // 定义路由key
    public static final String SECKILL_ORDER_ROUTING_KEY = "seckill_order_routing.key";

    //声明交换机
    @Bean
    public DirectExchange seckillExchange() {
        return new DirectExchange(SECKILL_EXCHANGE, true, false);
    }

    // 声明队列（持久化）
    @Bean
    public Queue seckillOrderQueue() {
        return new Queue(SECKILL_ORDER_QUEUE, true, false, false);
    }

    // 绑定队列到交换机
    @Bean
    public Binding bindingSeckillOrder() {
        return BindingBuilder.bind(seckillOrderQueue())
                .to(seckillExchange())
                .with(SECKILL_ORDER_ROUTING_KEY);
    }

    // 关键：配置消息转换器为 JSON，避免默认 JDK 序列化导致乱码或反序列化失败
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
