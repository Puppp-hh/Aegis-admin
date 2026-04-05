package com.aegis.common.result;

import lombok.Data;

@Data
public class Result<T> {

    // 状态码 200成功 其他失败
    private Integer code;

    // 提示信息
    private String message;

    // 返回数据
    private T data;

    // 时间戳
    private Long timestamp;

    // 私有构造方法，不让外部直接 new
    private Result() {
        this.timestamp = System.currentTimeMillis();
    }

    // 成功，带数据
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.code = 200;
        result.message = "success";
        result.data = data;
        return result;
    }

    // 成功，不带数据
    public static <T> Result<T> success() {
        return success(null);
    }

    // 失败
    public static <T> Result<T> fail(Integer code, String message) {
        Result<T> result = new Result<>();
        result.code = code;
        result.message = message;
        return result;
    }

    // 失败，默认500
    public static <T> Result<T> fail(String message) {
        return fail(500, message);
    }

    // 直接传枚举
    public static <T> Result<T> fail(ResultCode resultCode) {
        return fail(resultCode.getCode(), resultCode.getMessage());
    }
}