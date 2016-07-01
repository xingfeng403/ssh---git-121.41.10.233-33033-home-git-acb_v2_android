package com.youtu.acb.activity;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.youtu.acb.R;
import com.youtu.acb.common.Common;
import com.youtu.acb.common.Settings;
import com.youtu.acb.util.DirectListener;
import com.youtu.acb.util.OkHttpUtils;
import com.youtu.acb.util.OnSingleClickListener;
import com.youtu.acb.util.ToastUtil;

/**
 * 绑定微信支付
 */
public class WxPayActivity extends BaseActivity {
    private RelativeLayout mTitleBar;
    private FrameLayout mBack;
    private TextView mCommon;
    private Context mSelf = WxPayActivity.this;
    private TextView mDesc;
    private Button mGotoBind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wx_pay);

        mTitleBar = (RelativeLayout) findViewById(R.id.wx_pay_titlebar);
        mTitleBar.getLayoutParams().height = Settings.TITLEBAR_HEIGHT;
        mBack = (FrameLayout) findViewById(R.id.wx_pay_back);
        mDesc = (TextView) findViewById(R.id.wx_pay_desc);
        mGotoBind = (Button) findViewById(R.id.goto_bind_now);

        mBack.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                onBackPressed();
            }
        });

        mCommon = (TextView) findViewById(R.id.wx_pay_common_ques);
        mCommon.setOnClickListener(new DirectListener(WxPayActivity.this, CommonQuesActivity.class));

        mGotoBind.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                Intent intent = new Intent();
                ComponentName cmp = new ComponentName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI");
                intent.setAction(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setComponent(cmp);
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    ToastUtil.show(WxPayActivity.this, "尚未安装微信客户端，请安装后再试");
                }
            }
        });

        mDesc.setText(getSharedPreferences(Common.configName, MODE_PRIVATE).getString("wechatMsg", ""));
        getWechatMsg();
    }


    String data;

    private void getWechatMsg() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String result = OkHttpUtils.get("wechatMsg", mSelf);


                try {
                    JSONObject jsonObject = (JSONObject) JSON.parse(result);

                    if (jsonObject.getIntValue("code") == 0) {
                        SharedPreferences sp = mSelf.getSharedPreferences(Common.configName, MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        data = jsonObject.getString("data");
                        editor.putString("wechatMsg", data).commit();


                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mDesc.setText(data == null ? "" : data);
                            }
                        });

                    } else {
                    }

                } catch (JSONException e) {
                }
            }
        }).start();
    }
}
