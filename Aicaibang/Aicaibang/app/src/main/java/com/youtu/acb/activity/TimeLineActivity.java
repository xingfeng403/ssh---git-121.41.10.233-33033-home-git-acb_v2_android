package com.youtu.acb.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
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
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
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

import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;
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
    private String mIssueId;
    private Context mSelf = TimeLineActivity.this;
    private ArrayList<TimeLineInfo> mList = new ArrayList<>();
    private TimeLineAdapter mAdapter;
    private int mGridViewItemWidth;
    private TimeLineActivity mActivity = this;
    private TextView noRecord;
    private PtrFrameLayout ptrFrame;
    private int mCurrentPage = 1;
    private long mStartTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_line);

        mIssueId = getIntent().getStringExtra("merid");

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

        noRecord = (TextView) findViewById(R.id.no_recorder);
        ptrFrame = (PtrFrameLayout) findViewById(R.id.timeline_ptr_frame);

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



        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER);
        TextView tv = new TextView(this);
        tv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tv.setText("Refreshing...");
        header.addView(tv);
        ptrFrame.setHeaderView(header);

        ptrFrame.setPtrHandler(new PtrHandler() {
            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                mStartTime = System.currentTimeMillis();
                mCurrentPage = 1;
                getIssues();
            }

            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return PtrDefaultHandler.checkContentCanBePulledDown(frame, content, header);
            }

        });

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

                                    if (mList.size() > 0) {
                                        noRecord.setVisibility(View.INVISIBLE);
                                    } else {
                                        noRecord.setVisibility(View.VISIBLE);
                                    }
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

                    long currentTime = System.currentTimeMillis();
                    mStartTime = currentTime - mStartTime;

                    if (mStartTime < 2000) {
                        try {
                            Thread.sleep(2000 - mStartTime);
                        } catch (InterruptedException e) {
                        }
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (ptrFrame.isRefreshing())
                                ptrFrame.refreshComplete();
                        }
                    });
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

            holder.head.setOnClickListener(new ComemtClickListener(position));
            holder.commentLayout.setOnClickListener(new ComemtClickListener(position));
            holder.mContent.setOnClickListener(new ComemtClickListener(position));
            holder.shareLayout.setOnClickListener(new OnSingleClickListener() {
                @Override
                public void doOnClick(View v) {
                    DialogUtil.showShareDialog(mActivity, mActivity);
                }
            });

            holder.zanLayout.setOnClickListener(new ZanCLickListener(position));
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
            public LinearLayout zanLayout;

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
                zanLayout = (LinearLayout) itemView.findViewById(R.id.item_tl_zan_layout);
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
        int position;

        public ComemtClickListener(int position){
            this.position = position;
        }

        @Override
        public void doOnClick(View v) {
            TimeLineInfo info = mList.get(position);
            mApplication.mInfo = info;
            startActivity(new Intent(TimeLineActivity.this, OneTopicActivity.class).putExtra("replyid", info.id));
        }
    }


    int clickPos;
    class ZanCLickListener extends  OnSingleClickListener {

        public ZanCLickListener(int position) {
            clickPos = position;
        }

        @Override
        public void doOnClick(View v) {
                doZan(mList.get(clickPos).id + "");
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

            }
        }).start();
    }


}
