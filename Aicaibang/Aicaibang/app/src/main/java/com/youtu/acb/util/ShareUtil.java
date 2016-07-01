package com.youtu.acb.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.widget.Toast;

import com.tencent.mm.sdk.openapi.IWXAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.bean.SocializeEntity;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.listener.SocializeListeners;
import com.umeng.socialize.media.QQShareContent;
import com.umeng.socialize.media.QZoneShareContent;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.sso.QZoneSsoHandler;
import com.umeng.socialize.sso.UMQQSsoHandler;
import com.umeng.socialize.weixin.controller.UMWXHandler;
import com.umeng.socialize.weixin.media.CircleShareContent;
import com.umeng.socialize.weixin.media.WeiXinShareContent;
import com.youtu.acb.common.Settings;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

/*
 * 分享帮助类
 */
public class ShareUtil {

	private ArrayList<String> picList = new ArrayList<String>();
	private Activity activity;
	private Context mContext;
	/** 微信分享 */
	private IWXAPI mWeixinApi;

	private String title = "haha"; // 标题
	private String targetUrl = "http://www.qq.com/news/1.html"; // 跳转链接
	private String description = "hahha"; // 描述
	private String imgUrl = "http://imgcache.qq.com/qzone/space_item/pre/0/66768.gif"; // 图片url

	UMSocialService mController;

	public ShareUtil(Activity activity, Context mContext) {
		this.activity = activity;
		this.mContext = mContext;
	}

	/*
	 * 将本地分享内容填入shareUtil
	 */
	public boolean init() {
		mController = UMServiceFactory.getUMSocialService("com.umeng.share");
		SocializeListeners.SnsPostListener mSnsPostListener  = new SocializeListeners.SnsPostListener() {

			@Override
			public void onStart() {

			}

			@Override
			public void onComplete(SHARE_MEDIA platform, int stCode,
								   SocializeEntity entity) {
				if (stCode == 200) {
					Toast.makeText(mContext, "分享成功", Toast.LENGTH_SHORT)
							.show();
				} else {
//					Toast.makeText(mContext,
//							"分享失败 : error code : " + stCode, Toast.LENGTH_SHORT)
//							.show();
				}
			}
		};
		mController.registerListener(mSnsPostListener);


		String jsonStr = DaoUtil.getShareInfoStr(mContext);
		if (jsonStr == null)
			return false;
		try {
			JSONObject obj = new JSONObject(jsonStr);
			title = obj.getString("title");
			description = title;
			targetUrl = obj.getString("url");
			imgUrl = obj.getString("img");
		} catch (JSONException e) {
			return false;
		}
		return true;
	}

	/*
	 * 传入分享详情
	 */
	public void setDescription(String description) {
		if (description != null)
			this.description = description;
	}


	boolean misToFriend;

	/*
	 * 微信分享
	 */
	public void doShareToWeixin(boolean isToCircle) {
//		mWeixinApi = WXAPIFactory.createWXAPI(mContext, Settings.WEIXIN_APP_ID, true);
//		mWeixinApi.registerApp(Settings.WEIXIN_APP_ID);

//		// 初始化一个WXTextObject对象
//		WXTextObject textObj = new WXTextObject();
//		textObj.text = title;
//		// 用WXTextObject对象初始化一个WXMediaMessage对象
//
//		WXWebpageObject webObj = new WXWebpageObject();
//		if (targetUrl != null && !targetUrl.equals(""))
//			webObj.webpageUrl = targetUrl;
//		final WXMediaMessage msg = new WXMediaMessage();
//		msg.title = title;
//		msg.description = description;
//		msg.mediaObject = webObj;
//
//		if (imgUrl != null && !imgUrl.equals("")) { // 分享图片的本地位置，再转格式
//
//			Glide.with(mContext).load(imgUrl).asBitmap().into(new SimpleTarget<Bitmap>() {
//				@Override
//				public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
//					if (bitmap != null) {
//						setBmp(msg, bitmap);
//					} else {
//						shareDefaultPicToWx(msg);
//					}
//				}
//			});
//		} else {
//			shareDefaultPicToWx(msg);
//		}

		if (isToCircle) {
			CircleShareContent weixinContent = new CircleShareContent();
			weixinContent.setShareContent(description);
			weixinContent.setTitle(title);
			weixinContent.setTargetUrl(targetUrl);
			UMImage urlImage = new UMImage(activity, imgUrl);
			weixinContent.setShareImage(urlImage);
			mController.setShareMedia(weixinContent);
			// 添加微信平台
			UMWXHandler wxHandler = new UMWXHandler(activity, Settings.WEIXIN_APP_ID,
					Settings.WEIXIN_SECRET);
			wxHandler.setToCircle(true);
			wxHandler.addToSocialSDK();
			performShare(SHARE_MEDIA.WEIXIN_CIRCLE);
		} else {
			WeiXinShareContent weixinContent = new WeiXinShareContent();
			weixinContent.setShareContent(description);
			weixinContent.setTitle(title);
			weixinContent.setTargetUrl(targetUrl);
			UMImage urlImage = new UMImage(activity, imgUrl);
			weixinContent.setShareImage(urlImage);
			mController.setShareMedia(weixinContent);

			// 添加微信平台
			UMWXHandler wxHandler = new UMWXHandler(activity, Settings.WEIXIN_APP_ID,
					Settings.WEIXIN_SECRET);
			wxHandler.addToSocialSDK();
			performShare(SHARE_MEDIA.WEIXIN);
		}

	}

	private void performShare(SHARE_MEDIA platform) {
		mController.postShare(mContext, platform, new SocializeListeners.SnsPostListener() {

			@Override
			public void onStart() {

			}

			@Override
			public void onComplete(SHARE_MEDIA arg0, int eCode, SocializeEntity arg2) {
			}
		});

	}

	private String buildTransaction(final String type) {
		return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
	}

//	private void shareDefaultPicToWx(WXMediaMessage msg) {
//		// 使用launcher作为默认分享图片
//		Resources r = activity.getResources();
//		InputStream is = r.openRawResource(R.drawable.luoli);
//		BitmapDrawable bmpDraw = new BitmapDrawable(is);
//		Bitmap bmp = bmpDraw.getBitmap();
//
//		setBmp(msg, bmp);
//	}
//	private void setBmp(WXMediaMessage msg, Bitmap bmp) {
//		msg.thumbData = bmpToByteArray(bmp, true);
//
//		// 构造一个Req
//		SendMessageToWX.Req req = new SendMessageToWX.Req();
//		req.transaction = String.valueOf(System.currentTimeMillis());
//		req.message = msg;
//		if (misToFriend) {
//			if (mWeixinApi.getWXAppSupportAPI() > 0x21020001) {
//				req.scene = SendMessageToWX.Req.WXSceneTimeline;
//			} else {
//				ToastUtil.show(mContext, "安装的微信版本过低，无法分享到朋友圈");
//			}
//		}
		// 调用接口发送数据
//		mWeixinApi.sendReq(req);
//	}

	/*
	 * convert bitmap to byteArray
	 */
	public static byte[] bmpToByteArray(Bitmap bmp, final boolean needRecycle) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		bmp.compress(CompressFormat.PNG, 100, output);
		if (needRecycle) {
			bmp.recycle();
		}

		byte[] result = output.toByteArray();
		try {
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public void doShareQQ(boolean isToFriend) {
		if (isToFriend) {
			 QQShareContent qqShareContent = new QQShareContent();
			 qqShareContent.setShareContent(description);
			 qqShareContent.setTitle(title);
			 UMImage urlImage = new UMImage(activity,
			 imgUrl);
			 qqShareContent.setShareImage(urlImage);
			 qqShareContent.setTargetUrl(targetUrl);
			 mController.setShareMedia(qqShareContent);
			 // 添加QQ支持, 并且设置QQ分享内容的target url
			 UMQQSsoHandler qqSsoHandler = new
			 UMQQSsoHandler(activity, Settings.APP_ID,
			 Settings.APP_SECRET);
			 qqSsoHandler.setTargetUrl("http://www.umeng.com/social");
			 qqSsoHandler.addToSocialSDK();
			 performShare(SHARE_MEDIA.QQ);
		} else {
			QZoneShareContent qZoneShareContent = new QZoneShareContent();
			qZoneShareContent.setShareContent(description);
			qZoneShareContent.setTitle(title);
			UMImage urlImage = new UMImage(activity,
					imgUrl);
			qZoneShareContent.setShareImage(urlImage);
			qZoneShareContent.setTargetUrl(targetUrl);
			mController.setShareMedia(qZoneShareContent);

			QZoneSsoHandler qZoneSsoHandler = new QZoneSsoHandler(activity, Settings.APP_ID, Settings.APP_SECRET);
			qZoneSsoHandler.setTargetUrl("http://www.umeng.com/social");
			qZoneSsoHandler.addToSocialSDK();
			performShare(SHARE_MEDIA.QZONE);
		}
	}
}
