package com.dtdream.portal.quality.controller;

import com.dtdream.portal.quality.service.SignService;
import com.dtdream.portal.quality.utils.RestResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Api(tags = "数据加签、验签对接", produces = "application/json")
@RestController
@RequestMapping("/feature/cipher/third")
@RequiredArgsConstructor
public class SignController {

    private final SignService signService;

    @PostMapping("/sign")
    @ApiOperation(value = "加签", notes = "该接口用于数据加签", produces = MediaType.APPLICATION_JSON_VALUE)
    public RestResponse<Map<String, Object>> sign(@RequestBody Map<String, Object> data) {
        return signService.sign(data, false);
    }

    @PostMapping("/verify")
    @ApiOperation(value = "验签", notes = "该接口用于数据验签", produces = MediaType.APPLICATION_JSON_VALUE)
    public RestResponse<Boolean> verify(@RequestBody Map<String, Object> dataWithSign) {
        return signService.verify(dataWithSign, false);
    }

    @PostMapping("/scopeSign")
    @ApiOperation(value = "加签", notes = "该接口用于数据加签(指定范围)", produces = MediaType.APPLICATION_JSON_VALUE)
    public RestResponse<Map<String, Object>> scopeSign(@RequestBody Map<String, Object> data) {
        return signService.sign(data, true);
    }

    @PostMapping("/scopeVerify")
    @ApiOperation(value = "验签", notes = "该接口用于数据验签(指定范围)", produces = MediaType.APPLICATION_JSON_VALUE)
    public RestResponse<Boolean> scopeVerify(@RequestBody Map<String, Object> dataWithSign) {
        return signService.verify(dataWithSign, true);
    }

}
