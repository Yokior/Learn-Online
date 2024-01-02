package com.xuecheng.base.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
@Component
@ResponseBody
@Slf4j
public class GlobalExceptionHandler
{


    @ExceptionHandler(XueChengPlusException.class)
    public RestErrorResponse handlerServiceException(XueChengPlusException e)
    {
        // 记录异常信息
        log.error("业务异常：{}",e.getErrMessage());

        return new RestErrorResponse(e.getErrMessage());
    }


    @ExceptionHandler(Exception.class)
    public RestErrorResponse handlerException(Exception e)
    {
        log.error("系统异常：{}",e.getMessage());

        return new RestErrorResponse(CommonError.UNKOWN_ERROR);
    }
}
