package com.youtu.acb.Views;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.youtu.acb.R;
import com.youtu.acb.common.Settings;
import com.youtu.acb.util.DensityUtils;

/**
 * Created by xingf on 16/6/27.
 */
public class LabelsLayout extends LinearLayout {

    private String[] labels;
    private int maxLength;

    public LabelsLayout(Context context, String[] labels, int maxLength) {
        super(context);
        this.labels = labels;
        this.maxLength = maxLength;

        init();
    }

    public void setData(String[] labels, int max) {
        this.labels = labels;
        this.maxLength = max;
        init();
    }


    public LabelsLayout(Context context) {
        super(context);
    }

    public LabelsLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LabelsLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private Context mContext;

    private void init() {
        mContext = getContext();
        int height32 = (int) (Settings.RATIO_HEIGHT * 32);
        int padding10 = (int) (Settings.RATIO_WIDTH * 10);
        int margin10 = DensityUtils.dp2px(mContext, 5);
        int textsize = DensityUtils.px2dp(mContext, Settings.RATIO_HEIGHT * 20);


        int total = 0;
        for (String str : labels) {
            total += str.length();
        }

        int addType = 0; // 0: 1line 1:2lines

        if (total > maxLength) {
            addType = 1;
        }

        int line1length = 0;
        int line2length = 0;
        int count = 0;
        int size = labels.length;
        setOrientation(VERTICAL);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, height32);
        params.setMargins(0, 0, margin10, 0);
        if (addType == 1) {
            LinearLayout lin1 = new LinearLayout(mContext);
            lin1.setOrientation(HORIZONTAL);
            lin1.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            LinearLayout lin2 = new LinearLayout(mContext);
            lin2.setOrientation(HORIZONTAL);
            LayoutParams paramLin2 = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            paramLin2.setMargins(0, margin10, 0, 0);
            lin2.setLayoutParams(paramLin2);

            TextView tv;
            while (count + 1 < size && line1length + labels[count + 1].length() <= maxLength) {
                tv = new TextView(mContext);

                tv.setLayoutParams(params);
                tv.setPadding(padding10, 0, padding10, 0);
                tv.setText(labels[count]);
                tv.setTextSize(textsize);
                tv.setTextColor(Color.parseColor("#fbb4b1"));
                tv.setGravity(Gravity.CENTER);
                tv.setBackgroundResource(R.drawable.shape_orange_round_stroke);

                lin1.addView(tv);

                line1length += labels[count].length();
                count++;
            }

            tv = new TextView(mContext);

            tv.setLayoutParams(params);
            tv.setPadding(padding10, 0, padding10, 0);
            tv.setText(labels[count]);
            tv.setTextSize(textsize);
            tv.setTextColor(Color.parseColor("#fbb4b1"));
            tv.setGravity(Gravity.CENTER);
            tv.setBackgroundResource(R.drawable.shape_orange_round_stroke);

            lin1.addView(tv);
            count++;

            while (count + 1 < size && line2length + labels[count + 1].length() <= maxLength) {
                tv = new TextView(mContext);
                tv.setLayoutParams(params);
                tv.setPadding(padding10, 0, padding10, 0);
                tv.setText(labels[count]);
                tv.setTextColor(Color.parseColor("#fbb4b1"));
                tv.setTextSize(textsize);
                tv.setGravity(Gravity.CENTER);
                tv.setBackgroundResource(R.drawable.shape_orange_round_stroke);

                lin2.addView(tv);

                line2length += labels[count].length();
                count++;
            }

            if (count + 1 < size) {
                tv = new TextView(mContext);

                tv.setLayoutParams(params);
                tv.setPadding(padding10, 0, padding10, 0);
                tv.setText(labels[count]);
                tv.setTextSize(textsize);
                tv.setTextColor(Color.parseColor("#fbb4b1"));
                tv.setGravity(Gravity.CENTER);
                tv.setBackgroundResource(R.drawable.shape_orange_round_stroke);

                lin2.addView(tv);
                count++;

                addView(lin1);
                addView(lin2);
            }

        } else {
            LinearLayout lin1 = new LinearLayout(mContext);
            lin1.setOrientation(HORIZONTAL);
            lin1.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            while (count + 1 < size && line1length + labels[count + 1].length() <= maxLength) {
                TextView tv = new TextView(mContext);
                tv.setLayoutParams(params);
                tv.setPadding(padding10, 0, padding10, 0);
                tv.setText(labels[count]);
                tv.setTextColor(Color.parseColor("#fbb4b1"));
                tv.setTextSize(textsize);
                tv.setGravity(Gravity.CENTER);
                tv.setBackgroundResource(R.drawable.shape_orange_round_stroke);

                lin1.addView(tv);

                line1length += labels[count].length();
                count++;
            }

            TextView tv = new TextView(mContext);
            tv.setLayoutParams(params);
            tv.setPadding(padding10, 0, padding10, 0);
            tv.setText(labels[count]);
            tv.setTextColor(Color.parseColor("#fbb4b1"));
            tv.setTextSize(textsize);
            tv.setGravity(Gravity.CENTER);
            tv.setBackgroundResource(R.drawable.shape_orange_round_stroke);

            lin1.addView(tv);
            count++;

            addView(lin1);
        }

    }
}
