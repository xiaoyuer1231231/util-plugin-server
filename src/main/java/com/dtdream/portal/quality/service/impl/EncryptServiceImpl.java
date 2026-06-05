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
        JSONObject result = new JSONObject();
        String uuid = generate20CharUUID();
        result.put("transId", uuid);
        result.put("version", "1");
        result.put("signAlgo", "HmacSHA256");
        result.put("appId", appId);
        result.put("deviceId", deviceId);
        result.put("keyId", keyId);
        result.put("encData", data);
        Map<String, String> parameters = new TreeMap<>();
        parameters.put("version","1");
        parameters.put("transId",uuid);
        parameters.put("signAlgo","HmacSHA256");
        parameters.put("appId",appId);
        parameters.put("deviceId",deviceId);
        parameters.put("keyId",keyId);
        parameters.put("encData",data);
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
        parameters.put("mode",encMode);
        parameters.put("padding","PKCS7Padding");
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
        // 配置参数（与 application.yml 一致）
        String keyId = "3a724c42856a4ab4abfc04940c959351";
        String encMode = "ECB";
        String appId = "APP_FDDAF5805445434C83239787F0B285FD";
        String deviceId = "DEV_C4B96F69BA1449BB8C80276670584E29";
        String secret = "YGFSCeMB0KTrhcDkN6kVlavMEdEgZrcJ";

        // ========== 加密请求 JSON ==========
        String plainText = "zhangsan"; // 待加密明文
        String source = Base64.getEncoder().encodeToString(plainText.getBytes(StandardCharsets.UTF_8));
        String uuid = generate20CharUUID();

        JSONObject encryptResult = new JSONObject();
        encryptResult.put("version", "1");
        encryptResult.put("signAlgo", "HmacSHA256");
        encryptResult.put("appId", appId);
        encryptResult.put("deviceId", deviceId);
        encryptResult.put("transId", uuid);
        encryptResult.put("keyId", keyId);
        encryptResult.put("mode", encMode);
        encryptResult.put("iv", "1234567812345678");

        encryptResult.put("padding", "PKCS7Padding");
        encryptResult.put("plainText", source);

        Map<String, String> encryptParams = new TreeMap<>();
        encryptParams.put("version", "1");
        encryptParams.put("signAlgo", "HmacSHA256");
        encryptParams.put("appId", appId);
        encryptParams.put("deviceId", deviceId);
        encryptParams.put("transId", uuid);
        encryptParams.put("keyId", keyId);
        encryptParams.put("mode", encMode);
        encryptParams.put("padding", "PKCS7Padding");
        encryptParams.put("plainText", source);
        encryptParams.put("iv", "1234567812345678");
        String encryptSorted = sign.sortParameters(encryptParams);
        String encryptSignature = sign.hmacSHA256(encryptSorted, secret);
        encryptResult.put("signature", encryptSignature);

        System.out.println("========== 加密请求 JSON ==========");
        System.out.println("明文: " + plainText);
        System.out.println("明文Base64: " + source);
        System.out.println(encryptResult.toJSONString());
        System.out.println();

        // ========== 解密请求 JSON ==========
        // 用加密返回的 cipherTextBlob 作为解密输入，直接传入无需再编码
        String encData = "MCoCAQIwEgIBATAKBggqgRzPVQFoAQoBAQMRAC/DaikTGS6KnfpfTMUwLF0="; // 替换为加密返回的cipherTextBlob
        String uuid2 = generate20CharUUID();

        JSONObject decryptResult = new JSONObject();
        decryptResult.put("transId", uuid2);
        decryptResult.put("version", "1");
        decryptResult.put("signAlgo", "HmacSHA256");
        decryptResult.put("appId", appId);
        decryptResult.put("deviceId", deviceId);
        decryptResult.put("keyId", keyId);
        decryptResult.put("encData", encData);

        Map<String, String> decryptParams = new TreeMap<>();
        decryptParams.put("version", "1");
        decryptParams.put("transId", uuid2);
        decryptParams.put("signAlgo", "HmacSHA256");
        decryptParams.put("appId", appId);
        decryptParams.put("deviceId", deviceId);
        decryptParams.put("keyId", keyId);
        decryptParams.put("encData", encData);
        String decryptSorted = sign.sortParameters(decryptParams);
        String decryptSignature = sign.hmacSHA256(decryptSorted, secret);
        decryptResult.put("signature", decryptSignature);

        System.out.println("========== 解密请求 JSON ==========");
        System.out.println("密文cipherTextBlob: " + encData);
        System.out.println(decryptResult.toJSONString());
        System.out.println();

        // ========== 启动秘钥授权请求 JSON ==========
        String uuid3 = generate20CharUUID();

        JSONObject activateResult = new JSONObject();
        activateResult.put("version", "1");
        activateResult.put("signAlgo", "HmacSHA256");
        activateResult.put("appId", appId);
        activateResult.put("deviceId", deviceId);
        activateResult.put("transId", uuid3);
        activateResult.put("keyId", keyId);

        Map<String, String> activateParams = new TreeMap<>();
        activateParams.put("version", "1");
        activateParams.put("signAlgo", "HmacSHA256");
        activateParams.put("appId", appId);
        activateParams.put("deviceId", deviceId);
        activateParams.put("transId", uuid3);
        activateParams.put("keyId", keyId);
        String activateSorted = sign.sortParameters(activateParams);
        String activateSignature = sign.hmacSHA256(activateSorted, secret);
        activateResult.put("signature", activateSignature);

        System.out.println("========== 启动秘钥授权请求 JSON ==========");
        System.out.println(activateResult.toJSONString());
        System.out.println();

        // ========== 生成签名（HMAC）请求 JSON ==========
        String signSource = "zhangsan"; // 待签名原文
        String signSourceBase64 = Base64.getEncoder().encodeToString(signSource.getBytes(StandardCharsets.UTF_8));
        String uuid4 = generate20CharUUID();

        JSONObject signResult = new JSONObject();
        signResult.put("transId", uuid4);
        signResult.put("appId", appId);
        signResult.put("keyId", keyId);
        signResult.put("version", "1");
        signResult.put("source", signSourceBase64);
        signResult.put("signAlgo", "HmacSHA256");
        signResult.put("deviceId", deviceId);
        Map<String, String> signParams = new TreeMap<>();
        signParams.put("transId", uuid4);
        signParams.put("appId", appId);
        signParams.put("keyId", keyId);
        signParams.put("version", "1");
        signParams.put("source", signSourceBase64);
        signParams.put("signAlgo", "HmacSHA256");
        signParams.put("deviceId", deviceId);
        String signSorted = sign.sortParameters(signParams);
        String signSignature = sign.hmacSHA256(signSorted, secret);
        signResult.put("signature", signSignature);

        System.out.println("========== 生成签名请求 JSON ==========");
        System.out.println("原文: " + signSource);
        System.out.println("原文Base64: " + signSourceBase64);
        System.out.println(signResult.toJSONString());
        System.out.println();

        // ========== 验证签名请求 JSON ==========
        // 用生成签名返回的 hmac 值作为验签的 signature
        String hmacValue = "替换为生成签名返回的hmac值";
        String uuid5 = generate20CharUUID();
        JSONObject verifyResult = new JSONObject();
        verifyResult.put("transId", uuid5);
        verifyResult.put("appId", appId);
        verifyResult.put("keyId", keyId);
        verifyResult.put("version", "1");
        verifyResult.put("hmac", hmacValue);
        verifyResult.put("signAlgo", "HmacSHA256");
        verifyResult.put("deviceId", deviceId);

        Map<String, String> verifyParams = new TreeMap<>();
        verifyParams.put("transId", uuid5);
        verifyParams.put("appId", appId);
        verifyParams.put("keyId", keyId);
        verifyParams.put("version", "1");
        verifyParams.put("source", signSourceBase64);
        verifyParams.put("hmac", hmacValue);
        verifyParams.put("signAlgo", "HmacSHA256");

        String verifySorted = sign.sortParameters(verifyParams);
        String verifySignature = sign.hmacSHA256(verifySorted, secret);
        verifyResult.put("signature", verifySignature);

        System.out.println("========== 验证签名请求 JSON ==========");
        System.out.println("原文Base64: " + signSourceBase64);
        System.out.println("待验签hmac: " + hmacValue);
        System.out.println(verifyResult.toJSONString());
    }


}
