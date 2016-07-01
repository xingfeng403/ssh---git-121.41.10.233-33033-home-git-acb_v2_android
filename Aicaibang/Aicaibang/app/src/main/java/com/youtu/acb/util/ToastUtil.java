package com.youtu.acb.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by xingf on 15/12/10.
 */
public class ToastUtil {
    private static long mLastShowTime = -1;

    public static void show(Context context, CharSequence str) {
        long nowTime = System.currentTimeMillis();
        if (nowTime - mLastShowTime < 1000) {
            return;
        } else {
            mLastShowTime = nowTime;
        }
        if (str != null) {
            Toast.makeText(context.getApplicationContext(), str, Toast.LENGTH_SHORT).show();
        }

    }
}
