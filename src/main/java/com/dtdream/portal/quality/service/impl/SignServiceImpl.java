package com.dtdream.portal.quality.service.impl;

import cn.hutool.http.HttpRequest;
import com.dtdream.portal.quality.common.SignReturnCode;
import com.dtdream.portal.quality.config.SignProperties;
import com.dtdream.portal.quality.service.SignService;
import com.dtdream.portal.quality.utils.DataProcessUtil;
import com.dtdream.portal.quality.utils.RestResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.koalii.lib.com.alibaba.fastjson.JSONObject;
import com.koalii.svs.client.Svs2ClientHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 加签、验签服务
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SignServiceImpl implements SignService {

    private final SignProperties properties;
    @Value("${encryptOrDecrypt.checksumVerifyUrl}")
    private String checksumVerifyUrl;

    private final String keyId="422c1c4c711648f0a58a8e7d39f87515";
    private final String appId="APP_662C3B95D1B445AFA707402654205020";
    /**
     * 生成签名
     * @param data 原数据
     * @return 原数据对应签名
     */
    private String generateSignature(byte[] data) throws UnsupportedEncodingException {
        // TODO 对数据生成签名 (对接密码机/sdk...
        // ************ 实现示例 start ************
        Svs2ClientHelper helper = Svs2ClientHelper.getInstance();

        // 调用密码机加签接口，判断
        String s = new String(data,"UTF-8");
        log.info("generateSignature[]start"+s);
        String source = Base64.getEncoder().encodeToString(s.getBytes());
        String uuid = generate20CharUUID();
        JSONObject result = new JSONObject();
        result.put("transId",uuid);
        result.put("appId",appId);
        result.put("keyId",keyId);
        result.put("source",source);
        log.info("generateSignature[]start"+result.toJSONString());
        String postResults = HttpRequest.post(checksumVerifyUrl)
                .body(result.toJSONString())
                .timeout(2000).execute().body();
        JSONObject parse = JSONObject.parseObject(postResults);
        if (parse.getInteger("status")!=200){
            log.error("sign[]error. ErrorCode: {}. ErrorMessage: {}",
                    parse.getInteger("code"), parse.getInteger("msg"));
            return null;
        }
        String hmac = parse.getJSONObject("data").getString("hmac");
        return hmac;
        // ************ 实现示例 end ************
    }

    /**
     * 验证签名
     * @param data      原数据
     * @param signature 原数据签名
     * @return 是否验签通过
     */
    private boolean verifySignature(byte[] data, String signature) throws UnsupportedEncodingException {
        // TODO 对数据验证签名 (对接密码机/sdk...
        // ************ 实现示例 start ************
        String s = new String(data,"UTF-8");
        String source = Base64.getEncoder().encodeToString(s.getBytes());
        log.info("verifySignature[]start"+source);

        JSONObject result = new JSONObject();
        String uuid = generate20CharUUID();
        result.put("transId",uuid);
        result.put("appId",appId );
        result.put("keyId",keyId);
        result.put("source",source);
        String postResults = HttpRequest.post(checksumVerifyUrl)
                .body(result.toJSONString())
                .timeout(2000).execute().body();
        log.info("verifySignature[]start"+postResults);

        JSONObject parse = JSONObject.parseObject(postResults);
        if (parse.getInteger("status")==200){
            if (parse.getJSONObject("data").getString("hmac").equals(signature)){
                return true;
            }
            return false;
        }
        return true;
        // ************ 实现示例 end ************
    }

    @Override
    public RestResponse<Map<String, Object>> sign(Map<String, Object> data, boolean filterFields) {
        try {
            log.info("sign[]start. dataSize: {}, needFilter: {}.", data.size(), filterFields);

            // 获取需加签的字段（可配置）并按自然序排序
            TreeMap<String, Object> signData = new TreeMap<>(String::compareTo);
            signData.putAll(filterFields ?
                    DataProcessUtil.filterFields(properties.getFields(), data) : data);

            // 对 signData 整体加签，并将签名加入原数据中
            byte[] originData = new ObjectMapper().writeValueAsString(signData).getBytes(StandardCharsets.UTF_8);
            String signature = this.generateSignature(originData);
            if (null == signature) {
                return RestResponse.createResponse(Response.Status.INTERNAL_SERVER_ERROR);
            }
            data.put("sign", signature);

            log.info("sign[]success. 签名结果（base64）前六位 ==> {}", signature.substring(0, 6));
            return RestResponse.createResponseWithBody(Response.Status.OK, data);
        } catch (JsonProcessingException | UnsupportedEncodingException e) {
            log.info("sign[]error. cause: {}", Throwables.getStackTraceAsString(e));
            return RestResponse.createResponse(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public RestResponse<Boolean> verify(Map<String, Object> dataWithSign, boolean filterFields) {
        try {
            log.info("verify[]start. dataSize: {}, needFilter: {}.", dataWithSign.size(), filterFields);

            // 获取签名
            if (Objects.isNull(dataWithSign.get("sign"))) {
                return RestResponse.createResponse(Response.Status.INTERNAL_SERVER_ERROR);
            }
            String signature = String.valueOf(dataWithSign.remove("sign"));

            // 获取需验签的字段（可配置）并按自然序排序
            TreeMap<String, Object> signData = new TreeMap<>(String::compareTo);
            signData.putAll(filterFields ?
                    DataProcessUtil.filterFields(properties.getFields(), dataWithSign) : dataWithSign);

            // 对 signData 整体验签, 得到验签结果
            byte[] originData = new ObjectMapper().writeValueAsString(signData).getBytes(StandardCharsets.UTF_8);
            boolean result = this.verifySignature(originData, signature);
            log.info("verify[]success. 验签结果 ==> {}", result);
            return RestResponse.createResponseWithBody(Response.Status.OK, result);
        } catch (JsonProcessingException | UnsupportedEncodingException e) {
            log.info("verify[]error. cause: {}", Throwables.getStackTraceAsString(e));
            return RestResponse.createResponse(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    public static String generate20CharUUID() {
        UUID uuid = UUID.randomUUID();
        String str = uuid.toString();
        // 将UUID中的'-'移除，转换成20位字符串
        return str;
    }




}
