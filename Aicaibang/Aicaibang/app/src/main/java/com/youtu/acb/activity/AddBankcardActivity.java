package com.youtu.acb.activity;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.youtu.acb.R;
import com.youtu.acb.common.Settings;
import com.youtu.acb.util.CommonUtil;
import com.youtu.acb.util.DaoUtil;
import com.youtu.acb.util.OnSingleClickListener;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 添加银行卡
 */
public class AddBankcardActivity extends BaseActivity {

    private RelativeLayout mTitleBar;
    private FrameLayout mBack;
    private Button mNextStep;
    private EditText mName;
    private EditText mNumber;
    private Context mSelf = AddBankcardActivity.this;
    private String mNameStr;
    private String mNumberStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_bankcard);

        mTitleBar = (RelativeLayout) findViewById(R.id.add_card_titlebar);
        mTitleBar.getLayoutParams().height = Settings.TITLEBAR_HEIGHT;
        mBack = (FrameLayout) findViewById(R.id.add_card_back);

        mBack.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                onBackPressed();
            }
        });

        mNextStep = (Button) findViewById(R.id.add_card_next);
        mName = (EditText) findViewById(R.id.add_card_name);
        mNumber = (EditText) findViewById(R.id.add_card_number);

        mNextStep.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                mNameStr = mName.getText().toString();
                mNumberStr = mNumber.getText().toString();

                doSubmit();
            }
        });

        mName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (mName.getText().length() > 0 && mNumber.getText().length() > 0) {
                    mNextStep.setEnabled(true);
                } else {
                    mNextStep.setEnabled(false);
                }
            }
        });

        mNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (mName.getText().length() > 0 && mNumber.getText().length() > 0) {
                    mNextStep.setEnabled(true);
                } else {
                    mNextStep.setEnabled(false);
                }
            }
        });

    }

    String msg;
    private void doSubmit(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();

                FormBody body = new FormBody.Builder()
                        .add("account", mNumberStr)
                        .build();

                Request request = new Request.Builder()
                        .url(Settings.BASE_URL + "bankcard")
                        .addHeader("CLIENT", "android").addHeader("TOKEN", CommonUtil.getMobileUniqueId(mSelf))
                        .addHeader("authorization", DaoUtil.getAuthorization(mSelf))
                        .post(body)
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    JSONObject resultObj = JSON.parseObject(response.body().string());
                    if (resultObj.getIntValue("code") == 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mSelf, "添加成功", Toast.LENGTH_SHORT).show();
                                AddBankcardActivity.this.finish();
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
