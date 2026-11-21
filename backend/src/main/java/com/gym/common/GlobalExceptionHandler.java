package com.gym.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理：所有 Controller 抛出的异常都在这里转成统一格式，
 * 前端拿到的是友好提示，不会暴露原始堆栈信息。
 *
 * 这是分工文档模块 9（公共）的内容，改动前请与队友同步。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        // 服务端日志保留完整堆栈，方便排查；返回给前端的只有友好提示
        log.error("接口处理异常", e);
        return Result.fail("服务器内部错误，请稍后重试或联系管理员");
    }
}
