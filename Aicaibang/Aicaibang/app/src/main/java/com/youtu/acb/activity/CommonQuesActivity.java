package com.youtu.acb.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.youtu.acb.R;
import com.youtu.acb.common.Settings;
import com.youtu.acb.util.OnSingleClickListener;

public class CommonQuesActivity extends BaseActivity {

    private RelativeLayout mTitleBar;
    private FrameLayout mBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_ques);

        mTitleBar = (RelativeLayout) findViewById(R.id.common_ques_titlebar);
        mTitleBar.getLayoutParams().height = Settings.TITLEBAR_HEIGHT;
        mBack = (FrameLayout) findViewById(R.id.common_ques_back);

        mBack.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                onBackPressed();
            }
        });
    }
}
