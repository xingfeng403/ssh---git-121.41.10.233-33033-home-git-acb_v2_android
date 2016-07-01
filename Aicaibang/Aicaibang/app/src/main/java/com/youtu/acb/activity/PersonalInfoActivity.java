package com.youtu.acb.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.yalantis.ucrop.UCrop;
import com.youtu.acb.AcbApplication;
import com.youtu.acb.R;
import com.youtu.acb.Views.CircleImageView;
import com.youtu.acb.Views.Titlebar;
import com.youtu.acb.common.Constants;
import com.youtu.acb.common.Settings;
import com.youtu.acb.entity.UserInfo;
import com.youtu.acb.util.BitmapUtils;
import com.youtu.acb.util.CommonUtil;
import com.youtu.acb.util.DaoUtil;
import com.youtu.acb.util.DirectListener;
import com.youtu.acb.util.OkHttpCallback;
import com.youtu.acb.util.OnSingleClickListener;
import com.youtu.acb.util.ToastUtil;
import com.youtu.acb.util.UpdatePhoto;

import org.json.JSONException;

import java.io.File;
import java.util.UUID;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 个人信息
 */
public class PersonalInfoActivity extends BaseActivity {
    private Titlebar mTitleBar;
    private LinearLayout mAvatarLin;
    private LinearLayout mNickLin;
    private LinearLayout mAccountLin;
    private LinearLayout mAccountTypeLin;
    private LinearLayout mLoginPwdLin;
    private LinearLayout mRealNameLin;
    private TextView mLogoutTv;
    private CircleImageView mAvatar;
    private TextView mNick;
    private TextView mAcc;
    private TextView mAccType;
    private TextView mRealname;
    // 图像加载类
    private UpdatePhoto mUpdatePhoto;
    private static final int CAMERA_REQUEST_CODE = 1001;
    private static final int ALBUM_REQUEST_CODE = 1002;
    private static final int CROP_REQUEST_CODE = 1004;
    // 临时图片名
    private static final String PHOTO_TEMP_FILE = "Image.jpg";
    // 图片源文件 缩略图
    private File mOriginalFile, mThumbnailFile, mCorpFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_info);

        // 初始化从相册相机选择图片类
        mUpdatePhoto = new UpdatePhoto(this);

        mTitleBar = (Titlebar) findViewById(R.id.pinfo_titlebar);
        mTitleBar.getLayoutParams().height = Settings.TITLEBAR_HEIGHT;
        mTitleBar.setTitle(getString(R.string.pinfo_title));
        mTitleBar.getmLeftPart().setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                onBackPressed();
            }
        });

        mAvatarLin = (LinearLayout) findViewById(R.id.avatar_lin);
        mNickLin = (LinearLayout) findViewById(R.id.nick_lin);
        mAccountLin = (LinearLayout) findViewById(R.id.account_lin);
        mAccountTypeLin = (LinearLayout) findViewById(R.id.account_type_lin);
        mRealNameLin = (LinearLayout) findViewById(R.id.real_name_lin);
        mLoginPwdLin = (LinearLayout) findViewById(R.id.login_pwd_lin);
        mLogoutTv = (TextView) findViewById(R.id.logout_tv);

        mAvatar = (CircleImageView) findViewById(R.id.pinfo_avatar);
        mNick = (TextView) findViewById(R.id.pinfo_nick);
        mAcc = (TextView) findViewById(R.id.pinfo_acc);
        mAccType = (TextView) findViewById(R.id.pinfo_acc_type);
        mRealname = (TextView) findViewById(R.id.pinfo_realname);

        int height100 = (int) (Settings.RATIO_HEIGHT * 100);
        mAvatarLin.getLayoutParams().height = (int) (Settings.RATIO_HEIGHT * 175);
        mNickLin.getLayoutParams().height = height100;
        mAccountLin.getLayoutParams().height = height100;
        mAccountTypeLin.getLayoutParams().height = height100;
        mLogoutTv.getLayoutParams().height = height100;

        mLoginPwdLin.getLayoutParams().height = height100 * 4 / 5;
        mRealNameLin.getLayoutParams().height = height100 * 4 / 5;

        mNickLin.setOnClickListener(new DirectListener(PersonalInfoActivity.this, ModiNickActivity.class));

        mLogoutTv.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                doLogout();
            }
        });

        mAvatarLin.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                mUpdatePhoto.getDialog();
            }
        });

        mLoginPwdLin.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                startActivity(new Intent(PersonalInfoActivity.this, ModiLoginPwdActivity.class));
            }
        });
    }

    private void doLogout() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Request request = new Request.Builder().url(Settings.BASE_URL + "logout").addHeader("CLIENT", "android").addHeader("TOKEN", CommonUtil.getMobileUniqueId(PersonalInfoActivity.this)).addHeader("authorization", DaoUtil.getAuthorization(PersonalInfoActivity.this)).build();
                Response response = null;
                try {
                    response = new OkHttpClient().newCall(request).execute();
                    JSONObject reObj = JSON.parseObject(response.body().string());
                    if (reObj.getIntValue("code") == 0) {
                        DaoUtil.clearUserinfo(PersonalInfoActivity.this);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(PersonalInfoActivity.this, "退出登录成功", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(PersonalInfoActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                PersonalInfoActivity.this.finish();
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
    protected void onResume() {
        super.onResume();

        UserInfo info = DaoUtil.getUserInfoFromLocal(PersonalInfoActivity.this);

        if (!TextUtils.isEmpty(info.icon)) {
            Glide.with(PersonalInfoActivity.this).load(info.icon).into(mAvatar);
        } else {
            mAvatar.setImageDrawable(null);
        }
        if (!TextUtils.isEmpty(info.nick)) {
            mNick.setText(info.nick);
        } else {
            mNick.setText("");
        }
        if (!TextUtils.isEmpty(info.phone)) {
            mAcc.setText(info.phone);
        } else {
            mAcc.setText("");
        }
        if (info.account_type == 0) {
            mAccType.setText("普通账号");
        }
        if (!TextUtils.isEmpty(info.realname)) {
            mRealname.setText(info.realname);
            mRealNameLin.setOnClickListener(null);
        } else {
            mRealname.setText("");
            mRealNameLin.setOnClickListener(new DirectListener(PersonalInfoActivity.this, RealNameVerifyActivity.class));
        }

    }

    private Uri mDestinationUri;
    private int mAspectRatioX = 1, mAspectRatioY = 1;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 相机返回1001
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            mOriginalFile = new File(Settings.TEMP_PATH, Constants.PICTURE_TMPURL);
            String uuid = UUID.randomUUID().toString();
            Constants.PICTURE_TMPURL = uuid + PHOTO_TEMP_FILE;
            mThumbnailFile = new File(Settings.TEMP_PATH, Constants.PICTURE_TMPURL);
            if (data != null && data.hasExtra("data")) {
                Bitmap photo = data.getParcelableExtra("data");
                mUpdatePhoto.saveBitmapToFile(photo, mOriginalFile);
                Uri oUri = Uri.fromFile(mOriginalFile);
                mDestinationUri = Uri.fromFile(mThumbnailFile);
                UCrop.of(mDestinationUri, mDestinationUri)
                        .withAspectRatio(mAspectRatioX, mAspectRatioY)
                        .withMaxResultSize(200, 200)
                        .start(PersonalInfoActivity.this);
            } else {
                if (mOriginalFile.exists()) {
                    Uri oUri = Uri.fromFile(mOriginalFile);
                    mDestinationUri = Uri.fromFile(mThumbnailFile);
                    UCrop.of(oUri, mDestinationUri)
                            .withAspectRatio(mAspectRatioX, mAspectRatioY)
                            .withMaxResultSize(200, 200)
                            .start(PersonalInfoActivity.this);
                }
            }

        }

        // 相册返回1002
        if (requestCode == ALBUM_REQUEST_CODE && resultCode == RESULT_OK) {
            String uuid = UUID.randomUUID().toString();
            Uri uri = data.getData();
            Constants.PICTURE_TMPURL = uuid + PHOTO_TEMP_FILE;
            mThumbnailFile = new File(Settings.TEMP_PATH, Constants.PICTURE_TMPURL);
            mDestinationUri = Uri.fromFile(mThumbnailFile);
            UCrop.of(uri, mDestinationUri)
                    .withAspectRatio(mAspectRatioX, mAspectRatioY)
                    .withMaxResultSize(200, 200)
                    .start(PersonalInfoActivity.this);
        }
        // 返回裁剪
        if (requestCode == CROP_REQUEST_CODE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                Bitmap photo = data.getParcelableExtra("data");
                String uuid = UUID.randomUUID().toString();
                mCorpFile = new File(Settings.TEMP_PATH, uuid + PHOTO_TEMP_FILE);
                mUpdatePhoto.saveBitmapToFile(photo, mCorpFile);
                mAvatar.setImageBitmap(photo);

                uploadIcon(BitmapUtils.bitmapToBase64(photo));
            }
        }

        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            final Uri resultUri = UCrop.getOutput(data);
            Bitmap b = BitmapUtils.getBitmapFromFile(mThumbnailFile, 480, 360, false);
            mAvatar.setImageBitmap(b);

            uploadIcon(BitmapUtils.bitmapToBase64(b));
        } else if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
        }

    }

    /**
     * 上传头像
     */
    String newIcon;
    private void uploadIcon(String base64str) {
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("type", "icon");
        builder.add("icon", base64str);
        builder.build();
        Request request = new Request.Builder().url(Settings.USER_URL).addHeader("ACCEPT", "*/*").addHeader("Authorization", DaoUtil.getAuthorization(PersonalInfoActivity.this))
                .addHeader("CLIENT", "android").addHeader("TOKEN", CommonUtil.getMobileUniqueId(PersonalInfoActivity.this)).put(builder.build()).build();
        new OkHttpClient().newCall(request).enqueue(new OkHttpCallback(PersonalInfoActivity.this) {
            @Override
            protected void onError(org.json.JSONObject result) {

            }

            @Override
            protected void onSuccess(org.json.JSONObject result) {
                try {
                    newIcon = result.getString("icon");
                    AcbApplication.getmUserInfo().icon = newIcon;
                    String userInfo = JSON.toJSONString(JSON.toJSON(AcbApplication.getmUserInfo()));
                    DaoUtil.saveUserInfo(userInfo, PersonalInfoActivity.this);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Glide.with(PersonalInfoActivity.this).load(newIcon).into(mAvatar);
                            ToastUtil.show(PersonalInfoActivity.this, "上传头像成功");
                        }
                    });
                } catch (JSONException e) {
                }
            }

            @Override
            protected void onFinish() {

            }
        });
    }
}
