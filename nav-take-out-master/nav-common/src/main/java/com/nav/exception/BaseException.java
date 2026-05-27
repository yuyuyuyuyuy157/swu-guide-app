package com.nav.exception;

/**
 * 异常基类
*/
public class BaseException extends RuntimeException {
    public BaseException() {
    }

    public BaseException(String msg) {
        super(msg);
    }
}