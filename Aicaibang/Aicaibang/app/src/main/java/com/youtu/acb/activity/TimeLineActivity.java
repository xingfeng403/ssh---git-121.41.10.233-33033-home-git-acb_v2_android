package com.youtu.acb.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
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
import com.youtu.acb.Views.CircleImageView;
import com.youtu.acb.Views.SpaceItemDecoration;
import com.youtu.acb.common.Settings;
import com.youtu.acb.entity.TimeLineInfo;
import com.youtu.acb.util.CommonUtil;
import com.youtu.acb.util.DaoUtil;
import com.youtu.acb.util.DensityUtils;
import com.youtu.acb.util.DialogUtil;
import com.youtu.acb.util.OnSingleClickListener;


import java.util.ArrayList;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 投友圈
 */
public class TimeLineActivity extends BaseActivity {

    private RelativeLayout mTitleBar;
    private FrameLayout mBack;
    private RecyclerView mIssues;
    private Button mFabiao;
    private int mIssueId = 181;
    private Context mSelf = TimeLineActivity.this;
    private ArrayList<TimeLineInfo> mList = new ArrayList<>();
    private TimeLineAdapter mAdapter;
    private int mGridViewItemWidth;
    private TimeLineActivity mActivity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_line);

        mTitleBar = (RelativeLayout) findViewById(R.id.time_line_titlebar);
        mTitleBar.getLayoutParams().height = Settings.TITLEBAR_HEIGHT;

        mGridViewItemWidth = (Settings.DISPLAY_WIDTH - DensityUtils.dp2px(TimeLineActivity.this, 62)) / 3;

        mBack = (FrameLayout) findViewById(R.id.time_line_back);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimeLineActivity.this.finish();
            }
        });

        mIssues = (RecyclerView) findViewById(R.id.timeline_issues);
        mFabiao = (Button) findViewById(R.id.assign_issue);
        mFabiao.getLayoutParams().height = (int) (Settings.RATIO_HEIGHT * 96);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mIssues.setLayoutManager(mLayoutManager);
        mAdapter = new TimeLineAdapter();
        mIssues.setAdapter(mAdapter);

        mFabiao.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                startActivity(new Intent(TimeLineActivity.this, PublishTopicActivity.class).putExtra("appid", mIssueId + ""));
            }
        });

        int spacePixels = DensityUtils.dp2px(mSelf, 10);
        mIssues.addItemDecoration(new SpaceItemDecoration(spacePixels));

        getIssues();
    }

    private String msg;
    private JSONArray array;

    private void getIssues() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();

                Request request = new Request.Builder()
                        .url(Settings.BASE_URL + "comment/" + mIssueId + "?id=" + mIssueId)
                        .addHeader("CLIENT", "android").addHeader("TOKEN", CommonUtil.getMobileUniqueId(mSelf))
                        .addHeader("authorization", DaoUtil.getAuthorization(mSelf))
                        .get()
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    JSONObject resultObj = JSON.parseObject(response.body().string());
                    if (resultObj.getIntValue("code") == 0) {
                        array = resultObj.getJSONArray("list");
                        int length = array.size();
                        if (length > 0) {
                            mList.clear();

                            for (int i = 0; i < length; i++) {
                                TimeLineInfo info = JSON.toJavaObject(array.getJSONObject(i), TimeLineInfo.class);
                                mList.add(info);
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mAdapter.notifyDataSetChanged();
                                }
                            });
                        }
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


    private class TimeLineAdapter extends RecyclerView.Adapter<TimeLineAdapter.TimeLineViewHolder> {

        @Override
        public TimeLineViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_timeline, parent, false);
            TimeLineViewHolder vh = new TimeLineViewHolder(view);
            return vh;
        }

        @Override
        public void onBindViewHolder(TimeLineViewHolder holder, int position) {
            TimeLineInfo info = mList.get(position);
            if (info.nick != null) {
                holder.mName.setText(info.nick);
            } else {
                holder.mName.setText("");
            }

            if (info.add_time != null) {
                holder.mTime.setText(info.add_time);
            } else {
                holder.mTime.setText("");
            }

            if (info.content != null) {
                holder.mContent.setText(info.content);
            } else {
                holder.mContent.setText("");
            }

            if (info.icon != null) {
                Glide.with(TimeLineActivity.this).load(info.icon).into(holder.mAvatar);
            } else {
                holder.mAvatar.setImageDrawable(null);
            }

            if (info.is_zambia == 0) {
                holder.zan.setImageResource(R.drawable.timeline_weizan);
            } else {
                holder.zan.setImageResource(R.drawable.timeline_yizan);
            }

            holder.zan.setOnClickListener(new ZanCLickListener(position));

            holder.mContainer.removeAllViews();
            if (info.activity != null && info.activity.length > 0) {
                int length = info.activity.length;
                LayoutInflater inflater = getLayoutInflater();

                for (int i = 0; i < length; i++) {
                    View divider = inflater.inflate(R.layout.view_white_divider, null);
                    holder.mContainer.addView(divider);
                    View v = inflater.inflate(R.layout.lin_misson_cpl, null);
                    TextView name = (TextView) v.findViewById(R.id.item_name);
                    name.setText(info.activity[i] != null ? info.activity[i] : "");
                    holder.mContainer.addView(v);
                }
            }

            holder.zanNum.setText(info.zambia + "");
            holder.commentNum.setText(info.count + "");
            if (info.images != null && info.images.length > 0) {
                int length = info.images.length;
                ArrayList<String> imgs = new ArrayList();

                for (int i=0; i<length; i++) {
                    imgs.add(info.images[i]);
                }
                holder.gridview.setAdapter(new GridAdapter(imgs));
                holder.gridview.setVisibility(View.VISIBLE);

            } else {
                holder.gridview.setAdapter(null);
                holder.gridview.setVisibility(View.GONE);
            }

            holder.head.setOnClickListener(new ComemtClickListener(info.id));
            holder.commentLayout.setOnClickListener(new ComemtClickListener(info.id));
            holder.mContent.setOnClickListener(new ComemtClickListener(info.id));
            holder.shareLayout.setOnClickListener(new OnSingleClickListener() {
                @Override
                public void doOnClick(View v) {
                    DialogUtil.showShareDialog(mActivity, mActivity);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mList.size();
        }

        public class TimeLineViewHolder extends RecyclerView.ViewHolder {
            public TextView mName;
            public TextView mTime;
            public CircleImageView mAvatar;
            public TextView mContent;
            public LinearLayout mContainer;
            public TextView zanNum;
            public TextView commentNum;
            public GridView gridview;
            public LinearLayout head;
            public ImageView zan;
            public LinearLayout commentLayout;
            public LinearLayout shareLayout;

            public TimeLineViewHolder(View itemView) {
                super(itemView);

                head = (LinearLayout) itemView.findViewById(R.id.item_tl_headpart);
                mName = (TextView) itemView.findViewById(R.id.item_tl_name);
                mTime = (TextView) itemView.findViewById(R.id.item_tl_time);
                mContent = (TextView) itemView.findViewById(R.id.item_tl_content);
                mAvatar = (CircleImageView) itemView.findViewById(R.id.item_tl_avatar);
                mContainer = (LinearLayout) itemView.findViewById(R.id.item_tl_container);
                zanNum = (TextView) itemView.findViewById(R.id.item_tl_zan_num);
                commentNum = (TextView) itemView.findViewById(R.id.item_tl_comment_num);
                gridview = (GridView) itemView.findViewById(R.id.item_tl_gridview);
                zan = (ImageView) itemView.findViewById(R.id.item_tl_zan);
                commentLayout = (LinearLayout) itemView.findViewById(R.id.item_tl_comment_layout);
                shareLayout = (LinearLayout) itemView.findViewById(R.id.item_tl_share_layout);
            }
        }
    }

    class GridAdapter extends  BaseAdapter {
        ArrayList<String> addrs;

        public GridAdapter(ArrayList<String> imgs) {
            addrs = imgs;
        }

        @Override
        public int getCount() {
            return addrs.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ImageView img = new ImageView(TimeLineActivity.this);
            img.setLayoutParams(new AbsListView.LayoutParams(mGridViewItemWidth, mGridViewItemWidth));
            Glide.with(TimeLineActivity.this).load(addrs.get(i)).into(img);

            return img;
        }
    }

    class ComemtClickListener extends OnSingleClickListener {
        long id;

        public ComemtClickListener(long id){
            this.id = id;
        }

        @Override
        public void doOnClick(View v) {
            startActivity(new Intent(TimeLineActivity.this, OneTopicActivity.class).putExtra("replyid", id));
        }
    }


    int clickPos;
    boolean lock;
    class ZanCLickListener extends  OnSingleClickListener {

        public ZanCLickListener(int position) {
            clickPos = position;
        }

        @Override
        public void doOnClick(View v) {

            if (!lock) {
                lock = true;

                doZan(mList.get(clickPos).id + "");
            }
        }
    }


    private void doZan(final String topicId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();

                FormBody body = new FormBody.Builder()
                        .add("id", topicId)
                        .build();

                Request request = new Request.Builder()
                        .url(Settings.BASE_URL + "topicZambia")
                        .addHeader("CLIENT", "android").addHeader("TOKEN", CommonUtil.getMobileUniqueId(mSelf))
                        .addHeader("authorization", DaoUtil.getAuthorization(mSelf))
                        .post(body)
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    JSONObject resultObj = JSON.parseObject(response.body().string());
                    if (resultObj.getIntValue("code") == 0) {
                        if (resultObj.getIntValue("is_zambia") == 1) {
                            // zan guo
                            mList.get(clickPos).is_zambia = 1;
                        } else {
                            mList.get(clickPos).is_zambia = 0;
                        }

                        mList.get(clickPos).zambia = resultObj.getIntValue("zambia");


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
                                Toast.makeText(mSelf, msg, Toast.LENGTH_SHORT).show();
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
