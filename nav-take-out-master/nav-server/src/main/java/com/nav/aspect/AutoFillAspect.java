package com.nav.aspect;

import com.nav.annotation.AutoFill;
import com.nav.context.BaseContext;
import com.nav.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
public class AutoFillAspect {

    // 切入点：拦截 com.nav.mapper 包下所有类的方法，且方法上加了 @AutoFill 注解
    @Pointcut("execution(* com.nav.mapper.*.*(..)) && @annotation(com.nav.annotation.AutoFill)")
    public void autoFillPointcut() {}

    @Before("autoFillPointcut()")
    public void autoFill(JoinPoint joinPoint) {
        log.info("开始进行公共字段自动填充(审计流)...");

        // 1. 获取当前被拦截的方法上的数据库操作类型（INSERT 或 UPDATE）
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);
        OperationType operationType = autoFill.value();

        // 2. 获取当前被拦截的方法的参数（约定第一个参数为实体类，如 ScenicSpot）
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return;
        }
        Object entity = args[0];

        // 3. 准备赋值的数据
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId(); // 从 ThreadLocal 中获取当前操作的管理员ID

        // 4. 根据不同的操作类型，利用反射为对应的属性赋值
        try {
            if (operationType == OperationType.INSERT) {
                // 获得 set 方法
                Method setCreateTime = entity.getClass().getDeclaredMethod("setCreatedAt", LocalDateTime.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod("setUpdatedAt", LocalDateTime.class);
                Method setUpdatedBy = entity.getClass().getDeclaredMethod("setUpdatedBy", Long.class);

                // 反射调用赋值
                setCreateTime.invoke(entity, now);
                setUpdateTime.invoke(entity, now);
                setUpdatedBy.invoke(entity, currentId);
            } else if (operationType == OperationType.UPDATE) {
                Method setUpdateTime = entity.getClass().getDeclaredMethod("setUpdatedAt", LocalDateTime.class);
                Method setUpdatedBy = entity.getClass().getDeclaredMethod("setUpdatedBy", Long.class);

                setUpdateTime.invoke(entity, now);
                setUpdatedBy.invoke(entity, currentId);
            }
        } catch (Exception e) {
            log.error("公共字段自动填充失败: {}", e.getMessage());
        }
    }
}
