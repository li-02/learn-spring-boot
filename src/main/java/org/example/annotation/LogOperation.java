package org.example.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogOperation {
    String value() default ""; // 操作描述

    String module() default ""; // 所属模块

    boolean logParams() default true; // 是否记录请求参数

    boolean logResult() default true; // 是否记录响应结果
}
