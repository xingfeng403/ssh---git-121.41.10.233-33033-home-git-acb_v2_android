package com.youtu.acb.Views;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.youtu.acb.R;
import com.youtu.acb.util.OnSingleClickListener;


/**
 * Created by xingf on 15/12/8.
 */
public class Titlebar extends RelativeLayout {

    LinearLayout mLeftPart; // 左侧布局
    OnClickListener mLeftListener; // 左侧点击事件
    TextView mTitleTv; // 中间标题
    RelativeLayout mRightPart; // 右侧布局
    OnClickListener mRightListener; // 左侧点击事件
    TextView mRightTv;   // 右侧文字
    ImageView mRightImg; // 右侧图片
    TextView mLeftTv; // 左侧文字

    public Titlebar(Context context) {
        super(context);
        init(context);
    }

    public Titlebar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public Titlebar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        if (isInEditMode()) {
            return;
        }
        View view = LayoutInflater.from(context).inflate(R.layout.view_titlebar, this);

        setBackgroundColor(Color.parseColor("#607ee0"));

        mTitleTv = (TextView) view.findViewById(R.id.title_center_text);

        mTitleTv.setText("更多");

        // 添加点击事件监听
        mLeftPart = (LinearLayout) view.findViewById(R.id.title_left_part);
        mLeftPart.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                if (mLeftListener != null) {
                    mLeftListener.onClick(v);
                }
            }
        });
        mRightPart = (RelativeLayout) view.findViewById(R.id.title_right_part);
        mRightPart.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                if (mRightListener != null) {
                    mRightListener.onClick(v);
                }
            }
        });

        mRightTv = (TextView) view.findViewById(R.id.title_right_tv);
//        mRightImg = (ImageView) view.findViewById(R.id.title_right_iv);
    }

    /**
     * 传递左边组合view的点击事件处理
     *
     * @param onClickListener
     */
    public void setLeftClickListener(OnClickListener onClickListener) {
        mLeftListener = onClickListener;
    }

    /**
     * 传递右边点击事件处理
     *
     * @param onClickListener
     */
    public void setRightClickListener(OnClickListener onClickListener) {
        mRightListener = onClickListener;
    }

    /**
     * 给标题栏设置标题
     * @param str
     */
    public void setTitle(CharSequence str) {
        if (str != null) {
            mTitleTv.setText(str);
        } else {
            mTitleTv.setText("");
        }
    }

    /**
     * 获取左侧布局
     */
    public LinearLayout getmLeftPart (){
        return mLeftPart;
    }

    /**
     * 获取右侧布局
     * @return
     */
    public RelativeLayout getmRightPart() {
        return mRightPart;
    }

    /**
     * 获取右侧Tv
     * @return
     */
    public TextView getmRightTv() {
        return mRightTv;
    }

    /**
     * 显示右侧图片按钮
     */
    public void showRightImg(){
        mRightTv.setVisibility(View.GONE);
        mRightImg.setVisibility(View.VISIBLE);
        mRightPart.setVisibility(View.VISIBLE);
    }

    /**
     * 获取左侧tv
     */
    public TextView getLeftTv(){
        return mLeftTv;
    }

}
