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

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;

/**
 * Created by bottle on 11/2/17.
 */
@Slf4j
public class JsonProc {

    private static final Gson gson = new Gson();

    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }

    public static <T> T toObj(String json, Class<T> classOfT) {
        return gson.fromJson(json, classOfT);
    }

    public static <T> T toObj(String json, Type typeOfT) {
        return gson.fromJson(json, typeOfT);
    }

    public static <T> T fromJson(String json, Type type) {
        try {
            return gson.fromJson(json, type);
        } catch (JsonSyntaxException e) {
            log.debug("[debug]fromJson JsonSyntaxEx, json: {}, type: {}", json, type);
            throw e;
        }
    }
}