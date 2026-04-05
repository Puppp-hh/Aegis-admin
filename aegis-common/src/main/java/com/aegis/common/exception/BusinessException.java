package com.aegis.common.exception;

import com.aegis.common.result.ResultCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final Integer code;

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    //传自定义消息
    public BusinessException(Integer code, String message) {
        super(message);
        this.code=code;
    }

    //传自定义消息,默认五百
    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }
}
