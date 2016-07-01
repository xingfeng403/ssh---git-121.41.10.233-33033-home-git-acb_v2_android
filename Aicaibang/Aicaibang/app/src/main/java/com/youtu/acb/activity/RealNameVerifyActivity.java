package com.youtu.acb.activity;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.youtu.acb.AcbApplication;
import com.youtu.acb.R;
import com.youtu.acb.Views.InputControlView;
import com.youtu.acb.Views.Titlebar;
import com.youtu.acb.common.Settings;
import com.youtu.acb.entity.UserInfo;
import com.youtu.acb.interfaces.GetUserInfoFinishedListener;
import com.youtu.acb.util.CommonUtil;
import com.youtu.acb.util.DaoUtil;
import com.youtu.acb.util.OnSingleClickListener;
import com.youtu.acb.util.ToastUtil;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 实名认证
 */
public class RealNameVerifyActivity extends BaseActivity {

    private Titlebar mTitleBar;
    private EditText mName, mIdc;
    private Button mSubmit;
    private Context mSelf = RealNameVerifyActivity.this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_real_name_verify);

        mTitleBar = (Titlebar) findViewById(R.id.real_name_v_titlebar);
        mTitleBar.getLayoutParams().height = Settings.TITLEBAR_HEIGHT;
        mTitleBar.setTitle(getString(R.string.real_name_v_title));

        mTitleBar.getmLeftPart().setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                onBackPressed();
            }
        });

        mName = (EditText) findViewById(R.id.real_name_v_input_name);
        mIdc = (EditText) findViewById(R.id.real_name_v_input_idc);
        mSubmit = (Button) findViewById(R.id.real_name_v_btn);

        int height90 = (int) (Settings.RATIO_HEIGHT * 90);
        mName.getLayoutParams().height = height90;
        mIdc.getLayoutParams().height = height90;
        mSubmit.getLayoutParams().height = (int) (Settings.RATIO_HEIGHT * 80);

        mName.setHint(getString(R.string.hint_name));
        mIdc.setHint(getString(R.string.hint_idc));

        mName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (mIdc.getText().length() > 0 && editable.length() > 0) {
                    mSubmit.setEnabled(true);
                } else {
                    mSubmit.setEnabled(false);
                }
            }
        });

        mIdc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (mIdc.getText().length() > 0 && editable.length() > 0) {
                    mSubmit.setEnabled(true);
                } else {
                    mSubmit.setEnabled(false);
                }
            }
        });

        mSubmit.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                if (mName.getText().length() == 0) {
                    ToastUtil.show(mSelf, "请输入姓名");
                } else if (mIdc.getText().length() == 0) {
                    ToastUtil.show(mSelf, "请输入身份证号");
                } else {
                    mRealNameStr = mName.getText().toString();
                    mIdcardStr = mIdc.getText().toString();
                    doVerify();
                }
            }
        });
    }

    private String mRealNameStr;
    private String mIdcardStr;
    private String msg;

    private void doVerify() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();

                FormBody.Builder builder = new FormBody.Builder();
                builder.add("type", "realname");
                builder.add("realname", mRealNameStr);
                builder.add("idc", mIdcardStr);
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
                                DaoUtil.getUserInfoFromServer(mSelf, new GetUserInfoFinishedListener() {
                                    @Override
                                    public void doFinish(boolean success, String msg) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(mSelf, "实名认证成功", Toast.LENGTH_SHORT).show();
                                                RealNameVerifyActivity.this.finish();
                                            }
                                        });
                                    }
                                });
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

    @Override
    protected void onResume() {
        super.onResume();
        if (mName.getText().length() > 0 && mIdc.getText().length() > 0) {
            mSubmit.setEnabled(true);
        } else {
            mSubmit.setEnabled(false);
        }

    }
}
