package com.dtdream.portal.quality.service;

import com.dtdream.portal.quality.utils.RestResponse;

import java.util.Map;

/**
 * 加密、解密服务
 */
public interface EncryptService {

    /**
     * 加密
     * @param plainMap 明文
     * @return 密文 map
     */
    RestResponse<Map<String, Object>> encrypt(Map<String, Object> plainMap);

    /**
     * 解密
     * @param cipherMap 密文
     * @return 明文 map
     */
    RestResponse<Map<String, Object>> decrypt(Map<String, Object> cipherMap);

    /**
     * PKCS7 原文签名
     * @param signMap 签名参数（oriData、base64Cert等）
     * @return 签名结果
     */
    RestResponse<Map<String, Object>> pkcs7Sign(Map<String, Object> signMap);



}
