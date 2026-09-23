package io.chn.redisstudy.utils;

import io.chn.redisstudy.common.RedisPrefix;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class RedisCreateLock implements IRedisCreateLock{
    private StringRedisTemplate stringRedisTemplate;
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT;
    private String name;

    static {
        UNLOCK_SCRIPT = new DefaultRedisScript<>();
        //指定lua脚本的位置
        UNLOCK_SCRIPT.setLocation(new ClassPathResource("unlock.lua"));
        //指定lua脚本的返回类型
        UNLOCK_SCRIPT.setResultType(Long.class);
    }


    public RedisCreateLock(StringRedisTemplate stringRedisTemplate, String name) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.name = name;
    }

    // 创建分布式锁
    @Override
    public boolean setLock(long timeout) {
        // 获取当前线程名称
        long threadId = Thread.currentThread().getId();
        // 尝试获取分布式锁
        Boolean result = stringRedisTemplate.opsForValue().setIfAbsent(RedisPrefix.LOCK_KEY + name, String.valueOf(threadId), timeout, TimeUnit.SECONDS);
        // 返回锁获取结果
        return Boolean.TRUE.equals(result);

    }

    //释放锁
    @Override
    public void deleteLock() {
        stringRedisTemplate.execute(UNLOCK_SCRIPT, Collections.singletonList(RedisPrefix.LOCK_KEY+name), String.valueOf(Thread.currentThread().getId()));
    }


}
