package com.hikvision.artemis.sdk.config;

/**
 * artemis host、appKey、appSecret配置
 * @author zhangtuo
 * @Date 2017/4/26
 */
public class ArtemisConfig {
    public static String host;
    public static String appKey;
    public static String appSecret;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getAppSecret() {
        return appSecret;
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }
}
