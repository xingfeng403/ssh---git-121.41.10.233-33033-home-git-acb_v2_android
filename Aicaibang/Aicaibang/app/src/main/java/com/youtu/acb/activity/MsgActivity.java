package com.youtu.acb.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.youtu.acb.R;
import com.youtu.acb.Views.SpaceItemDecoration;
import com.youtu.acb.Views.Titlebar;
import com.youtu.acb.common.Settings;
import com.youtu.acb.entity.MsgInfo;
import com.youtu.acb.util.CommonUtil;
import com.youtu.acb.util.DaoUtil;
import com.youtu.acb.util.OnSingleClickListener;

import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MsgActivity extends BaseActivity {

    private Titlebar mTitleBar;
    private RecyclerView mRecyclerView;
    private Context mSelf = MsgActivity.this;
    private int mCurrentPage = 1;
    private boolean hasMore;
    private ArrayList<MsgInfo> mInfos = new ArrayList<>();
    private MsgAdapter mAdapter;
    private int mReadPos; // 阅读的消息位置
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_msg);

        mTitleBar = (Titlebar) findViewById(R.id.msg_titlebar);
        mTitleBar.setTitle("消息");
        mTitleBar.getLayoutParams().height = Settings.TITLEBAR_HEIGHT;
        mTitleBar.getmLeftPart().setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                MsgActivity.this.finish();
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.msg_list);
        mAdapter = new MsgAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mLayoutManager = new LinearLayoutManager(MsgActivity.this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mRecyclerView.addItemDecoration(new SpaceItemDecoration(4));

        getNews();
    }


    private String errMsg;

    private void getNews() {
        lock = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(Settings.BASE_URL + "thenews?page=" + mCurrentPage)
                        .addHeader("CLIENT", "android")
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
                            if (mCurrentPage == 1) {
                                mInfos.clear();
                            }
                            int length = array.size();
                            for (int i = 0; i < length; i++) {
                                MsgInfo info = JSON.toJavaObject(array.getJSONObject(i), MsgInfo.class);

                                mInfos.add(info);
                            }

                            // 是否还有更多
                            if (mInfos.size() < result.getIntValue("total")) {
                                hasMore = true;
                            } else {
                                hasMore = false;
                            }
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

    boolean lock;

    class MsgAdapter extends RecyclerView.Adapter<MsgAdapter.MsgVh> {

        @Override
        public MsgAdapter.MsgVh onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = getLayoutInflater().inflate(R.layout.item_msg, null);
            return new MsgVh(v);
        }

        @Override
        public void onBindViewHolder(MsgAdapter.MsgVh holder, int position) {
            MsgInfo info = mInfos.get(position);

            holder.name.setText(info.title == null ? "" : info.title);
            holder.time.setText(info.add_time == null ? "" : info.add_time);
            if (info.status == 1) {
                holder.newTag.setVisibility(View.INVISIBLE);
            } else {
                holder.newTag.setVisibility(View.VISIBLE);
            }

            //判断当前列表所在位置，当到最后两项时就加载
            int end = ((LinearLayoutManager) mLayoutManager).findLastVisibleItemPosition();
            if (end > getItemCount() - 3 && end <= getItemCount() - 1) {
                if (hasMore) {
                    mCurrentPage++;
                    if (!lock) {
                        getNews();
                    }

                }
            }

            holder.getRootView().setOnClickListener(new ItemClickListener(position));
        }

        @Override
        public int getItemCount() {
            return mInfos.size();
        }

        class MsgVh extends RecyclerView.ViewHolder {
            TextView name;
            TextView time;
            TextView newTag;

            View rootView;

            public MsgVh(View itemView) {
                super(itemView);

                rootView = itemView;

                name = (TextView) itemView.findViewById(R.id.msg_name);
                time = (TextView) itemView.findViewById(R.id.msg_time);
                newTag = (TextView) itemView.findViewById(R.id.msg_new_tag);
            }

            private View getRootView() {
                return rootView;
            }

        }


        class ItemClickListener extends OnSingleClickListener {
            int clickPos;

            public ItemClickListener(int position) {
                clickPos = position;
            }

            @Override
            public void doOnClick(View v) {
                MsgInfo info = mInfos.get(clickPos);

                Bundle bundle = new Bundle();
                bundle.putInt("id", info.id);
                bundle.putInt("type", info.type);

                mReadPos = clickPos;
                startActivityForResult(new Intent(MsgActivity.this, MsgDetailActivity.class).putExtras(bundle), 10001);

            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == 10002) {
            mInfos.get(mReadPos).status = 1;
            mAdapter.notifyDataSetChanged();
        }
    }
}
