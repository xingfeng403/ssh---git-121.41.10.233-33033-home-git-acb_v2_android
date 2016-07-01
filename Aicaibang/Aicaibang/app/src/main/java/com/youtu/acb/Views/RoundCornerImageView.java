package com.youtu.acb.Views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * 圆角imageview
 *
 * @author xingf
 */
public class RoundCornerImageView extends ImageView {

    public RoundCornerImageView(Context context) {
        super(context);
    }

    public RoundCornerImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RoundCornerImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Drawable drawable = getDrawable();

        if (drawable == null) {
            return;
        }

        if (getWidth() == 0 || getHeight() == 0) {
            return;
        }

        Bitmap fullSizeBitmap = drawableToBitamp(drawable);

        int scaledWidth = getMeasuredWidth();
        int scaledHeight = getMeasuredHeight();

        Bitmap mScaledBitmap;
        if (scaledWidth == fullSizeBitmap.getWidth() && scaledHeight == fullSizeBitmap.getHeight()) {
            mScaledBitmap = fullSizeBitmap;
        } else {
            mScaledBitmap = Bitmap.createScaledBitmap(fullSizeBitmap, scaledWidth, scaledHeight, true /* filter */);
        }

        Bitmap roundBitmap = getRoundedCornerBitmap(getContext(), mScaledBitmap, 5, scaledWidth, scaledHeight, false, false, false, false);
        canvas.drawBitmap(roundBitmap, 0, 0, null);

    }

    public static Bitmap getRoundedCornerBitmap(Context context, Bitmap input, int pixels, int w, int h, boolean squareTL, boolean squareTR, boolean squareBL, boolean squareBR) {

        Bitmap output = Bitmap.createBitmap(w, h, Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final float densityMultiplier = context.getResources().getDisplayMetrics().density;

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, w, h);
        final RectF rectF = new RectF(rect);

        // make sure that our rounded corner is scaled appropriately
        final float roundPx = pixels * densityMultiplier;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        // draw rectangles over the corners we want to be square
        if (squareTL) {
            canvas.drawRect(0, 0, w / 2, h / 2, paint);
        }
        if (squareTR) {
            canvas.drawRect(w / 2, 0, w, h / 2, paint);
        }
        if (squareBL) {
            canvas.drawRect(0, h / 2, w / 2, h, paint);
        }
        if (squareBR) {
            canvas.drawRect(w / 2, h / 2, w, h, paint);
        }

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(input, 0, 0, paint);

        return output;
    }

    private Bitmap drawableToBitamp(Drawable drawable) {
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Config.ARGB_8888
                : Config.RGB_565;
        Bitmap bitmap = Bitmap.createBitmap(w,h,config);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0,0,w,h);
        drawable.draw(canvas);

        return bitmap;
    }
}
