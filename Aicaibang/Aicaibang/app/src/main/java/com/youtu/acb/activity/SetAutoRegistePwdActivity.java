package com.youtu.acb.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.youtu.acb.R;
import com.youtu.acb.Views.InputControlView;
import com.youtu.acb.Views.Titlebar;
import com.youtu.acb.common.Settings;
import com.youtu.acb.util.CommonUtil;
import com.youtu.acb.util.DaoUtil;
import com.youtu.acb.util.OnSingleClickListener;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 自动注册密码
 */
public class SetAutoRegistePwdActivity extends BaseActivity {

    private Titlebar mTitleBar;
    private InputControlView mCodeOnce;
    private InputControlView mCodeTwice;
    private Button mNextStep;
    private Context mSelf = SetAutoRegistePwdActivity.this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_auto_registe_pwd);

        mTitleBar = (Titlebar) findViewById(R.id.set_apwd_titlebar);
        mTitleBar.getLayoutParams().height = Settings.TITLEBAR_HEIGHT;
        mTitleBar.setTitle(getString(R.string.set_auto_pwd_title));

        mCodeOnce = (InputControlView) findViewById(R.id.set_apwd_input_once);
        mCodeTwice = (InputControlView) findViewById(R.id.set_apwd_input_twice);
        mNextStep = (Button) findViewById(R.id.set_apwd_btn);

        int height90 = (int) (Settings.RATIO_HEIGHT * 90);
        mCodeOnce.getLayoutParams().height = height90;
        mCodeTwice.getLayoutParams().height = height90;
        mNextStep.getLayoutParams().height = (int) (Settings.RATIO_HEIGHT * 80);

        mCodeOnce.getEditText().setHint(getString(R.string.hint_pwd));
        mCodeTwice.getEditText().setHint(getString(R.string.hint_confirm_pwd1));

        mNextStep.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                if (checkPwds()) {
                    doSubmit();
                } else {
                    Toast.makeText(mSelf, "输入密码不一致!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private boolean checkPwds() {
        String pwdUp = mCodeOnce.getEditText().getText().toString();
        String pwdDown = mCodeTwice.getEditText().getText().toString();

        if (pwdUp.equals(pwdDown)) {
            return true;
        }

        return false;
    }

    String msg;

    private void doSubmit() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();

                FormBody.Builder builder = new FormBody.Builder();
                builder.add("pwd", mCodeOnce.getEditText().getText().toString());
                Request request = new Request.Builder()
                        .url(Settings.BASE_URL + "platformpwd")
                        .addHeader("authorization", DaoUtil.getAuthorization(mSelf))
                        .addHeader("CLIENT", "android")
                        .addHeader("TOKEN", CommonUtil.getMobileUniqueId(mSelf))
                        .post(builder.build())
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    JSONObject resultObj = JSON.parseObject(response.body().string());
                    if (resultObj.getIntValue("code") == 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mSelf, "设置自动注册密码成功", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        msg = resultObj.getString("msg");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mSelf, msg, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        }).start();
    }
}
