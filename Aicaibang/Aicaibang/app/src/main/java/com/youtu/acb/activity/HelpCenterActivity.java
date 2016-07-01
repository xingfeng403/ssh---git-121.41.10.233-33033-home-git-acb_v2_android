package com.youtu.acb.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.youtu.acb.R;
import com.youtu.acb.Views.Titlebar;
import com.youtu.acb.common.Settings;
import com.youtu.acb.util.DaoUtil;
import com.youtu.acb.util.DirectListener;
import com.youtu.acb.util.OnSingleClickListener;

/**
 * 帮助中心
 */
public class HelpCenterActivity extends BaseActivity {
    private Titlebar mTitleBar;
    private LinearLayout mFeedback;
    private LinearLayout mPlatRep;
    private LinearLayout mCommonQues;
    private LinearLayout mHotline;
    private LinearLayout mWxClient;
    private LinearLayout mQQGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_center);

        mTitleBar = (Titlebar) findViewById(R.id.help_center_titlebar);
        mTitleBar.getLayoutParams().height = Settings.TITLEBAR_HEIGHT;
        mTitleBar.setTitle("帮助中心");
        mTitleBar.getmLeftPart().setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                onBackPressed();
            }
        });

        mFeedback = (LinearLayout) findViewById(R.id.feedback);
        mPlatRep = (LinearLayout) findViewById(R.id.plat_report);
        mCommonQues = (LinearLayout) findViewById(R.id.usual_ques);
        mHotline = (LinearLayout) findViewById(R.id.hotline);
        mWxClient = (LinearLayout) findViewById(R.id.wx_client);
        mQQGroup = (LinearLayout) findViewById(R.id.formal_qq_group);

        mFeedback.setOnClickListener(new DirectListener(HelpCenterActivity.this, FeedbackActivity.class));
        mPlatRep.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                if (DaoUtil.isLogined(HelpCenterActivity.this)) {
                    startActivity(new Intent(HelpCenterActivity.this, PlatReportActivity.class));
                } else {
                    startActivity(new Intent(HelpCenterActivity.this, LoginActivity.class));
                }
            }
        });


        int height90 = (int) (Settings.RATIO_HEIGHT * 90);
        mFeedback.getLayoutParams().height = height90;
        mPlatRep.getLayoutParams().height = height90;
        mCommonQues.getLayoutParams().height = height90;
        mHotline.getLayoutParams().height = height90;
        mWxClient.getLayoutParams().height = height90;
        mQQGroup.getLayoutParams().height = height90;

    }
}
