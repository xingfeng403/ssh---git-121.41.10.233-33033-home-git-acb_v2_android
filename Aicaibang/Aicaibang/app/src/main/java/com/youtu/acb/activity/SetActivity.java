package com.youtu.acb.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.youtu.acb.R;
import com.youtu.acb.Views.Titlebar;
import com.youtu.acb.common.Settings;
import com.youtu.acb.util.DirectListener;
import com.youtu.acb.util.OnSingleClickListener;

/**
 * 设置
 */
public class SetActivity extends BaseActivity {

    private Titlebar mTitleBar;
    private LinearLayout mTuisong;          // 活动推送
    private LinearLayout mAboutUs;               // 关于我们
    private LinearLayout mHelpCenter;            // 帮助中心
    private LinearLayout mClearCache;            // 清理缓存
    private Context mSelf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set);

        mSelf = SetActivity.this;

        mTitleBar = (Titlebar) findViewById(R.id.set_titlebar);
        mTitleBar.getLayoutParams().height = Settings.TITLEBAR_HEIGHT;
        mTitleBar.setTitle(getString(R.string.set_title));
        mTitleBar.getmLeftPart().setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                SetActivity.this.finish();
            }
        });

        mTuisong = (LinearLayout) findViewById(R.id.set_login_pwd);
        mAboutUs = (LinearLayout) findViewById(R.id.set_auto_reg);
        mHelpCenter = (LinearLayout) findViewById(R.id.set_plat_acc);
        mClearCache = (LinearLayout) findViewById(R.id.set_plat_pwd);

        int height100 = (int) (Settings.RATIO_HEIGHT * 100);
        mTuisong.getLayoutParams().height = height100;
        mAboutUs.getLayoutParams().height = height100;
        mHelpCenter.getLayoutParams().height = height100;
        mClearCache.getLayoutParams().height = height100;

        mHelpCenter.setOnClickListener(new DirectListener(SetActivity.this, HelpCenterActivity.class));

    }
}
