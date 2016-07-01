package com.youtu.acb.activity;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.youtu.acb.R;
import com.youtu.acb.Views.InputControlView;
import com.youtu.acb.Views.Titlebar;
import com.youtu.acb.common.Settings;
import com.youtu.acb.entity.UserInfo;
import com.youtu.acb.util.DaoUtil;
import com.youtu.acb.util.DirectListener;
import com.youtu.acb.util.OkHttpUtils;
import com.youtu.acb.util.OnSingleClickListener;

import java.util.HashMap;

/**
 *  登录
 */
public class LoginActivity extends BaseActivity {

    private TextView mForgetPwd;
    private Titlebar mTitleBar;
    private InputControlView mPhoneNum;
    private InputControlView mPwd;
    private Button mNextStep;
    private Context mSelf = LoginActivity.this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mTitleBar = (Titlebar) findViewById(R.id.login_titlebar);
        mTitleBar.getLayoutParams().height = Settings.TITLEBAR_HEIGHT;
        mTitleBar.setTitle(getString(R.string.login_title));
        mTitleBar.getmRightTv().setText(getString(R.string.registe));
        mTitleBar.getmRightPart().setVisibility(View.VISIBLE);
        mTitleBar.getmLeftPart().setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                onBackPressed();
            }
        });

        int height90 = (int) (Settings.RATIO_HEIGHT * 90);
        mPhoneNum = (InputControlView) findViewById(R.id.login_input_phonenum);
        mPwd = (InputControlView) findViewById(R.id.login_input_pwd);
        mPhoneNum.getLayoutParams().height = height90;
        mPwd.getLayoutParams().height = height90;

        mForgetPwd = (TextView) findViewById(R.id.login_forget_pwd);
        mNextStep = (Button) findViewById(R.id.login_btn);
        mNextStep.getLayoutParams().height = (int) (Settings.RATIO_HEIGHT * 80);

        mTitleBar.getmRightPart().setOnClickListener(new DirectListener(LoginActivity.this, RegisteActivity.class));
        mForgetPwd.setOnClickListener(new DirectListener(LoginActivity.this, FindPwdActivity.class));

        mPhoneNum.getEditText().setHint(getString(R.string.hint_phonenum));
        mPwd.getEditText().setHint(getString(R.string.hint_pwd));

        mNextStep.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                doLogin();
            }
        });

        mPhoneNum.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int count) {
                if (count == 1){
                    int length = s.toString().length();
                    if (length == 3 || length == 8){
                        mPhoneNum.getEditText().setText(s + " ");
                        mPhoneNum.getEditText().setSelection(mPhoneNum.getEditText().getText().toString().length());
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private JSONObject resultObj;
    private void doLogin() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HashMap params = new HashMap<>();
                params.put("phone", mPhoneNum.getEditText().getText().toString().replaceAll(" ", ""));
                params.put("pwd", mPwd.getEditText().getText().toString());
                String result = OkHttpUtils.post("login", params, mSelf);

                resultObj = JSON.parseObject(result);
                if (resultObj.getIntValue("code") == 0) {
                    String token_id = resultObj.getString("token_id");
                    String token = resultObj.getString("token");
                    DaoUtil.saveLoginedInfo(mSelf, token_id + "", token);
                    UserInfo info = JSON.toJavaObject(resultObj.getJSONObject("userinfo"), UserInfo.class);
                    DaoUtil.saveUserInfo(JSON.toJSONString(info), mSelf);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mSelf, "登录成功", Toast.LENGTH_SHORT).show();
                            finishActivity();
                        }
                    });

                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mSelf, resultObj.getString("msg"), Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }
        }).start();
    }

    private void finishActivity() {
        LoginActivity.this.finish();
    }

}
