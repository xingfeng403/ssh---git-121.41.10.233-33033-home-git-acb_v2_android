package com.youtu.acb;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.youtu.acb.common.Settings;
import com.youtu.acb.entity.UserInfo;
import com.youtu.acb.util.DaoUtil;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xingf on 16/4/28.
 */

public class AcbApplication extends Application {
    public static final String TAG = "";
    private static AcbApplication mApplication;
    public static boolean mIsTaskStart;
    private Context self = this;
    private static UserInfo mUserInfo;
    public static boolean IS_DEBUG = false;

    // 启动的Activity集合
    public static List<Activity> mActivityList = new ArrayList<Activity>();

    public static UserInfo getmUserInfo() {
        return mUserInfo;
    }

    public static void setmUserInfo(UserInfo mUserInfo) {
        AcbApplication.mUserInfo = mUserInfo;
    }

//    public static RefWatcher getRefWatcher(Context context) {
//        LicaibaoApplication application = (LicaibaoApplication) context.getApplicationContext();
//        return application.refWatcher;
//    }
//
//    private RefWatcher refWatcher;

    @Override
    public void onCreate() {
        super.onCreate();

//        refWatcher = LeakCanary.install(this);

        mApplication = this;

        // 出现应用级异常时的处理
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            String errMsg = "";

            @Override
            public void uncaughtException(Thread thread, Throwable throwable) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                throwable.printStackTrace(pw);
                errMsg = sw.toString();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        if (mActivityList.size() > 0) {

                            new AlertDialog.Builder(getCurrentActivity()).setTitle(R.string.app_name)
                                    .setMessage(R.string.err_msg)
                                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            // 强制退出程序
                                            finish();
                                        }
                                    }).setCancelable(false).show();

                        } else {
                            Toast.makeText(self, "程序异常需要重启", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, errMsg);
                            finish();
                        }
                        Looper.loop();
                    }
                }).start();

                // 错误LOG
                Log.e(TAG, throwable.getMessage(), throwable);
            }
        });

        init();

    }

    /**
     * 启动程序时的处理
     */
    public void init() {
        // 获得屏幕高度（像素）
        Settings.DISPLAY_HEIGHT = getResources().getDisplayMetrics().heightPixels;
        // 获得屏幕宽度（像素）
        Settings.DISPLAY_WIDTH = getResources().getDisplayMetrics().widthPixels;
        // 获得系统状态栏高度（像素）
        Settings.STATUS_BAR_HEIGHT = getStatusBarHeight();
        // 获得屏幕高度比例
        Settings.RATIO_HEIGHT = Settings.DISPLAY_HEIGHT / 1344.0f;
        // 获得屏幕宽度比例
        Settings.RATIO_WIDTH = Settings.DISPLAY_WIDTH / 748.0f;
        // 计算标题栏高度
        Settings.TITLEBAR_HEIGHT = (int) (88 * Settings.RATIO_HEIGHT);

        String parentPath = null;
        // 存在SDCARD的时候，路径设置到SDCARD
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            parentPath = Environment.getExternalStorageDirectory().getPath() + File.separator + getPackageName();
            // 不存在SDCARD的时候，路径设置到ROM
        } else {
            parentPath = Environment.getDataDirectory().getPath() + "/data/" + getPackageName();
        }

        // 临时文件路径设置
        Settings.TEMP_PATH = parentPath + "/tmp";
        // 图片缓存路径设置
        Settings.PIC_PATH = parentPath + "/pic";
        // 更新APK路径设置
        Settings.APK_SAVE = parentPath + "/upd";

        // 创建各目录
        new File(Settings.TEMP_PATH).mkdirs();
        new File(Settings.PIC_PATH).mkdirs();
        new File(Settings.PIC_PATH, ".nomedia").mkdir();
        new File(Settings.APK_SAVE).mkdirs();

        setmUserInfo(DaoUtil.getUserInfoFromLocal(this));
    }

    /**
     * 移除当前的activity
     *
     * @param activity
     */
    public void removeCurrentActivity(Activity activity) {
        if (activity != null)
            mActivityList.remove(activity);
    }

    // 生成Activity存入列表
    public static void addCurrentActivity(Activity activity) {
        mActivityList.add(activity);
    }

    // 获取当前Activity对象
    public static void removeActivity(Activity activity) {
        mActivityList.remove(activity);
    }

    public static Activity getCurrentActivity() {
        if (mActivityList.size() > 0) {
            return mActivityList.get(mActivityList.size() - 1);
        }
        return null;
    }

    public void clearActivityList() {
        for (int i = 0; i < mActivityList.size(); i++) {
            Activity activity = mActivityList.get(i);
            activity.finish();
        }

        mActivityList.clear();
    }

    public void finish() {
        clearActivityList();
        System.exit(0);
    }


    /**
     * 获得系统状态栏高度
     *
     * @return 系统状态栏高度（像素）
     */
    private int getStatusBarHeight() {
        try {
            Class<?> cls = Class.forName("com.android.internal.R$dimen");
            Object obj = cls.newInstance();
            Field field = cls.getField("status_bar_height");
            int x = Integer.parseInt(field.get(obj).toString());
            return getResources().getDimensionPixelSize(x);
        } catch (Exception e) {
        }
        return 0;
    }


    public static AcbApplication getInstance() {
        return mApplication;
    }
}
