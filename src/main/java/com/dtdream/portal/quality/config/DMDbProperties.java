package com.dtdream.portal.quality.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "dmdb")
@Data
@Component
public class DMDbProperties {

    /**
     * 初始化配置文件路径
     */
    private String setting;

    /**
     * 加解密服务开关
     */
    private Boolean cipher;

}
