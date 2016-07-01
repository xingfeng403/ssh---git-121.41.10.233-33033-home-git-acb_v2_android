package com.youtu.acb.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.youtu.acb.R;
import com.youtu.acb.Views.SpaceItemDecoration;
import com.youtu.acb.Views.Titlebar;
import com.youtu.acb.common.Settings;
import com.youtu.acb.entity.AccountBookInfo;
import com.youtu.acb.entity.AccountInfo;
import com.youtu.acb.util.CommonUtil;
import com.youtu.acb.util.DaoUtil;
import com.youtu.acb.util.OnSingleClickListener;

import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 账本
 */
public class AccountBookActivity extends BaseActivity {

    private Titlebar mTitleBar;
    private RecyclerView mRecycler;
    private Button mNote;
    private AccountBookAdapter mAdapter;
    private int mCurrentPage = 1;
    private Context mSelf = AccountBookActivity.this;
    private boolean hasMore;
    private ArrayList<AccountBookInfo> mInfos = new ArrayList<>();
    private TextView mNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_book);

        mTitleBar = (Titlebar) findViewById(R.id.account_book_titlebar);
        mTitleBar.getmLeftPart().setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                onBackPressed();
            }
        });

        mTitleBar.getmRightTv().setText("历史明细");
        mTitleBar.getmRightPart().setVisibility(View.VISIBLE);
        mTitleBar.getLayoutParams().height = Settings.TITLEBAR_HEIGHT;
        mTitleBar.setTitle("账本");

        mRecycler = (RecyclerView) findViewById(R.id.account_book_recycler);
        mNote = (Button) findViewById(R.id.account_book_note);
        mNum = (TextView) findViewById(R.id.account_book_num);

        mRecycler.setLayoutManager(new LinearLayoutManager(AccountBookActivity.this));
        mAdapter = new AccountBookAdapter();
        mRecycler.setAdapter(mAdapter);

        int divider20 = (int) (Settings.RATIO_HEIGHT * 20);
        mRecycler.addItemDecoration(new SpaceItemDecoration(divider20));

        getAccountBook();

    }

    class AccountBookAdapter extends RecyclerView.Adapter<AccountBookAdapter.AccountBookVh> {

        @Override
        public AccountBookAdapter.AccountBookVh onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = getLayoutInflater().inflate(R.layout.item_account_book, null);
            return new AccountBookVh(v);
        }

        @Override
        public void onBindViewHolder(AccountBookAdapter.AccountBookVh holder, int position) {
            AccountBookInfo info = mInfos.get(position);

            if (info.title != null) {
                holder.name.setText(info.title);
            } else {
                holder.name.setText("");
            }

            if (info.name != null) {
                holder.plat.setText(info.name);
            } else {
                holder.plat.setText("");
            }

            holder.num.setText(info.amount + "");

            if (info.repay_name != null) {
                holder.repay.setText(info.repay_name);
            } else {
                holder.repay.setText("");
            }

            if (info.invest_date != null) {
                holder.sdate.setText("开始日期 " + info.invest_date);
            } else {
                holder.sdate.setText("");
            }

            if (info.expire_date != null) {
                holder.ddate.setText("结束日期 " + info.expire_date);
            } else {
                holder.ddate.setText("");
            }
            if (info.type == 2) {
                holder.type.setText("编辑");
            } else {
                holder.type.setText("查看详情");
            }
            if (info.is_edit == 0) {
                holder.icon.setImageResource(R.drawable.arrow_right_grey);
            } else {
                holder.icon.setImageResource(R.drawable.a_book_edit);
            }
        }

        @Override
        public int getItemCount() {
            return mInfos.size();
        }

        class AccountBookVh extends RecyclerView.ViewHolder {

            TextView name;
            TextView type;
            ImageView icon;
            TextView plat;
            TextView num;
            TextView repay;
            TextView sdate;
            TextView ddate;

            public AccountBookVh(View itemView) {
                super(itemView);

                name = (TextView) itemView.findViewById(R.id.item_ab_name);
                type = (TextView) itemView.findViewById(R.id.item_ab_type);
                plat = (TextView) itemView.findViewById(R.id.item_ab_plat);
                num = (TextView) itemView.findViewById(R.id.item_ab_num);
                repay = (TextView) itemView.findViewById(R.id.item_ab_repay_type);
                sdate = (TextView) itemView.findViewById(R.id.item_ab_start_date);
                ddate = (TextView) itemView.findViewById(R.id.item_ab_dead_date);
                icon = (ImageView) itemView.findViewById(R.id.item_ab_icon);

            }
        }
    }

    private String errMsg;
    private int amount;

    private void getAccountBook() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(Settings.BASE_URL + "collection?page=" + mCurrentPage).addHeader("CLIENT", "android")
                        .addHeader("TOKEN", CommonUtil.getMobileUniqueId(mSelf))
                        .addHeader("authorization", DaoUtil.getAuthorization(mSelf))
                        .get()
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    JSONObject result = JSON.parseObject(response.body().string());

                    if (result.getIntValue("code") == 0) {

                        amount = result.getIntValue("amount");
                        JSONArray array = result.getJSONArray("list");
                        if (array != null && array.size() > 0) {
                            if (mCurrentPage == 1) {
                                mInfos.clear();
                            }
                            int length = array.size();
                            for (int i = 0; i < length; i++) {
                                AccountBookInfo info = JSON.toJavaObject(array.getJSONObject(i), AccountBookInfo.class);

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
                                mNum.setText(amount + "");
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
}
