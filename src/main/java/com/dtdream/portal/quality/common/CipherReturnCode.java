package com.dtdream.portal.quality.common;

/**
 * 返回码枚举类
 */
public enum CipherReturnCode {

    DM_OK(1, "初始化成功"),
    UNKNOWN_ERROR(0x1100001, "未知错误"),
    FILE_NOT_EXIST(0x1100002, "指定的文件不存在"),
    FILE_RESOLVE_FAILED(0x1100003, "文件解析失败"),
    DATA_INVALID(0x1100004, "数据不合法"),
    INIT_ERROR(0x1100005, "初始化异常或未初始化"),
    REQUEST_ERROR(0x1100006, "http接口请求异常"),
    SCHEMA_NOT_EXIST(0x1100007, "策略不存在"),
    SCHEMA_DISABLE(0x1100008, "策略制止使用"),
    SCHEMA_WARN(0x1100009, "策略告警"),
    SCHEMA_DISABLE_WARN(0x110000A, "策略制止并告警"),
    SCHEMA_EXPIRE(0x110000B, "策略过期"),
    SCHEMA_KEY_NOT_EXIST(0x110000C, "本地策略密钥不存在"),
    SCHEMA_KEY_INVALID(0x110000D, "本地策略密钥长度不合法");

    private Integer value;

    private String desc;

    CipherReturnCode(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static CipherReturnCode from(Integer value) {
        for (CipherReturnCode status : CipherReturnCode.values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }

    public Integer getValue() {
        return value;
    }

    @Override
    public String toString() {
        return desc;
    }

}
