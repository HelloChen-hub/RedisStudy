package io.chn.redisstudy.test;

import io.chn.redisstudy.config.redisConfig.RedisIdConfig;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class RedisIdTest {

    @Resource
    RedisIdConfig redisIdConfig;

    private final int taskCount = 300;
    private final int perTaskCount = 100;

    @Test
    public void testRedisId() throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(500);
        List<Future<?>> futures = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        try {
            for (int i = 0; i < taskCount; i++) {
                futures.add(executorService.submit(() -> {
                    for (int j = 0; j < perTaskCount; j++) {
                        long id = redisIdConfig.createRedisId("test");
                        System.out.println(id);
                    }
                }));
            }

            //逐个 get：任务内部异常会被 Future 捕获，这里重新抛出，让测试真正失败而不是静默吞掉
            for (Future<?> future : futures) {
                future.get();
            }
            long endTime = System.currentTimeMillis();
            System.out.println("总耗时: " + (endTime - startTime) + " ms");
        } finally {
            //无论成败都立即停掉线程池并等线程退出，避免后台任务在 Spring 容器销毁后仍操作 Redis 造成异常刷屏
            executorService.shutdownNow();
            executorService.awaitTermination(10, TimeUnit.SECONDS);
        }
    }
}
