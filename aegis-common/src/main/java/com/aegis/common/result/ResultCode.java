package com.aegis.common.result;

import lombok.Getter;

@Getter
public enum ResultCode {

    // 成功
    SUCCESS(200, "操作成功"),

    // 客户端错误
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或token已过期"),
    FORBIDDEN(403, "没有权限"),
    NOT_FOUND(404, "资源不存在"),

    // 服务端错误
    INTERNAL_ERROR(500, "服务器内部错误"),

    // 业务错误
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_DISABLED(1002, "用户已被禁用"),
    USERNAME_OR_PASSWORD_ERROR(1003, "用户名或密码错误"),
    LOGIN_FAIL_LOCKED(1004, "登录失败次数过多，账号已锁定"),
    ROLE_NOT_FOUND(1005, "角色不存在"),
    MENU_NOT_FOUND(1006, "菜单不存在");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}