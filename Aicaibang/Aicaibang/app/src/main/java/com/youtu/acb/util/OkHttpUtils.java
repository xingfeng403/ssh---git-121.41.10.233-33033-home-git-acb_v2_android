package com.youtu.acb.util;

import android.content.Context;

import com.youtu.acb.common.Settings;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by xingf on 16/5/31.
 */
public class OkHttpUtils {
    private static OkHttpClient client = new OkHttpClient();


    /**
     * get请求
     *
     * @param url
     */
    public static String get(String url, Context context) {
        Request request = new Request.Builder().url(getAbsoluteUrl(url)).addHeader("CLIENT", "android").addHeader("TOKEN", CommonUtil.getMobileUniqueId(context)).build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * post请求
     *
     * @param url
     * @param params
     * @return
     * @throws IOException
     */
    public static String post(String url, HashMap<String, String> params, Context context) {
        FormBody.Builder builder = new FormBody.Builder();
        for (String key : params.keySet()) {
            builder.add(key, params.get(key));
        }
        FormBody formBody = builder.build();
        Request request = new Request.Builder().url(getAbsoluteUrl(url)).addHeader("CLIENT", "android").addHeader("TOKEN", CommonUtil.getMobileUniqueId(context)).post(formBody).build();
        Response response;
        try {
            response = client.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return Settings.BASE_URL + relativeUrl;
    }

}