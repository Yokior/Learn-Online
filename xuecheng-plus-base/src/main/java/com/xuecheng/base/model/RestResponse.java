package com.xuecheng.base.model;

import lombok.Data;

/**
 * @Description：
 * @Auther：Yokior
 * @Date：2024/1/19 15:07
 */
@Data
public class RestResponse<T>
{
    // 响应码 0正常 -1失败
    private int code;

    // 响应提示信息
    private String msg;

    // 响应内容
    private T result;

    public RestResponse()
    {
        this(0,"success");
    }

    public RestResponse(int code, String msg)
    {
        this.code = code;
        this.msg = msg;
    }

    public static <T> RestResponse<T> success(T result)
    {
        RestResponse<T> response = new RestResponse<>();
        response.setResult(result);
        return response;
    }

    public static <T> RestResponse<T> success(T result, String msg)
    {
        RestResponse<T> response = new RestResponse<>();
        response.setResult(result);
        response.setMsg(msg);
        return response;
    }

    public static <T> RestResponse<T> validfail(String msg)
    {
        RestResponse<T> response = new RestResponse<>();
        response.setCode(-1);
        response.setMsg(msg);
        return response;
    }

    public static <T> RestResponse<T> validfail(T result, String msg)
    {
        RestResponse<T> response = new RestResponse<>();
        response.setCode(-1);
        response.setResult(result);
        response.setMsg(msg);
        return response;
    }


}
