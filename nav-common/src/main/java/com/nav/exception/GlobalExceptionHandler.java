package com.nav.exception;

import com.nav.exception.BaseException;
import com.nav.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 1. 🎯 拦截捕获自定义的业务异常（如：密码错误、账号不存在）
     * 痛点驱动：对应接口文档中的 400 级别参数/业务非法错误，返回具体的提示给前端展示
     */
    @ExceptionHandler(BaseException.class)
    public Result<String> exceptionHandler(BaseException ex) {
        log.error("业务异常信息：{}", ex.getMessage());
        // 对应文档错误码 400：参数错误/缺失/格式非法/业务不满足
        return Result.error(400, ex.getMessage());
    }

    /**
     * 2. 🎯 拦截处理自 Spring Validation 框架抛出的【参数校验不通过】异常
     * 痛点驱动：当用户输入的手机号不是11位，或者密码为空时，直接精准提取注解里的 message 提示
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Result<String> validationExceptionHandler(Exception ex) {
        log.error("参数校验异常引发拦截：{}", ex.getMessage());
        String errorMsg = "参数格式非法";

        if (ex instanceof MethodArgumentNotValidException methodArgumentNotValidException) {
            errorMsg = methodArgumentNotValidException.getBindingResult().getAllErrors().stream()
                    .map(ObjectError::getDefaultMessage)
                    .collect(Collectors.joining(", "));
        }

        return Result.error(400, errorMsg);
    }

    /**
     * 3. 🎯 拦截捕获最终的未知系统异常（如：NullPointerException、数据库连接失败）
     * 痛点驱动：绝不向前端暴露大段报错代码，统一兜底，保护服务器安全
     */
    @ExceptionHandler(Exception.class)
    public Result<String> exceptionHandler(Exception ex) {
        log.error("💥 系统发生未知致命崩溃：", ex); // 记录完整的堆栈日志供后端排查
        // 对应文档错误码 500：提示系统繁忙
        return Result.error(500, "系统繁忙，请稍后再试");
    }
}