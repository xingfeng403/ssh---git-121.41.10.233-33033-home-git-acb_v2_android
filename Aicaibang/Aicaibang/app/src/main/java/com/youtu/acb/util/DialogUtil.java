package com.youtu.acb.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.test.mock.MockDialogInterface;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.youtu.acb.R;
import com.youtu.acb.common.Settings;

/**
 * Created by xingf on 16/6/30.
 */
public class DialogUtil {
    /**
     * 显示分享对话框
     *
     * @param context
     */
    static Dialog mDialog;
    public static void showShareDialog(final Activity activity, final Context context) {
        if (!DaoUtil.isLogined(context)) {
            ToastUtil.show(context, "请先登录");
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        mDialog = builder.create();
        mDialog.show();
        mDialog.setContentView(R.layout.dialog_share);
        mDialog.setCanceledOnTouchOutside(true);
        mDialog.setCancelable(true);
        mDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        WindowManager.LayoutParams params = mDialog.getWindow().getAttributes();
        params.width = Settings.DISPLAY_WIDTH;
        params.height = DensityUtils.dp2px(context, 165);
        params.gravity = Gravity.BOTTOM;
        mDialog.getWindow().setAttributes(params);

        RelativeLayout cancel = (RelativeLayout) mDialog.findViewById(R.id.share_cancel);
        LinearLayout shareToFriend = (LinearLayout) mDialog.findViewById(R.id.share_to_friend);
        LinearLayout shareToTimecycle = (LinearLayout) mDialog.findViewById(R.id.share_to_timecycle);
        LinearLayout shareToQQ = (LinearLayout) mDialog.findViewById(R.id.share_to_qq);
        LinearLayout shareToQZone = (LinearLayout) mDialog.findViewById(R.id.share_to_qq_space);

        cancel.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                closeDialog(mDialog);
            }
        });

        shareToFriend.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                ShareUtil util = new ShareUtil(activity, context);
                if (util.init()) {
                    util.doShareToWeixin(false);
                } else {
                    getShareUrl(context);
                    ToastUtil.show(context, "暂时无法分享，请稍后再试");
                }
                closeDialog(mDialog);

            }
        });

        shareToTimecycle.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                ShareUtil util = new ShareUtil(activity, context);
                if (util.init()) {
                    util.doShareToWeixin(true);
                } else {
                    getShareUrl(context);
                    ToastUtil.show(context, "暂时无法分享，请稍后再试");
                }
                closeDialog(mDialog);
            }
        });

        shareToQQ.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                ShareUtil util = new ShareUtil(activity, context);
                if (util.init()) {
                    util.doShareQQ(true);
                } else {
                    getShareUrl(context);
                    ToastUtil.show(context, "暂时无法分享，请稍后再试");
                }
                closeDialog(mDialog);
            }
        });

        shareToQZone.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                ShareUtil util = new ShareUtil(activity, context);
                if (util.init()) {
                    util.doShareQQ(false);
                } else {
                    getShareUrl(context);
                    ToastUtil.show(context, "暂时无法分享，请稍后再试");
                }
                closeDialog(mDialog);
            }
        });
    }

    public static void getShareUrl(Context mContext) {

    }

    public static void closeDialog(Dialog dialog) {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }
}
