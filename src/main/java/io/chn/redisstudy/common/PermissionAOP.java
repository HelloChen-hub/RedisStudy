package io.chn.redisstudy.common;

import io.chn.redisstudy.po.UserVO;
import io.chn.redisstudy.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Slf4j
@Component
@Order(1) // 数字越小优先级越高：权限校验在日志切面之前执行
public class PermissionAOP {

    @Resource
    private UserService userService;

    @Around("@annotation(permission)")
    public Object permissionAround(ProceedingJoinPoint joint, Permission permission) throws Throwable {
        // 前置校验身份逻辑：UserContext 必须由调用方（Controller/拦截器）在进入代理前写入
        Long currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("用户未登录");
        }
        UserVO userVO = userService.getUserById(currentUser);
        if (userVO == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!permission.value().equals(userVO.getRoleCode())) {
            throw new RuntimeException("用户没有权限");
        }
        log.info("用户通过权限校验");
        // 调用目标方法
        return joint.proceed();
    }
}
