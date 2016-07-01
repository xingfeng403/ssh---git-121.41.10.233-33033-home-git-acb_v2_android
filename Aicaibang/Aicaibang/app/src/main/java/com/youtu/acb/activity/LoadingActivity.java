package com.youtu.acb.activity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.umeng.analytics.MobclickAgent;
import com.youtu.acb.R;
import com.youtu.acb.common.Common;
import com.youtu.acb.common.Settings;
import com.youtu.acb.database.AcbDatabaseHelper;
import com.youtu.acb.util.CommonUtil;
import com.youtu.acb.util.OkHttpUtils;
import com.youtu.acb.util.OnSingleClickListener;


import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by xingf on 16/5/26.
 */
public class LoadingActivity extends BaseActivity {

    private Context mSelf = LoadingActivity.this;
    private String msg = null;
    private SQLiteDatabase db;
    private String mImgUrl; // ad's url
    private ImageView mAd;
    private String secret_key = "5201314";
    private Button mJump; // 跳过广告
    private Timer mTimer;
    int currentSecs = 3; // 3，2，1

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MobclickAgent.UMAnalyticsConfig config = new MobclickAgent.UMAnalyticsConfig(this, "57198477e0f55a7b3c0012d7", CommonUtil.getChannelCode(LoadingActivity.this));
        MobclickAgent.startWithConfigure(config);

        setContentView(R.layout.activity_loading);

        mAd = (ImageView) findViewById(R.id.loading_img_ad);
        mJump = (Button) findViewById(R.id.jump_ad);

        mAd.getLayoutParams().height = (Settings.DISPLAY_HEIGHT - Settings.STATUS_BAR_HEIGHT) * 5 / 7;

        mJump.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                if (mTimer != null) {
                    mTimer.cancel();
                    mTimer = null;
                }
                startActivity(new Intent(mSelf, MainActivity.class));
            }
        });


        SQLiteDatabase.loadLibs(mSelf);
        AcbDatabaseHelper dbHelper = new AcbDatabaseHelper(mSelf, "acb.db", null, 1);
        db = dbHelper.getWritableDatabase(secret_key);


        Cursor cursorCount = db.rawQuery("SELECT COUNT(img) FROM Ads", null);
        int count = cursorCount.getCount();
        cursorCount.close();


        Random ra =new Random();
        int index = ra.nextInt(count); // 0 ... count-1

        int i=0;
        Cursor cursor = db.query("Ads", null, null, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {

                if (i == index) {
                    mImgUrl = cursor.getString(cursor.getColumnIndex("img"));

                    if (!TextUtils.isEmpty(mImgUrl)) {

                        Glide.with(LoadingActivity.this).load(mImgUrl).into(mAd);

                    }

                    break;
                }

                i++;
            }
        }
        cursor.close();


        new Thread(new Runnable() {
            @Override
            public void run() {
                String result = OkHttpUtils.get("startad", mSelf);


                try {
                    JSONObject jsonObject = (JSONObject) JSON.parse(result);
                    msg = jsonObject.getString("msg");

                    if (jsonObject.getIntValue("code") == 0) {
                        JSONArray array = jsonObject.getJSONArray("list");

                        int length = array.size();
                        for (int i=0; i< length; i++) {
                            JSONObject obj = array.getJSONObject(i);
                            ContentValues values = new ContentValues();
                            values.put("id", obj.getIntValue("id"));
                            values.put("link", obj.getString("link"));
                            values.put("start_time", obj.getString("start_time"));
                            values.put("end_time", obj.getString("end_time"));
                            values.put("img", obj.getString("img"));
                            db.insert("Ads", null, values);
                        }
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mSelf, msg, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                } catch (JSONException e) {
                    if (TextUtils.isEmpty(msg)) {

                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mSelf, msg, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }
        }).start();

        mTimer =  new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                startActivity(new Intent(mSelf, MainActivity.class));
            }
        }, 3000);

        new Thread(new Runnable() {
            @Override
            public void run() {
                for (;currentSecs > 0;currentSecs--) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mJump.setText("跳过 " + currentSecs);
                        }
                    });

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }).start();


        getConfig();
    }

    private void getConfig() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String result = OkHttpUtils.get("config", mSelf);


                try {
                    JSONObject jsonObject = (JSONObject) JSON.parse(result);

                    if (jsonObject.getIntValue("code") == 0) {
                        SharedPreferences sp = LoadingActivity.this.getSharedPreferences(Common.configName, MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("withdrawals_msg", jsonObject.getString("withdrawals_msg")).commit();
                    } else {
                    }

                } catch (JSONException e) {
                }
            }
        }).start();
    }

}
