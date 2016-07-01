package com.youtu.acb.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.youtu.acb.R;
import com.youtu.acb.Views.InputControlView;
import com.youtu.acb.Views.Titlebar;
import com.youtu.acb.common.Settings;
import com.youtu.acb.util.OkHttpCallback;
import com.youtu.acb.util.OnSingleClickListener;
import com.youtu.acb.util.ToastUtil;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * 验证码
 */
public class CheckCodeActivity extends BaseActivity {
    private Titlebar mTitleBar;
    private TextView mPhoneNum;
    private InputControlView mInput;
    private Button mGetCode;
    private Button mNextStep;
    private String mPhoneStr;
    private String mCodeStr;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_code);

        mTitleBar = (Titlebar) findViewById(R.id.check_code_titlebar);
        mTitleBar.getmLeftPart().setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                onBackPressed();
            }
        });
        mTitleBar.setTitle("输入验证码");
        mTitleBar.getLayoutParams().height = Settings.TITLEBAR_HEIGHT;

        mPhoneNum = (TextView) findViewById(R.id.check_code_phone);

        mPhoneStr = getIntent().getStringExtra("phone").replaceAll(" ", "");
        mPhoneNum.setText(mPhoneStr == null ? "" : mPhoneStr);

        mInput = (InputControlView) findViewById(R.id.input_code_input_code);
        mGetCode = (Button) findViewById(R.id.check_code_getcode);
        mNextStep = (Button) findViewById(R.id.check_code_nextstep);
        mNextStep.setEnabled(false);


        requireCodeRequest();

        mInput.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() >= 6) {
                    mNextStep.setEnabled(true);
                } else {
                    mNextStep.setEnabled(false);
                }
            }
        });

        mNextStep.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {

                mCodeStr = mInput.getEditText().getText().toString();
                verifyCode();
            }
        });

        mGetCode.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                requireCodeRequest();
            }
        });

    }

    String errmsg;
    private void verifyCode() {

        FormBody formBody = new FormBody.Builder()
                .add("type", "regcode")
                .add("phone", mPhoneStr)
                .add("code", mCodeStr)
                .build();

        Request request = new Request.Builder()
                .url(Settings.BASE_URL + "verify")
                .post(formBody)
                .addHeader("ACCEPT", "*/*")
                .addHeader("CLIENT", "android")
                .build();

        new OkHttpClient().newCall(request).enqueue(new OkHttpCallback(CheckCodeActivity.this) {
            @Override
            protected void onSuccess(JSONObject result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(CheckCodeActivity.this, SetPwdActivity.class).putExtra("phone", mPhoneStr).putExtra("code", mCodeStr));
//                        CheckCodeActivity.this.finish();
                    }
                });
            }

            @Override
            protected void onFinish() {

            }

            @Override
            protected void onError(JSONObject result) {
                try {
                    errmsg = result.getString("msg");
                    if (!TextUtils.isEmpty(errmsg))
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.show(CheckCodeActivity.this, errmsg);
                            }
                        });

                } catch (JSONException e) {
                }


            }
        });
    }


    /**
     * 获取验证码
     */
    private int count;
    Thread timeTh;
    private void requireCodeRequest() {
        count = 60; // init
        mGetCode.setClickable(false);
        FormBody formBody = new FormBody.Builder()
                .add("type", "reg")
                .add("phone", mPhoneStr)
                .build();

        Request request = new Request.Builder()
                .url(Settings.BASE_URL + "code")
                .post(formBody)
                .addHeader("ACCEPT", "*/*")
                .addHeader("CLIENT", "android")
                .build();

        new OkHttpClient().newCall(request).enqueue(new OkHttpCallback(CheckCodeActivity.this) {
            @Override
            protected void onError(JSONObject result) {
            }

            @Override
            protected void onSuccess(JSONObject result) {
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

        startTimeTh();
    }

    private void startTimeTh() {

        // 回调处  start a new thread count 60 seconds
        timeTh = new Thread(new Runnable() {

            @Override
            public void run() {

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
                        mGetCode.setEnabled(true);
                    }
                });
            }
        });

        timeTh.start();
    }

    private void stopTimeTh() {
        timeTh.interrupt();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timeTh != null) {
            timeTh = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        stopTimeTh();
    }

    @Override
    protected void onResume() {
        super.onResume();

        startTimeTh();
    }
}
