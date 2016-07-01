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
import com.youtu.acb.entity.BondInfo;
import com.youtu.acb.util.CommonUtil;
import com.youtu.acb.util.DaoUtil;
import com.youtu.acb.util.OnSingleClickListener;
import com.youtu.acb.util.StringUtil;

import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * 我的保证金
 */
public class MyBondActivity extends BaseActivity {

    private RelativeLayout mTitleBar;
    private FrameLayout mBack;
    private RecyclerView mRecycler;
    private MyBondAdapter mAdapter;
    private float mBondNum;
    private TextView mMyBondNum;
    private Context mSelf = MyBondActivity.this;
    private ArrayList<BondInfo> mBonds = new ArrayList<>();
    private Button mWhatIsBond;
    private RecyclerView.LayoutManager mLayoutManager;
    private boolean hasMore;
    private int mCurrentPage = 1;
    private TextView mNoRecorder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_bond);

        mTitleBar = (RelativeLayout) findViewById(R.id.my_bond_titlebar);
        mTitleBar.getLayoutParams().height = Settings.TITLEBAR_HEIGHT;
        mBack = (FrameLayout) findViewById(R.id.my_bond_back);

        mBack.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                onBackPressed();
            }
        });

        mRecycler = (RecyclerView) findViewById(R.id.my_bond_list);
        mLayoutManager = new LinearLayoutManager(MyBondActivity.this);
        mRecycler.setLayoutManager(mLayoutManager);

        mAdapter = new MyBondAdapter();
        mRecycler.setAdapter(mAdapter);

        mMyBondNum = (TextView) findViewById(R.id.my_bond_num);
        mMyBondNum.setText(StringUtil.FormatFloat(getIntent().getFloatExtra("bondnum", 0f) + ""));

        mWhatIsBond = (Button) findViewById(R.id.my_bond_explain);


        mWhatIsBond.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                if (bindStr != null) {
                    startActivity(new Intent(mSelf, WebActivity.class).putExtra("url", bindStr).putExtra("title", "风险保证金"));
                }
            }
        });


        mNoRecorder = (TextView) findViewById(R.id.no_recorder);

    }

    @Override
    protected void onResume() {
        super.onResume();
        getBond();
    }

    class MyBondAdapter extends RecyclerView.Adapter<MyBondAdapter.BondVh> {

        @Override
        public BondVh onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_my_bond, null);
            return new BondVh(view);
        }

        @Override
        public void onBindViewHolder(BondVh holder, int position) {
            BondInfo info = mBonds.get(position);
            if (info.name != null) {
                holder.name.setText(info.name);
            } else {
                holder.name.setText("");
            }

            if (info.add_time != null) {
                holder.time.setText(info.add_time);
            } else {
                holder.time.setText(info.add_time);
            }


            String amountStr = StringUtil.FormatFloat(info.amount + "");
            if (info.amount > 0) {
                holder.num.setText("+" + amountStr + "元");
                holder.num.setTextColor(Color.parseColor("#fe9c9a"));
            } else {
                holder.num.setText(amountStr + "元");
                holder.num.setTextColor(Color.parseColor("#71e5a9"));
            }

            if (position == getItemCount() - 1) {
                ((LinearLayout.LayoutParams) holder.line.getLayoutParams()).setMargins(0, 0, 0, 0);
            } else {
                holder.line.setVisibility(View.VISIBLE);
            }


            //判断当前列表所在位置，当到最后两项时就加载
            int end = ((LinearLayoutManager) mLayoutManager).findLastVisibleItemPosition();
            if (end > getItemCount() - 3 && end <= getItemCount() - 1) {
                if (hasMore) {
                    mCurrentPage++;
                    getBond();
                }
            }
        }

        @Override
        public int getItemCount() {
            return mBonds.size();
        }

        class BondVh extends RecyclerView.ViewHolder {
            private TextView name;
            private TextView time;
            private TextView num;
            private View line;

            public BondVh(View itemView) {
                super(itemView);

                name = (TextView) itemView.findViewById(R.id.mb_item_name);
                time = (TextView) itemView.findViewById(R.id.mb_item_time);
                num = (TextView) itemView.findViewById(R.id.mb_item_num);
                line = itemView.findViewById(R.id.mb_item_divider_item_divider);
            }
        }
    }

    private String errMsg;
    private String bindStr = null;

    private void getBond() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(Settings.BASE_URL + "bond").addHeader("CLIENT", "android")
                        .addHeader("TOKEN", CommonUtil.getMobileUniqueId(mSelf))
                        .addHeader("authorization", DaoUtil.getAuthorization(mSelf))
                        .get()
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    JSONObject result = JSON.parseObject(response.body().string());

                    if (result.getIntValue("code") == 0) {
                        bindStr = result.getString("link");
                        JSONArray array = result.getJSONArray("list");
                        if (array != null && array.size() > 0) {
                            if (mCurrentPage == 1) {
                                mBonds.clear();
                            }
                            int length = array.size();
                            for (int i = 0; i < length; i++) {
                                BondInfo info = JSON.toJavaObject(array.getJSONObject(i), BondInfo.class);

                                mBonds.add(info);
                            }

                            // 是否还有更多
                            if (mBonds.size() < result.getIntValue("total")) {
                                hasMore = true;
                            } else {
                                hasMore = false;
                            }


                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mAdapter.notifyDataSetChanged();
                                    mNoRecorder.setVisibility(View.INVISIBLE);
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
                    e.printStackTrace();
                }
            }
        }).start();
    }


}
