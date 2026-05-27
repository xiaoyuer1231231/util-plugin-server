package com.dtdream.portal.quality.controller;

import com.dtdream.portal.quality.service.EncryptService;
import com.dtdream.portal.quality.utils.RestResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Api(tags = "数据加密、解密对接", produces = "application/json")
@RestController
@RequestMapping("/feature/cipher/third")
@RequiredArgsConstructor
public class EncryptController {
    @Autowired
    private RedisTemplate redisTemplate;
    private final EncryptService encryptService;

    @PostMapping("/encrypt")
    @ApiOperation(value = "加密", notes = "该接口用于数据加密", produces = MediaType.APPLICATION_JSON_VALUE)
    public RestResponse<Map<String, Object>> encrypt(@RequestBody Map<String, Object> plainMap) {
        return encryptService.encrypt(plainMap);
    }

    @PostMapping("/decrypt")
    @ApiOperation(value = "解密", notes = "该接口用于数据解密", produces = MediaType.APPLICATION_JSON_VALUE)
    public RestResponse<Map<String, Object>> decrypt(@RequestBody Map<String, Object> cipherMap) {
        return encryptService.decrypt(cipherMap);
    }

    @PostMapping("/pkcs7Sign")
    @ApiOperation(value = "PKCS7原文签名", notes = "该接口用于PKCS7原文签名", produces = MediaType.APPLICATION_JSON_VALUE)
    public RestResponse<Map<String, Object>> pkcs7Sign(@RequestBody Map<String, Object> signMap) {
        return encryptService.pkcs7Sign(signMap);
    }


    @GetMapping("/getInfo")
    @ApiOperation(value = "解密", notes = "该接口用于数据解密", produces = MediaType.APPLICATION_JSON_VALUE)
    public RestResponse<Map<String, Object>> decrypt(@RequestParam String msg) {
        //Redis简单示例：缓存String类型的数据
        ValueOperations valueOperations = redisTemplate.opsForValue();
//        valueOperations.set(msg,"ss");
//        redisTemplate.expire(msg, Duration.ofHours(1L).toHours(), TimeUnit.HOURS);
        Object o = valueOperations.get(msg);
        redisTemplate.opsForValue().set(msg, "sdsds", 72, TimeUnit.HOURS);
        //获取String类型的缓存数据

        System.out.println(o);

        return null;

    }



}
