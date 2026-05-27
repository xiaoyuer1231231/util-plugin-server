package com.dtdream.portal.quality.common;

public enum SignReturnCode {

    SUCCESS(0, "加签/验签成功"),
    CODE_1101(1101,"无效的输入参数"),
    CODE_1102(1102,"初始化失败"),
    CODE_1103(1103,"读取文件失败"),
    CODE_1105(1105,"不支持的摘要算法"),
    CODE_1106(1106,"解析pkcs7签名包失败"),
    CODE_1107(1107,"解析x509证书失败"),
    CODE_1108(1108,"原文为空"),
    CODE_1109(1109,"验签原文错误"),
    CODE_1110(1110,"创建数字信封失败"),
    CODE_1111(1111,"解析数字信封失败"),
    CODE_1112(1112,"Base数据校验失败"),
    CODE_1113(1113,"证书错误，需要sm2证书"),
    CODE_1115(1115,"解析公钥错误"),
    CODE_1201(1201,"未知的证书类型"),
    CODE_1202(1202,"数据加密失败"),
    CODE_1203(1203,"数据解密失败"),
    CODE_1204(1204,"参数无效"),
    CODE_1205(1205,"未知的对称加密算法"),
    CODE_1206(1206,"未知的对称加密补位类型"),
    CODE_1208(1208,"签名参数无效"),
    CODE_1209(1209,"验签参数无效"),
    CODE_1210(1210,"证书ID无效"),
    CODE_1211(1211,"服务器错误"),
    CODE_1212(1212,"URL错误"),
    CODE_1213(1213,"客户端负载执行错误"),
    CODE_1214(1214,"获取服务端响应值错误"),
    CODE_1215(1215,"获取文件输入流错误"),
    CODE_1216(1216,"读取文件错误");

    private Integer value;

    private String desc;

    SignReturnCode(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static SignReturnCode from(Integer value) {
        for (SignReturnCode status : SignReturnCode.values()) {
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
