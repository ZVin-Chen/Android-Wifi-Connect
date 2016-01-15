package com.zvin.wificonnect.util;

/**
 * Created by chenzhengwen on 2015/12/28.
 */
public class Const {
    public static final String MAIN_OBSERBER_TAG = "main_observer_tag";

    public static final String PRE_LOGIN_TAG = "pre_login_tag--->";

    public static final long http_timeout=60 * 1000; //网络连接超时 60秒
    public static final long http_logout_timeout=60 * 1000; //下线时网络连接超时 20秒
    public static final long free_biz_http_timeout=10 * 1000; //下线时网络连接超时 20秒

    public static final String BAIDU_URL= "http://www.baidu.com";
    public static final String NEWS_BAIDU = "http://news.baidu.com";

    // 准现网地址，用于测试联调时使用

    public static final String HOST = "120.197.230.56";// "120.197.230.56";

    //portal判断是否是漫游的依据
    public static final String JUDGE_ROAMING = "pccwwifi;roamhk";
}
