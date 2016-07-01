package com.youtu.acb.activity;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.youtu.acb.R;
import com.youtu.acb.common.Settings;
import com.youtu.acb.entity.UserInfo;
import com.youtu.acb.interfaces.GetUserInfoFinishedListener;
import com.youtu.acb.util.DaoUtil;
import com.youtu.acb.util.DirectListener;
import com.youtu.acb.util.OnSingleClickListener;
import com.youtu.acb.util.StringUtil;

/**
 * 我的钱包
 */
public class MyWalletActivity extends BaseActivity {

    private RelativeLayout mTitleBar;
    private FrameLayout mBack;
    private Context mSelf = MyWalletActivity.this;
    private TextView mAvailableAmount;
    private TextView mGold;
    private TextView mBond;
    private TextView mWechat;
    private TextView mBankCardNum;
    private LinearLayout mGotoMyGold;
    private LinearLayout mGotoMyAvailable;
    private LinearLayout mGotoMyBond;
    private LinearLayout mGotoWxPay;
    private LinearLayout mGotoCards;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_wallet);

        mTitleBar = (RelativeLayout) findViewById(R.id.my_wallet_titlebar);
        mTitleBar.getLayoutParams().height = Settings.TITLEBAR_HEIGHT;
        mBack = (FrameLayout) findViewById(R.id.my_wallet_back);

        mBack.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                onBackPressed();
            }
        });

        mAvailableAmount = (TextView) findViewById(R.id.my_wallet_available_amount);
        mGold = (TextView) findViewById(R.id.my_wallet_gold);
        mBond = (TextView) findViewById(R.id.my_wallet_bond);
        mWechat = (TextView) findViewById(R.id.my_wallet_wechat);
        mBankCardNum = (TextView) findViewById(R.id.my_wallet_bankcard_num);
        mGotoMyGold = (LinearLayout) findViewById(R.id.go_to_mygold);
        mGotoMyAvailable = (LinearLayout) findViewById(R.id.go_to_my_avail);
        mGotoMyBond = (LinearLayout) findViewById(R.id.go_to_my_bond);
        mGotoWxPay = (LinearLayout) findViewById(R.id.go_to_wx_pay);
        mGotoCards = (LinearLayout) findViewById(R.id.go_to_my_cards);

        mGotoWxPay.setOnClickListener(new DirectListener(mSelf, WxPayActivity.class));
        mGotoCards.setOnClickListener(new DirectListener(mSelf, BankCardListActivity.class));

    }

    @Override
    protected void onResume() {
        super.onResume();

        showUserInfo();

        DaoUtil.getUserInfoFromServer(MyWalletActivity.this, new GetUserInfoFinishedListener() {
            @Override
            public void doFinish(boolean success, String msg) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showUserInfo();
                    }
                });
            }
        });
    }

    private void showUserInfo() {
        UserInfo info = DaoUtil.getUserInfoFromLocal(mSelf);

        fillInfo(info);
    }

    private void fillInfo(UserInfo info) {
        if (!TextUtils.isEmpty(info.wechat)) {
            mWechat.setText(info.wechat);
        } else {
            mWechat.setText("");
        }

        mGold.setText(info.gold + "");

        mBond.setText(StringUtil.FormatFloat(info.bond + ""));

        mAvailableAmount.setText(StringUtil.FormatFloat(info.available_amount + ""));

        mBankCardNum.setText(info.bank_number + "张");


        Bundle bundle = new Bundle();
        bundle.putInt("goldnum", info.gold);
        mGotoMyGold.setOnClickListener(new DirectListener(MyWalletActivity.this, MyGoldActivity.class, bundle));


        Bundle bundle1 = new Bundle();
        bundle1.putFloat("availnum", info.available_amount);
        mGotoMyAvailable.setOnClickListener(new DirectListener(MyWalletActivity.this, MyAvailableAmountActivity.class, bundle1));


        Bundle bundle2 = new Bundle();
        bundle2.putFloat("bondnum", info.bond);
        mGotoMyBond.setOnClickListener(new DirectListener(MyWalletActivity.this, MyBondActivity.class, bundle2));
    }
}
