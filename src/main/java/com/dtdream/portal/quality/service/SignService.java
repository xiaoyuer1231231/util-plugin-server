package com.dtdream.portal.quality.service;

import com.dtdream.portal.quality.utils.RestResponse;

import java.util.Map;

/**
 * 加签、验签服务
 */
public interface SignService {

    /**
     * 加签
     * @param data 原文
     * @param filterFields 是否过滤字段
     * @return 原文 + 签名
     */
    RestResponse<Map<String, Object>> sign(Map<String, Object> data, boolean filterFields);

    /**
     * 验签
     * @param dataWithSign 原文 + 签名
     * @param filterFields 是否过滤字段
     * @return true: 验签通过
     *         false: 验签失败
     */
    RestResponse<Boolean> verify(Map<String, Object> dataWithSign, boolean filterFields);

}
