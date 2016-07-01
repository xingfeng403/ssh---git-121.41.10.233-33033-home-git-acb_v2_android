package com.youtu.acb.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.youtu.acb.R;
import com.youtu.acb.common.Settings;
import com.youtu.acb.entity.PlatFormInfo;
import com.youtu.acb.entity.TheyInvestInfo;
import com.youtu.acb.util.CommonUtil;
import com.youtu.acb.util.DaoUtil;
import com.youtu.acb.util.OnSingleClickListener;
import com.youtu.acb.util.StringUtil;

import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 他们投了
 */
public class TheyInvestActivity extends BaseActivity {
    private RelativeLayout mTitleBar;
    private FrameLayout mBack;
    private RecyclerView mRecycler;
    private TheyInvestAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private TextView noRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_they_invest);

        mTitleBar = (RelativeLayout) findViewById(R.id.they_invest_titlebar);
        mTitleBar.getLayoutParams().height = Settings.TITLEBAR_HEIGHT;
        mBack = (FrameLayout) findViewById(R.id.they_invest_back);
        mBack.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                onBackPressed();
            }
        });

        noRecord = (TextView) findViewById(R.id.no_recorder);
        mRecycler = (RecyclerView) findViewById(R.id.they_invest_recycler);
        mLayoutManager = new LinearLayoutManager(TheyInvestActivity.this);
        mRecycler.setLayoutManager(mLayoutManager);

        mAdapter = new TheyInvestAdapter();
        mRecycler.setAdapter(mAdapter);

        platID = getIntent().getStringExtra("platid");

        getList();
    }

    private String platMsg;
    private int mCurrentPage = 1;
    private boolean hasMore;
    private ArrayList<TheyInvestInfo> mInfos = new ArrayList<>();
    private String platID;

    private void getList() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(Settings.BASE_URL + "investLst/" + platID + "?page=" + mCurrentPage)
                        .addHeader("TOKEN", CommonUtil.getMobileUniqueId(TheyInvestActivity.this))
                        .addHeader("authorization", DaoUtil.getAuthorization(TheyInvestActivity.this))
                        .addHeader("CLIENT", "android")
                        .get()
                        .build();

                try {
                    Response response = client.newCall(request).execute();
                    JSONObject resultObj = JSON.parseObject(response.body().string());

                    if (resultObj.getIntValue("code") == 0) {
                        JSONArray array = resultObj.getJSONArray("list");
                        int length = array.size();
                        if (mCurrentPage == 1) {
                            mInfos.clear();
                        }
                        for (int i = 0; i < length; i++) {
                            TheyInvestInfo info = JSON.toJavaObject(array.getJSONObject(i), TheyInvestInfo.class);
                            mInfos.add(info);
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mAdapter.notifyDataSetChanged();

                                if (mInfos.size() > 0) {
                                    noRecord.setVisibility(View.INVISIBLE);
                                } else {
                                    noRecord.setVisibility(View.VISIBLE);
                                }
                            }
                        });

                        if (resultObj.getIntValue("total") > mInfos.size()) {
                            hasMore = true;
                        } else {
                            hasMore = false;
                        }

                    } else {
                        platMsg = resultObj.getString("msg");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (platMsg == null)
                                    return;
                                Toast.makeText(TheyInvestActivity.this, platMsg, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        }).start();
    }

    public class TheyInvestAdapter extends RecyclerView.Adapter<TheyInvestAdapter.TheyInvestVh> {

        @Override
        public TheyInvestVh onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_they_invest, null);
            return new TheyInvestVh(view);
        }

        @Override
        public void onBindViewHolder(TheyInvestVh holder, int position) {
            TheyInvestInfo info = mInfos.get(position);
            holder.name.setText(info.nick == null ? "" : info.nick);
            holder.time.setText(info.add_time == null ? "" : info.add_time);
            String numStr = StringUtil.FormatFloat(info.amount + "");
            SpannableString ss = new SpannableString(numStr + " 元");
            ss.setSpan(new ForegroundColorSpan(Color.parseColor("#fe766f")), 0, numStr.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            holder.num.setText(ss);
            Glide.with(TheyInvestActivity.this).load(info.icon).into(holder.icon);

            //判断当前列表所在位置，当到最后两项时就加载
            int end = ((LinearLayoutManager) mLayoutManager).findLastVisibleItemPosition();
            if (end > getItemCount() - 3 && end <= getItemCount() - 1) {
                if (hasMore) {
                    mCurrentPage++;
                    getList();
                }
            }
        }

        @Override
        public int getItemCount() {
            return mInfos.size();
        }

        public class TheyInvestVh extends RecyclerView.ViewHolder {
            ImageView icon;
            TextView name;
            TextView time;
            TextView num;

            public TheyInvestVh(View itemView) {
                super(itemView);

                icon = (ImageView) itemView.findViewById(R.id.tv_icon);
                name = (TextView) itemView.findViewById(R.id.tv_name);
                time = (TextView) itemView.findViewById(R.id.tv_time);
                num = (TextView) itemView.findViewById(R.id.tv_num);
            }
        }
    }
}
