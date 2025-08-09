package com.printercloud.exception;

import com.printercloud.common.R;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PageCountException.class)
    public R<Void> handlePageCount(PageCountException ex) {
        return R.badRequest(ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public R<Void> handleIllegalArg(IllegalArgumentException ex) {
        return R.badRequest(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public R<Void> handleOther(Exception ex) {
        return R.error("服务器异常: " + ex.getMessage());
    }
}

