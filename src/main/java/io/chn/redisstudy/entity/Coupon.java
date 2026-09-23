package io.chn.redisstudy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("coupon")
public class Coupon {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 优惠券名称 */
    private String name;

    /** 类型：1-满减 2-折扣 */
    private Integer type;

    /** 优惠值：满减为金额，折扣为折扣率(如0.8表示8折) */
    private BigDecimal discountValue;

    /** 使用门槛(满多少可用) */
    private BigDecimal minAmount;

    /** 发放总量 */
    private Integer totalCount;

    /** 剩余数量 */
    private Integer remainCount;

    /** 生效开始时间 */
    private LocalDateTime startTime;

    /** 生效结束时间 */
    private LocalDateTime endTime;

    /** 创建时间 */
    private LocalDateTime createTime;
}
