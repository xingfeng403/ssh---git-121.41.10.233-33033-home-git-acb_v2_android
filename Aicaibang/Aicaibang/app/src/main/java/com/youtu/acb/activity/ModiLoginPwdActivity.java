package com.youtu.acb.activity;

import android.content.Context;
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
import com.youtu.acb.util.OnSingleClickListener;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 修改登录密码
 */
public class ModiLoginPwdActivity extends BaseActivity {

    private Titlebar mTitleBar;
    private InputControlView mCodeOld;
    private InputControlView mCodeOnce;
    private InputControlView mCodeTwice;
    private Button mNextStep;
    private Context mSelf = ModiLoginPwdActivity.this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modi_login_pwd);

        mTitleBar = (Titlebar) findViewById(R.id.modi_lpwd_titlebar);
        mTitleBar.getLayoutParams().height = Settings.TITLEBAR_HEIGHT;
        mTitleBar.setTitle(getString(R.string.modi_lpwd_title));
        mTitleBar.getmLeftPart().setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                ModiLoginPwdActivity.this.finish();
            }
        });

        mCodeOld = (InputControlView) findViewById(R.id.modi_lpwd_input_old);
        mCodeOnce = (InputControlView) findViewById(R.id.modi_lpwd_input_new);
        mCodeTwice = (InputControlView) findViewById(R.id.modi_lpwd_input_again);
        mNextStep = (Button) findViewById(R.id.modi_lpwd_btn);
        mNextStep.setEnabled(false);

        int height90 = (int) (Settings.RATIO_HEIGHT * 90);
        mCodeOld.getLayoutParams().height = height90;
        mCodeOnce.getLayoutParams().height = height90;
        mCodeTwice.getLayoutParams().height = height90;
        mNextStep.getLayoutParams().height = (int) (Settings.RATIO_HEIGHT * 80);

        mCodeOld.getEditText().setHint(getString(R.string.hint_old_pwd));
        mCodeOnce.getEditText().setHint(getString(R.string.hint_new_pwd));
        mCodeTwice.getEditText().setHint(getString(R.string.hint_confirm_pwd));
        
        mNextStep.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                doSubmit();
            }
        });

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

        mCodeOld.getEditText().addTextChangedListener(new TextWatcher() {
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

    }

    private void checkBtnState() {
        if (mCodeOld.getEditText().getText().length() > 0
                && mCodeOnce.getEditText().getText().length() > 0
                && mCodeTwice.getEditText().getText().length() > 0) {
            mNextStep.setEnabled(true);
        } else {
            mNextStep.setEnabled(false);
        }
    }

    String msg;
    private void doSubmit() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();

                FormBody.Builder builder = new FormBody.Builder();
                builder.add("type", "edpwd");
                builder.add("old_pwd", mCodeOld.getEditText().getText().toString());
                builder.add("pwd", mCodeOnce.getEditText().getText().toString());
                builder.add("repwd",mCodeTwice.getEditText().getText().toString());
                Request request = new Request.Builder()
                        .url(Settings.USER_URL)
                        .addHeader("CLIENT", "android").addHeader("TOKEN", CommonUtil.getMobileUniqueId(mSelf))
                        .addHeader("authorization", DaoUtil.getAuthorization(mSelf))
                        .put(builder.build())
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    JSONObject resultObj = JSON.parseObject(response.body().string());
                    if (resultObj.getIntValue("code") == 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mSelf, "修改登录密码成功", Toast.LENGTH_SHORT).show();
                                onBackPressed();
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
