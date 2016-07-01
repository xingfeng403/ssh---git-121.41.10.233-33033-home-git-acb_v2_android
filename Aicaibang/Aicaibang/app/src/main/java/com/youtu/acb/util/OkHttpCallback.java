package com.youtu.acb.util;

import android.content.Context;
import android.os.Message;
import android.text.TextUtils;


import com.youtu.acb.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by xingf on 16/1/20.
 */
public abstract class OkHttpCallback implements Callback {
    Context mContext;

    public OkHttpCallback(Context context) {
        mContext = context;
    }

    @Override
    public void onFailure(Call call, IOException e) {
        String errMsg = mContext.getString(R.string.net_err);
        if (!TextUtils.isEmpty(errMsg)) {
//            ToastHandler handler = new ToastHandler(mContext.getApplicationContext(), mContext.getMainLooper());
//            Message msg = new Message();
//            msg.obj = errMsg;
//            handler.sendMessage(msg);
        }
        onFinish();
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        if (response.isSuccessful()) {
            try {
                JSONObject result = new JSONObject(response.body().string());
                if (result.getInt("code") == 0) {
                    onSuccess(result);
                    if (mContext != null) {
                        mContext = null;
                    }
                } else if (result.getInt("code") == 401) {
//                    // reLogin
//                    ReloginHandler handler = new ReloginHandler(mContext, mContext.getMainLooper());
//                    handler.sendEmptyMessage(0);
                } else {
                    onError(result);
                }
            } catch (JSONException e) {

            }
        }

        onFinish();
    }

    protected abstract void onError(JSONObject result);


    protected abstract void onSuccess(JSONObject result);

    protected abstract void onFinish();
}
