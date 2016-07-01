package com.youtu.acb.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;


import com.youtu.acb.R;
import com.youtu.acb.common.Constants;
import com.youtu.acb.common.Settings;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;


/**
 * 更新照片 拍照和相册
 */
public class UpdatePhoto {

    private Activity activity;
    private static final String PHOTO_TEMP_FILE = "Image.jpg";
    private Dialog mAlert;

    public UpdatePhoto(Activity activity) {
        this.activity = activity;
    }

    // 从本地获取照片
    public void getDialog() {
        mAlert = new Dialog(activity);
        mAlert.setContentView(R.layout.camera_dialog);
        mAlert.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        WindowManager.LayoutParams params = mAlert.getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        mAlert.getWindow().setAttributes(params);

        try {
            int dividerID = activity.getResources().getIdentifier("android:id/titleDivider", null, null);
            View divider = mAlert.findViewById(dividerID);
            divider.setBackgroundColor(Color.TRANSPARENT);
        } catch (Exception e) { //上面的代码，是用来去除Holo主题的蓝色线条
            e.printStackTrace();
        }

        TextView photograph = (TextView) mAlert.findViewById(R.id.photo);
        TextView album = (TextView) mAlert.findViewById(R.id.camera);
        TextView cancel = (TextView) mAlert.findViewById(R.id.cancle);
        // 拍照
        album.setOnClickListener(new OnSingleClickListener() {

            @Override
            public void doOnClick(View v) {
                mAlert.dismiss();
                String uuid = UUID.randomUUID().toString();
                Constants.PICTURE_TMPURL = uuid + PHOTO_TEMP_FILE;
                setImgUrl(Constants.PICTURE_TMPURL);
                File tempFile = new File(Settings.TEMP_PATH, Constants.PICTURE_TMPURL);
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempFile));
                activity.startActivityForResult(intent, Settings.CAMERA);
            }
        });

        photograph.setOnClickListener(new OnSingleClickListener() {

            @Override
            public void doOnClick(View v) {
                mAlert.dismiss();
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activity.startActivityForResult(intent, Settings.ALBUM);

            }
        });

        cancel.setOnClickListener(new OnSingleClickListener() {

            @Override
            public void doOnClick(View v) {
                mAlert.dismiss();
                mAlert = null;
            }
        });
        mAlert.show();
    }

    // 截取图片
    public void cropPhoto(Bitmap data, int width, int height) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setType("image/*");
        intent.putExtra("data", data);
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", width);
        intent.putExtra("aspectY", height);
        intent.putExtra("scale", true);// 黑边
        intent.putExtra("scaleUpIfNeeded", true);// 黑边
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", width);
        intent.putExtra("outputY", height);
        intent.putExtra("return-data", true);
        activity.startActivityForResult(intent, Settings.CROP);
    }

    // 截取图片
    public void cropPhoto(File file, int width, int height) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(Uri.fromFile(file), "image/*");
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", width);
        intent.putExtra("aspectY", height);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("scale", true);// 黑边
        intent.putExtra("scaleUpIfNeeded", true);// 黑边
        intent.putExtra("outputX", width);
        intent.putExtra("outputY", height);
        intent.putExtra("return-data", true);
        activity.startActivityForResult(intent, Settings.CROP);
    }

    public boolean saveBitmapToFile(Bitmap bitmap, File file) {
        BufferedOutputStream bos = null;
        try {
            File tempPicFile =
                    new File(Settings.TEMP_PATH, FileUtil.getFileNameByPath(file.getPath())
                            + Settings.PICTURE_TEMP_EXTENSION);
            tempPicFile.delete();
            file.delete();

            tempPicFile.getParentFile().mkdirs();
            tempPicFile.createNewFile();

            bos = new BufferedOutputStream(new FileOutputStream(tempPicFile));
            bitmap.compress(CompressFormat.JPEG, 100, bos);

            bos.flush();
            bos.close();
            bos = null;

            return tempPicFile.renameTo(file);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                bos = null;
            }
        }
        return false;
    }

    /**
     * 获取文件缓存名
     *
     * @return 文件缓存名
     */
    public String getImgUrl() {
        SharedPreferences share = activity.getSharedPreferences("img", Context.MODE_PRIVATE);
        return share.getString("imgurl", "");// String 用户id
    }

    /**
     * 存储缓存图片名
     */
    public void setImgUrl(String url) {
        SharedPreferences share = activity.getSharedPreferences("img", Context.MODE_PRIVATE);
        Editor editor = share.edit();
        editor.putString("imgurl", url);
        editor.commit();
    }

}
