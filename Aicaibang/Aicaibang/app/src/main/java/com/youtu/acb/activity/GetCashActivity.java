package com.youtu.acb.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.youtu.acb.R;
import com.youtu.acb.common.Common;
import com.youtu.acb.common.Settings;
import com.youtu.acb.entity.AccountInfo;
import com.youtu.acb.entity.UserInfo;
import com.youtu.acb.util.CommonUtil;
import com.youtu.acb.util.DaoUtil;
import com.youtu.acb.util.DirectListener;
import com.youtu.acb.util.OnSingleClickListener;
import com.youtu.acb.util.StringUtil;
import com.youtu.acb.util.ToastUtil;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 提现
 */
public class GetCashActivity extends BaseActivity {

    private RelativeLayout mTitleBar;
    private FrameLayout mBack;
    private Context mSelf = GetCashActivity.this;
    private ArrayList<AccountInfo> mAccounts = new ArrayList<>();
    private LinearLayout mWeixinAccountLayout;
    private LinearLayout mBankCardAccountLayout;
    private View mLine;
    private TextView mAlipay;
    private TextView mWeixin;
    private float mAvailableNum = 0f;
    private TextView mGetAll;
    private EditText mGetNum;
    private String mGetCashExactNum;
    private Button mGetCashBtn;
    private TextView mAvailNum;
    private TextView mDescription;
    private ImageView mImage1; // 微信支付 图标
    private ImageView mImage1Sel; // 是否选中微信支付方式
    private ImageView mImage2;   // 银行卡图标
    private ImageView mImage2Sel; // 是否选择银行卡提现


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_g);

        mAvailableNum = getIntent().getFloatExtra("availnum", 0f);

        mTitleBar = (RelativeLayout) findViewById(R.id.get_cash_titlebar);
        mTitleBar.getLayoutParams().height = Settings.TITLEBAR_HEIGHT;
        mBack = (FrameLayout) findViewById(R.id.get_cash_back);

        mBack.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                onBackPressed();
            }
        });

        mAvailNum = (TextView) findViewById(R.id.get_cash_own);
        mAvailNum.setText(StringUtil.FormatFloat(mAvailableNum + ""));
        mWeixinAccountLayout = (LinearLayout) findViewById(R.id.weixin_account_layout);
        mBankCardAccountLayout = (LinearLayout) findViewById(R.id.alipay_account_layout);
        mLine = findViewById(R.id.account_layout_divider);
        mWeixin = (TextView) findViewById(R.id.weixin_account);
        mAlipay = (TextView) findViewById(R.id.alipay_account);
        mGetAll = (TextView) findViewById(R.id.get_cash_get_all);
        mGetNum = (EditText) findViewById(R.id.get_cash_num);
        mGetCashBtn = (Button) findViewById(R.id.get_cash_submit);
        mDescription = (TextView) findViewById(R.id.get_cash_description);
        mImage1 = (ImageView) findViewById(R.id.weixin_icon);
        mImage1Sel = (ImageView) findViewById(R.id.weixin_sel);
        mImage2 = (ImageView) findViewById(R.id.alipay_icon);
        mImage2Sel = (ImageView) findViewById(R.id.alipay_sel);

        mGetAll.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                if (mAvailableNum <= 0f) {
                    mGetNum.setText("");

                    ToastUtil.show(GetCashActivity.this, "余额不足");
                } else {
                    mGetCashExactNum = mAvailableNum + "";
                    mGetNum.setText(mGetCashExactNum);
                    mGetNum.setSelection(mGetCashExactNum.length());
                }

            }
        });

        mGetCashBtn.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                if (mGetNum.getText().length() == 0) {
                    ToastUtil.show(mSelf, "请输入提现金额");
                } else {
                    mGetCashExactNum = mGetNum.getText().toString();
                    doSubmit();
                }
            }
        });

        mGetNum.addTextChangedListener(new TextWatcher() {
            boolean hasDot = false;

            @Override
            public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {
                if (s.toString().contains(".")) {
                    hasDot = true;
                } else {
                    hasDot = false;
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String str = editable.toString();

                if (str.startsWith(".")) {
                    mGetNum.setText("0.");
                } else if (str.length() == 2 && str.startsWith("0") && !str.startsWith("0.")) {  // 0.
                    mGetNum.setText("0." + str.substring(1, 2));
                    mGetNum.setSelection(3);
                } else if (hasDot && str.lastIndexOf(".") == str.length() - 1) { // double dot
                    mGetNum.setText(str.substring(0, str.length() - 1));
                    mGetNum.setSelection(mGetNum.getText().length());
                } else if (hasDot && str.length() - str.indexOf(".") == 4) { // more than 2 digits after dot
                    mGetNum.setText(str.toString().substring(0, str.length() - 1));
                    mGetNum.setSelection(str.length() - 1);
                } else if (str.endsWith(".0")) {
                    mGetNum.setText(str + "0");
                    mGetNum.setSelection(str.length());
                }

                if (mGetNum.getText().length() == 0) {
                    mGetCashBtn.setEnabled(false);
                } else {
                    mGetCashBtn.setEnabled(true);
                }
            }
        });


        mDescription.setText(getSharedPreferences(Common.configName, MODE_PRIVATE).getString("withdrawals_msg", ""));

    }


    @Override
    protected void onResume() {
        super.onResume();

        getAccounts();
    }

    private String errMsg;

    private void getAccounts() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(Settings.BASE_URL + "wallets").addHeader("CLIENT", "android")
                        .addHeader("TOKEN", CommonUtil.getMobileUniqueId(mSelf))
                        .addHeader("authorization", DaoUtil.getAuthorization(mSelf))
                        .get()
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    JSONObject result = JSON.parseObject(response.body().string());

                    if (result.getIntValue("code") == 0) {
                        JSONArray array = result.getJSONArray("list");
                        if (array != null && array.size() > 0) {
                            mAccounts.clear();
                            int length = array.size();
                            for (int i = 0; i < length; i++) {
                                AccountInfo info = JSON.toJavaObject(array.getJSONObject(i), AccountInfo.class);

                                mAccounts.add(info);
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateAccounts();
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mWeixinAccountLayout.setVisibility(View.GONE);
                                    mBankCardAccountLayout.setVisibility(View.GONE);
                                }
                            });
                        }
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


    private AccountInfo weixinInfo;
    private AccountInfo bankcardInfo;
    private AccountInfo selInfo;

    private void updateAccounts() {
        int size = mAccounts.size();
        if (size == 0) {
            return;
        }


        AccountInfo info = mAccounts.get(0);
        if (info.type == 2) {
            if (size > 1) {
                weixinInfo = info;
                bankcardInfo = mAccounts.get(1);

                selInfo = weixinInfo;

                mWeixinAccountLayout.setOnClickListener(new OnSingleClickListener() {
                    @Override
                    public void doOnClick(View v) {
                        if (selInfo.type != 2) {
                            selInfo = weixinInfo;
                            mImage1Sel.setImageResource(R.drawable.way_sel);
                            mImage2Sel.setImageResource(R.drawable.way_unsel);
                        }
                    }
                });

                mBankCardAccountLayout.setOnClickListener(new OnSingleClickListener() {
                    @Override
                    public void doOnClick(View v) {
                        if (selInfo.type != 3) {
                            selInfo = bankcardInfo;
                            mImage2Sel.setImageResource(R.drawable.way_sel);
                            mImage1Sel.setImageResource(R.drawable.way_unsel);
                        }
                    }
                });

                Glide.with(GetCashActivity.this).load(info.icon).into(mImage1);
                mImage1Sel.setImageResource(R.drawable.way_sel);
                Glide.with(GetCashActivity.this).load(bankcardInfo.icon).into(mImage2);
                mImage2Sel.setImageResource(R.drawable.way_unsel);

                if (weixinInfo.name != null) {
                    mWeixin.setText(weixinInfo.name);
                } else {
                    mWeixin.setText("");
                }
                if (bankcardInfo.name != null) {
                    mAlipay.setText(bankcardInfo.name);
                } else {
                    mAlipay.setText("");
                }
            } else {
                weixinInfo = info;
                selInfo = weixinInfo;
                Glide.with(GetCashActivity.this).load(info.icon).into(mImage1);
                mImage1Sel.setImageResource(R.drawable.way_sel);

                mBankCardAccountLayout.setVisibility(View.GONE);
                mLine.setVisibility(View.GONE);

                if (weixinInfo.name != null) {
                    mWeixin.setText(weixinInfo.name);
                } else {
                    mWeixin.setText("");
                }
            }
        } else {
            bankcardInfo = info;
            selInfo = bankcardInfo;
            Glide.with(GetCashActivity.this).load(bankcardInfo.icon).into(mImage2);
            mImage2Sel.setImageResource(R.drawable.way_sel);

            mWeixinAccountLayout.setVisibility(View.GONE);
            mLine.setVisibility(View.GONE);

            if (bankcardInfo.name != null) {
                mAlipay.setText(info.name);
            } else {
                mAlipay.setText("");
            }

        }
    }


    private void doSubmit() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();

                FormBody body = new FormBody.Builder()
                        .add("wallet_id", selInfo.id + "")
                        .add("amount", mGetCashExactNum)
                        .build();

                Request request = new Request.Builder().url(Settings.BASE_URL + "withdraw")
                        .addHeader("CLIENT", "android")
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
                                Toast.makeText(mSelf, "提现成功", Toast.LENGTH_SHORT).show();
                                GetCashActivity.this.finish();
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

    private boolean checkFloat(String str) {
        Pattern pattern = Pattern.compile("^(?!0(\\\\d|\\\\.0+$|$))\\\\d+(\\\\.\\\\d{1,2})?$");
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }

}
