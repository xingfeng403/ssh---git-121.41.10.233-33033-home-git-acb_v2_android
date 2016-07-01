package com.youtu.acb.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yalantis.ucrop.UCrop;
import com.youtu.acb.R;
import com.youtu.acb.common.Constants;
import com.youtu.acb.common.Settings;
import com.youtu.acb.util.BitmapUtils;
import com.youtu.acb.util.CommonUtil;
import com.youtu.acb.util.DaoUtil;
import com.youtu.acb.util.OnSingleClickListener;
import com.youtu.acb.util.RequestBodyUtil;
import com.youtu.acb.util.ToastUtil;
import com.youtu.acb.util.UpdatePhoto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PublishTopicActivity extends BaseActivity {

    private RelativeLayout mTitleBar;
    private FrameLayout mBack;
    private RelativeLayout mPtRel;
    private GridView mGridView;
    private PtAdapter mAdapter;
    // 图像加载类
    private UpdatePhoto mUpdatePhoto;
    private static final int CAMERA_REQUEST_CODE = 1001;
    private static final int ALBUM_REQUEST_CODE = 1002;
    private static final int CROP_REQUEST_CODE = 1004;
    // 临时图片名
    private static final String PHOTO_TEMP_FILE = "Image.jpg";
    // 图片源文件 缩略图
    private File mOriginalFile, mThumbnailFile, mCorpFile;
    private ArrayList<Bitmap> ninePics = new ArrayList<>();
    private int mGridViewItemWidth;
    private String mAppId;
    private String mSubmitContent; // 提交内容
    private FrameLayout mSubmit;
    private EditText mContent;
    private Context mSelf = PublishTopicActivity.this;
    private HashMap<String, String> picNames = new HashMap();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish_topic);

        mGridViewItemWidth = Settings.DISPLAY_WIDTH / 4;

        // 初始化从相册相机选择图片类
        mUpdatePhoto = new UpdatePhoto(this);

//        mAppId = getIntent().getStringExtra("appid");

        mAppId = "105";
        mTitleBar = (RelativeLayout) findViewById(R.id.publish_topic_titlebar);
        mTitleBar.getLayoutParams().height = Settings.TITLEBAR_HEIGHT;

        mSubmit = (FrameLayout) findViewById(R.id.publish_topic_sends);
        mContent = (EditText) findViewById(R.id.pt_edit);
        mSubmit.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                mSubmitContent = mContent.getText().toString();
                String removeSpace = mSubmitContent.replaceAll(" ", "");
                if (removeSpace.length() == 0) {
                    ToastUtil.show(mSelf, "内容不能全部为空格");
                    return;
                }

                if (mSubmitContent.length() == 0) {
                    ToastUtil.show(mSelf, "先说点什么");
                } else {
                    if (ninePics.size() > 0) {
                        uploadPics();
                    } else {
                        submitTextAndImgs();
                    }
                }

            }
        });

        mBack = (FrameLayout) findViewById(R.id.publish_topic_back);
        mBack.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                onBackPressed();
            }
        });

        mPtRel = (RelativeLayout) findViewById(R.id.pt_rel);
        mPtRel.setMinimumHeight((int) (470 * Settings.RATIO_WIDTH));

        mGridView = (GridView) findViewById(R.id.pt_gridview);

        mAdapter = new PtAdapter();
        mGridView.setAdapter(mAdapter);

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                getDialog(i);
            }
        });


    }

    class PtAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return ninePics.size() + 1;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ImageView img = new ImageView(PublishTopicActivity.this);
            img.setLayoutParams(new AbsListView.LayoutParams(mGridViewItemWidth, mGridViewItemWidth));
            img.setScaleType(ImageView.ScaleType.FIT_XY);
            if (ninePics.size() == 9) {
                img.setImageBitmap(ninePics.get(8));
            } else if (i == ninePics.size()) {
                // add
                img.setImageResource(R.drawable.add_pic);
                img.setOnClickListener(new OnSingleClickListener() {
                    @Override
                    public void doOnClick(View v) {
                        mUpdatePhoto.getDialog();
                    }
                });
            } else {
                img.setImageBitmap(ninePics.get(i));
            }
            return img;
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
                        .start(PublishTopicActivity.this);
            } else {
                if (mOriginalFile.exists()) {
                    Uri oUri = Uri.fromFile(mOriginalFile);
                    mDestinationUri = Uri.fromFile(mThumbnailFile);
                    UCrop.of(oUri, mDestinationUri)
                            .withAspectRatio(mAspectRatioX, mAspectRatioY)
                            .withMaxResultSize(200, 200)
                            .start(PublishTopicActivity.this);
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
                    .start(PublishTopicActivity.this);
        }
        // 返回裁剪
        if (requestCode == CROP_REQUEST_CODE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                Bitmap photo = data.getParcelableExtra("data");
                ninePics.add(photo);
                String uuid = UUID.randomUUID().toString();
                mCorpFile = new File(Settings.TEMP_PATH, uuid + PHOTO_TEMP_FILE);
                mUpdatePhoto.saveBitmapToFile(photo, mCorpFile);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                    }
                });

//                uploadIcon(BitmapUtils.bitmapToBase64(photo));
            }
        }

        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            final Uri resultUri = UCrop.getOutput(data);
            Bitmap b = BitmapUtils.getBitmapFromFile(mThumbnailFile, 480, 360, false);
            ninePics.add(b);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAdapter.notifyDataSetChanged();
                }
            });

//            uploadIcon(BitmapUtils.bitmapToBase64(b));
        } else if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
        }

    }

    private String ptMsg;

    private void submitTextAndImgs() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();

                int length = picNames.keySet().size();
                String imgss = "";
                if (length > 0) {
                    StringBuilder imgs = new StringBuilder();
                    imgs.append("{");
                    for (int i = 0; i < length; i++) {
                        imgs.append(picNames.get("" + i));
                        if (i != length - 1) {
                            imgs.append(",");
                        }
                    }
                    imgs.append("}");

                    imgss = imgs.toString();
                }


                FormBody formBody = new FormBody.Builder()
                        .add("id", mAppId)
                        .add("content", mSubmitContent)
                        .add("images", imgss)
                        .build();

                Request request = new Request.Builder()
                        .url(Settings.BASE_URL + "comment")
                        .addHeader("CLIENT", "android").addHeader("TOKEN", CommonUtil.getMobileUniqueId(mSelf))
                        .addHeader("authorization", DaoUtil.getAuthorization(mSelf))
                        .post(formBody)
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    JSONObject resultObj = JSON.parseObject(response.body().string());
                    if (resultObj.getIntValue("code") == 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mSelf, "提交成功", Toast.LENGTH_SHORT).show();

                                onBackPressed();
                            }
                        });
                    } else {
                        ptMsg = resultObj.getString("msg");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mSelf, ptMsg, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    private int count = 0; // 统计上传图片线程数
    public final int MAX_UPLOAD_THREADS_NUM = 5;

    private void uploadPics() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int length = ninePics.size();
                for (int i = 0; i < length; i++) {
                    count++;
                    while (count == MAX_UPLOAD_THREADS_NUM) {
                        try {
                            Thread.sleep(100);
                        } catch (Exception e) {
                        }

                    }
                    uploadOnePic(bitmap2InputString(ninePics.get(i)), i);
                }
            }
        }).start();

    }

    private void uploadOnePic(InputStream inputStream, int pos) {
        OkHttpClient client = new OkHttpClient();

        MediaType MEDIA_TYPE_MARKDOWN
                = MediaType.parse("text/x-markdown; charset=utf-8");


        RequestBody requestBody = RequestBodyUtil.create(MEDIA_TYPE_MARKDOWN, inputStream);
        Request request = new Request.Builder()
                .url(Settings.BASE_URL + "commentImage")
                .post(requestBody)
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful())
                throw new IOException("Unexpected code " + response);
            else {
                JSONObject resultObj = JSON.parseObject(response.body().string());
                picNames.put(pos + "", resultObj.getString("img"));
                count--;

                if (count == 0 && picNames.keySet().size() == ninePics.size()) {
                    // 图片全部上传完毕
                    submitTextAndImgs();
                }
            }

            Log.d("POST", response.body().string());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public InputStream bitmap2InputString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return new ByteArrayInputStream(baos.toByteArray());
    }


    // 从本地获取照片
    Dialog mAlert;
    public void getDialog(final int pos) {
        mAlert = new Dialog(PublishTopicActivity.this);
        mAlert.setContentView(R.layout.camera_dialog);
        mAlert.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        WindowManager.LayoutParams params = mAlert.getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        mAlert.getWindow().setAttributes(params);

        try {
            int dividerID = getResources().getIdentifier("android:id/titleDivider", null, null);
            View divider = mAlert.findViewById(dividerID);
            divider.setBackgroundColor(Color.TRANSPARENT);
        } catch (Exception e) { //上面的代码，是用来去除Holo主题的蓝色线条
            e.printStackTrace();
        }

        TextView photograph = (TextView) mAlert.findViewById(R.id.photo);
        TextView album = (TextView) mAlert.findViewById(R.id.camera);
        TextView cancel = (TextView) mAlert.findViewById(R.id.cancle);
        photograph.setText("删除");
        album.setText("预览");

        album.setOnClickListener(new OnSingleClickListener() {

            @Override
            public void doOnClick(View v) {
                ninePics.remove(pos);
                mAdapter.notifyDataSetChanged();
            }
        });

        photograph.setOnClickListener(new OnSingleClickListener() {

            @Override
            public void doOnClick(View v) {

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
}
