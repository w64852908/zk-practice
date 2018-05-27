package com.lanxiang.zk.practice.common.exception;

/**
 * Created by lanxiang on 2018/5/27.
 */
public class PracticeException extends RuntimeException {

    public PracticeException() {
        super();
    }

    public PracticeException(String message) {
        super(message);
    }

    public PracticeException(String message, Throwable cause) {
        super(message, cause);
    }

    public PracticeException(Throwable cause) {
        super(cause);
    }
}
