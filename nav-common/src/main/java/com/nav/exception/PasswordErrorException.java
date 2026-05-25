package com.nav.exception;

import lombok.NoArgsConstructor;

public class PasswordErrorException extends BaseException {
    // 自动生成带 String 参数的构造方法，并自动调用 super(msg)
    public PasswordErrorException(String msg) {
        super(msg);
    }
}