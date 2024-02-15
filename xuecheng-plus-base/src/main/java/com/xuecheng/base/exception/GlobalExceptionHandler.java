package com.xuecheng.base.exception;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.stream.Collectors;

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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public RestErrorResponse handlerServiceException(MethodArgumentNotValidException e)
    {
        // 记录异常信息
        BindingResult bindingResult = e.getBindingResult();
        List<String> errorList = bindingResult.getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());

        // 将list信息错误信息拼接
        String errMessage = StringUtils.join(errorList, ",");

        return new RestErrorResponse(errMessage);
    }


    @ExceptionHandler(Exception.class)
    public RestErrorResponse handlerException(Exception e)
    {
        if ("不允许访问".equals(e.getMessage()))
        {
            return new RestErrorResponse(CommonError.ACCESS_DENIED_ERROR);
        }

        log.error("系统异常：{}",e);
        return new RestErrorResponse(CommonError.UNKOWN_ERROR);
    }
}
