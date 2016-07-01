package com.youtu.acb.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
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

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 消息详情
 */
public class MsgDetailActivity extends BaseActivity {

    private Titlebar mTitleBar;
    private TextView mTitle;
    private TextView mTime;
    private TextView mContent;
    private Context mSelf = MsgDetailActivity.this;
    private int mId;
    private int mType;
    private boolean mReadSuccess = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_msg_detail);

        mTitleBar = (Titlebar) findViewById(R.id.msg_detail_titlebar);
        mTitleBar.getLayoutParams().height = Settings.TITLEBAR_HEIGHT;
        mTitleBar.setTitle("消息");
        mTitleBar.getmLeftPart().setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                onBackPressed();
            }
        });


        Bundle bundle = getIntent().getExtras();
        mId = bundle.getInt("id", 0);
        mType = bundle.getInt("type", 0);

        mTime = (TextView) findViewById(R.id.msg_detail_time);
        mTitle = (TextView) findViewById(R.id.msg_detail_title);
        mContent = (TextView) findViewById(R.id.msg_detail_content);

        getNews();
    }


    private String errMsg;
    private String title, time, content;

    private void getNews() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(Settings.BASE_URL + "thenews/" + mId + "?id=" + mId + "&type=" + mType)
                        .addHeader("CLIENT", "android")
                        .addHeader("TOKEN", CommonUtil.getMobileUniqueId(mSelf))
                        .addHeader("authorization", DaoUtil.getAuthorization(mSelf))
                        .get()
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    JSONObject result = JSON.parseObject(response.body().string());

                    if (result.getIntValue("code") == 0) {
                        title = result.getString("title");
                        time = result.getString("add_time");
                        content = result.getString("content");

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mTitle.setText(title == null ? "" : title);
                                mTime.setText(time == null ? "" : time);
                                mContent.setText(content == null ? "" : content);

                                mReadSuccess = true;
                            }
                        });

                    } else {
                        errMsg = result.getString("msg");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mSelf, errMsg, Toast.LENGTH_SHORT).show();
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
    public void onBackPressed() {
        if (mReadSuccess == true) {
            setResult(10002);
        } else {
            setResult(10003);
        }
        super.onBackPressed();
    }
}
