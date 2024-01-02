package com.xuecheng.base.exception;

import lombok.Data;

/**
 * @Description：项目自定义异常信息
 * @Auther：Yokior
 * @Date：2024/1/2 15:07
 */
@Data
public class XueChengPlusException extends RuntimeException
{
    private String errMessage;


    public static void cast(String errMessage)
    {
        throw new XueChengPlusException(errMessage);
    }

    public static void cast(CommonError commonError)
    {
        throw new XueChengPlusException(commonError.getErrMessage());
    }

    public XueChengPlusException()
    {

    }

    public XueChengPlusException(String errMessage)
    {
        super(errMessage);
        this.errMessage = errMessage;
    }
}
