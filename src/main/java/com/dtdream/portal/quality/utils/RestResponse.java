/*
 * ------------------------------------------------------------------
 * Copyright © 2017 Hangzhou DtDream Technology Co.,Ltd. All rights reserved.
 * ------------------------------------------------------------------
 *  Product: tms
 *  Module Name: tms
 *  Date Created: 2017/11/03
 *  Description:
 * ------------------------------------------------------------------
 * Modification History
 * DATE            Name           Description
 * ------------------------------------------------------------------
 * 2017/11/03      bottle
 * ------------------------------------------------------------------
 */

package com.dtdream.portal.quality.utils;

import javax.ws.rs.core.Response;

/**
 * Created by bottle on 11/2/17.
 */
public class RestResponse<T> {

    private Integer code;
    private String message;
    private T data;

    public RestResponse(Integer code, String message) {

        this.code = code;
        this.message = message;
        this.data = null;
    }

    public RestResponse(Integer code, String message, T data) {

        this.code = code;
        this.message = message;
        this.data = data;

    }

//    public static String createResponse(Response.Status status) {
//        return new RestResponse(status.getStatusCode(), status.toString()).toString();
//    }

    public static RestResponse createResponse(Response.Status status) {
        return new RestResponse(status.getStatusCode(), status.toString());
    }

    @Override
    public String toString() {
        return JsonProc.toJson(this);
    }

//    public static <T> String createResponseWithBody(Response.Status status, T body) {
//        return new RestResponse<T>(status.getStatusCode(), status.toString(), body).toString();
//    }

    public static <T> RestResponse createResponseWithBody(Response.Status status, T data) {
        return new RestResponse<T>(status.getStatusCode(), status.toString(), data);
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}