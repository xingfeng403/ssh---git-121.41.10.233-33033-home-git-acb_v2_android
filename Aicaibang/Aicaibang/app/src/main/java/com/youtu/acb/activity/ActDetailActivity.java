package com.youtu.acb.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.youtu.acb.entity.EntryInfo;
import com.youtu.acb.entity.RepayTypeInfo;
import com.youtu.acb.util.CommonUtil;
import com.youtu.acb.util.DaoUtil;
import com.youtu.acb.util.OnSingleClickListener;

import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 活动详情
 */
public class ActDetailActivity extends BaseActivity {

    private RelativeLayout mTitleBar;
    private ImageView mBtnJiyibi;
    private TextView mTitle;
    private ImageView mBack;
    private ImageView mShare;
    private String mActId;
    private String mActType;
    private int mEntry; // 我的活动为1
    private ImageView mBanner;
    private LinearLayout mContainer;
    private TextView mRule;
    private TextView mDesc;
    private LinearLayout mPicContainer;
    private Button mFloatBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_detail);


        mActId = getIntent().getStringExtra("actid");
        mActType = getIntent().getStringExtra("acttype");
        if (getIntent().getBooleanExtra("myacts", false)) {
            mEntry = 1;
        } else {
        }

        mTitleBar = (RelativeLayout) findViewById(R.id.act_detail_titlebar);
        mTitleBar.getLayoutParams().height = Settings.TITLEBAR_HEIGHT;

        mBack = (ImageView) findViewById(R.id.act_detail_back);
        mTitle = (TextView) findViewById(R.id.act_detail_title);
        mShare = (ImageView) findViewById(R.id.act_detail_share);
        mBanner = (ImageView) findViewById(R.id.act_detail_banner);
        mContainer = (LinearLayout) findViewById(R.id.act_detail_item_container);
        mRule = (TextView) findViewById(R.id.act_detail_rule);
        mPicContainer = (LinearLayout) findViewById(R.id.act_detail_pic_container);
        mDesc = (TextView) findViewById(R.id.act_detail_desc);
        mFloatBtn = (Button) findViewById(R.id.act_float_button);

        mBtnJiyibi = (ImageView) findViewById(R.id.ad_jiyibi_btn);

        mBack.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                onBackPressed();
            }
        });

        mBtnJiyibi.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                startActivity(new Intent(ActDetailActivity.this, JiyibiActivity.class).putExtra("actid", mActId).putExtra("acttype", mActType));
            }
        });

        getDetails();
    }


    private String platMsg;
    private JSONObject resultObj;

    private void getDetails() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(Settings.BASE_URL + "activity?id=" + mActId + "&type=" + mActType + "&entry=" + mEntry)
                        .addHeader("TOKEN", CommonUtil.getMobileUniqueId(ActDetailActivity.this))
                        .addHeader("authorization", DaoUtil.getAuthorization(ActDetailActivity.this))
                        .addHeader("CLIENT", "android")
                        .get()
                        .build();

                try {
                    Response response = client.newCall(request).execute();
                    resultObj = JSON.parseObject(response.body().string());

                    if (resultObj.getIntValue("code") == 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    mTitle.setText(resultObj.getString("title"));
                                    Glide.with(ActDetailActivity.this).load(resultObj.getString("banner_image")).into(mBanner);
                                    JSONArray entrys = resultObj.getJSONArray("entry");
                                    int entryLength = entrys.size();
                                    LayoutInflater inflater = null;
                                    if (entryLength > 0) {
                                        inflater = getLayoutInflater();
                                        mContainer.removeAllViews();
                                    }
                                    for (int i = 0; i < entryLength; i++) {
                                        EntryInfo info = JSON.toJavaObject(entrys.getJSONObject(i), EntryInfo.class);

                                        View view = inflater.inflate(R.layout.item_act_detail, null);
                                        TextView amount = (TextView) view.findViewById(R.id.item_invest_amount);
                                        TextView date = (TextView) view.findViewById(R.id.item_dates);
                                        TextView award = (TextView) view.findViewById(R.id.item_awards);
                                        TextView assure = (TextView) view.findViewById(R.id.item_assure);

                                        amount.setText(info.amount == null ? "" : info.amount);
                                        date.setText(info.term == null ? "" : info.term);
                                        award.setText(info.reward == null ? "" : info.reward);
                                        assure.setText(info.bond == null ? "" : info.bond); // 保证金

                                        view.setMinimumHeight((int) (Settings.RATIO_WIDTH * 90));

                                        mContainer.addView(view);

                                        // add divider
                                        View line = new View(ActDetailActivity.this);
                                        line.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 4));
                                        line.setBackgroundColor(Color.parseColor("#f5f6f8"));

                                        mContainer.addView(line);
                                    }


                                    String rule = resultObj.getString("rule");
                                    String desc = resultObj.getString("explain");
                                    mRule.setText(rule == null ? "" : rule);
                                    mDesc.setText(desc == null ? "" : desc);

                                    if (resultObj.getIntValue("is_docking") == 0) {
                                        int indicate = getSharedPreferences(Common.sharedPrefName, MODE_PRIVATE).getInt("jiyibi", 0);
                                        if (indicate == 1) {
                                            mBtnJiyibi.setVisibility(View.VISIBLE);
                                        } else {
                                            startActivity(new Intent(ActDetailActivity.this, DialogActivity.class));
                                        }
                                    } else {
                                        mBtnJiyibi.setVisibility(View.INVISIBLE); // 已对接
                                    }

                                    switch (resultObj.getIntValue("state")) {
                                        case 0:
                                            mFloatBtn.setText("立即参与");
                                            mFloatBtn.setEnabled(true);
                                            break;

                                        case 1:
                                            mFloatBtn.setText("进行中");
                                            mFloatBtn.setEnabled(true);
                                            break;

                                        case 2:
                                            mFloatBtn.setText("失效");
                                            mFloatBtn.setEnabled(true);
                                            break;

                                        case 3:
                                            mFloatBtn.setText("完成");
                                            mFloatBtn.setEnabled(false);
                                            break;

                                        case 4:
                                            mFloatBtn.setText("已结束");
                                            mFloatBtn.setEnabled(false);
                                            break;

                                        case 5:
                                            mFloatBtn.setText("未开始");
                                            mFloatBtn.setEnabled(false);
                                            break;

                                        case 6:
                                            mFloatBtn.setText("不支持终端");
                                            mFloatBtn.setEnabled(false);
                                            break;

                                        default:
                                            break;
                                    }

                                    JSONArray imgArr = resultObj.getJSONArray("images");
                                    String[] imgPics = imgArr.toArray(new String[imgArr.size()]);
                                    if (imgPics != null && imgPics.length > 0) {
                                        mPicContainer.removeAllViewsInLayout();

                                        int picWidth = (int) (Settings.RATIO_WIDTH * 240);
                                        int picHeight = (int) (Settings.RATIO_WIDTH * 434);
                                        int margin12 = (int) (Settings.RATIO_WIDTH * 12);
                                        boolean isFirst = true;
                                        for (String url : imgPics) {
                                            ImageView imgView = new ImageView(ActDetailActivity.this);

                                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(picWidth, picHeight);
                                            if (isFirst) {
                                                isFirst = false;
                                            } else {
                                                params.setMargins(margin12, 0, 0, 0);
                                            }

                                            imgView.setLayoutParams(params);

                                            Glide.with(ActDetailActivity.this).load(url).into(imgView);

                                            mPicContainer.addView(imgView);
                                        }
                                    }

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    } else {
                        platMsg = resultObj.getString("msg");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (platMsg == null)
                                    return;
                                Toast.makeText(ActDetailActivity.this, platMsg, Toast.LENGTH_SHORT).show();
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
