package com.youtu.acb.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.youtu.acb.R;
import com.youtu.acb.Views.InputControlView;
import com.youtu.acb.Views.Titlebar;
import com.youtu.acb.common.Settings;
import com.youtu.acb.util.CommonUtil;
import com.youtu.acb.util.OkHttpCallback;
import com.youtu.acb.util.OnSingleClickListener;
import com.youtu.acb.util.ToastUtil;

import org.json.JSONObject;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * 找回密码
 */
public class FindPwdActivity extends BaseActivity {

    private Titlebar mTitleBar;
    private InputControlView mPhoneNum;
    private InputControlView mCheckCode;
    private Button mNextStep;
    private Button mGetCode;
    private String mPhoneStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_pwd);

        mTitleBar = (Titlebar) findViewById(R.id.find_pwd_titlebar);
        mTitleBar.getLayoutParams().height = Settings.TITLEBAR_HEIGHT;
        mTitleBar.setTitle(getString(R.string.find_pwd_title));
        mTitleBar.getmLeftPart().setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                onBackPressed();
            }
        });

        mPhoneNum = (InputControlView) findViewById(R.id.find_pwd_input_phonenum);
        mCheckCode = (InputControlView) findViewById(R.id.find_pwd_input_code);

        int height90 = (int) (Settings.RATIO_HEIGHT * 90);
        mPhoneNum.getLayoutParams().height = height90;
        mCheckCode.getLayoutParams().height = height90;
        mNextStep = (Button) findViewById(R.id.find_pwd_btn);
        mNextStep.getLayoutParams().height = (int) (Settings.RATIO_HEIGHT * 80);
        mNextStep.setEnabled(false);

        mPhoneNum.getEditText().setHint(getString(R.string.hint_phone_num));
        mCheckCode.getEditText().setHint(getString(R.string.hint_check_code));
        mGetCode = (Button) findViewById(R.id.find_pwd_get_code);

        mPhoneNum.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int count) {
                if (count == 1) {
                    int length = s.toString().length();
                    if (length == 3 || length == 8) {
                        mPhoneNum.getEditText().setText(s + " ");
                        mPhoneNum.getEditText().setSelection(mPhoneNum.getEditText().getText().toString().length());
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                checkBtnState();
            }
        });

        mCheckCode.getEditText().addTextChangedListener(new TextWatcher() {
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

        mGetCode.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                mPhoneStr = mPhoneNum.getEditText().getText().toString().replaceAll(" ", "");
                if (!CommonUtil.isMobileNO(mPhoneStr)) {
                    ToastUtil.show(FindPwdActivity.this, "请输入正确的手机号");
                    return;
                }

                if (mPhoneStr.length() == 11) {
                    requireCodeRequest();
                } else {
                    ToastUtil.show(FindPwdActivity.this, "请输入完整的手机号");
                }
            }
        });

        mNextStep.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                verifyCode();
            }
        });

    }

    private void checkBtnState() {
        if (mPhoneNum.getEditText().getText().toString().replaceAll(" ", "").length() == 11 && mCheckCode.getEditText().getText().length() >= 6) {
            mNextStep.setEnabled(true);
        } else {
            mNextStep.setEnabled(false);
        }
    }

    /**
     * 获取验证码
     */
    private void requireCodeRequest() {
        mGetCode.setClickable(false);
        FormBody formBody = new FormBody.Builder()
                .add("type", "pwd")
                .add("phone", mPhoneStr)
                .build();

        Request request = new Request.Builder()
                .url(Settings.BASE_URL + "code")
                .post(formBody)
                .addHeader("ACCEPT", "*/*")
                .addHeader("CLIENT", "android")
                .build();

        new OkHttpClient().newCall(request).enqueue(new OkHttpCallback(FindPwdActivity.this) {
            @Override
            protected void onError(JSONObject result) {

            }

            @Override
            protected void onSuccess(org.json.JSONObject result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            }

            @Override
            protected void onFinish() {

            }
        });

        // 回调处  start a new thread count 60 seconds
        new Thread(new Runnable() {
            int count;

            @Override
            public void run() {
                count = 60; // init

                while (count > 0) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // 归0
                    }

                    count--;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mGetCode.setText("获取" + count + "s");
                        }
                    });

                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mGetCode.setText("重新获取");
                        mGetCode.setClickable(true);
                    }
                });
            }
        }).start();
    }

    private String mCodeStr;
    private String errMsg;

    private void verifyCode() {
        if (mPhoneStr == null) {
            mPhoneStr = mPhoneNum.getEditText().getText().toString();
        }
        mCodeStr = mCheckCode.getEditText().getText().toString();
        FormBody formBody = new FormBody.Builder()
                .add("type", "pwdcode")
                .add("phone", mPhoneStr)
                .add("code", mCodeStr)
                .build();

        Request request = new Request.Builder()
                .url(Settings.BASE_URL + "verify")
                .post(formBody)
                .addHeader("ACCEPT", "*/*")
                .addHeader("CLIENT", "android")
                .build();

        new OkHttpClient().newCall(request).enqueue(new OkHttpCallback(FindPwdActivity.this) {
            @Override
            protected void onError(JSONObject result) {
                try {
                    errMsg = result.getString("msg");
                    if (!TextUtils.isEmpty(errMsg))
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.show(FindPwdActivity.this, errMsg);
                            }
                        });
                } catch (Exception e) {
                }

            }

            @Override
            protected void onSuccess(JSONObject result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(FindPwdActivity.this, SetPwdActivity.class).putExtra("phone", mPhoneStr).putExtra("code", mCodeStr).putExtra("findpwd", true));
                        FindPwdActivity.this.finish();
                    }
                });
            }

            @Override
            protected void onFinish() {

            }
        });
    }
}
