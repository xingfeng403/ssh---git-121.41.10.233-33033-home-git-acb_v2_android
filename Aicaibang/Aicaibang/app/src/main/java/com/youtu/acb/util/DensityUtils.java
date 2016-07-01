package com.youtu.acb.util;

import android.content.Context;


import com.youtu.acb.common.Settings;

import java.lang.reflect.Field;

public class DensityUtils {

    public final static int dp2px(Context context, float dpValue) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * density + 0.5f);
    }

    public final static int px2dp(Context context, float pxValue) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / density + 0.5f);
    }

    /**
     * 获得系统状态栏高度
     */
    public final static int getStatusBarHeight(Context context) {
        try {
            Class<?> cls = Class.forName("com.android.internal.R$dimen");
            Object obj = cls.newInstance();
            Field field = cls.getField("status_bar_height");
            int x = Integer.parseInt(field.get(obj).toString());
            return context.getResources().getDimensionPixelSize(x);
        } catch (Exception e) {}
        return 0;
    }

    public final static float getTextSize(Context context, int pixel) {
        float density = context.getResources().getDisplayMetrics().density;
        return pixel* Settings.RATIO_HEIGHT / density;
    }
}