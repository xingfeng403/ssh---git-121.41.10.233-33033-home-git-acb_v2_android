package com.youtu.acb.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.youtu.acb.Views.CircleImageView;
import com.youtu.acb.Views.ImageCycle.ImageCycleView;
import com.youtu.acb.Views.LabelsLayout;
import com.youtu.acb.Views.RoundCornerImageView;
import com.youtu.acb.Views.SpaceItemDecoration;
import com.youtu.acb.Views.TextViewAd;
import com.youtu.acb.common.Common;
import com.youtu.acb.common.Settings;

import net.simonvt.menudrawer.MenuDrawer;

import com.youtu.acb.R;
import com.youtu.acb.entity.ActivityInfo;
import com.youtu.acb.entity.AdEntity;
import com.youtu.acb.entity.BannerInfo;
import com.youtu.acb.entity.MainNewsInfo;
import com.youtu.acb.entity.PlatFormInfo;
import com.youtu.acb.entity.SignInfo;
import com.youtu.acb.entity.UserInfo;
import com.youtu.acb.util.CommonUtil;
import com.youtu.acb.util.DaoUtil;
import com.youtu.acb.util.DensityUtils;
import com.youtu.acb.util.DirectListener;
import com.youtu.acb.util.OnSingleClickListener;
import com.youtu.acb.util.ToastUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 首页
 */
public class MainActivity extends BaseActivity {

    private MenuDrawer menuDrawer;  // menu
    private LinearLayout mPinfo;    // personal info
    private LinearLayout mSet;      // set
    private boolean mIsLogined;      // true: logined
    private TextView mMenuLoginType; // show if not logined
    private TextView mMenuName;      // user's nickname
    private TextView mMenuPhone;      // User's phone num
    private CircleImageView mAvatar;  // avatar
    private LinearLayout mMenuHelp;
    private RelativeLayout mTitleBar; // title bar
    private FrameLayout mDrawMenu;    // show menu
    private TextViewAd mAd; // ad for texts
    private RecyclerView mRecyclerView;
    private LinearLayout mMenuWallet;
    private LinearLayout mMenuActs;
    private LinearLayout mMenuFriends;
    private LinearLayout mMenuRecommend;
    private LinearLayout mMenuSign;
    private LinearLayout mMenuAccountBook;
    private ArrayList<SignInfo> mInfos = new ArrayList<>();
    private FrameLayout mMsgLayout;
    private View msgBall, zixunBall, infoBall;
    private MyAdapter mAdapter;
    private ImageCycleView mViewPager;
    private LinearLayout mHeader;
    private RecyclerView.LayoutManager mLayoutManager;
    private FrameLayout mZixun;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        menuDrawer = MenuDrawer.attach(this);
        menuDrawer.setContentView(R.layout.activity_main);
        menuDrawer.setMenuView(R.layout.view_menu);

        menuDrawer.setMenuSize(Settings.DISPLAY_WIDTH * 655 / 750);

        setContentView(menuDrawer);

        mHeader = (LinearLayout) getLayoutInflater().inflate(R.layout.header_main_list, null);
        mViewPager = (ImageCycleView) mHeader.findViewById(R.id.main_image_cycle);
        mPinfo = (LinearLayout) findViewById(R.id.menu_pinfo);
        mSet = (LinearLayout) findViewById(R.id.menu_set_lin);
        mMenuLoginType = (TextView) findViewById(R.id.menu_login_type);
        mMenuName = (TextView) findViewById(R.id.menu_name);
        mMenuPhone = (TextView) findViewById(R.id.menu_phone_num);
        mAvatar = (CircleImageView) findViewById(R.id.menu_avatar);
        mMenuHelp = (LinearLayout) findViewById(R.id.menu_help_center);
        mTitleBar = (RelativeLayout) findViewById(R.id.main_titlebar);
        mDrawMenu = (FrameLayout) findViewById(R.id.main_content_menu);
        mAd = (TextViewAd) mHeader.findViewById(R.id.main_tv_ad);
        mMenuWallet = (LinearLayout) findViewById(R.id.menu_my_wallet);
        mMenuActs = (LinearLayout) findViewById(R.id.menu_activitys);
        mMenuFriends = (LinearLayout) findViewById(R.id.menu_friends_layout);
        mMenuRecommend = (LinearLayout) findViewById(R.id.menu_recommend_layout);
        mMenuSign = (LinearLayout) findViewById(R.id.menu_sign_layout);
        mMenuAccountBook = (LinearLayout) findViewById(R.id.menu_my_account_book);
        mMsgLayout = (FrameLayout) findViewById(R.id.main_content_msg);
        msgBall = findViewById(R.id.msg_ball);
        zixunBall = findViewById(R.id.zixun_ball);
        infoBall = findViewById(R.id.menu_ball);
        mZixun = (FrameLayout) findViewById(R.id.main_content_url);

        mZixun.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                startActivity(new Intent(MainActivity.this, WebActivity.class).putExtra("title", "资讯").putExtra("url", "https://caijing.gongshidai.com/active/tlist"));
            }
        });

        mTitleBar.getLayoutParams().height = Settings.TITLEBAR_HEIGHT;

        mMenuFriends.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                if (mIsLogined) {
                    startActivity(new Intent(MainActivity.this, MyFriendsActivity.class));
                } else {
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                }
            }
        });

        mPinfo.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                if (mIsLogined) {
                    startActivity(new Intent(MainActivity.this, PersonalInfoActivity.class));
                } else {
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                }
            }
        });

        mSet.setOnClickListener(new DirectListener(MainActivity.this, SetActivity.class));


        mMenuHelp.setOnClickListener(new DirectListener(MainActivity.this, HelpCenterActivity.class));

        mDrawMenu.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                menuDrawer.toggleMenu(true);
            }
        });

        mMenuWallet.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                if (mIsLogined) {
                    startActivity(new Intent(MainActivity.this, MyWalletActivity.class));
                } else {
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                }
            }
        });

        mMenuActs.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                if (mIsLogined) {
                    startActivity(new Intent(MainActivity.this, EventsActivity.class));
                } else {
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                }
            }
        });

        mMenuFriends.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                if (mIsLogined) {
                    startActivity(new Intent(MainActivity.this, MyFriendsActivity.class));
                } else {
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                }
            }
        });

        mMenuRecommend.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                if (mIsLogined) {
                    startActivity(new Intent(MainActivity.this, RecommendAwardActivity.class));
                } else {
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                }
            }
        });

        mMenuSign.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                if (mIsLogined) {
                    startActivity(new Intent(MainActivity.this, SignDetailActivity.class));
                } else {
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                }
            }
        });

        mMenuAccountBook.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                if (mIsLogined) {
                    startActivity(new Intent(MainActivity.this, AccountBookActivity.class));
                } else {
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                }
            }
        });

        mMsgLayout.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                startActivity(new Intent(MainActivity.this, MsgActivity.class));
            }
        });


        List data = new ArrayList();
        AdEntity entity = new AdEntity("lala1", "lala2", "http://www.baidu.com");
        AdEntity entity1 = new AdEntity("lala2", "lala3", "http://www.baidu.com");
        AdEntity entity2 = new AdEntity("lala3", "lala1", "http://www.baidu.com");
        data.add(entity);
        data.add(entity1);
        data.add(entity2);
        mAd.setmTexts(data);

        mAd.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                ToastUtil.show(MainActivity.this, mAd.getContent());
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.main_content_recycler);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // 添加item 间距
        int itemSpace = DensityUtils.dp2px(MainActivity.this, 9);
        SpaceItemDecoration decoration = new SpaceItemDecoration(itemSpace);
        mRecyclerView.addItemDecoration(decoration);

        //创建并设置Adapter
        String[] datas = new String[2];
        datas[0] = "0";
        datas[1] = "1";
        mAdapter = new MyAdapter();
        mRecyclerView.setAdapter(mAdapter);

        getSignData();

        getMainTop();

        getPlatForm();

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (DaoUtil.isLogined(MainActivity.this)) {
            mIsLogined = true;
            ShowUserInfo();
        } else {
            mIsLogined = false;
        }
    }


    /**
     * init list saved banners
     */
    private void initBanners() {
        int length = mBanners.size();
        if (length == 0) {
            return;
        }

        mViewPager.setIndicators(new int[]{R.drawable.selected_indicator_white, R.drawable.unselected_indicator_white});
        mViewPager.setImageResources(mBanners, new ImageCycleView.ImageCycleViewListener() {
            @Override
            public void displayImage(String imageURL, ImageView imageView) {
                Glide.with(MainActivity.this).load(imageURL).into(imageView);
            }

            @Override
            public void onImageClick(BannerInfo info, int postion, View imageView) {
                // 判断登录情况
//                if (!DaoUtil.isLogined(mActivity)) {
//                    gotoLoginPage();
//                    return;
//                }

                if (info.link != null) {
                    if (info.type.equals("app")) {
                    } else if (info.type.equals("web")) {
                        if (info.link != null && !info.link.equals("#")) {
                            startActivity(new Intent(MainActivity.this, WebActivity.class).putExtra("title", true).putExtra("title", info.title).putExtra("url", info.link));
                        }
                    } else if (info.type.equals("invite")) {
//                        startActivity(new Intent(MainActivity.this, MyInvitationActivity.class));
                    }
                }
            }
        });
    }

    private void ShowUserInfo() {
        UserInfo info = DaoUtil.getUserInfoFromLocal(MainActivity.this);

        mMenuLoginType.setVisibility(View.INVISIBLE);

        if (!TextUtils.isEmpty(info.nick)) {
            mMenuName.setText(info.nick);
        } else {
        }
        if (!TextUtils.isEmpty(info.phone)) {
            mMenuPhone.setText(info.phone);
        } else {
        }
        if (!TextUtils.isEmpty(info.icon)) {
            Glide.with(MainActivity.this).load(info.icon).into(mAvatar);
        } else {
            mAvatar.setImageDrawable(null);
        }
    }

    public class MyAdapter extends RecyclerView.Adapter {
        private static final int TYPE_HEADER = 0;
        private static final int TYPE_ITEM = 1;

        //创建新View，被LayoutManager所调用
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            if (viewType == TYPE_ITEM) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_main_content, viewGroup, false);
                ViewHolder vh = new ViewHolder(view);
                return vh;
            } else if (viewType == TYPE_HEADER) {
                return new VHHeader(mHeader);
            }
            return null;
        }

        //将数据与界面进行绑定的操作
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
            if (viewHolder instanceof ViewHolder) {
                ViewHolder viewHolder1 = (ViewHolder) viewHolder;
                position = position - 1;
                PlatFormInfo info = mPlatforms.get(position);
                viewHolder1.mTextView.setText(info.name);
                viewHolder1.mTitlePart.getLayoutParams().height = (int) (Settings.RATIO_HEIGHT * 85);
                Glide.with(MainActivity.this).load(info.icon).into(viewHolder1.mImage);
                viewHolder1.root.setOnClickListener(new ItemClick(info.id));


                ActivityInfo[] infos = info.activity;
                boolean isFirstItem = true;
                if (infos != null) {
                    viewHolder1.mCPart.removeAllViewsInLayout();
                    for (ActivityInfo temp : infos) {
                        if (isFirstItem) {
                            isFirstItem = false;
                        } else {
                            View line = new View(MainActivity.this);
                            line.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 4));
                            line.setBackgroundColor(Color.parseColor("#f5f6f8"));

                            viewHolder1.mCPart.addView(line);
                        }

                        View view = getLayoutInflater().inflate(R.layout.item_acts, null);

                        TextView name = (TextView) view.findViewById(R.id.item_task_name);
                        TextView rewardName = (TextView) view.findViewById(R.id.item_act_reward);
                        TextView rewardNum = (TextView) view.findViewById(R.id.item_reward_num);
                        LinearLayout layout = (LinearLayout) view.findViewById(R.id.item_labels_layout);

                        if (temp.label != null && temp.label.length > 0) {
                            LabelsLayout labels = new LabelsLayout(MainActivity.this, temp.label, 20);

                            labels.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            layout.addView(labels);
                        }

                        name.setText(temp.title == null ? "" : temp.title);

                        SpannableString ss = new SpannableString(temp.amount + temp.unit_type);
                        ss.setSpan(new AbsoluteSizeSpan(12, true), ss.length() - 2, ss.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                        rewardNum.setText(ss);

                        view.setOnClickListener(new ActClickListener(temp.id + "", temp.type + ""));
                        viewHolder1.mCPart.addView(view);

                    }
                }
//            viewHolder.mCPart.setOnClickListener(new ActClickListener(info.activity[0].id + "", info.activity[0].type + ""));
//            Bundle bundle = new Bundle();
//            bundle.putString("appid", "lalal");
//            view.setOnClickListener(new DirectListener(MainActivity.this, ActDetailActivity.class, bundle));


                //判断当前列表所在位置，当到最后两项时就加载
                int end = ((LinearLayoutManager) mLayoutManager).findLastVisibleItemPosition();
                if (end > getItemCount() - 3 && end <= getItemCount() - 1) {
                    if (hasMore) {
                        mCurrentPage++;
                        getPlatForm();
                    }
                }
            } else {

            }

        }

        //获取数据的数量
        @Override
        public int getItemCount() {
            return mPlatforms.size() + 1;
        }

        //自定义的ViewHolder，持有每个Item的的所有界面元素
        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView mTextView;
            public LinearLayout mTitlePart;
            public LinearLayout mCPart;
            public RoundCornerImageView mImage;
            public LinearLayout root;

            public ViewHolder(View view) {
                super(view);
                mTextView = (TextView) view.findViewById(R.id.main_content_name);
                mTitlePart = (LinearLayout) view.findViewById(R.id.item_main_content_title);
                mCPart = (LinearLayout) view.findViewById(R.id.main_content_acts);
                mImage = (RoundCornerImageView) view.findViewById(R.id.main_content_icon);
                root = (LinearLayout) view.findViewById(R.id.mc_root_view);
            }
        }


        class VHHeader extends RecyclerView.ViewHolder {

            public VHHeader(View itemView) {
                super(itemView);
            }
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

        public class ActClickListener extends OnSingleClickListener {
            private String actid, acttype;

            public ActClickListener(String actid, String acttype) {
                this.actid = actid;
                this.acttype = acttype;
            }

            @Override
            public void doOnClick(View v) {
                startActivity(new Intent(MainActivity.this, ActDetailActivity.class).putExtra("actid", actid).putExtra("acttype", acttype));
            }
        }

        public class ItemClick extends OnSingleClickListener {
            int sid;

            public ItemClick(int id) {
                this.sid = id;
            }


            @Override
            public void doOnClick(View v) {
                MainActivity.this.startActivity(new Intent(MainActivity.this, StoreDetailActivity.class).putExtra("storeid", sid + ""));
            }
        }
    }

    /**
     * 第一下退出时间
     */
    long mExitTime;

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - mExitTime > 2000) {
            mExitTime = System.currentTimeMillis();
            Toast.makeText(this, "再按一下，退出APP", Toast.LENGTH_SHORT).show();
        } else {
            mApplication.clearActivityList();
            this.finish();
        }
    }

    /**
     * 获取签到数据
     */
    String msg;

    private void getSignData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(Settings.BASE_URL + "signConfig")
                        .addHeader("TOKEN", CommonUtil.getMobileUniqueId(MainActivity.this))
                        .addHeader("authorization", DaoUtil.getAuthorization(MainActivity.this))
                        .addHeader("CLIENT", "android")
                        .get()
                        .build();

                try {
                    Response response = client.newCall(request).execute();
                    JSONObject resultObj = JSON.parseObject(response.body().string());


                    if (resultObj.getIntValue("code") == 0) {

                        JSONArray array = resultObj.getJSONArray("list");
                        int length = array.size();
                        mInfos.clear();
                        for (int i = 0; i < length; i++) {
                            SignInfo info = JSON.toJavaObject(array.getJSONObject(i), SignInfo.class);

                            mInfos.add(info);
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (isShown()) {
                                } else {
                                    if (DaoUtil.isLogined(MainActivity.this)) {
                                        showQiandao();
                                    }
                                }

                            }
                        });

                    } else {
                        msg = resultObj.getString("msg");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    private boolean isShown() {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        String dateStr = "y" + year + "m" + month + "d" + day;

        String signDate = getSharedPreferences(Common.sharedPrefName, MODE_PRIVATE).getString("signdate", "");
        if (TextUtils.isEmpty(signDate)) {
            saveSignDate(dateStr);
            return false;
        } else {
            if (signDate.equals(dateStr)) {
                return true;
            } else {
                saveSignDate(dateStr);
                return false;
            }
        }
    }

    private void saveSignDate(String dateStr) {
        getSharedPreferences(Common.sharedPrefName, MODE_PRIVATE).edit().putString("signdate", dateStr).commit();
    }

    /**
     * 签到
     */
    boolean hasFirst;
    Dialog mDialog;
    Button gotoSign;

    private void showQiandao() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        mDialog = builder.create();
        mDialog.show();
        mDialog.setContentView(R.layout.dialog_sign);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(false);
        mDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        WindowManager.LayoutParams params = mDialog.getWindow().getAttributes();
        params.width = (int) (Settings.RATIO_WIDTH * 600);
        params.height = (int) (Settings.RATIO_WIDTH * 825);
        params.gravity = Gravity.CENTER;
        mDialog.getWindow().setAttributes(params);


        ImageView cancel = (ImageView) mDialog.findViewById(R.id.dialog_cancel);
        cancel.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                mDialog.dismiss();
            }
        });
        LinearLayout container = (LinearLayout) mDialog.findViewById(R.id.container);
        gotoSign = (Button) mDialog.findViewById(R.id.go_to_sign);

        gotoSign.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                doSign();
            }
        });

        int listSize = mInfos.size();
        hasFirst = false;
        for (int i = 0; i < listSize; i++) {
            SignInfo info = mInfos.get(i);
            View temp;
            if (info.sign == 0) {
                // 未签到
                if (hasFirst) {
                    temp = mDialog.getLayoutInflater().inflate(R.layout.item_to_sign, null);

                    temp.setLayoutParams(new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                    TextView name = (TextView) temp.findViewById(R.id.item_name);
                    TextView content = (TextView) temp.findViewById(R.id.item_content);
                    ImageView sel = (ImageView) temp.findViewById(R.id.item_sel);

                    name.setText(info.name == null ? "" : info.name);
                    content.setText(info.title == null ? "" : info.title);
                    sel.setVisibility(View.INVISIBLE);

                } else {
                    hasFirst = true;


                    temp = mDialog.getLayoutInflater().inflate(R.layout.item_sign, null);

                    temp.setLayoutParams(new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                    TextView name = (TextView) temp.findViewById(R.id.item_name);
                    TextView content = (TextView) temp.findViewById(R.id.item_content);
                    ImageView sel = (ImageView) temp.findViewById(R.id.item_sel);

                    name.setText(info.name == null ? "" : info.name);
                    content.setText(info.title == null ? "" : info.title);

                    sel.setVisibility(View.INVISIBLE);
                }

            } else {

                temp = mDialog.getLayoutInflater().inflate(R.layout.item_signed, null);

                temp.setLayoutParams(new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                TextView name = (TextView) temp.findViewById(R.id.item_name);
                TextView content = (TextView) temp.findViewById(R.id.item_content);
                ImageView sel = (ImageView) temp.findViewById(R.id.item_sel);

                name.setText(info.name == null ? "" : info.name);
                content.setText(info.title == null ? "" : info.title);

            }

            container.addView(temp);
        }

    }


    private String topMsg;
    private ArrayList<BannerInfo> mBanners = new ArrayList<>();
    private ArrayList<MainNewsInfo> mMainNews = new ArrayList<>();
    private JSONObject resultObj;

    private void getMainTop() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(Settings.BASE_URL + "indexTop")
                        .addHeader("TOKEN", CommonUtil.getMobileUniqueId(MainActivity.this))
                        .addHeader("authorization", DaoUtil.getAuthorization(MainActivity.this))
                        .addHeader("CLIENT", "android")
                        .get()
                        .build();

                try {
                    Response response = client.newCall(request).execute();
                    resultObj = JSON.parseObject(response.body().string());

                    if (resultObj.getIntValue("code") == 0) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (resultObj.getIntValue("info") == 1) {
                                    infoBall.setVisibility(View.VISIBLE);
                                } else {
                                    infoBall.setVisibility(View.INVISIBLE);
                                }

                                if (resultObj.getIntValue("news") == 1) {
                                    msgBall.setVisibility(View.VISIBLE);
                                } else {
                                    msgBall.setVisibility(View.INVISIBLE);
                                }

//                                if (resultObj.getIntValue("zixun") == 1) {
//                                    zixunBall.setVisibility(View.VISIBLE);
//                                } else {
//                                    zixunBall.setVisibility(View.INVISIBLE);
//                                }
                            }
                        });

                        JSONArray array = resultObj.getJSONArray("bannerList");
                        JSONArray array2 = resultObj.getJSONArray("newsList");
                        if (array != null) {
                            int length = array.size();
                            mBanners.clear();
                            for (int i = 0; i < length; i++) {
                                BannerInfo info = JSON.toJavaObject(array.getJSONObject(i), BannerInfo.class);

                                mBanners.add(info);
                            }
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                initBanners();
                            }
                        });


                        if (array2 != null) {
                            int length2 = array2.size();
                            mMainNews.clear();
                            for (int i = 0; i < length2; i++) {
                                MainNewsInfo info = JSON.toJavaObject(array.getJSONObject(i), MainNewsInfo.class);

                                mMainNews.add(info);
                            }
                        }
                    } else {
                        topMsg = resultObj.getString("msg");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, topMsg, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        }).start();
    }


    private String platMsg;
    private int mCurrentPage = 1;
    private boolean hasMore;
    private ArrayList<PlatFormInfo> mPlatforms = new ArrayList<>();

    private void getPlatForm() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(Settings.BASE_URL + "platform?page=" + mCurrentPage)
                        .addHeader("TOKEN", CommonUtil.getMobileUniqueId(MainActivity.this))
                        .addHeader("authorization", DaoUtil.getAuthorization(MainActivity.this))
                        .addHeader("CLIENT", "android")
                        .get()
                        .build();

                try {
                    Response response = client.newCall(request).execute();
                    JSONObject resultObj = JSON.parseObject(response.body().string());

                    if (resultObj.getIntValue("code") == 0) {
                        JSONArray array = resultObj.getJSONArray("list");
                        int length = array.size();
                        if (mCurrentPage == 1) {
                            mPlatforms.clear();
                        }
                        for (int i = 0; i < length; i++) {
                            PlatFormInfo info = JSON.toJavaObject(array.getJSONObject(i), PlatFormInfo.class);
                            mPlatforms.add(info);
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mAdapter.notifyDataSetChanged();
                            }
                        });

                    } else {
                        platMsg = resultObj.getString("msg");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (platMsg == null)
                                    return;
                                Toast.makeText(MainActivity.this, platMsg, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    if (resultObj.getIntValue("total") > mPlatforms.size()) {
                        hasMore = true;
                    } else {
                        hasMore = false;
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        }).start();
    }


    private String errMsg;

    private void doSign() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(Settings.BASE_URL + "sign").addHeader("CLIENT", "android")
                        .addHeader("TOKEN", CommonUtil.getMobileUniqueId(MainActivity.this))
                        .addHeader("authorization", DaoUtil.getAuthorization(MainActivity.this))
                        .post(new FormBody.Builder().build())
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    JSONObject result = JSON.parseObject(response.body().string());

                    if (result.getIntValue("code") == 0) {
                        // 签到成功
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "签到成功", Toast.LENGTH_SHORT).show();
                                if (mDialog != null && mDialog.isShowing()) {
                                    mDialog.dismiss();
                                }

                                gotoSign.setEnabled(false);

                            }
                        });
                    } else {
                        errMsg = result.getString("msg");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, errMsg, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDialog != null) {
            if (mDialog.isShowing())
                mDialog.dismiss();
            mDialog = null;
        }
    }
}
