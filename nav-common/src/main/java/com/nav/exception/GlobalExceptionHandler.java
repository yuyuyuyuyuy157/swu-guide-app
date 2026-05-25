package com.nav.exception;

import com.nav.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 完全对齐《苍穹外卖》企业级开发规范与接口文档错误码标准
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 1. 🎯 拦截捕获自定义的业务异常（如：密码错误、账号不存在）
     * 对应接口文档中的 400 级别业务非法错误，返回具体的提示给前端展示
     */
    @ExceptionHandler(BaseException.class)
    public Result<String> exceptionHandler(BaseException ex) {
        log.error("违反业务规则引发拦截：{}", ex.getMessage());
        // 对应文档错误码 400：参数错误/格式非法/业务不满足
        return Result.error(400, ex.getMessage());
    }

    /**
     * 2. 🎯 补丁：单独拦截数据库唯一键索引冲突（如：注册时手机号已存在）
     * 彻底解决前端收到模糊的 500 错误引发的用户体验灾难
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public Result<String> exceptionHandler(SQLIntegrityConstraintViolationException ex) {
        log.error("数据库完整性约束冲突（极大概率是唯一索引重复）：{}", ex.getMessage());
        String message = ex.getMessage();

        // 苍穹经典解析算法：提取报错信息中的 Duplicate entry 'xxx'
        if (message.contains("Duplicate entry")) {
            String[] split = message.split(" ");
            String duplicateValue = split[2]; // 抓取到重复的具体值（如用户的手机号）
            return Result.error(400, "操作失败：" + duplicateValue + " 已存在，请勿重复录入");
        }

        return Result.error(400, "数据库存在冲突约束，错误详情请联系管理员");
    }

    /**
     * 3. 🎯 升级：全方位拦截 Validation 框架抛出的【参数校验不通过】异常
     * 完美合并 MethodArgumentNotValidException、BindException 以及 ConstraintViolationException 三大马车
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class, ConstraintViolationException.class})
    public Result<String> validationExceptionHandler(Exception ex) {
        log.error("前端提交的数据格式校验未通过，引发拦截：{}", ex.getMessage());
        String errorMsg = "参数格式非法";

        // 场景A：拦截处理 @RequestBody 实体类校验失败
        if (ex instanceof MethodArgumentNotValidException methodArgumentNotValidException) {
            errorMsg = methodArgumentNotValidException.getBindingResult().getAllErrors().stream()
                    .map(ObjectError::getDefaultMessage)
                    .collect(Collectors.joining(", "));
        }
        // 场景B：拦截处理表单/Query 传参拼接校验失败
        else if (ex instanceof BindException bindException) {
            errorMsg = bindException.getBindingResult().getAllErrors().stream()
                    .map(ObjectError::getDefaultMessage)
                    .collect(Collectors.joining(", "));
        }
        // 场景C：补丁：拦截处理 Controller 方法单参数直接校验失败（如 @Min, @NotBlank 挂在参数上）
        else if (ex instanceof ConstraintViolationException constraintViolationException) {
            errorMsg = constraintViolationException.getConstraintViolations().stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
        }

        return Result.error(400, errorMsg);
    }

    /**
     * 4. 🎯 统一底线：拦截捕获最终的未知系统内部故障（如：NullPointerException、Redis连不上）
     * 绝不向前端暴露堆栈代码，提示系统繁忙，全面保证微服务网络安全
     */
    @ExceptionHandler(Exception.class)
    public Result<String> exceptionHandler(Exception ex) {
        log.error("💥 系统发生未知致命崩溃，请紧急排查：", ex); // 必须打出完整的 ex 堆栈，方便去日志里揪 Bug
        // 对应文档错误码 500：系统内部错误提示
        return Result.error(500, "系统繁忙，请稍后再试");
    }
}