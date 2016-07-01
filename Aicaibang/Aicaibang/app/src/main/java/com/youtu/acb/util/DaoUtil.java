package com.youtu.acb.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.widget.Toast;


import com.alibaba.fastjson.JSON;
import com.youtu.acb.AcbApplication;
import com.youtu.acb.common.Common;
import com.youtu.acb.common.Settings;
import com.youtu.acb.entity.UserInfo;
import com.youtu.acb.interfaces.GetUserInfoFinishedListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * save and get user data or tag reserved local
 * helper class
 *
 * Created by xingf on 15/12/25.
 */
public class DaoUtil {
    static UserInfo userInfo;

    /**
     * 从服务器获取用户信息(不带回调)
     *
     * @param client
     * @param tokenid
     * @param context
     */
    public static UserInfo getUserInfoFromServer(OkHttpClient client, String tokenid, final Context context) {
        Request request = new Request.Builder().url(Settings.USER_URL + "/" + tokenid).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String result = response.body().string();
                        JSONObject obj = new JSONObject(result);
                        if (obj.getInt("code") == 0) {
                            saveUserInfo(result, context);
                        } else {
                            String temp = obj.getString("msg");
                            if (!TextUtils.isEmpty(temp)) {
                                Toast.makeText(context, temp, Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (JSONException e) {
                    }

                }
            }
        });

        return userInfo != null ? userInfo : new UserInfo();
    }

    /**
     * 从服务器获取用户信息(带回调)
     *
     * @param client
     * @param tokenid
     * @param context
     * @param mListener
     */
    public static void getUserInfoFromServer(OkHttpClient client, String tokenid, final Context context, final GetUserInfoFinishedListener mListener) {
        Request request = new Request.Builder().url(Settings.USER_URL + "/" + tokenid).addHeader("ACCEPT", "*/*")
                .addHeader("CLIENT", "android").addHeader("Authorization", CommonUtil.authorizeStr(DaoUtil.getUserId(context), DaoUtil.getToken(context))).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (mListener != null) {
                    mListener.doFinish(false, "");
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String result = response.body().string();
                        JSONObject obj = new JSONObject(result);
                        String msg = obj.getString("msg");
                        if (obj.getInt("code") == 0) {
                            saveUserInfo(result, context);
                            if (mListener != null) {
                                mListener.doFinish(true, msg);
                            }
                        } else {
                            if (mListener != null) {
                                mListener.doFinish(false, msg);
                            }
                        }
                    } catch (JSONException e) {
                        if (mListener != null) {
                            mListener.doFinish(false, "");
                        }
                    }

                }
            }
        });

    }

    public static void getUserInfoFromServer(final Context context, final GetUserInfoFinishedListener mListener) {
        String tokenid = DaoUtil.getUserId(context);
        Request request = new Request.Builder().url(Settings.USER_URL + "/" + tokenid).addHeader("ACCEPT", "*/*")
                .addHeader("CLIENT", "android").addHeader("Authorization", CommonUtil.authorizeStr(tokenid, DaoUtil.getToken(context))).build();
        new OkHttpClient().newCall(request).enqueue(new OkHttpCallback(context) {

            @Override
            public void onFailure(Call call, IOException e) {
                super.onFailure(call, e);
                if (mListener != null) {
                    mListener.doFinish(false, "");
                }
            }

            @Override
            protected void onError(JSONObject result) {

            }

            @Override
            protected void onSuccess(JSONObject result) {
                saveUserInfo(result.toString(), context);
                if (mListener != null) {
                    mListener.doFinish(true, "");
                }
            }

            @Override
            protected void onFinish() {

            }

//            @Override
//            public void onError(JSONObject result) throws JSONException {
//                super.onError(result);
//                if (mListener != null) {
//                    mListener.doFinish(false, "");
//                }
//            }
        });

    }

    /**
     * 不传tokenid
     *
     * @param client
     * @param context
     * @param mListener
     */
    public static void getUserInfoFromServer(OkHttpClient client, final Context context, final GetUserInfoFinishedListener mListener) {
        Request request = new Request.Builder().url(Settings.USER_URL + "/" + DaoUtil.getUserId(context)).addHeader("ACCEPT", "*/*")
                .addHeader("CLIENT", "android").addHeader("Authorization", CommonUtil.authorizeStr(DaoUtil.getUserId(context), DaoUtil.getToken(context))).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (mListener != null) {
                    mListener.doFinish(false, "");
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String result = response.body().string();
                        JSONObject obj = new JSONObject(result);
                        String msg = obj.getString("msg");
                        if (obj.getInt("code") == 0) {
                            saveUserInfo(result, context);
                            if (mListener != null) {
                                mListener.doFinish(true, msg);
                            }
                        } else {
                            if (mListener != null) {
                                mListener.doFinish(false, msg);
                            }
                        }
                    } catch (JSONException e) {
                        if (mListener != null) {
                            mListener.doFinish(false, "");
                        }
                    }

                }
            }
        });


    }

    /**
     * 存储用户信息到本地
     *
     * @param userinfo
     * @param context
     */
    public static void saveUserInfo(String userinfo, Context context) {
        // save String to local
        SharedPreferences.Editor editor = context.getSharedPreferences(Common.sharedPrefName, Context.MODE_PRIVATE).edit();
        editor.putString("userinfo", userinfo);
        editor.commit();
        // refresh UserInfo in LicaibaoApplication
        AcbApplication.getInstance().setmUserInfo(JSON.toJavaObject(JSON.parseObject(userinfo), UserInfo.class));

        saveRefreshTag(context, false);
    }

    /**
     * 从本地读取用户信息
     *
     * @param context
     * @return
     */
    public static UserInfo getUserInfoFromLocal(Context context) {
        String userinfo = context.getSharedPreferences(Common.sharedPrefName, context.MODE_PRIVATE).getString("userinfo", null);
        return userinfo != null ? JSON.toJavaObject(JSON.parseObject(userinfo), UserInfo.class) : new UserInfo();
    }

    /**
     * 获取用户id
     *
     * @param context
     * @return
     */
    public static String getUserId(Context context) {
        SharedPreferences spf = context.getSharedPreferences(Common.sharedPrefName, Context.MODE_PRIVATE);
        if (spf != null) {
            return spf.getString("tokenid", null);
        }
        return null;
    }

    /**
     * 获取登录token
     *
     * @param context
     * @return
     */
    public static String getToken(Context context) {
        SharedPreferences spf = context.getSharedPreferences(Common.sharedPrefName, Context.MODE_PRIVATE);
        if (spf != null) {
            return spf.getString("token", null);
        }
        return null;
    }

    /**
     * 存储登录token
     *
     * @param context
     * @param token
     */
    public static void saveToken(Context context, String token) {
        SharedPreferences spf = context.getSharedPreferences(Common.sharedPrefName, Context.MODE_PRIVATE);
        if (spf != null) {
            spf.edit().putString("token", token).commit();
        }
    }

    /**
     * 存储tokenid
     *
     * @param context
     * @param token_id
     */
    public static void saveTokenId(Context context, String token_id) {
        SharedPreferences spf = context.getSharedPreferences(Common.sharedPrefName, Context.MODE_PRIVATE);
        if (spf != null) {
            spf.edit().putString("tokenid", token_id).commit();
        }
    }


    /**
     * 存储登录信息
     *
     */
    public static void saveLoginedInfo(Context context,  String token_id,  String token) {
        SharedPreferences spf = context.getSharedPreferences(Common.sharedPrefName, Context.MODE_PRIVATE);
        if (spf != null) {
            SharedPreferences.Editor editor = spf.edit();
            editor.putString("token", token).commit();
            editor.putString("tokenid", token_id + "").commit();
            editor.putString("authorization", CommonUtil.authorizeStr(token_id + "" , token)).commit();
        }
    }


    /**
     * 获取认证头部
     *
     * @param context
     * @return
     */
    public static String getAuthorization(Context context) {
        String auth = context.getSharedPreferences(Common.sharedPrefName, Context.MODE_PRIVATE).getString("authorization", null);
        return auth == null ? CommonUtil.authorizeStr(getUserId(context), getToken(context)) : auth;
    }

    /**
     * 判断是否已经登录
     *
     * @param context
     * @return
     */
    public static boolean isLogined(Context context) {
        String userid = null;
        try {
            userid = getUserId(context);
        } catch (Exception e) {

        }
        return userid != null;
    }

    /**
     * 清空本地用户信息
     *
     * @param context
     */
    public static void clearUserinfo(Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(Common.sharedPrefName, Context.MODE_PRIVATE).edit();
        editor.clear();
        editor.commit();
        AcbApplication.getInstance().setmUserInfo(null);
    }

    /**
     * 存储刷新标记
     *
     * @param context
     * @param refresh
     */
    public static void saveRefreshTag(Context context, boolean refresh) {
        SharedPreferences.Editor editor = context.getSharedPreferences(Common.sharedPrefName, Context.MODE_PRIVATE).edit();
        editor.putBoolean("shouldrefresh", refresh);
        editor.commit();
    }

    /**
     * 获取刷新标记
     *
     * @param context
     * @return
     */
    public static boolean getRefreshTag(Context context) {
        SharedPreferences spf = context.getSharedPreferences(Common.sharedPrefName, Context.MODE_PRIVATE);
        if (spf != null && spf.getBoolean("shouldrefresh", false)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 存储城市列表
     *
     * @param context
     * @param addressJson
     */
    public static void saveAddress(Context context, String addressJson) {
        SharedPreferences.Editor editor = context.getSharedPreferences(Common.sharedPrefName, Context.MODE_PRIVATE).edit();
        editor.putString("address", addressJson);
        editor.commit();
    }

    /**
     * 获取存储的城市列表
     *
     * @param context
     * @return
     */
    public static String getAddress(Context context) {
        SharedPreferences spf = context.getSharedPreferences(Common.sharedPrefName, Context.MODE_PRIVATE);
        if (spf != null && spf.getString("address", null) != null) {
            return spf.getString("address", null);
        } else {
            return null;
        }
    }

    /**
     * 存储手势开关
     */
    public static void saveToggleState(Context context, boolean toggle) {
        SharedPreferences.Editor editor = context.getSharedPreferences(Common.sharedPrefName, Context.MODE_PRIVATE).edit();
        editor.putBoolean("gesturetoggle", toggle);
        editor.commit();
    }

    /**
     * 获取收拾开关状态
     */
    public static boolean getToggleState(Context context) {
        return context.getSharedPreferences(Common.sharedPrefName, Context.MODE_PRIVATE).getBoolean("gesturetoggle", false);
    }

    /**
     * 读取手势密码
     * @param context
     * @return
     */
    public static String getPatternStr(Context context) {
        return context.getSharedPreferences(Common.sharedPrefName, Context.MODE_PRIVATE).getString("patternstring",
                null);
    }

    /**
     * 保存手势密码
     * @param context
     * @param patternString
     */
    public static void savePatternStr(Context context, String patternString) {
        context.getSharedPreferences(Common.sharedPrefName, Context.MODE_PRIVATE).edit()
                .putString("patternstring", patternString).commit();
    }


    /**
     * 读取手势密码
     * @param context
     * @return
     */
    public static String getShareInfoStr(Context context) {
        return context.getSharedPreferences(Common.sharedPrefName, Context.MODE_PRIVATE).getString("shareinfo",
                null);
    }

    /**
     * 保存手势密码
     * @param context
     * @param shareInfoStr
     */
    public static void saveShareInfoStr(Context context, String shareInfoStr) {
        context.getSharedPreferences(Common.sharedPrefName, Context.MODE_PRIVATE).edit()
                .putString("shareinfo", shareInfoStr).commit();
    }

}
