package com.youtu.acb.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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
import com.youtu.acb.entity.FlowInfo;
import com.youtu.acb.entity.UserInfo;
import com.youtu.acb.util.CommonUtil;
import com.youtu.acb.util.DaoUtil;
import com.youtu.acb.util.DirectListener;
import com.youtu.acb.util.OnSingleClickListener;
import com.youtu.acb.util.StringUtil;

import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 我的余额
 */
public class MyAvailableAmountActivity extends BaseActivity {

    private RelativeLayout mTitleBar;
    private FrameLayout mBack;
    private RecyclerView mRecycler;
    private MyAvailAdapter mAdapter;
    private float mAvailNum;
    private TextView mAvailNumTv;
    private Context mSelf = MyAvailableAmountActivity.this;
    private ArrayList<FlowInfo> mFlows = new ArrayList<>();
    private Button mExchange;
    private RecyclerView.LayoutManager mLayoutManager;
    private boolean hasMore;
    private int mCurrentPage = 1;
    private TextView mNoRecorder;
    Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_available_amount);

        mTitleBar = (RelativeLayout) findViewById(R.id.my_avail_titlebar);
        mTitleBar.getLayoutParams().height = Settings.TITLEBAR_HEIGHT;
        mBack = (FrameLayout) findViewById(R.id.my_avail_back);
        mAvailNumTv = (TextView) findViewById(R.id.my_avail_num);

        mAvailNum = getIntent().getFloatExtra("availnum", 0f);
        mAvailNumTv.setText(StringUtil.FormatFloat(mAvailNum + ""));

        mBack.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                onBackPressed();
            }
        });

        mRecycler = (RecyclerView) findViewById(R.id.my_avail_list);
        mLayoutManager = new LinearLayoutManager(MyAvailableAmountActivity.this);
        mRecycler.setLayoutManager(mLayoutManager);

        mAdapter = new MyAvailAdapter();
        mRecycler.setAdapter(mAdapter);

        mExchange = (Button) findViewById(R.id.my_avail_exchange);

        bundle = new Bundle();
        bundle.putFloat("availnum", mAvailNum);
        mExchange.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                UserInfo info = DaoUtil.getUserInfoFromLocal(mSelf);
                if (info.bank_number > 0) {
                    startActivity(new Intent(mSelf, GetCashActivity.class).putExtras(bundle));
                } else {
                    showAddCardDialog();
                    return;
                }
            }
        });


        mNoRecorder = (TextView) findViewById(R.id.no_recorder);

    }

    @Override
    protected void onResume() {
        super.onResume();
        getAvail();
    }

    class MyAvailAdapter extends RecyclerView.Adapter<MyAvailAdapter.Vh> {

        @Override
        public Vh onCreateViewHolder(ViewGroup parent, int viewType) {
            View item = getLayoutInflater().inflate(R.layout.item_my_gold, null);
            Vh vh = new Vh(item);
            return vh;
        }

        @Override
        public void onBindViewHolder(Vh holder, int position) {
            FlowInfo info = mFlows.get(position);
            if (info.name != null) {
                holder.itemName.setText(info.name);
            } else {
                holder.itemName.setText("");
            }

            String amountStr = StringUtil.FormatFloat(info.amount + "");
            if (info.amount > 0) {
                holder.goldNum.setText("+" + amountStr + "元");
                holder.goldNum.setTextColor(Color.parseColor("#fe9c9a"));
            } else {
                holder.goldNum.setText(amountStr + "元");
                holder.goldNum.setTextColor(Color.parseColor("#71e5a9"));
            }
            if (info.add_time != null) {
                holder.itemTime.setText(info.add_time);
            } else {
                holder.itemTime.setText("");
            }

            if (position == getItemCount() - 1) {
                ((LinearLayout.LayoutParams) holder.divider.getLayoutParams()).setMargins(0, 0, 0, 0);
            } else {
                holder.divider.setVisibility(View.VISIBLE);
            }

            //判断当前列表所在位置，当到最后两项时就加载
            int end = ((LinearLayoutManager) mLayoutManager).findLastVisibleItemPosition();
            if (end > getItemCount() - 3 && end <= getItemCount() - 1) {
                if (hasMore) {
                    mCurrentPage++;
                    getAvail();
                }
            }

        }

        @Override
        public int getItemCount() {
            return mFlows.size();
        }


        class Vh extends RecyclerView.ViewHolder {
            private TextView itemName;
            private TextView goldNum;
            private View divider;
            private TextView itemTime;

            public Vh(View itemView) {
                super(itemView);

                itemName = (TextView) itemView.findViewById(R.id.mg_item_name);
                goldNum = (TextView) itemView.findViewById(R.id.mg_item_num);
                divider = itemView.findViewById(R.id.mg_item_divider);
                itemTime = (TextView) itemView.findViewById(R.id.mg_item_time);
            }
        }

    }

    private String errMsg;

    private void getAvail() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(Settings.BASE_URL + "flow").addHeader("CLIENT", "android")
                        .addHeader("TOKEN", CommonUtil.getMobileUniqueId(mSelf))
                        .addHeader("authorization", DaoUtil.getAuthorization(mSelf))
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    JSONObject result = JSON.parseObject(response.body().string());

                    if (result.getIntValue("code") == 0) {
                        JSONArray array = result.getJSONArray("list");
                        if (array != null && array.size() > 0) {
                            if (mCurrentPage == 1) {
                                mFlows.clear();
                            }
                            int length = array.size();
                            for (int i = 0; i < length; i++) {
                                FlowInfo info = JSON.toJavaObject(array.getJSONObject(i), FlowInfo.class);

                                mFlows.add(info);
                            }

                            // 是否还有更多
                            if (mFlows.size() < result.getIntValue("total")) {
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

    private void showAddCardDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mSelf);
        final Dialog mDialog = builder.create();
        mDialog.show();
        mDialog.setContentView(R.layout.dialog_add_account);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(false);
        mDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        WindowManager.LayoutParams params = mDialog.getWindow().getAttributes();
        params.width = (int) (Settings.RATIO_WIDTH * 560);
        params.height = (int) (Settings.RATIO_WIDTH * 420);
        params.gravity = Gravity.CENTER;
        mDialog.getWindow().setAttributes(params);

        TextView cancel = (TextView) mDialog.findViewById(R.id.dialog_cancel);
        TextView ok = (TextView) mDialog.findViewById(R.id.dialog_ok);
        TextView word = (TextView) mDialog.findViewById(R.id.congratulation_word);

        word.setText("您尚未绑定银行卡，是否前去添加?");

        cancel.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                mDialog.dismiss();
            }
        });

        ok.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                startActivity(new Intent(mSelf, AddBankcardActivity.class));
                mDialog.dismiss();
            }
        });
    }

}
