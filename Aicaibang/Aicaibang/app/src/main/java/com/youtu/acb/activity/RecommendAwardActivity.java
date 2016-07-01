package com.youtu.acb.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.youtu.acb.R;
import com.youtu.acb.Views.CircleImageView;
import com.youtu.acb.Views.Titlebar;
import com.youtu.acb.common.Settings;
import com.youtu.acb.entity.RecommendInfo;
import com.youtu.acb.util.CommonUtil;
import com.youtu.acb.util.DaoUtil;
import com.youtu.acb.util.OnSingleClickListener;

import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 推荐有奖
 */
public class RecommendAwardActivity extends BaseActivity {

    private Titlebar mTitleBar;
    private RecyclerView mRecycler;
    private Button mRecContinue;
    private Button mGetMore;
    private TextView mNum;
    private Context mSelf = RecommendAwardActivity.this;
    private ArrayList<RecommendInfo> mInfos = new ArrayList<>();
    private RecoomedAdapter mAdapter;
    private String mUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommend_award);

        mTitleBar = (Titlebar) findViewById(R.id.recommend_titlebar);
        mTitleBar.getLayoutParams().height = Settings.TITLEBAR_HEIGHT;
        mTitleBar.getmLeftPart().setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                RecommendAwardActivity.this.finish();
            }
        });
        mTitleBar.setTitle("推荐有奖");

        mRecycler = (RecyclerView) findViewById(R.id.rec_list);
        mRecContinue = (Button) findViewById(R.id.rec_continue);
        mGetMore = (Button) findViewById(R.id.rec_look_more);
        mNum = (TextView) findViewById(R.id.rec_num);

        mAdapter = new RecoomedAdapter();
        mRecycler.setLayoutManager(new LinearLayoutManager(RecommendAwardActivity.this));
        mRecycler.setAdapter(mAdapter);

        mGetMore.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                mGetMore.setVisibility(View.GONE);
                showNum = mInfos.size();
                mAdapter.notifyDataSetChanged();
            }
        });

        mUid = DaoUtil.getUserId(RecommendAwardActivity.this);

        mRecContinue.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                startActivity(new Intent(RecommendAwardActivity.this, WebActivity.class).putExtra("url", "www.youtuker.com/m/app/share.html?iid=" + mUid).putExtra("title", "邀请好友"));
            }
        });

        getRec();
    }


    private String errMsg;
    private int total;

    private void getRec() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(Settings.BASE_URL + "invite").addHeader("CLIENT", "android")
                        .addHeader("TOKEN", CommonUtil.getMobileUniqueId(mSelf))
                        .addHeader("authorization", DaoUtil.getAuthorization(mSelf))
                        .get()
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    JSONObject result = JSON.parseObject(response.body().string());

                    if (result.getIntValue("code") == 0) {

                        if (result.getIntValue("count") == 0) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mRecContinue.setText("马上邀请");
                                }
                            });
                        } else {
                            if (result.getIntValue("count") == 0) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mRecContinue.setText("继续邀请");
                                    }
                                });
                            }
                        }

                        JSONArray array = result.getJSONArray("list");
                        int length = array.size();

                        mInfos.clear();
                        for (int i = 0; i < length; i++) {
                            RecommendInfo info = JSON.toJavaObject(array.getJSONObject(i), RecommendInfo.class);
                            mInfos.add(info);
                        }

                        if (length > 5) {
                            showNum = 5;

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mGetMore.setText("查看所有" + mInfos.size() + "位好友");
                                    mGetMore.setVisibility(View.VISIBLE);
                                }
                            });

                        } else {
                            showNum = length;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mGetMore.setVisibility(View.GONE);
                                }
                            });
                        }

                        total = result.getInteger("gold");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mNum.setText("" + total);
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
            }
        }).start();
    }


    private int showNum;

    class RecoomedAdapter extends RecyclerView.Adapter<RecoomedAdapter.RecVh> {

        @Override
        public RecoomedAdapter.RecVh onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_rec, null);
            return new RecVh(view);
        }

        @Override
        public void onBindViewHolder(RecoomedAdapter.RecVh holder, int position) {

        }

        @Override
        public int getItemCount() {
            return showNum;
        }

        class RecVh extends RecyclerView.ViewHolder {

            CircleImageView icon;
            TextView name;
            TextView time;
            TextView num;

            public RecVh(View itemView) {
                super(itemView);

                icon = (CircleImageView) itemView.findViewById(R.id.rec_item_icon);
                name = (TextView) itemView.findViewById(R.id.rec_item_name);
                time = (TextView) itemView.findViewById(R.id.rec_item_time);
                num = (TextView) itemView.findViewById(R.id.rec_item_num);
            }
        }
    }

}
