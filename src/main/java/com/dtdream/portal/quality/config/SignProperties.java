package com.dtdream.portal.quality.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@ConfigurationProperties(prefix = "sign")
@Data
@Component
public class SignProperties {

    /**
     * 完整性校验的字段
     */
    private List<String> fields;

}
