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
import com.youtu.acb.R;
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
 * 反馈
 */
public class FeedbackActivity extends BaseActivity {
    private Titlebar mTitleBar;
    private EditText mContent;
    private Button mSubmit;
    private Context mSelf = FeedbackActivity.this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        mTitleBar = (Titlebar) findViewById(R.id.feedback_titlebar);
        mTitleBar.getLayoutParams().height = Settings.TITLEBAR_HEIGHT;
        mTitleBar.setTitle("反馈建议");
        mTitleBar.getmLeftPart().setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                onBackPressed();
            }
        });

        mContent = (EditText) findViewById(R.id.feedback_content);
        mSubmit = (Button) findViewById(R.id.feedback_btn);
        mSubmit.setEnabled(false);

        mContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() >= 6) {
                    mSubmit.setEnabled(true);
                } else {
                    mSubmit.setEnabled(false);
                }

            }
        });

        mSubmit.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                doSubmit();
            }
        });
    }

    private String msg;

    private void doSubmit() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();

                FormBody.Builder builder = new FormBody.Builder();
                builder.add("content", mContent.getText().toString());
                Request request = new Request.Builder()
                        .url(Settings.BASE_URL + "advice")
                        .addHeader("CLIENT", "android").addHeader("TOKEN", CommonUtil.getMobileUniqueId(mSelf))
                        .addHeader("authorization", DaoUtil.getAuthorization(mSelf))
                        .post(builder.build())
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    JSONObject resultObj = JSON.parseObject(response.body().string());
                    if (resultObj.getIntValue("code") == 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mContent.setText("");
                                Toast.makeText(mSelf, "反馈成功，等待处理", Toast.LENGTH_SHORT).show();
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
