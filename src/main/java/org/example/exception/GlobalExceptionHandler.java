package org.example.exception;

import org.example.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常捕获器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    @ResponseBody //将result对象转为json输出
    public Result handleException(Exception e){
        // 输出异常信息到控制台
        log.error("系统异常", e);
        return Result.error(StringUtils.hasLength(e.getMessage()) ? e.getMessage() : "系统异常");
    }

    @ExceptionHandler(CustomerException.class)
    @ResponseBody
    public Result customerError(CustomerException e) {
        log.error("自定义异常", e);
        return Result.error(e.getCode(), e.getMessage());
    }
}
