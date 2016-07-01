package com.youtu.acb.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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
 * 平台举报
 */
public class PlatReportActivity extends BaseActivity {
    private Titlebar mTitleBar;
    private TextView mName;
    private EditText mContent;
    private Button mSubmit;
    private Context mSelf = PlatReportActivity.this;
    private boolean hasName; // 是否选择或输入平台名称

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plat_report);

        mTitleBar = (Titlebar) findViewById(R.id.palt_rep_titlebar);
        mTitleBar.getLayoutParams().height = Settings.TITLEBAR_HEIGHT;
        mTitleBar.setTitle("平台举报");
        mTitleBar.getmLeftPart().setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                onBackPressed();
            }
        });

        mContent = (EditText) findViewById(R.id.plat_rep_content);
        mName = (TextView) findViewById(R.id.plat_rep_name);
        mSubmit = (Button) findViewById(R.id.plat_rep_btn);

        String pName = getIntent().getStringExtra("name");
        if (!TextUtils.isEmpty(pName)) {
            mName.setText(pName);
            mName.setTextColor(Color.parseColor("#333333"));
            hasName = true;

            checkBtnState();
        }


        mSubmit.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                if (!hasName) {
                    Toast.makeText(mSelf, "请输入平台名称", Toast.LENGTH_SHORT).show();
                    return;
                } else if (mContent.getText().length() == 0) {
                    Toast.makeText(mSelf, "请输入举报内容", Toast.LENGTH_SHORT).show();
                    return;
                }
                doSubmit();
            }
        });


        mName.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                startActivityForResult(new Intent(PlatReportActivity.this, PlatSelActivity.class), 100001);
            }
        });

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
    }

    private String msg;

    private void doSubmit() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();

                FormBody.Builder builder = new FormBody.Builder();
                builder.add("name", mName.getText().toString());
                builder.add("content", mContent.getText().toString());
                Request request = new Request.Builder()
                        .url(Settings.BASE_URL + "report")
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
                                mName.setText("");
                                mContent.setText("");
                                Toast.makeText(mSelf, "举报成功，等待处理", Toast.LENGTH_SHORT).show();
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

        checkBtnState();
    }

    private void checkBtnState() {
        if (hasName && mContent.getText().length() > 0) {
            mSubmit.setEnabled(true);
        } else {
            mSubmit.setEnabled(false);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == 10004 && data != null) {
            // 返回选中平台名
            String name = data.getStringExtra("name");
            if (!TextUtils.isEmpty(name)) {
                mName.setText(name);
                mName.setTextColor(Color.parseColor("#333333"));
                hasName = true;

                checkBtnState();
            } else {
                hasName = false;
            }
        }

    }
}
