package com.dtdream.portal.quality.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.TreeMap;

public class sign {
    public static void main(String[] args) throws UnsupportedEncodingException {
//            String twentyCharUUID = generate20CharUUID();
//
//            // 编码字符串
        String originalString = "MCoCAQIwEgIBATAKBggqgRzPVQFoAQoBAQMRAEpucqpnd0RVBDqaojkZB/4=";
        String encodedString = Base64.getEncoder().encodeToString(originalString.getBytes());
        System.out.println("Encoded String: " + encodedString);
//
//            // 解码字符串
//
        String ss="YWp4ZHNqai15Ynk=";
            byte[] decodedBytes = Base64.getDecoder().decode(ss);
            String decodedString = new String(decodedBytes,"UTF-8");
            System.out.println("Decoded String: " + decodedString);

        Map<String, String> parameters = new TreeMap<>();
        parameters.put("version", "1");
        parameters.put("signAlgo", "HmacSHA256");
        parameters.put("appId", "APP_662C3B95D1B445AFA707402654205020");
        parameters.put("deviceId", "DEV_C059C6FF92CC4ADF892CA086F58617FE");
        parameters.put("transId", "edd502f2-e5ee-453b-8ee1-9d198ee5a995");
//        parameters.put("keyUsage", "ENCRYPT/DECRYPT");
//        parameters.put("keySpec", "SM4_128");
//        parameters.put("alias", "安吉云管平台")
        parameters.put("keyId", "422c1c4c711648f0a58a8e7d39f87515");
        parameters.put("mode", "ECB");
        parameters.put("iv", "MTIzNDU2NzgxMjM0NTY3OA");
        parameters.put("padding", "PKCS7Padding");
        parameters.put("plainText", "emhhbmdzYW4=");
//        parameters.put("encData", "MCoCAQIwEgIBATAKBggqgRzPVQFoAQoBAQMRAEpucqpnd0RVBDqaojkZB/4=");

        String str = sortParameters(parameters);
        System.out.println(str);

        String stringSignTemp = hmacSHA256(str, "OKHgKMNe31iGISVkMAV5iI7UyM3vryFK");
        System.out.println(stringSignTemp);



    }

    public static String sortParameters(Map<String, String> parameters) {
        Map<String, String> sortedParameters = new TreeMap<>();
        parameters.entrySet().stream()
                .filter(entry -> entry.getValue() != null && !entry.getValue().isEmpty())
                .forEach(entry -> sortedParameters.put(entry.getKey(), entry.getValue()));
        StringBuilder stringBuilder = new StringBuilder();
        sortedParameters.forEach((key, value) ->stringBuilder.append(key).append("=").append(value).append("&"));
        String newStr = stringBuilder.toString().replaceFirst("&$", "");
        return newStr;
    }


    public static String hmacSHA256(String plainString, String key) {
        String cipherString = null;
        try {
            // 指定算法
            String algorithm = "HmacSHA256";
            // 创建密钥规范
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), algorithm);
            // 获取Mac对象实例
            Mac mac = Mac.getInstance(algorithm);
            // 初始化mac
            mac.init(secretKeySpec);
            // 计算mac
            byte[] macBytes = mac.doFinal(plainString.getBytes(StandardCharsets.UTF_8));

            String encodedString = Base64.getEncoder().encodeToString(macBytes);

            cipherString = encodedString;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cipherString;

    }
}
