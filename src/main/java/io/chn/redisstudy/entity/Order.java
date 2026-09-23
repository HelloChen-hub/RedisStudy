package io.chn.redisstudy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@TableName("`order`")
public class Order {
    /** 订单号（由 RedisIdConfig 生成的全局唯一ID） */
    @TableId(type = IdType.INPUT)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 优惠券ID */
    private Long couponId;

    /** 状态：0-未支付 1-已支付 2-已取消 */
    private Integer status;

    /** 领取时间 */
    private LocalDateTime createTime;

    /** 支付时间 */
    private LocalDateTime payTime;
}
