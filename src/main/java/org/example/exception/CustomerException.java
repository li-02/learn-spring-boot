package org.example.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 自定义异常类(运行时异常)
 * 使用时 throw new CustomerException("500", "自定义异常信息");
 */
public class CustomerException extends RuntimeException{
    private String code;
    private String message;

    public CustomerException(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public CustomerException(String message) {
        this.code = "500";
        this.message = message;
    }

    public CustomerException() {}

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
