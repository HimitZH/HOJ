package top.hcode.hoj.common;

import lombok.Data;

import java.io.Serializable;


@Data
public class CommonResult implements Serializable {
    public final static Integer STATUS_SUCCESS = 200;
    public final static Integer STATUS_FAIL = 400;
    public final static Integer STATUS_NOT_FOUND = 404;
    public final static Integer STATUS_ERROR = 500;
    public final static Integer STATUS_ACCESS_DENIED = 401;

    private Integer status;   // 状态码
    private Object data;   // 返回的数据
    private String msg;    // 自定义信息

    public CommonResult(Integer status, Object data, String msg){
        this.status = status;
        this.data = data;
        this.msg = msg;
    }


    /**
     * 成功的结果
     * @param data 返回结果
     * @param msg  返回信息
     */
    public static CommonResult successResponse(Object data, String msg) {
        return new CommonResult(CommonResult.STATUS_SUCCESS, data, msg);
    }

    /**
     * 成功的结果
     * @param data    返回结果
     */
    public static CommonResult successResponse(Object data) {
        return new CommonResult(CommonResult.STATUS_SUCCESS, data, null);
    }


    /**
     * 失败的结果，无异常
     * @param msg  返回信息
     */
    public static CommonResult errorResponse(String msg) {
        return new CommonResult(CommonResult.STATUS_FAIL, null, msg);
    }


    public static CommonResult errorResponse(String msg,Integer status) {
        return new CommonResult(status, null,msg);
    }

}