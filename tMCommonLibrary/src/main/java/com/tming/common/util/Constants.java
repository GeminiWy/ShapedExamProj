package com.tming.common.util;

/**
 * <p>公用库常量和配置文件</p>
 * <p>Created by yusongying on 2014年8月15日</p>
 */
public interface Constants {

    /**
     * 标识是否为Debug模式，非Debug模式将不会输出 {@link com.tming.common.util.Log} 日志
     */
    public static final boolean DEBUG = true;
    /**
     * Http请求连接超时
     */
    public final static int HTTP_CONNECT_TIMEOUT = 5000;
    /**
     * Http请求读取超时
     */
    public final static int HTTP_READ_TIMEOUT = 60000;
    /**
     * 公用库配置文件
     */
    public final static String SHARE_PREFERENCES_NAME = "tming_common_config";
    /**
     * 屏幕亮度保存Key
     */
    public final static String SHARE_PREFERENCES_BIRGHTNESS_KEY = "screen_brightness";
    /**
     * 流量控制保存Key
     */
    public final static String SHARE_PREFERENCES_IS_FLOW_CONTROL = "is_flow_control";
}
