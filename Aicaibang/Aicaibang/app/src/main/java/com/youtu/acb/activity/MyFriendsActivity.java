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
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.youtu.acb.R;
import com.youtu.acb.Views.CircleImageView;
import com.youtu.acb.Views.Titlebar;
import com.youtu.acb.common.Settings;
import com.youtu.acb.entity.FriendInfo;
import com.youtu.acb.util.CommonUtil;
import com.youtu.acb.util.DaoUtil;
import com.youtu.acb.util.OnSingleClickListener;

import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 我的好友
 */
public class MyFriendsActivity extends BaseActivity {

    private Titlebar mTitleBar;
    private RecyclerView mList;
    private ArrayList<FriendInfo> infos = new ArrayList<>();
    private FriendAdapter mAdapter;
    private int mCurrentPage = 1;
    private boolean hasMore;
    private RecyclerView.LayoutManager mLayoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_friends);

        mTitleBar = (Titlebar) findViewById(R.id.my_friend_titlebar);
        mTitleBar.setTitle("我的好友");
        mTitleBar.getmLeftPart().setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                onBackPressed();
            }
        });

        mTitleBar.getLayoutParams().height = Settings.TITLEBAR_HEIGHT;
        mList = (RecyclerView) findViewById(R.id.my_friend_recycler);

        mLayoutManager = new LinearLayoutManager(MyFriendsActivity.this);
        mList.setLayoutManager(mLayoutManager);

        mAdapter = new FriendAdapter();
        mList.setAdapter(mAdapter);

        getFriends();

    }

    class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendVh> {


        @Override
        public FriendVh onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_my_friends, null);
            return new FriendVh(view);
        }

        @Override
        public void onBindViewHolder(FriendVh holder, int position) {
            FriendInfo info = infos.get(position);

            if (info.icon != null) {
                Glide.with(MyFriendsActivity.this).load(info.icon).into(holder.icon);
            } else {
                holder.icon.setImageDrawable(null);
            }

            holder.name.setText(info.nick == null ? "" : info.nick);

            holder.time.setText(info.subtitle == null ? "" : info.subtitle);

            SpannableString ss = new SpannableString("完成活动: " + info.count + "个");
            ss.setSpan(new ForegroundColorSpan(Color.parseColor("#fe766f")), 5, ss.length() - 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            holder.acts.setText(ss);

            SpannableString ss1 = new SpannableString("累计奖励: " + info.gold + "金币");
            ss1.setSpan(new ForegroundColorSpan(Color.parseColor("#fe766f")), 5, ss1.length() - 2, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            holder.awards.setText(ss1);


            //判断当前列表所在位置，当到最后两项时就加载
            int end = ((LinearLayoutManager) mLayoutManager).findLastVisibleItemPosition();
            if (end > getItemCount() - 3 && end <= getItemCount() - 1) {
                if (hasMore) {
                    mCurrentPage++;
                    getFriends();
                }
            }

        }

        @Override
        public int getItemCount() {
            return infos.size();
        }


        class FriendVh extends RecyclerView.ViewHolder {

            CircleImageView icon;
            TextView name;
            TextView time;
            TextView acts;
            TextView awards;

            public FriendVh(View itemView) {
                super(itemView);

                icon = (CircleImageView) itemView.findViewById(R.id.avatar);
                name = (TextView) itemView.findViewById(R.id.name);
                time = (TextView) itemView.findViewById(R.id.invite_time);
                acts = (TextView) itemView.findViewById(R.id.activitys_total);
                awards = (TextView) itemView.findViewById(R.id.award_total);
            }
        }
    }

    private String msg;

    private void getFriends() {
        new Thread(new Runnable() {
            @Override
            public void run() {


                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(Settings.BASE_URL + "myinvite?page=" + mCurrentPage).addHeader("CLIENT", "android")
                        .addHeader("TOKEN", CommonUtil.getMobileUniqueId(MyFriendsActivity.this))
                        .addHeader("authorization", DaoUtil.getAuthorization(MyFriendsActivity.this))
                        .get()
                        .build();

                try {
                    Response response = client.newCall(request).execute();
                    JSONObject resultObj = JSON.parseObject(response.body().string());


                    if (resultObj.getIntValue("code") == 0) {

                        if (mCurrentPage == 1) {
                            infos.clear();
                        }

                        JSONArray array = resultObj.getJSONArray("list");
                        int length = array.size();
                        for (int i = 0; i < length; i++) {
                            FriendInfo info = JSON.toJavaObject(array.getJSONObject(i), FriendInfo.class);
                            infos.add(info);
                        }

                        if (resultObj.getIntValue("total") > infos.size()) {
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
                        msg = resultObj.getString("msg");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MyFriendsActivity.this, msg, Toast.LENGTH_SHORT).show();
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
