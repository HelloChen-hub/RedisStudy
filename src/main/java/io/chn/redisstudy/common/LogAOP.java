package io.chn.redisstudy.common;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/*
@Aspect
@Slf4j
@Component
@Order(2) // 数字越小优先级越高：日志切面在权限切面之后执行
public class LogAOP {
    @Pointcut("execution(* io.chn.redisstudy.service.impl.OrderServiceImpl.*(..))")
    public void logPointCut(){}

    @Before("logPointCut()")
    public void advanceLog() {
        log.info("订单业务被调用");
    }

    @Around("logPointCut()")
    public Object aroundLog(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("日志开启");
        // 环绕通知必须调用 proceed()，否则调用链被短路，目标方法与内层切面都不会执行
        return joinPoint.proceed();
    }
}
*/
