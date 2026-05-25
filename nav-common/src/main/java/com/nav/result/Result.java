package com.nav.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;

@Data
@Schema(description = "全局统一返回结果")
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L; // 💡 补齐序列化版本号，保证高并发分布式环境下的稳定性

    @Schema(description = "状态码")
    private Integer code;

    @Schema(description = "提示信息")
    private String message;

    @Schema(description = "返回数据")
    private T data;

    /**
     * 快捷成功1：带返回数据的成功 (如获取个人信息、查询景点详情)
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.code = 200; // 对应接口文档: 200 请求成功
        result.message = "请求成功"; // 💡 建议改用标准中文，前端 Toast 提示更友好
        result.data = data;
        return result;
    }

    /**
     * 💡 快捷成功2：不带返回数据的成功 (重载方法，专门给修改密码、删除景点、注销等接口使用)
     */
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.code = 200;
        result.message = "操作成功";
        result.data = null;
        return result;
    }

    /**
     * 灵活失败：支持自定义错误状态码和自定义提示信息 (如 GlobalExceptionHandler 核心调用)
     */
    public static <T> Result<T> error(Integer code, String msg) {
        Result<T> result = new Result<>();
        result.code = code;
        result.message = msg;
        return result;
    }

    /**
     * 💡 快捷失败：默认 400 级别错误的快捷响应
     */
    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.code = 400; // 默认对应文档 400: 参数错误/缺失/格式非法/业务不满足
        result.message = msg;
        return result;
    }
}