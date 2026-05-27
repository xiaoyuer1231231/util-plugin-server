package com.dtdream.portal.quality.utils;

import com.dtdream.portal.quality.config.SignProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
public final class DataProcessUtil {

    private DataProcessUtil() {
        // do nothing
    }

    /**
     * 过滤得到需完整性校验数据
     * @see SignProperties
     *
     * @param fields 字段列表
     * @param data   原数据
     * @return 过滤后的字段 map
     */
    public static Map<String, Object> filterFields(List<String> fields, Map<String, Object> data) {
        if (CollectionUtils.isEmpty(fields)) {
            return Collections.emptyMap();
        }
        Map<String, Object> signData = new HashMap<>(data.size());
        // 需要完整性校验的字段，配置在 fields 中
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (fields.contains(entry.getKey()) && Objects.nonNull(entry.getValue())) {
                String plain = entry.getValue().toString();
                // 空串 / "null" / "--" 不参与验签
                if (entry.getValue() instanceof String && (StringUtils.isEmpty(plain)
                        || "null".equals(plain) || "--".equals(plain))) {
                    continue;
                }
                signData.put(entry.getKey(), entry.getValue());
            }
        }
        log.info("sign[]after filter dataSize: {}.", signData.size());
        return signData;
    }

    public static String b64Encode(String text) {
        byte[] data = text.getBytes(StandardCharsets.UTF_8);
        return Base64.getEncoder().encodeToString(data);
    }

    public static String b64Decode(String text) {
        byte[] base64Data = Base64.getDecoder().decode(text);
        return new String(base64Data, StandardCharsets.UTF_8);
    }

}
