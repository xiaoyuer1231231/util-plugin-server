package com.dtdream.portal.quality.service.impl;

import cn.hutool.http.HttpRequest;
import com.dtdream.portal.quality.common.CipherReturnCode;
import com.dtdream.portal.quality.service.EncryptService;
import com.dtdream.portal.quality.utils.RestResponse;
import com.dtdream.portal.quality.utils.sign;
import com.koalii.lib.com.alibaba.fastjson.JSONObject;
import com.olymtech.dbte.DbteDb;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 加解、解密服务
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EncryptServiceImpl implements EncryptService {
    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${encryptOrDecrypt.encryptUrl}")
    private String encryptUrl;

    @Value("${encryptOrDecrypt.decryptUrl}")
    private String decryptUrl;

    @Value("${encryptOrDecrypt.signUrl}")
    private String signUrl;

    @Value("${encryptOrDecrypt.keyId}")
    private  String keyId;

    @Value("${encryptOrDecrypt.encMode}")
    private  String encMode="ECB";

    @Value("${encryptOrDecrypt.appId}")
    private  String appId;

    @Value("${encryptOrDecrypt.deviceId}")
    private  String deviceId;

    @Value("${encryptOrDecrypt.secret}")
    private  String secret;


    /**
     * 加密为密文
     * @param plain 明文
     * @return 明文对应密文
     */
    private String generateCipherText(byte[] plain) throws UnsupportedEncodingException {
        // TODO 对数据进行加密 (对接密码机/sdk...
        // ************ 实现示例 start ************
        log.info("generateCipherText[]start");
        // 获取输入的明文;
        String request = new String(plain,"UTF-8");
        // 调用加密接口
        String msg = sysEncryptNum(request);
        JSONObject parse = JSONObject.parseObject(msg);
        if (parse.getInteger("status")!=200){
            log.error("decrypt[]error. cipher: {}, ErrorCode: {}. ErrorMessage: {}",
                    plain, parse.getString("code"), parse.getString("msg"));
            return null;
        }
        String data = parse.getJSONObject("data").getString("cipherTextBlob");
        // 插入已密文为key缓存
        log.info("generateCipherText[]start"+data+"|"+request);
        // 获取密文返回
        return data;
        // ************ 实现示例 end ************
    }

    /**
     * 解密为明文
     * @param cipher 密文
     * @return 密文对应明文
     */
    private byte[] generatePlainText(String  cipher) throws UnsupportedEncodingException {
        // TODO 对数据进行解密 (对接密码机/sdk...
        // ************ 实现示例 start ************
        // 获取输入的密文
        log.info("generatePlainText[]start"+cipher+"|"+cipher);
        //调用解密接口
        String msg = sysDecryptKey(cipher);
        JSONObject parse = JSONObject.parseObject(msg);
        if (parse.getInteger("status")!=200){
            log.info("decrypt[]error. cipher: {}, ErrorCode: {}. ErrorMessage: {}",
                    cipher, parse.getString("code"), parse.getString("msg"));
            return null;
        }
        String data = parse.getJSONObject("data").getString("data");
        // 添加base64 解密
        return data.getBytes(StandardCharsets.UTF_8);
        // ************ 实现示例 end ************
    }

    @SneakyThrows
    @Override
    public RestResponse<Map<String, Object>> encrypt(Map<String, Object> plainMap) {
        log.info("encrypt[]start. dataSize: {}.", plainMap.size());
        log.info("encrypt[]start. plainMap: {}.", plainMap);
        Map<String, Object> cipherMap = new HashMap<>(plainMap.size());
        // 对每条数据加密处理
        for (Map.Entry<String, Object> entry : plainMap.entrySet()) {
            // 空数据不做处理
            if (Objects.isNull(entry.getValue())) {
                cipherMap.put(entry.getKey(), null);
                continue;
            }
            byte[] plain = entry.getValue().toString().getBytes(StandardCharsets.UTF_8);
            String pwd = this.generateCipherText(plain);
            if (null == pwd) {
                return RestResponse.createResponse(Response.Status.INTERNAL_SERVER_ERROR);
            }
            cipherMap.put(entry.getKey(), pwd);
        }

        log.info("encrypt[]success. dataSize: {}.", cipherMap.size());
        return RestResponse.createResponseWithBody(Response.Status.OK, cipherMap);
    }

    @SneakyThrows
    @Override
    public RestResponse<Map<String, Object>> decrypt(Map<String, Object> cipherMap) {
        log.info("decrypt[]start. dataSize: {}.", cipherMap.size());
        log.info("decrypt[]start. cipherMap: {}.", cipherMap);
        Map<String, Object> plainMap = new HashMap<>(cipherMap.size());
        // 对每条数据解密处理
        for (Map.Entry<String, Object> entry : cipherMap.entrySet()) {
            // 空数据不做处理
            if (Objects.isNull(entry.getValue())) {
                plainMap.put(entry.getKey(), null);
                continue;
            }
            // Base64 解码后开始处理
            Object value = entry.getValue();
            byte[] plain = this.generatePlainText(value.toString());
            if (null == plain) {
                return RestResponse.createResponse(Response.Status.INTERNAL_SERVER_ERROR);
            }
            plainMap.put(entry.getKey(), new String(plain, StandardCharsets.UTF_8));
        }
        log.info("decrypt[]success. dataSize: {}.", plainMap.size());
        return RestResponse.createResponseWithBody(Response.Status.OK, plainMap);
    }
    /**
     * 解密
     * @param data
     * @return
     */
    public  String sysDecryptKey(String data){
        String source = Base64.getEncoder().encodeToString(data.getBytes(StandardCharsets.UTF_8));
        JSONObject result = new JSONObject();
        String uuid = generate20CharUUID();
        result.put("transId", uuid);
        result.put("version", "1");
        result.put("signAlgo", "HmacSHA256");
        result.put("appId", appId);
        result.put("deviceId", deviceId);
        result.put("keyId", keyId);
        result.put("encData", source);
        Map<String, String> parameters = new TreeMap<>();
        parameters.put("version","1");
        parameters.put("transId",uuid);
        parameters.put("signAlgo","HmacSHA256");
        parameters.put("appId",appId);
        parameters.put("deviceId",deviceId);
        parameters.put("keyId",keyId);
        parameters.put("encData",source);
        String s = sign.sortParameters(parameters);
        String signature = sign.hmacSHA256(s, secret);
        result.put("signature",signature);
        log.info("encryptData[]request: {}", result.toJSONString());
        String postResults = HttpRequest.post(decryptUrl)
                .body(result.toJSONString())
                .timeout(2000).execute().body();
        return postResults;

    }

    /**
     *加密
     * @param data
     * @return
     */
    public  String sysEncryptNum(String data){
        // 明文进行Base64编码
        String source = Base64.getEncoder().encodeToString(data.getBytes(StandardCharsets.UTF_8));
        JSONObject result = new JSONObject();
        String uuid = generate20CharUUID();
        result.put("version", "1");
        result.put("signAlgo", "HmacSHA256");
        result.put("appId", appId);
        result.put("deviceId", deviceId);
        result.put("transId", uuid);
        result.put("keyId", keyId);
        result.put("mode", encMode);
        result.put("padding", "PKCS7Padding");
        result.put("plainText", source);
        Map<String, String> parameters = new TreeMap<>();
        parameters.put("version","1");
        parameters.put("signAlgo","HmacSHA256");
        parameters.put("appId",appId);
        parameters.put("deviceId",deviceId);
        parameters.put("keyId",keyId);
        parameters.put("transId",uuid);
        parameters.put("plainText",source);
        String s = sign.sortParameters(parameters);
        String signature = sign.hmacSHA256(s, secret);
        result.put("signature",signature);
        log.info("encryptData[]request: {}", result.toJSONString());
        String postResults = HttpRequest.post(encryptUrl)
                .body(result.toJSONString())
                .timeout(2000).execute().body();
        JSONObject parse = JSONObject.parseObject(postResults);
        if (parse.getInteger("status") != 200) {
            log.error("encryptData[]error. plainText: {}, status: {}, message: {}",
                    parse, parse.getInteger("status"), parse.getString("message"));
            return null;
        }
        return postResults;
    }
    public static String generate20CharUUID() {
        UUID uuid = UUID.randomUUID();
        String str = uuid.toString();
        // 将UUID中的'-'移除，转换成20位字符串
        return str;
    }



    /**
     * PKCS7 原文签名
     */
    @SneakyThrows
    @Override
    public RestResponse<Map<String, Object>> pkcs7Sign(Map<String, Object> signMap) {
        log.info("pkcs7Sign[]start. params: {}.", signMap);

        String oriData = (String) signMap.get("oriData");
        String base64Cert = (String) signMap.get("base64Cert");
        String signAlgIdentifier = (String) signMap.get("signAlgIdentifier");
        String attach = (String) signMap.get("attach");
        String verifyCert = (String) signMap.get("verifyCert");
        String keyStore = (String) signMap.get("keyStore");
        String keyPairId = (String) signMap.get("keyPairId");
        Object keyPairVersionObj = signMap.get("keyPairVersion");
        Integer keyPairVersion = null;
        if (keyPairVersionObj instanceof Integer) {
            keyPairVersion = (Integer) keyPairVersionObj;
        } else if (keyPairVersionObj != null) {
            keyPairVersion = Integer.valueOf(keyPairVersionObj.toString());
        }

        String msg = sysPkcs7Sign(oriData, base64Cert, signAlgIdentifier, attach, verifyCert, keyStore, keyPairId, keyPairVersion);
        JSONObject parse = JSONObject.parseObject(msg);
        if (parse.getInteger("status") != 200) {
            log.error("pkcs7Sign[]error. oriData: {}, ErrorCode: {}. ErrorMessage: {}",
                    oriData, parse.getString("code"), parse.getString("msg"));
            return RestResponse.createResponse(Response.Status.INTERNAL_SERVER_ERROR);
        }

        Map<String, Object> resultMap = new HashMap<>();
        JSONObject data = parse.getJSONObject("data");
        resultMap.put("transId", data.getString("transId"));
        resultMap.put("signValue", data.getString("signValue"));

        log.info("pkcs7Sign[]success. transId: {}.", data.getString("transId"));
        return RestResponse.createResponseWithBody(Response.Status.OK, resultMap);
    }

    /**
     * 调用PKCS7签名接口
     */
    public String sysPkcs7Sign(String oriData, String base64Cert, String signAlgIdentifier,
                               String attach, String verifyCert, String keyStore,
                               String keyPairId, Integer keyPairVersion) {
        JSONObject result = new JSONObject();
        String uuid = generate20CharUUID();
        result.put("version", 1);
        result.put("signAlgo", "HmacSHA256");
        result.put("transId", uuid);
        result.put("appId", appId);
        result.put("deviceId", deviceId);
        result.put("signAlgIdentifier", signAlgIdentifier);
        result.put("oriData", oriData);
        result.put("base64Cert", base64Cert);
        result.put("attach", attach);
        result.put("verifyCert", verifyCert);
        result.put("keyStore", keyStore);
        result.put("keyPairId", keyPairId);
        result.put("keyPairVersion", keyPairVersion);

        Map<String, String> parameters = new TreeMap<>();
        parameters.put("version", "1");
        parameters.put("signAlgo", "HmacSHA256");
        parameters.put("transId", uuid);
        parameters.put("appId", appId);
        parameters.put("deviceId", deviceId);
        parameters.put("signAlgIdentifier", signAlgIdentifier);
        parameters.put("oriData", oriData);
        parameters.put("base64Cert", base64Cert);
        parameters.put("attach", attach);
        parameters.put("verifyCert", verifyCert);
        parameters.put("keyStore", keyStore);
        parameters.put("keyPairId", keyPairId);
        parameters.put("keyPairVersion", String.valueOf(keyPairVersion));
        String sortedParams = sign.sortParameters(parameters);
        String signature = sign.hmacSHA256(sortedParams, secret);
        result.put("signature", signature);

        log.info("sysPkcs7Sign[]request: {}", result.toJSONString());
        String postResults = HttpRequest.post(signUrl)
                .body(result.toJSONString())
                .timeout(2000).execute().body();
        return postResults;
    }


    public static void main(String[] args) throws UnsupportedEncodingException {
        System.out.println(System.currentTimeMillis());
//        String data="TUNvQ0FRSXdFZ0lCQVRBS0JnZ3FnUnpQVlFGb0FRb0JBUU1SQUpCRDRabDBWN2xxamFlbStTZmx3M2c9";
//        byte[] cipher = Base64.getDecoder().decode(data);
//        String request = new String(cipher,"UTF-8");
//        System.out.println(request);
//
//        String keyId="422c1c4c711648f0a58a8e7d39f87515";
//        String encMode="ECB";
//        String iv="MTIzNDU2NzgxMjM0NTY3OA";
//        String padMode="PKCS7Padding";
//        String appId="APP_662C3B95D1B445AFA707402654205020";
//        String deviceId="DEV_C059C6FF92CC4ADF892CA086F58617FE";
//        String secret="OKHgKMNe31iGISVkMAV5iI7UyM3vryFK";
//        JSONObject result = new JSONObject();
//        String uuid = generate20CharUUID();
//        result.put("version","1");
//        result.put("signAlgo","HmacSHA256");
//        result.put("appId",appId);
//        result.put("deviceId",deviceId);
//        result.put("keyId",keyId);
//        result.put("transId",uuid);
//        result.put("encData",request);
//        Map<String, String> parameters = new TreeMap<>();
//        parameters.put("version","1");
//        parameters.put("signAlgo","HmacSHA256");
//        parameters.put("appId",appId);
//        parameters.put("deviceId",deviceId);
//        parameters.put("keyId",keyId);
//        parameters.put("transId",uuid);
//        parameters.put("encData",request);
//        String s = sign.sortParameters(parameters);
//        String signature = sign.hmacSHA256(s, secret);
//        result.put("signature",signature);
//
//        log.info("sysDecryptKey[]start"+result.toJSONString());

    }


}
