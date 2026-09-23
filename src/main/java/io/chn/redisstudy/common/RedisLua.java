package io.chn.redisstudy.common;

import jakarta.annotation.Resource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RedisLua {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    // 缓存已经加载过的脚本，避免每次读文件与重复创建对象
    private final ConcurrentHashMap<String, RedisScript<?>> map = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public <T> T execute(String path, Class<T> type, List<String> keys, Object...args) {
        // 缓存key
        String cacheKey = path + "#" + type.getName();
        // 从缓存获取脚本地址，拿不到创建并放入缓存
        RedisScript<T> script = (RedisScript<T>) map.computeIfAbsent(cacheKey,
                k -> {
                    DefaultRedisScript<T> redisScript = new DefaultRedisScript<>();
                    redisScript.setLocation(new ClassPathResource(path));
                    redisScript.setResultType(type);
                    return redisScript;
                });
        // 执行脚本
        return stringRedisTemplate.execute(script, keys, args);

    }

    public Long executeLong(String path, List<String> keys, Object...args) {
        return execute(path, Long.class, keys, args);
    }
}
