package com.youtu.acb.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.youtu.acb.R;
import com.youtu.acb.common.Settings;
import com.youtu.acb.entity.BankCardInfo;
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
 * 银行卡列表
 */
public class BankCardListActivity extends BaseActivity {
    private RelativeLayout mTitleBar;
    private FrameLayout mBack;
    private RecyclerView mRecycler;
    private BankCardAdapter mAdapter;
    private Context mSelf = BankCardListActivity.this;
    private ArrayList<BankCardInfo> mCards = new ArrayList<>();
    private TextView mCommon;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank_card_list);

        mTitleBar = (RelativeLayout) findViewById(R.id.my_card_titlebar);
        mTitleBar.getLayoutParams().height = Settings.TITLEBAR_HEIGHT;
        mBack = (FrameLayout) findViewById(R.id.my_card_back);

        mBack.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                onBackPressed();
            }
        });

        mRecycler = (RecyclerView) findViewById(R.id.my_card_list);
        mRecycler.setLayoutManager(new LinearLayoutManager(BankCardListActivity.this));

        mCommon = (TextView) findViewById(R.id.my_card_common_ques);
        mCommon.setOnClickListener(new DirectListener(mSelf, CommonQuesActivity.class));

//        DividerLine dividerLine = new DividerLine(DividerLine.VERTICAL);
//        dividerLine.setSize((int)(Settings.RATIO_WIDTH * 40));
//        dividerLine.setColor(0xFF0000);
//        mRecycler.addItemDecoration(dividerLine);

        mAdapter = new BankCardAdapter();
        mRecycler.setAdapter(mAdapter);

    }

    class BankCardAdapter extends RecyclerView.Adapter {
        final int TYPE_FOOTER = 1;
        final int TYPE_ITEM = 2;

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == TYPE_FOOTER) {
                View v = getLayoutInflater().inflate(R.layout.add_bankcard, parent, false);
                return new Footer(v);
            } else {
                View v = getLayoutInflater().inflate(R.layout.item_bankcard, parent, false);
                return new Vh(v);
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (position == getItemCount() - 1) {
                Footer foot = (Footer) holder;

                foot.add.setOnClickListener(new AddCardListener());
            } else {
                BankCardInfo info = mCards.get(position);

                Vh vh = (Vh) holder;

                if (info.name != null) {
                    vh.bankname.setText(info.name);
                } else {
                    vh.bankname.setText("");
                }

                if (info.category != null) {
                    vh.cardType.setText(info.category);
                } else {
                    vh.cardType.setText("");
                }

                if (info.account != null) {
                    vh.cardNumber.setText(StringUtil.formatBankCard(info.account));
                } else {
                    vh.cardNumber.setText("");
                }

                Glide.with(BankCardListActivity.this).load(info.icon).into(vh.icon);

            }
        }

        @Override
        public int getItemCount() {
            return mCards.size() + 1;
        }

        class Vh extends RecyclerView.ViewHolder {
            private ImageView icon;
            private TextView bankname;
            private TextView cardType;
            private TextView cardNumber;

            public Vh(View itemView) {
                super(itemView);

                icon = (ImageView) itemView.findViewById(R.id.bank_icon);
                bankname = (TextView) itemView.findViewById(R.id.bank_name);
                cardType = (TextView) itemView.findViewById(R.id.bank_card_type);
                cardNumber = (TextView) itemView.findViewById(R.id.bank_card_number);
            }
        }

        class Footer extends RecyclerView.ViewHolder {

            private Button add;

            public Footer(View itemView) {
                super(itemView);

                add = (Button) itemView;
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == mCards.size())
                return TYPE_FOOTER;

            return TYPE_ITEM;
        }

    }

    private String errMsg;

    private void getCards() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(Settings.BASE_URL + "bankcard").addHeader("CLIENT", "android")
                        .addHeader("TOKEN", CommonUtil.getMobileUniqueId(mSelf))
                        .addHeader("authorization", DaoUtil.getAuthorization(mSelf))
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    JSONObject result = JSON.parseObject(response.body().string());

                    if (result.getIntValue("code") == 0) {
                        JSONArray array = result.getJSONArray("list");
                        if (array != null && array.size() > 0) {
                            mCards.clear();
                            int length = array.size();
                            for (int i = 0; i < length; i++) {
                                BankCardInfo info = JSON.toJavaObject(array.getJSONObject(i), BankCardInfo.class);

                                mCards.add(info);
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mAdapter.notifyDataSetChanged();
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
                }
            }
        }).start();
    }

    Boolean hasRealName = false;

    @Override
    protected void onResume() {
        super.onResume();

        UserInfo info = DaoUtil.getUserInfoFromLocal(mSelf);
        if (!TextUtils.isEmpty(info.realname)) {
            hasRealName = true;
        } else {
            hasRealName = false;
        }
        getCards();
    }

    private void showBindDialog() {
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

        cancel.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                mDialog.dismiss();
            }
        });

        ok.setOnClickListener(new DirectListener(mSelf, RealNameVerifyActivity.class));
    }

    public class AddCardListener extends OnSingleClickListener {

        @Override
        public void doOnClick(View v) {
            if (hasRealName) {
                mSelf.startActivity(new Intent(mSelf, AddBankcardActivity.class));
            } else {
                showBindDialog();
            }
        }
    }
}
