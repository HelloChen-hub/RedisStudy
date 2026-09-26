package io.chn.redisstudy.common;

import io.chn.redisstudy.config.logConfig.LogFileWriteConfig;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;


@Aspect
@Slf4j
@Component
@Order(2) // 数字越小优先级越高：日志切面在权限切面之后执行
public class LogAOP {
    private static final String INFO_LOG = "INFO";
    private static final String ERROR_LOG = "ERROR";

    @Resource
    LogFileWriteConfig logFileWriteConfig;

    // 创建Around通知逻辑
    // 注意：注解实例必须通过 @annotation(log) 直接绑定到同名形参上；
    // 若写成 @Pointcut("@annotation(全限定类名)") + argNames，log 参数无法绑定，
    // Spring 会报 "formal unbound in pointcut" 并静默丢弃该通知（仅 DEBUG 日志可见），切面永远不生效
    @Around("@annotation(log)")
    public Object aroundLog(ProceedingJoinPoint joinPoint, Log log) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String detail = log.value();
        Object[] args = joinPoint.getArgs();
        // 执行方法前置逻辑
        logFileWriteConfig.writeLog(INFO_LOG, "【" + detail + "】方法开始：" + methodName
                + "，入参：" + Arrays.toString(args));

        long startTime = System.currentTimeMillis();

        try {
            // 执行目标方法
            Object result = joinPoint.proceed();
            long costTime = System.currentTimeMillis() - startTime;
            // 写入日志
            logFileWriteConfig.writeLog(INFO_LOG, "【" + detail + "】方法结束：" + methodName
                    + "，耗时：" + costTime + "ms，返回值：" + result);
            return result;
        } catch (Throwable e) {
            logFileWriteConfig.writeLog(ERROR_LOG, "【" + detail + "】方法异常：" + methodName
                    + "，异常信息：" + e.getMessage());
            throw e;
        }
    }
}
