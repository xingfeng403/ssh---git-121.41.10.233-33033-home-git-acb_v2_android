package com.youtu.acb.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.youtu.acb.util.OkHttpUtils;
import com.youtu.acb.util.OnSingleClickListener;
import com.youtu.acb.util.ToastUtil;

import java.util.HashMap;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * 设置密码
 */
public class SetPwdActivity extends BaseActivity {

    private Titlebar mTitleBar;
    private InputControlView mCodeOnce;
    private InputControlView mCodeTwice;
    private Button mNextStep;
    private String mPhoneNum = "";
    private String mCode = "";
    private String mPwd = "";
    private boolean isFindPwd = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_pwd);

        mTitleBar = (Titlebar) findViewById(R.id.set_pwd_titlebar);
        mTitleBar.getLayoutParams().height = Settings.TITLEBAR_HEIGHT;
        mTitleBar.setTitle(getString(R.string.set_pwd_title));

        mTitleBar.getmLeftPart().setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                onBackPressed();
            }
        });

        mPhoneNum = getIntent().getStringExtra("phone");
        mCode = getIntent().getStringExtra("code");
        isFindPwd = getIntent().getBooleanExtra("findpwd", false);

        mCodeOnce = (InputControlView) findViewById(R.id.set_pwd_input_phonenum);
        mCodeTwice = (InputControlView) findViewById(R.id.set_pwd_input_code);
        mNextStep = (Button) findViewById(R.id.set_pwd_btn);

        int height90 = (int) (Settings.RATIO_HEIGHT * 90);
        mCodeOnce.getLayoutParams().height = height90;
        mCodeTwice.getLayoutParams().height = height90;
        mNextStep.getLayoutParams().height = (int) (Settings.RATIO_HEIGHT * 80);
        mNextStep.setEnabled(false);

        mCodeOnce.getEditText().setHint(getString(R.string.hint_pwd_once));
        mCodeTwice.getEditText().setHint(getString(R.string.hint_pwd_twice));

        mCodeOnce.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                checkBtnState();
            }
        });
        mCodeTwice.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                checkBtnState();
            }
        });

        mNextStep.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                String pwd1 = mCodeOnce.getEditText().getText().toString();
                String pwd2 = mCodeTwice.getEditText().getText().toString();
                if (pwd1.length() >= 6 && pwd1.length() <= 20) {

                    if (pwd2.length() >= 6 && pwd2.length() <= 20) {

                        if (pwd1.equals(pwd2)) {
                            mPwd = mCodeTwice.getEditText().getText().toString();
                            if (isFindPwd) {
                                modiLoginPwd();
                            } else {
                                doRegiste();
                            }

                        } else {
                            ToastUtil.show(SetPwdActivity.this, "两次输入密码不一致");
                        }

                    } else {
                        ToastUtil.show(SetPwdActivity.this, "密码为6-20位");
                    }

                } else {
                    ToastUtil.show(SetPwdActivity.this, "密码为6-20位");
                }

            }
        });

    }

    /**
     * 检查按钮可点击状态
     */
    private void checkBtnState() {
        if (mCodeOnce.getEditText().getText().length() > 0 && mCodeTwice.getEditText().getText().length() > 0) {
            mNextStep.setEnabled(true);
        } else {
            mNextStep.setEnabled(false);
        }
    }

    String msg; // 返回信息

    /**
     * 注册
     */
    private void doRegiste() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                FormBody body = new FormBody.Builder()
                        .add("pwd", mPwd)
                        .add("channel", CommonUtil.getChannelCode(SetPwdActivity.this))
                        .add("phone", mPhoneNum)
                        .add("code", mCode)
                        .add("invite_id", "")
                        .build();

                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(Settings.USER_URL)
                        .addHeader("CLIENT", "android")
                        .addHeader("TOKEN", CommonUtil.getMobileUniqueId(SetPwdActivity.this))
                        .addHeader("authorization", DaoUtil.getAuthorization(SetPwdActivity.this))
                        .post(body)
                        .build();

                try {
                    Response response = client.newCall(request).execute();
                    JSONObject resultObj = JSON.parseObject(response.body().string());

                    if (resultObj.getIntValue("code") == 0) {
//                        int token_id = resultObj.getIntValue("token_id");
//                        String token = resultObj.getString("token");

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(SetPwdActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(SetPwdActivity.this, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                SetPwdActivity.this.finish();
                            }
                        });
                    } else {
                        msg = resultObj.getString("msg");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(SetPwdActivity.this, msg, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }




            }
        }).start();
    }

    /**
     * 修改登录密码
     */
    private void modiLoginPwd() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                FormBody body = new FormBody.Builder()
                        .add("type", "pwd")
                        .add("pwd", mPwd)
                        .add("repwd", mPwd)
                        .add("phone", mPhoneNum)
                        .add("code", mCode)
                        .build();


                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(Settings.USER_URL).addHeader("CLIENT", "android")
                        .addHeader("TOKEN", CommonUtil.getMobileUniqueId(SetPwdActivity.this))
                        .addHeader("authorization", DaoUtil.getAuthorization(SetPwdActivity.this))
                        .put(body)
                        .build();

                try {
                    Response response = client.newCall(request).execute();
                    JSONObject resultObj = JSON.parseObject(response.body().string());


                    if (resultObj.getIntValue("code") == 0) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(SetPwdActivity.this, "修改密码成功", Toast.LENGTH_SHORT).show();
                                SetPwdActivity.this.finish();
                            }
                        });
                    } else {
                        msg = resultObj.getString("msg");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(SetPwdActivity.this, msg, Toast.LENGTH_SHORT).show();
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
