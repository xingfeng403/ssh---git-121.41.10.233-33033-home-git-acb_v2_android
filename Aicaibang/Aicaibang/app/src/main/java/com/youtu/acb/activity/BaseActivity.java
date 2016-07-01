package com.youtu.acb.activity;

import android.app.Activity;
import android.os.Bundle;

import com.umeng.analytics.MobclickAgent;
import com.youtu.acb.AcbApplication;

/**
 * Created by xingf on 16/5/26.
 */
public class BaseActivity extends Activity {
    AcbApplication mApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mApplication = AcbApplication.getInstance();

        AcbApplication.addCurrentActivity(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mApplication != null)
            mApplication.removeCurrentActivity(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        MobclickAgent.onResume(this);
    }


    @Override
    protected void onPause() {
        super.onPause();

        MobclickAgent.onPause(this);
    }
}
