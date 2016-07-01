package com.youtu.acb.common;

/**
 * Created by xingf on 16/4/27.
 */
public class Settings {
    // test
    public final static String RESOURCE_URL = "http://121.41.10.233:8808";

//    public final static String RESOURCE_URL = "https://www.youtuker.com";

    public final static String BASE_URL = RESOURCE_URL + "/v2/";  // 正式

    public final static String USER_URL = BASE_URL + "user";

    public final static String VERIFY_URL = BASE_URL + "verify";

    public final static String CODE_URL = BASE_URL + "code";

    public static String APK_SAVE = "";

    public static String TEMP_PATH = "";

    public static String PIC_PATH = "";

    /**
     * 屏幕高度
     */
    public static int DISPLAY_HEIGHT;
    /**
     * 屏幕宽度
     */
    public static int DISPLAY_WIDTH;
    /**
     * 状态栏高度
     */
    public static int STATUS_BAR_HEIGHT;
    /**
     * 屏幕高度与基准比例
     */
    public static float RATIO_HEIGHT;
    /**
     * 屏幕宽度与基准比例
     */
    public static float RATIO_WIDTH;
    /**
     * 标题栏高度
     */
    public static int TITLEBAR_HEIGHT;
    /**
     * 字体大小基准
     */
    public static float RATIO_TEXT12;

    /******************************************************************/
    public static final String APP_ID = "1105216327"; // QQ分享appid
    public static final String APP_SECRET = "V0RKdCfc3VXFndIb"; // QQ secret
    public static final String WEIXIN_APP_ID = "wxb843c8a8cb7df630"; // 微信分享appid
    public static final String WEIXIN_SECRET = "30d51f8f98f055abf99ec8d16f3b2b74"; // 微信分享secret
    public static final String SINA_APP_KEY = ""; // 新浪分享 appid
    public static final String SINA_APP_SECRET = ""; // 新浪分享secret
    /******************************************************************/


    public static final int CAMERA = 1001;
    public static final int ALBUM = 1002;
    public static final int CROP = 1004;
    public static final String PICTURE_TEMP_EXTENSION = ".tmp";

}
