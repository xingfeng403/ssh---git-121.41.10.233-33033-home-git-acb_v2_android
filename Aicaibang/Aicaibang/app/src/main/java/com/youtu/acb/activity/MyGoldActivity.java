package com.youtu.acb.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.youtu.acb.R;
import com.youtu.acb.common.Settings;
import com.youtu.acb.entity.GoldInfo;
import com.youtu.acb.entity.UserInfo;
import com.youtu.acb.util.CommonUtil;
import com.youtu.acb.util.DaoUtil;
import com.youtu.acb.util.DirectListener;
import com.youtu.acb.util.OnSingleClickListener;

import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 我的金币
 */
public class MyGoldActivity extends BaseActivity {

    private RelativeLayout mTitleBar;
    private FrameLayout mBack;
    private RecyclerView mRecycler;
    private MyGoldAdapter mAdapter;
    private int mGoldNum;
    private TextView mMyGoldNum;
    private Context mSelf = MyGoldActivity.this;
    private ArrayList<GoldInfo> mInfos = new ArrayList<>();
    private Button mExchange;
    private RecyclerView.LayoutManager mLayoutManager;
    private boolean hasMore;
    private int mCurrentPage = 1;
    private TextView mNoRecorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_gold);

        mGoldNum = getIntent().getIntExtra("goldnum", 0);

        mTitleBar = (RelativeLayout) findViewById(R.id.my_gold_titlebar);
        mTitleBar.getLayoutParams().height = Settings.TITLEBAR_HEIGHT;
        mBack = (FrameLayout) findViewById(R.id.my_gold_back);

        mBack.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                onBackPressed();
            }
        });


        mLayoutManager = new LinearLayoutManager(MyGoldActivity.this);
        mRecycler = (RecyclerView) findViewById(R.id.my_gold_list);
        mRecycler.setLayoutManager(mLayoutManager);

        mAdapter = new MyGoldAdapter();
        mRecycler.setAdapter(mAdapter);

        mMyGoldNum = (TextView) findViewById(R.id.my_gold_goldnum);
        mMyGoldNum.setText(mGoldNum + "");

        mExchange = (Button) findViewById(R.id.my_gold_exchange);
        mExchange.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                startActivityForResult(new Intent(mSelf, GoldExchangeActivity.class), 1234);
            }
        });

        mNoRecorder = (TextView) findViewById(R.id.no_recorder);
    }


    class MyGoldAdapter extends RecyclerView.Adapter<MyGoldAdapter.Vh> {

        @Override
        public Vh onCreateViewHolder(ViewGroup parent, int viewType) {
            View item = getLayoutInflater().inflate(R.layout.item_my_gold, null);
            Vh vh = new Vh(item);
            return vh;
        }

        @Override
        public void onBindViewHolder(Vh holder, int position) {
            GoldInfo info = mInfos.get(position);
            if (info.name != null) {
                holder.itemName.setText(info.name);
            } else {
                holder.itemName.setText("");
            }

            if (info.gold > 0) {
                holder.goldNum.setText("+" + info.gold);
                holder.goldNum.setTextColor(Color.parseColor("#fe6a67"));
            } else {
                holder.goldNum.setText("" + info.gold);
                holder.goldNum.setTextColor(Color.parseColor("#2ad87e"));
            }

            if (info.add_time != null) {
                holder.itemTime.setText(info.add_time);
            } else {
                holder.itemTime.setText("");
            }


            if (position == getItemCount() - 1) {
                ((LinearLayout.LayoutParams) holder.divider.getLayoutParams()).setMargins(0, 0, 0, 0);
            } else {
                holder.divider.setVisibility(View.VISIBLE);
            }


            //判断当前列表所在位置，当到最后两项时就加载
            int end = ((LinearLayoutManager) mLayoutManager).findLastVisibleItemPosition();
            if (end > getItemCount() - 3 && end <= getItemCount() - 1) {
                if (hasMore) {
                    mCurrentPage++;
                    getGold();
                }
            }

        }

        @Override
        public int getItemCount() {
            return mInfos.size();
        }


        class Vh extends RecyclerView.ViewHolder {
            private TextView itemName;
            private TextView goldNum;
            private View divider;
            private TextView itemTime;

            public Vh(View itemView) {
                super(itemView);

                itemName = (TextView) itemView.findViewById(R.id.mg_item_name);
                goldNum = (TextView) itemView.findViewById(R.id.mg_item_num);
                divider = itemView.findViewById(R.id.mg_item_divider);
                itemTime = (TextView) itemView.findViewById(R.id.mg_item_time);
            }
        }

    }

    private String errMsg;

    private void getGold() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(Settings.BASE_URL + "golds?page=" + mCurrentPage).addHeader("CLIENT", "android")
                        .addHeader("TOKEN", CommonUtil.getMobileUniqueId(mSelf))
                        .addHeader("authorization", DaoUtil.getAuthorization(mSelf))
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    JSONObject result = JSON.parseObject(response.body().string());

                    if (result.getIntValue("code") == 0) {
                        JSONArray array = result.getJSONArray("list");
                        if (array != null && array.size() > 0) {
                            if (mCurrentPage == 1) {
                                // 刚进入 和 下拉刷新
                                mInfos.clear();
                            }
                            int length = array.size();
                            for (int i = 0; i < length; i++) {
                                GoldInfo info = JSON.toJavaObject(array.getJSONObject(i), GoldInfo.class);

                                mInfos.add(info);
                            }

                            // 是否还有更多
                            if (mInfos.size() < result.getIntValue("total")) {
                                hasMore = true;
                            } else {
                                hasMore = false;
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mAdapter.notifyDataSetChanged();
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mNoRecorder.setVisibility(View.VISIBLE);
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

    @Override
    protected void onResume() {
        super.onResume();

        getGold();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null && requestCode == 1234 && resultCode == 4321) {
            UserInfo info = DaoUtil.getUserInfoFromLocal(mSelf);

            mGoldNum = info.gold;
            mMyGoldNum.setText(mGoldNum + "");
        }
    }
}
