package com.youtu.acb.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
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
import com.youtu.acb.common.Settings;
import com.youtu.acb.entity.ReplyDetail;
import com.youtu.acb.entity.ReplyInfo;
import com.youtu.acb.entity.TimeLineInfo;
import com.youtu.acb.entity.UserInfo;
import com.youtu.acb.util.CommonUtil;
import com.youtu.acb.util.DaoUtil;
import com.youtu.acb.util.DensityUtils;
import com.youtu.acb.util.DialogUtil;
import com.youtu.acb.util.OnSingleClickListener;
import com.youtu.acb.util.ToastUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 单一话题 内容和评论的显示 以及回复
 */
public class OneTopicActivity extends BaseActivity {

    private RelativeLayout mTitleBar;  // 标题栏
    private FrameLayout mBack;        //返回上级
    private RecyclerView mRecycler;  // 列表
    private long mReplyId;          // 话题ID
    private Context mSelf = OneTopicActivity.this;
    private Replyadapter mAdapter;   // Adapter
    private ArrayList<ReplyInfo> mList = new ArrayList();  // 缓存列表
    private LinearLayout mInputLayout; // 输入框
    private final int INPUT_TYPE_REPLY = 1;
    private final int INPUT_TYPE_REPLY_TO_REPLY = 2;
    private int mReplyType;
    private String mCommentId;  // 单条评论ID
    private String mToUserId;
    private String mReplyToReplyId; // 单条回复ID
    private TextView mCancelInput;
    private TextView mSendInput;
    private EditText mContent;
    private String mContentStr;
    private String mToNick; // 回复回复时 对方的昵称
    private int mReplyPos; // 回复的位置
    private int mGridViewItemWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_topic);
        mTitleBar = (RelativeLayout) findViewById(R.id.one_topic_titlebar);
        mTitleBar.getLayoutParams().height = Settings.TITLEBAR_HEIGHT;

        mReplyId = getIntent().getLongExtra("replyid", -1);

        mGridViewItemWidth = (Settings.DISPLAY_WIDTH - DensityUtils.dp2px(OneTopicActivity.this, 62)) / 3;

        mBack = (FrameLayout) findViewById(R.id.one_topic_back);
        mBack.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                onBackPressed();
            }
        });

        mRecycler = (RecyclerView) findViewById(R.id.one_topic_comments);
        mRecycler.setLayoutManager(new LinearLayoutManager(OneTopicActivity.this));

        mAdapter = new Replyadapter();
        mRecycler.setAdapter(mAdapter);

        mInputLayout = (LinearLayout) findViewById(R.id.one_topic_input_layout);
        mInputLayout.setVisibility(View.GONE);
        mCancelInput = (TextView) findViewById(R.id.one_topic_input_cancel);
        mSendInput = (TextView) findViewById(R.id.one_topic_input_send);
        mContent = (EditText) findViewById(R.id.one_topic_input);

        mCancelInput.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                mInputLayout.setVisibility(View.GONE);
                mContent.setText("");
            }
        });

        mSendInput.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                mContentStr = mContent.getText().toString();
                if (mContentStr.length() == 0) {
                    ToastUtil.show(mSelf, "请说点什么");
                    return;
                }
                switch (mReplyType) {
                    case INPUT_TYPE_REPLY:
                        doReply();
                        break;
                    case INPUT_TYPE_REPLY_TO_REPLY:
                        doReplyToReply();
                        break;
                    default:
                        break;
                }
            }
        });

        getComments();
    }


    private String msg;
    private JSONArray array;

    /**
     * 获取当前话题的评论
     */
    private void getComments() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();

                Request request = new Request.Builder()
                        .url(Settings.BASE_URL + "reply/" + mReplyId + "?id=" + mReplyId)
                        .addHeader("CLIENT", "android").addHeader("TOKEN", CommonUtil.getMobileUniqueId(mSelf))
                        .addHeader("authorization", DaoUtil.getAuthorization(mSelf))
                        .get()
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    JSONObject resultObj = JSON.parseObject(response.body().string());
                    if (resultObj.getIntValue("code") == 0) {
                        // 下方评论列表
                        array = resultObj.getJSONArray("list");
                        int length = array.size();
                        if (length > 0) {
                            mList.clear();

                            for (int i = 0; i < length; i++) {
                                ReplyInfo info = JSON.toJavaObject(array.getJSONObject(i), ReplyInfo.class);
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


    private String detailMsg; // 服务器返回 message
    private JSONArray detailArray; // 回复数组

    /**
     * 获取更多评论
     *
     * @param cid
     * @param page
     * @param position
     */
    private void getMoreComments(final long cid, final int page, final int position) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();

                Request request = new Request.Builder()
                        .url(Settings.BASE_URL + "replydetail/" + cid + "?id=" + cid + "&page=" + page + "&page_size=10")
                        .addHeader("CLIENT", "android").addHeader("TOKEN", CommonUtil.getMobileUniqueId(mSelf))
                        .addHeader("authorization", DaoUtil.getAuthorization(mSelf))
                        .get()
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    JSONObject resultObj = JSON.parseObject(response.body().string());
                    if (resultObj.getIntValue("code") == 0) {
                        detailArray = resultObj.getJSONArray("list");
                        int length = detailArray.size();
                        ArrayList<ReplyDetail> copy = new ArrayList<ReplyDetail>();
                        List<ReplyDetail> list = Arrays.asList(mList.get(position).detail);
                        copy.addAll(list);
                        if (length > 0 && list != null) {

                            for (int i = 0; i < length; i++) {
                                ReplyDetail info = JSON.toJavaObject(detailArray.getJSONObject(i), ReplyDetail.class);
                                copy.add(info);
                            }

                            ReplyDetail[] target = new ReplyDetail[copy.size()];
                            mList.get(position).detail = copy.toArray(target);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                    } else {
                        detailMsg = resultObj.getString("msg");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mSelf, detailMsg, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    /**
     * 评论列表 Adapter
     */
    class Replyadapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int TYPE_HEADER = 0;
        private static final int TYPE_ITEM = 1;

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            if (viewType == TYPE_ITEM) {
                View view = getLayoutInflater().inflate(R.layout.item_comment_one_pic, null);
                ReplyViewHolder vh = new ReplyViewHolder(view);
                return vh;
            } else if (viewType == TYPE_HEADER) {
                View view = getLayoutInflater().inflate(R.layout.one_topic_recycler_header, null);
                return new VHHeader(view);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            if (holder instanceof ReplyViewHolder) {
                ReplyInfo info = mList.get(position - 1);

                ReplyViewHolder holder1 = (ReplyViewHolder) holder;

                if (info.nick != null) {
                    holder1.name.setText(info.nick);
                } else {
                    holder1.name.setText("");
                }

                if (info.add_time != null) {
                    holder1.time.setText(info.add_time);
                } else {
                    holder1.time.setText("");
                }

                if (info.content != null) {
                    holder1.content.setText(info.content);
                } else {
                    holder1.content.setText("");
                }

                if (info.icon != null) {
                    Glide.with(OneTopicActivity.this).load(info.icon).into(holder1.icon);
                } else {
                    holder1.icon.setImageDrawable(null);
                }

                holder1.container.removeAllViews();
                if (info.detail != null && info.detail.length > 0) {
                    int length = info.detail.length;
                    for (int i = 0; i < length; i++) {
                        ReplyDetail detail = info.detail[i];

                        if (detail.content != null) {
                            TextView tv = new TextView(OneTopicActivity.this);
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            if (i == length - 1) {
                                params.setMargins(0, 20, 0, 20);
                            } else {
                                params.setMargins(0, 20, 0, 0);
                            }

                            tv.setLayoutParams(params);
                            if (detail.to_user_id <= 0) {
                                tv.setText(detail.content);
                            } else {
                                tv.setText(detail.nick + " 回复 " + detail.to_nick + ":" + detail.content);
                            }


                            tv.setOnClickListener(new ReplyToReplyClickListener(info.id + "", detail.to_user_id + "", detail.id + "", detail.nick, position - 1));
                            holder1.container.addView(tv);
                        }

                    }


                    if (info.total > length) {
                        holder1.loadMore.setText("查看更多(还有" + (info.total - length) + "条)");
                        holder1.loadMore.setVisibility(View.VISIBLE);

                        holder1.loadMore.setOnClickListener(new LoadMoreClickListener(info.id, getPage(position - 1), position - 1));


                    } else {
                        holder1.loadMore.setVisibility(View.GONE);
                    }

                } else {
                    if (holder1.loadMore.getVisibility() == View.VISIBLE) {
                        holder1.loadMore.setVisibility(View.GONE);
                    }
                }

                holder1.content.setOnClickListener(new ReplyClickListener(info.id + ""));
            } else if (holder instanceof VHHeader) {
                VHHeader oneHeader = (VHHeader) holder;
                if (mApplication.mInfo != null) {
                    TimeLineInfo info = mApplication.mInfo;

                    if (info.nick != null) {
                        oneHeader.mName.setText(info.nick);
                    } else {
                        oneHeader.mName.setText("");
                    }

                    if (info.add_time != null) {
                        oneHeader.mTime.setText(info.add_time);
                    } else {
                        oneHeader.mTime.setText("");
                    }

                    if (info.content != null) {
                        oneHeader.mContent.setText(info.content);
                    } else {
                        oneHeader.mContent.setText("");
                    }

                    if (info.icon != null) {
                        Glide.with(OneTopicActivity.this).load(info.icon).into(oneHeader.mAvatar);
                    } else {
                        oneHeader.mAvatar.setImageDrawable(null);
                    }

                    if (info.is_zambia == 0) {
                        oneHeader.zan.setImageResource(R.drawable.timeline_weizan);
                    } else {
                        oneHeader.zan.setImageResource(R.drawable.timeline_yizan);
                    }

                    oneHeader.mContainer.removeAllViews();
                    if (info.activity != null && info.activity.length > 0) {
                        int length = info.activity.length;
                        LayoutInflater inflater = getLayoutInflater();

                        for (int i = 0; i < length; i++) {
                            View divider = inflater.inflate(R.layout.view_white_divider, null);
                            oneHeader.mContainer.addView(divider);
                            View v = inflater.inflate(R.layout.lin_misson_cpl, null);
                            TextView name = (TextView) v.findViewById(R.id.item_name);
                            name.setText(info.activity[i] != null ? info.activity[i] : "");
                            oneHeader.mContainer.addView(v);
                        }
                    }

                    oneHeader.zanNum.setText(info.zambia + "");
                    oneHeader.commentNum.setText(info.count + "");
                    if (info.images != null && info.images.length > 0) {
                        int length = info.images.length;
                        ArrayList<String> imgs = new ArrayList();

                        for (int i=0; i<length; i++) {
                            imgs.add(info.images[i]);
                        }
                        oneHeader.gridview.setAdapter(new GridAdapter(imgs));
                        oneHeader.gridview.setVisibility(View.VISIBLE);

                    } else {
                        oneHeader.gridview.setAdapter(null);
                        oneHeader.gridview.setVisibility(View.GONE);
                    }

//                    oneHeader.head.setOnClickListener(new ComemtClickListener(position));
//                    oneHeader.commentLayout.setOnClickListener(new ComemtClickListener(position));
//                    oneHeader.mContent.setOnClickListener(new ComemtClickListener(position));
                    oneHeader.shareLayout.setOnClickListener(new OnSingleClickListener() {
                        @Override
                        public void doOnClick(View v) {
                            // 举报
                        }
                    });

                    oneHeader.zanLayout.setOnClickListener(new ZanCLickListener(position));
                }


            }

        }

        class GridAdapter extends BaseAdapter {
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
                ImageView img = new ImageView(OneTopicActivity.this);
                img.setLayoutParams(new AbsListView.LayoutParams(mGridViewItemWidth, mGridViewItemWidth));
                Glide.with(OneTopicActivity.this).load(addrs.get(i)).into(img);

                return img;
            }
        }


        private int getPage(int pos) {
            ReplyInfo info = mList.get(pos);
            if (info == null || info.detail == null) {
                return 0;
            }

            int length = info.detail.length;
            if (length <= 3) {
                return 1;
            } else {
                return (length - 3) / 10 + 1;

            }
        }

        @Override
        public int getItemCount() {
            return mList.size() + 1;
        }

        public class ReplyViewHolder extends RecyclerView.ViewHolder {
            public TextView name;
            public TextView time;
            public TextView content;
            public CircleImageView icon;
            public LinearLayout container;
            public Button loadMore;

            public ReplyViewHolder(View itemView) {
                super(itemView);
                name = (TextView) itemView.findViewById(R.id.item_cop_name);
                time = (TextView) itemView.findViewById(R.id.item_cop_time);
                content = (TextView) itemView.findViewById(R.id.item_cop_content);
                icon = (CircleImageView) itemView.findViewById(R.id.item_cop_avatar);
                container = (LinearLayout) itemView.findViewById(R.id.item_cop_container);
                loadMore = (Button) itemView.findViewById(R.id.item_cop_load_more);
            }
        }

        class VHHeader extends RecyclerView.ViewHolder {
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

            public VHHeader(View itemView) {
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

        class ReplyClickListener extends OnSingleClickListener {

            public ReplyClickListener(String replyid) {
                mCommentId = replyid;
            }

            @Override
            public void doOnClick(View v) {
                mInputLayout.setVisibility(View.VISIBLE);
                mReplyType = INPUT_TYPE_REPLY;
                mContent.setHint("");
            }
        }

        class ReplyToReplyClickListener extends OnSingleClickListener {

            public ReplyToReplyClickListener(String replyId, String toUserId, String replyToReplyId, String nick, int position) {
                mCommentId = replyId;
                mToUserId = toUserId;
                mReplyToReplyId = replyToReplyId;
                mToNick = nick;
                mReplyPos = position;
            }

            @Override
            public void doOnClick(View v) {
                mInputLayout.setVisibility(View.VISIBLE);
                mReplyType = INPUT_TYPE_REPLY_TO_REPLY;
                mContent.setHint("回复" + mToNick + ":");
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
//                                // zan guo
//                                mList.get(clickPos).is_zambia = 1;
                            } else {
//                                mList.get(clickPos).is_zambia = 0;
                            }

//                            mList.get(clickPos).zambia = resultObj.getIntValue("zambia");


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

        /**
         * 判断当前item类型 是否是头部
         *
         * @param position
         * @return
         */
        @Override
        public int getItemViewType(int position) {
            if (isPositionHeader(position))
                return TYPE_HEADER;

            return TYPE_ITEM;
        }

        /**
         * 判断是否位于列表头部
         *
         * @param position
         * @return
         */
        private boolean isPositionHeader(int position) {
            return position == 0;
        }


        /**
         * 加在更多 点击事件
         */
        public class LoadMoreClickListener extends OnSingleClickListener {
            long id;
            int page;
            int position;

            public LoadMoreClickListener(long id, int page, int position) {
                this.id = id;
                this.page = page;
                this.position = position;
            }

            @Override
            public void doOnClick(View v) {
                getMoreComments(id, page, position);
            }
        }
    }


    private void doReply() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();

                FormBody formBody = new FormBody.Builder()
                        .add("id", mCommentId)
                        .add("content", mContentStr)
                        .build();

                Request request = new Request.Builder()
                        .url(Settings.BASE_URL + "reply")
                        .addHeader("CLIENT", "android").addHeader("TOKEN", CommonUtil.getMobileUniqueId(mSelf))
                        .addHeader("authorization", DaoUtil.getAuthorization(mSelf))
                        .post(formBody)
                        .build();


                try {
                    Response response = client.newCall(request).execute();
                    JSONObject resultObj = JSON.parseObject(response.body().string());

                    if (resultObj.getIntValue("code") == 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mSelf, "回复成功", Toast.LENGTH_SHORT).show();
                                mContent.setText("");
                                mInputLayout.setVisibility(View.GONE);
                            }
                        });
                    }

                } catch (Exception e) {

                }
            }
        }).start();
    }


    private void doReplyToReply() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();

                FormBody formBody = new FormBody.Builder()
                        .add("id", mCommentId)
                        .add("content", mContentStr)
                        .add("to_user_id", mToUserId)
                        .add("reply_id", mReplyToReplyId)
                        .build();

                Request request = new Request.Builder()
                        .url(Settings.BASE_URL + "replytoreply")
                        .addHeader("CLIENT", "android").addHeader("TOKEN", CommonUtil.getMobileUniqueId(mSelf))
                        .addHeader("authorization", DaoUtil.getAuthorization(mSelf))
                        .post(formBody)
                        .build();


                try {
                    Response response = client.newCall(request).execute();
                    JSONObject resultObj = JSON.parseObject(response.body().string());

                    if (resultObj.getIntValue("code") == 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ReplyInfo info = mList.get(mReplyPos);
                                ReplyDetail[] oldArray = info.detail;
                                int length = oldArray.length;
                                ReplyDetail[] newArray = new ReplyDetail[length + 1];
                                for (int i=0; i<length; i++) {
                                    newArray[i] = oldArray[i];
                                }
                                ReplyDetail rd = new ReplyDetail();
                                UserInfo uInfo = DaoUtil.getUserInfoFromLocal(OneTopicActivity.this);
                                rd.nick = TextUtils.isEmpty(uInfo.nick) ? uInfo.phone : uInfo.nick;
                                rd.to_nick = mToNick;
                                rd.content = mContentStr;
                                rd.to_user_id = Long.parseLong(mToUserId);
                                rd.id = Long.parseLong(mCommentId);
                                rd.user_id = uInfo.id;
                                newArray[length] = rd;
                                mList.get(mReplyPos).detail = newArray;
                                mAdapter.notifyDataSetChanged();

                                Toast.makeText(mSelf, "回复回复成功", Toast.LENGTH_SHORT).show();
                                mContent.setText("");
                                mInputLayout.setVisibility(View.GONE);
                            }
                        });
                    }

                } catch (Exception e) {

                }
            }
        }).start();
    }


}
