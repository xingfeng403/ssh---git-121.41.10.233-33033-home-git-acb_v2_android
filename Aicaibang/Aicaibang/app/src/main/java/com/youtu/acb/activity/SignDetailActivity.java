package com.youtu.acb.activity;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.youtu.acb.R;
import com.youtu.acb.Views.SpaceItemDecoration;
import com.youtu.acb.Views.Titlebar;
import com.youtu.acb.common.Settings;
import com.youtu.acb.entity.RecommendInfo;
import com.youtu.acb.entity.SignDeailInfo;
import com.youtu.acb.util.CommonUtil;
import com.youtu.acb.util.DaoUtil;
import com.youtu.acb.util.OnSingleClickListener;

import java.util.ArrayList;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 签到明细
 */
public class SignDetailActivity extends AppCompatActivity {

    private Titlebar mTitleBar;
    private RecyclerView mRecycler;
    private LinearLayout mGotoSign; // 点击签到
    private RecyclerView.LayoutManager mLayoutManager;
    private SignDetailAdapter mAdapter;
    private ArrayList<SignDeailInfo> mInfos = new ArrayList<>();
    private Context mSelf = SignDetailActivity.this;
    private boolean hasMore = false;
    private LinearLayout mSignSucLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_detail);

        mTitleBar = (Titlebar) findViewById(R.id.sign_detail_titlebar);
        mTitleBar.getmLeftPart().setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                onBackPressed();
            }
        });
        mTitleBar.getLayoutParams().height = Settings.TITLEBAR_HEIGHT;
        mTitleBar.setTitle("签到");

        mRecycler = (RecyclerView) findViewById(R.id.sign_detail_list);
        mGotoSign = (LinearLayout) findViewById(R.id.sign_detail_sign);

        mLayoutManager = new LinearLayoutManager(SignDetailActivity.this);
        mRecycler.setLayoutManager(mLayoutManager);
        mAdapter = new SignDetailAdapter();
        mRecycler.setAdapter(mAdapter);

        mRecycler.addItemDecoration(new SpaceItemDecoration(4));

        mSignSucLayout = (LinearLayout) findViewById(R.id.sign_suc_layout);

        mGotoSign.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                doSign();
            }
        });

        if (!lock)
            getSignDataDetail();

    }

    private void doSign() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(Settings.BASE_URL + "sign").addHeader("CLIENT", "android")
                        .addHeader("TOKEN", CommonUtil.getMobileUniqueId(mSelf))
                        .addHeader("authorization", DaoUtil.getAuthorization(mSelf))
                        .post(new FormBody.Builder().build())
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    JSONObject result = JSON.parseObject(response.body().string());

                    if (result.getIntValue("code") == 0) {
                        // 签到成功
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mSelf, "签到成功", Toast.LENGTH_SHORT).show();
                                getSignDataDetail();

                                mGotoSign.setVisibility(View.INVISIBLE);
                                mSignSucLayout.setVisibility(View.VISIBLE);

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

    class SignDetailAdapter extends RecyclerView.Adapter<SignDetailAdapter.SignDetailVh> {

        @Override
        public SignDetailAdapter.SignDetailVh onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = getLayoutInflater().inflate(R.layout.item_sign_detail, null);
            return new SignDetailVh(v);
        }

        @Override
        public void onBindViewHolder(SignDetailAdapter.SignDetailVh holder, int position) {
            SignDeailInfo info = mInfos.get(position);

            holder.name.setText(info.name == null ? "" : info.name);
            holder.num.setText("+" + info.gold);
        }

        @Override
        public int getItemCount() {
            return mInfos.size();
        }

        class SignDetailVh extends RecyclerView.ViewHolder {
            TextView name;
            TextView num;

            public SignDetailVh(View itemView) {
                super(itemView);

                name = (TextView) itemView.findViewById(R.id.item_sign_detail_content);
                num = (TextView) itemView.findViewById(R.id.item_sign_detail_num);
            }
        }
    }

    private String errMsg;
    boolean lock;

    private void getSignDataDetail() {
        lock = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(Settings.BASE_URL + "sign").addHeader("CLIENT", "android")
                        .addHeader("TOKEN", CommonUtil.getMobileUniqueId(mSelf))
                        .addHeader("authorization", DaoUtil.getAuthorization(mSelf))
                        .get()
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    JSONObject result = JSON.parseObject(response.body().string());

                    if (result.getIntValue("code") == 0) {

                        JSONArray array = result.getJSONArray("list");
                        int length = array.size();

                        if (result.getIntValue("sign") == 0) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mGotoSign.setVisibility(View.VISIBLE);
                                    mSignSucLayout.setVisibility(View.INVISIBLE);
                                }
                            });

                        } else {

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mGotoSign.setVisibility(View.INVISIBLE);
                                    mSignSucLayout.setVisibility(View.VISIBLE);
                                }
                            });

                        }

                        mInfos.clear();
                        for (int i = 0; i < length; i++) {
                            SignDeailInfo info = JSON.toJavaObject(array.getJSONObject(i), SignDeailInfo.class);
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

                lock = false;
            }
        }).start();
    }
}
