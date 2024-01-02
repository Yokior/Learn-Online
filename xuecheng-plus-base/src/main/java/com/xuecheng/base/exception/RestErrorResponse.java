package com.xuecheng.base.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * @Description：
 * @Auther：Yokior
 * @Date：2024/1/2 15:05
 */
@Data
@AllArgsConstructor
public class RestErrorResponse implements Serializable
{
    private String errMessage;

    public RestErrorResponse(CommonError commonError)
    {
        this.errMessage = commonError.getErrMessage();
    }
}
