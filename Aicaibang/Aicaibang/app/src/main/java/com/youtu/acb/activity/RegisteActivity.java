package com.youtu.acb.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.youtu.acb.R;
import com.youtu.acb.Views.InputControlView;
import com.youtu.acb.common.Settings;
import com.youtu.acb.util.CommonUtil;
import com.youtu.acb.util.CreateCode;
import com.youtu.acb.util.DaoUtil;
import com.youtu.acb.util.DirectListener;
import com.youtu.acb.util.OnSingleClickListener;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 注册
 */
public class RegisteActivity extends BaseActivity {

    private RelativeLayout mTitleBar;
    private InputControlView mPhoneNum;
    private InputControlView mCheckCode;
    private Button mNextStep;
    private ImageView mImgCode;
    private FrameLayout mLogin;
    private FrameLayout mBack;
    private TextView mProtocol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registe);

        mTitleBar = (RelativeLayout) findViewById(R.id.registe_titlebar);
        mTitleBar.getLayoutParams().height = Settings.TITLEBAR_HEIGHT;
        mBack = (FrameLayout) findViewById(R.id.registe_back);
        mLogin = (FrameLayout) findViewById(R.id.registe_login);

        mBack.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                onBackPressed();
            }
        });

        mLogin.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                onBackPressed();
            }
        });

        mPhoneNum = (InputControlView) findViewById(R.id.registe_input_phonenum);
        mCheckCode = (InputControlView) findViewById(R.id.registe_input_code);
        mNextStep = (Button) findViewById(R.id.registe_btn);

        int height90 = (int) (Settings.RATIO_HEIGHT * 90);
        mPhoneNum.getLayoutParams().height = height90;
        mCheckCode.getLayoutParams().height = height90;
        mNextStep.getLayoutParams().height = (int) (Settings.RATIO_HEIGHT * 80);

        mPhoneNum.getEditText().setHint(getString(R.string.hint_phonenum));
        mCheckCode.getEditText().setHint(getString(R.string.hint_img_code));

        mProtocol = (TextView) findViewById(R.id.registe_protocol);
        String str = "点击下一步,即代表你已阅读并同意《爱财帮注册协议》";
        int length = str.length();
        SpannableString ss = new SpannableString(str);
        ss.setSpan(new ForegroundColorSpan(Color.parseColor("#7790e3")), length - 9, length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        mProtocol.setText(ss);

        mProtocol.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                startActivity(new Intent(RegisteActivity.this, WebActivity.class).putExtra("title", "理财协议"));
            }
        });

        mImgCode = (ImageView) findViewById(R.id.registe_img_code);


        mNextStep.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                if (mCheckCode.getEditText().getText().toString().equalsIgnoreCase(CreateCode.code)) {
                    startActivity(new Intent(RegisteActivity.this, CheckCodeActivity.class).putExtra("phone", mPhoneNum.getEditText().getText().toString()));
                } else {
                    Toast.makeText(RegisteActivity.this, "验证码错误", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mImgCode.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                mImgCode.setImageBitmap(CreateCode.createRandomBitmap());
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
                if (editable.length() == 13) {
                    regPhone = editable.toString().replaceAll(" ", "");
                    checkPhoneNum();
                }
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

    }

    String regPhone;

    private void checkPhoneNum() {
        if (CommonUtil.isMobileNO(regPhone)) {
            isRegiste();
        } else {
            mPhoneNum.getEditText().setText("");
        }
    }

    private void checkBtnState() {
        if (mPhoneNum.getEditText().getText().toString().replaceAll(" ", "").length() == 11 && mCheckCode.getEditText().getText().length() == 4) {
            mNextStep.setEnabled(true);
        } else {
            mNextStep.setEnabled(false);
        }
    }

    String errMsg;

    private void isRegiste() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(Settings.BASE_URL + "isregister?phone=" + regPhone).addHeader("CLIENT", "android")
                        .addHeader("TOKEN", CommonUtil.getMobileUniqueId(RegisteActivity.this))
                        .addHeader("authorization", DaoUtil.getAuthorization(RegisteActivity.this))
                        .get()
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    JSONObject result = JSON.parseObject(response.body().string());

                    if (result.getIntValue("code") == 10002) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(RegisteActivity.this, "该号码已被注册", Toast.LENGTH_SHORT).show();
                                mPhoneNum.getEditText().setText("");
                            }
                        });
                    } else {
                        errMsg = result.getString("msg");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!TextUtils.isEmpty(errMsg))
                                    Toast.makeText(RegisteActivity.this, errMsg, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mImgCode.setImageBitmap(CreateCode.createRandomBitmap());
    }
}
