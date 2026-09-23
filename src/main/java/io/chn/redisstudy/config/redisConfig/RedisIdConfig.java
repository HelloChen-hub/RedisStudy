package io.chn.redisstudy.config.redisConfig;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Component
public class RedisIdConfig {
    private final StringRedisTemplate stringRedisTemplate;
    private final long beginTimeStamp = 1788857400;


    public RedisIdConfig (StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public long createRedisId(String keyPrefix) {
        //生成时间戳
        LocalDateTime now = LocalDateTime.now();
        long nowTime = now.toEpochSecond(ZoneOffset.UTC);
        long timeStamp = nowTime - beginTimeStamp;
        //生成序列号
        String date = now.format(java.time.format.DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        long serialNumber = stringRedisTemplate.opsForValue().increment("incr" + keyPrefix + date + ":");
        //返回id值
        return timeStamp << 32 | serialNumber;
    }
}
