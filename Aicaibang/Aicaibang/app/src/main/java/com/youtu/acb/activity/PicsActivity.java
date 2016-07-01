package com.youtu.acb.activity;

import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.youtu.acb.R;

public class PicsActivity extends BaseActivity {

    private ViewPager mVg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pics);

        mVg = (ViewPager) findViewById(R.id.pics_vg);



    }
}
