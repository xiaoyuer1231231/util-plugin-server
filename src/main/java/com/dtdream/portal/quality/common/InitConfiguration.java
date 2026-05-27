package com.dtdream.portal.quality.common;

import com.dtdream.portal.quality.config.DMDbProperties;
import com.koalii.svs.client.Svs2ClientHelper;
import com.olymtech.dbte.DbteDb;
import com.olymtech.dbte.DbteDbConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 初始化库配置类
 */
@Component
@Slf4j
public class InitConfiguration {

    @Autowired
    DMDbProperties properties;

    /**
     * 奥联初始化库（加解密）
     */
    @PostConstruct
    public void initCipher() {
        if (!properties.getCipher()) {
            return;
        }

        DbteDb.DMDbSetProperties(DbteDbConstants.DM_LOG_LEVEL, "3");

        // 1.属性配置
        // 平台下载的应用配置文件路径
        DbteDb.DMDbSetProperties(DbteDbConstants.DM_PLATFORM_FILEPATH, properties.getSetting());

        // 2.初始化库
        int iRet = DbteDb.DMDbInit();
        if (CipherReturnCode.DM_OK.getValue().equals(iRet)) {
            log.info("[init] init DMDb success: {}", CipherReturnCode.from(iRet).toString());
            return;
        } else {
            log.error("[init] init DMDb error. ErrorCode: {}. ErrorMessage: {}",
                    Integer.toHexString(iRet), CipherReturnCode.from(iRet).toString());
        }
    }

    /**
     * 格尔初始化库（加验签）
     */
    @PostConstruct
    public void initSign() {
        Svs2ClientHelper helper = Svs2ClientHelper.getInstance();
        helper.setLogFile("./log/svs_client.log");          //记录日志
        helper.enableHttps();                               //开启https，可选
//        helper.init("172.26.5.1", 5000, 20);
        helper.initServers("172.26.5.1:5000,172.26.5.2:5000,172.26.5.3:5000", 20);
        log.info("sign[]init. ge'er");
    }

}
