package com.youtu.acb.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.youtu.acb.R;
import com.youtu.acb.Views.LabelsLayout;
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
    private TextView mNoRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        mRecycler = (RecyclerView) findViewById(R.id.events_recycler);
        mTitleBar = (RelativeLayout) findViewById(R.id.events_titlebar);
        mTitleBar.getLayoutParams().height = Settings.TITLEBAR_HEIGHT;
        mBack = (FrameLayout) findViewById(R.id.events_back);
        mNoRecord = (TextView) findViewById(R.id.no_recorder);
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

    class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventVh> {

        @Override
        public EventAdapter.EventVh onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_events, null);
            return new EventVh(view);
        }

        @Override
        public void onBindViewHolder(final EventAdapter.EventVh holder, final int position) {

            EventInfo info = mInfos.get(position);
            if (info.name != null) {
                holder.platname.setText(info.name);
            } else {
                holder.platname.setText("");
            }

            if (info.add_time != null) {
                holder.addtime.setText(info.add_time);
            } else {
                holder.addtime.setText("");
            }

            if (info.title != null) {
                holder.name.setText(info.title);
            } else {
                holder.name.setText("");
            }

            SpannableString ss = new SpannableString(info.amount + info.unit_type);
            ss.setSpan(new AbsoluteSizeSpan(12, true), ss.length() - 2, ss.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            holder.amount.setText(ss);

            if (info.label != null && info.label.length > 0) {
                LabelsLayout labels = new LabelsLayout(EventsActivity.this, info.label, 20);

                labels.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                holder.mLabels.addView(labels);
            }

            holder.mRoot.setOnClickListener(new OnSingleClickListener() {
                @Override
                public void doOnClick(View v) {
                    startActivity(new Intent(EventsActivity.this, ActDetailActivity.class).putExtra("actid", mInfos.get(position).activity_id + "").putExtra("acttype", mInfos.get(position).type + "").putExtra("myacts", true));
                }
            });

        }

        @Override
        public int getItemCount() {
            return mInfos.size();
        }

        class EventVh extends RecyclerView.ViewHolder {
            private TextView platname;
            private TextView addtime;
            private TextView name;
            private TextView amount;
            private LinearLayout mLabels;
            private LinearLayout mRoot;

            public EventVh(View itemView) {
                super(itemView);

                platname = (TextView) itemView.findViewById(R.id.m_event_plat);
                addtime = (TextView) itemView.findViewById(R.id.m_event_time);
                name = (TextView) itemView.findViewById(R.id.m_event_name);
                amount = (TextView) itemView.findViewById(R.id.m_event_amount);
                mLabels = (LinearLayout) itemView.findViewById(R.id.m_event_labels);
                mRoot = (LinearLayout) itemView.findViewById(R.id.root_view);

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

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mAdapter.notifyDataSetChanged();

                                if (mInfos.size() > 0) {
                                    mNoRecord.setVisibility(View.INVISIBLE);
                                } else {
                                    mNoRecord.setVisibility(View.VISIBLE);
                                }
                            }
                        });
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
