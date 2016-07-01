package com.youtu.acb.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.youtu.acb.R;
import com.youtu.acb.Views.SpaceItemDecoration;
import com.youtu.acb.common.Settings;
import com.youtu.acb.entity.EventInfo;
import com.youtu.acb.entity.UserInfo;
import com.youtu.acb.util.CommonUtil;
import com.youtu.acb.util.DaoUtil;
import com.youtu.acb.util.DensityUtils;
import com.youtu.acb.util.OnSingleClickListener;

import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 我的活动
 */
public class EventsActivity extends BaseActivity {
    private RecyclerView mRecycler;
    private RelativeLayout mTitleBar;
    private FrameLayout mBack;
    private EventAdapter mAdapter;
    private ArrayList<EventInfo> mInfos = new ArrayList();
    private int mCurrentPage = 1;
    private Context mSelf = EventsActivity.this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        mRecycler = (RecyclerView) findViewById(R.id.events_recycler);
        mTitleBar = (RelativeLayout) findViewById(R.id.events_titlebar);
        mTitleBar.getLayoutParams().height = Settings.TITLEBAR_HEIGHT;
        mBack = (FrameLayout) findViewById(R.id.events_back);
        mBack.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                onBackPressed();
            }
        });

        mRecycler.setLayoutManager(new LinearLayoutManager(EventsActivity.this));
        mAdapter = new EventAdapter();
        mRecycler.setAdapter(mAdapter);

        int divier10 = DensityUtils.dp2px(EventsActivity.this, 10);
        SpaceItemDecoration decoration = new SpaceItemDecoration(divier10);
        mRecycler.addItemDecoration(decoration);

        getMyEvents();

    }

    class EventAdapter extends RecyclerView.Adapter {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_events, parent);
            return new EventVh(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return mInfos.size();
        }

        class EventVh extends RecyclerView.ViewHolder {

            public EventVh(View itemView) {
                super(itemView);
            }
        }
    }

    String msg;

    private void getMyEvents() {


        new Thread(new Runnable() {
            @Override
            public void run() {

                UserInfo info = DaoUtil.getUserInfoFromLocal(EventsActivity.this);
                OkHttpClient client = new OkHttpClient();

                Request request = new Request.Builder()
                        .url(Settings.BASE_URL + "activitylst/" + info.id + "?page=" + mCurrentPage)
                        .addHeader("CLIENT", "android").addHeader("TOKEN", CommonUtil.getMobileUniqueId(mSelf))
                        .addHeader("authorization", DaoUtil.getAuthorization(mSelf))
                        .get()
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    JSONObject resultObj = JSON.parseObject(response.body().string());
                    if (resultObj.getIntValue("code") == 0) {
                        if (mCurrentPage == 1) {
                            mInfos.clear();
                        }
                        JSONArray array = resultObj.getJSONArray("list");
                        int length = array.size();
                        for (int i = 0; i < length; i++) {
                            EventInfo temp = JSON.toJavaObject(array.getJSONObject(i), EventInfo.class);
                            mInfos.add(temp);
                        }

                        mAdapter.notifyDataSetChanged();
                    } else {
                        msg = resultObj.getString("msg");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mSelf, msg, Toast.LENGTH_SHORT).show();
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
