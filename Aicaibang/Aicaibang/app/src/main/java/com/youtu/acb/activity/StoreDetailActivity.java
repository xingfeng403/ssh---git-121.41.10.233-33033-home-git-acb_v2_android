package com.youtu.acb.activity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.view.View;
import android.view.ViewGroup;
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
import com.youtu.acb.Views.LabelsLayout;
import com.youtu.acb.Views.RoundCornerImageView;
import com.youtu.acb.common.Settings;
import com.youtu.acb.entity.ActivityInfo;
import com.youtu.acb.util.CommonUtil;
import com.youtu.acb.util.DaoUtil;
import com.youtu.acb.util.DirectListener;
import com.youtu.acb.util.OnSingleClickListener;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 商家详情
 */
public class StoreDetailActivity extends BaseActivity {
    private RelativeLayout mTitleBar;
    private LinearLayout mTimeLine;
    private LinearLayout mWhoBuys;
    private String mStoreId;
    private TextView mTitle;
    private TextView mCompName;
    private TextView mAverage;
    private TextView mArea;
    private TextView mRegisteInvest;
    private TextView mOnlineTime;
    private TextView mInvesters;
    private TextView mComments;
    private ImageView mBack;
    private RoundCornerImageView mIcon;
    private LabelsLayout mLabels;
    private TextView mPlatBrief;
    private TextView mCompInfo;
    private ImageView mZizhiPic1, mZizhiPic2, mZizhiPic3;
    private LinearLayout mTelLayout;
    private TextView mReport;
    private TextView mTelTv;
    private TextView mExtend; // button below details
    private LinearLayout mContainer;
    private RelativeLayout mGotoReport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_detail);

        mStoreId = getIntent().getStringExtra("storeid");

        mTitleBar = (RelativeLayout) findViewById(R.id.store_detail_titlebar);
        mTitleBar.getLayoutParams().height = Settings.TITLEBAR_HEIGHT;
        mTitle = (TextView) findViewById(R.id.store_detail_title);
        mCompName = (TextView) findViewById(R.id.sd_com_name);
        mArea = (TextView) findViewById(R.id.sd_average);
        mCompName = (TextView) findViewById(R.id.sd_com_name);
        mAverage = (TextView) findViewById(R.id.sd_average);
        mArea = (TextView) findViewById(R.id.sd_area);
        mRegisteInvest = (TextView) findViewById(R.id.sd_ziben);
        mOnlineTime = (TextView) findViewById(R.id.sd_time);
        mInvesters = (TextView) findViewById(R.id.sd_invest_num);
        mComments = (TextView) findViewById(R.id.sd_comment_num);
        mBack = (ImageView) findViewById(R.id.store_detail_back);
        mIcon = (RoundCornerImageView) findViewById(R.id.sd_icon);
        mLabels = (LabelsLayout) findViewById(R.id.sd_labels);
        mTelLayout = (LinearLayout) findViewById(R.id.sd_tel_layout);
        mExtend = (TextView) findViewById(R.id.store_detail_extend);
        mContainer = (LinearLayout) findViewById(R.id.store_detail_act_container) ;
        mGotoReport = (RelativeLayout) findViewById(R.id.go_to_report_layout);

        mTelLayout.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                // 跳转dial
            }
        });

        mPlatBrief = (TextView) findViewById(R.id.store_detail_brief);
        mCompInfo = (TextView) findViewById(R.id.store_detail_comp_info);
        int width180 = (int) (Settings.RATIO_WIDTH * 180);
        int height140 = (int) (Settings.RATIO_WIDTH * 140);
        mZizhiPic1 = (ImageView) findViewById(R.id.sd_zizhi1);
        mZizhiPic2 = (ImageView) findViewById(R.id.sd_zizhi2);
        mZizhiPic3 = (ImageView) findViewById(R.id.sd_zizhi3);
        mZizhiPic1.getLayoutParams().width = width180;
        mZizhiPic1.getLayoutParams().height = height140;
        mZizhiPic2.getLayoutParams().width = width180;
        mZizhiPic2.getLayoutParams().height = height140;
        mZizhiPic3.getLayoutParams().width = width180;
        mZizhiPic3.getLayoutParams().height = height140;
        mReport = (TextView) findViewById(R.id.sd_bm3);
        mTelTv = (TextView) findViewById(R.id.sd_tel_num);


        mTimeLine = (LinearLayout) findViewById(R.id.go_to_timeline);
        mTimeLine.setOnClickListener(new DirectListener(StoreDetailActivity.this, TimeLineActivity.class));

        mWhoBuys = (LinearLayout) findViewById(R.id.go_to_who_buy);
        mWhoBuys.setOnClickListener(new DirectListener(StoreDetailActivity.this, TheyInvestActivity.class));

        mBack.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                onBackPressed();
            }
        });


        mGotoReport.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                    if (DaoUtil.isLogined(StoreDetailActivity.this)) {
                        if (titleName != null) {
                            startActivity(new Intent(StoreDetailActivity.this, PlatReportActivity.class).putExtra("name", titleName));
                        }
                    } else {
                        startActivity(new Intent(StoreDetailActivity.this, LoginActivity.class));
                    }


            }
        });

        getDetails();
    }

    /**
     * 获取详情
     */
    private String platMsg;
    private JSONObject resultObj;
    private String briefStr;
    private int mExtendState;
    private String titleName;

    private void getDetails() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(Settings.BASE_URL + "platform/" + mStoreId)
                        .addHeader("TOKEN", CommonUtil.getMobileUniqueId(StoreDetailActivity.this))
                        .addHeader("authorization", DaoUtil.getAuthorization(StoreDetailActivity.this))
                        .addHeader("CLIENT", "android")
                        .get()
                        .build();

                try {
                    final Response response = client.newCall(request).execute();
                    resultObj = JSON.parseObject(response.body().string());

                    if (resultObj.getIntValue("code") == 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    titleName = resultObj.getString("title");
                                    mTitle.setText(titleName == null ? "" : titleName);
                                    mCompName.setText(resultObj.getString("name"));

                                    Glide.with(StoreDetailActivity.this).load(resultObj.getString("icon")).into(mIcon);
                                    mAverage.setText("平均年化: " + resultObj.getString("apr"));
                                    String province = resultObj.getString("pname");
                                    String city = resultObj.getString("city");
                                    if (!TextUtils.isEmpty(province) && !TextUtils.isEmpty(city)) {
                                        mArea.setText("所在地区: " + province + " | " + city);
                                    } else {
                                        mArea.setText("所在地区: ");
                                    }

                                    JSONArray array = resultObj.getJSONArray("label");
                                    String[] labels = array.toArray(new String[array.size()]);
                                    if (labels != null && labels.length > 0) {
                                        mLabels.setData(labels, 15);
                                        mLabels.setVisibility(View.VISIBLE);
                                    } else {
                                        mLabels.setVisibility(View.GONE);
                                    }

                                    mRegisteInvest.setText("注册资本: " + resultObj.getString("capital"));
                                    mOnlineTime.setText("上线时间: " + resultObj.getString("online"));
                                    mInvesters.setText(resultObj.getString("invite_msg"));
                                    mComments.setText(resultObj.getString("comment_msg"));

                                    briefStr = (resultObj.getString("detail") == null ? "" : resultObj.getString("detail"));
                                    TextPaint mTextPaint = mPlatBrief.getPaint();
                                    int mTextViewWidth = (int) mTextPaint.measureText(briefStr);
                                    if (mTextViewWidth > 4 * mPlatBrief.getWidth()) {
                                        mExtend.setVisibility(View.VISIBLE);
                                        mExtend.setOnClickListener(new OnSingleClickListener() {
                                            @Override
                                            public void doOnClick(View v) {
                                                if (mExtendState == 1) {
                                                    mPlatBrief.setMaxLines(7);
                                                    mPlatBrief.setText(briefStr);
                                                    mExtend.setText("展开");
                                                    mExtendState = 0;
                                                } else {
                                                    mPlatBrief.setMaxLines(50);
                                                    mPlatBrief.setText(briefStr);
                                                    mExtend.setText("收起");
                                                    mExtendState = 1;
                                                }
                                            }
                                        });
                                    } else {
                                        if (mExtend.getVisibility() == View.VISIBLE) {
                                            mExtend.setVisibility(View.GONE);
                                        }
                                    }
                                    mPlatBrief.setText(briefStr);


                                    JSONObject obj = resultObj.getJSONObject("company");
                                    StringBuilder builder = new StringBuilder();
                                    builder.append("企业名称: " + obj.getString("name") + "\n");
                                    builder.append("企业法人: " + obj.getString("boss") + "\n");
                                    builder.append("公司类型: " + obj.getString("type") + "\n");
                                    builder.append("注册资本: " + obj.getString("capital") + "\n");
                                    builder.append("注册地址: " + obj.getString("address") + "\n");
                                    builder.append("开业日期: " + obj.getString("online") + "\n");
                                    builder.append("登记机关: " + obj.getString("reg_address") + "\n");
                                    builder.append("组织机构代码: " + obj.getString("code"));

                                    mCompInfo.setText(new String(builder));

                                    String[] imgs = resultObj.getJSONArray("aptitude").toArray(new String[3]);
                                    Glide.with(StoreDetailActivity.this).load(imgs[0]).into(mZizhiPic1);
                                    Glide.with(StoreDetailActivity.this).load(imgs[1]).into(mZizhiPic2);
                                    Glide.with(StoreDetailActivity.this).load(imgs[2]).into(mZizhiPic3);

                                    boolean isFirstItem = true;
                                    JSONArray actArray = resultObj.getJSONArray("activity");
                                    if (actArray != null && actArray.size() > 0) {
                                            mContainer.removeAllViews();
                                            for (Object act : actArray) {

                                                ActivityInfo temp = JSON.toJavaObject((JSONObject)act, ActivityInfo.class);

                                                if (isFirstItem) {
                                                    isFirstItem = false;
                                                } else {
                                                    View line = new View(StoreDetailActivity.this);
                                                    line.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 4));
                                                    line.setBackgroundColor(Color.parseColor("#f5f6f8"));

                                                    mContainer.addView(line);
                                                }

                                                View view = getLayoutInflater().inflate(R.layout.item_acts_sd, null);

                                                TextView name = (TextView) view.findViewById(R.id.item_task_name);
                                                TextView rewardName = (TextView) view.findViewById(R.id.item_act_reward);
                                                TextView rewardNum = (TextView) view.findViewById(R.id.item_reward_num);
                                                LinearLayout layout = (LinearLayout) view.findViewById(R.id.item_labels_layout);

                                                if (temp.label != null && temp.label.length > 0) {
                                                    LabelsLayout labelsll = new LabelsLayout(StoreDetailActivity.this, temp.label, 20);

                                                    labelsll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                                                    layout.addView(labelsll);
                                                }

                                                name.setText(temp.title == null ? "" : temp.title);
                                                SpannableString ss = new SpannableString(temp.amount + temp.unit_type);
                                                ss.setSpan(new AbsoluteSizeSpan(12, true), ss.length() - 2, ss.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                                                rewardNum.setText(ss);

                                                view.setOnClickListener(new ActClickListener(temp.id + "", temp.type + ""));
                                                mContainer.addView(view);

                                            }
                                    }

                                    mTelTv.setText(resultObj.getString("tel"));
                                    mTelLayout.setOnClickListener(new OnSingleClickListener() {
                                        @Override
                                        public void doOnClick(View v) {
                                            String number = mTelTv.getText().toString();
                                            //用intent启动拨打电话
                                            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+number));
                                            StoreDetailActivity.this.startActivity(intent);
                                        }
                                    });

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
                                Toast.makeText(StoreDetailActivity.this, platMsg, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        }).start();
    }

    public class ActClickListener extends OnSingleClickListener {
        private String actid, acttype;

        public ActClickListener(String actid, String acttype) {
            this.actid = actid;
            this.acttype = acttype;
        }

        @Override
        public void doOnClick(View v) {
            startActivity(new Intent(StoreDetailActivity.this, ActDetailActivity.class).putExtra("actid", actid).putExtra("acttype", acttype));
        }
    }
}
