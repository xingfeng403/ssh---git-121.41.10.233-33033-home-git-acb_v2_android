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
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.youtu.acb.R;
import com.youtu.acb.common.Settings;
import com.youtu.acb.entity.UserInfo;
import com.youtu.acb.util.CommonUtil;
import com.youtu.acb.util.DaoUtil;
import com.youtu.acb.util.OnSingleClickListener;
import com.youtu.acb.util.ToastUtil;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 金币兑换
 */
public class GoldExchangeActivity extends BaseActivity {

    private RelativeLayout mTitleBar;
    private FrameLayout mBack;
    private EditText mNum;
    private TextView mGoldReady;
    private TextView mExchangeAll;
    private TextView mCanExchange;
    private Button mExchange;
    private int maxNum; // 能兑换的最大金额
    private Context mSelf = GoldExchangeActivity.this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gold_exchange);

        mTitleBar = (RelativeLayout) findViewById(R.id.gold_exchange_titlebar);
        mTitleBar.getLayoutParams().height = Settings.TITLEBAR_HEIGHT;

        mBack = (FrameLayout) findViewById(R.id.gold_exchange_back);

        mBack.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                onBackPressed();
            }
        });

        mNum = (EditText) findViewById(R.id.gold_exchange_num);
        mGoldReady = (TextView) findViewById(R.id.gold_exchange_own);
        mExchangeAll = (TextView) findViewById(R.id.gold_exchange_all);
        mCanExchange = (TextView) findViewById(R.id.gold_exchange_can);
        mExchange = (Button) findViewById(R.id.gold_exchange_next);

        mExchange.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                try {
                    int gold = Integer.parseInt(mNum.getText().toString());
                    goldExchange(gold);
                } catch (NumberFormatException e) {
                }
            }
        });

        mExchangeAll.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                mNum.setText(maxNum + "");
                mNum.setSelection(mNum.getText().length());
            }
        });
    }

    UserInfo info;

    @Override
    protected void onResume() {
        super.onResume();

        info = DaoUtil.getUserInfoFromLocal(GoldExchangeActivity.this);

        if (mApplication.IS_DEBUG) {
            info.gold = 1000;
        }

        maxNum = info.gold / 100;

        mGoldReady.setText(info.gold + "");
        mNum.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String str = mNum.getText().toString();
                if (str.length() == 0) {
                    if (mExchange.isEnabled()) {
                        mExchange.setEnabled(false);
                    }
                    mCanExchange.setText("可兑换现金: 0元");
                    return;
                }
                if (str.startsWith("0")) {
                    ToastUtil.show(GoldExchangeActivity.this, "输入金币数不能是0");
                    mNum.setText("");
                    return;
                }

                int num = Integer.parseInt(str);
                if (num <= maxNum) {
                    mCanExchange.setText("可兑换现金: " + mNum.getText().toString() + "元");
                    mExchange.setEnabled(true);
                } else {
                    ToastUtil.show(GoldExchangeActivity.this, "当前金币数不足以兑换，请赚取更多金币");
                    mNum.setText("");
                    mCanExchange.setText("可兑换现金: 0元");
                    mExchange.setEnabled(false);
                }

            }
        });

    }

    String errMsg;
    boolean goldChanged = false;

    private void goldExchange(final int egold) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();

                FormBody body = new FormBody.Builder()
                        .add("gold", egold * 100 + "")
                        .build();


                Request request = new Request.Builder().url(Settings.BASE_URL + "exchange").addHeader("CLIENT", "android")
                        .addHeader("TOKEN", CommonUtil.getMobileUniqueId(mSelf))
                        .addHeader("authorization", DaoUtil.getAuthorization(mSelf))
                        .post(body)
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    JSONObject result = JSON.parseObject(response.body().string());

                    if (result.getIntValue("code") == 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                goldChanged = true;
                                info.gold -= egold * 100;
                                DaoUtil.saveUserInfo(JSON.toJSONString(info), mSelf);
                                mGoldReady.setText(info.gold + "");
                                mNum.setText("");
                                maxNum -= egold;
                                Toast.makeText(mSelf, "兑换成功", Toast.LENGTH_SHORT).show();
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
                }
            }
        }).start();
    }

    @Override
    public void onBackPressed() {

        if (goldChanged) {
            setResult(4321);
        } else {
            setResult(0);
        }


        super.onBackPressed();
    }
}
