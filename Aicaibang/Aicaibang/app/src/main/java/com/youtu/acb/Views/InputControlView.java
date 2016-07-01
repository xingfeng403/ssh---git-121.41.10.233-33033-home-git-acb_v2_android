package com.youtu.acb.Views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.LoginFilter;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.youtu.acb.R;
import com.youtu.acb.util.DensityUtils;
import com.youtu.acb.util.OnSingleClickListener;


/**
 * Created by xingf on 15/12/10.
 */
public class InputControlView extends LinearLayout {
    EditText mContent;
    ImageView mClearText, mEyeSwitch;
    boolean mShowPwd; // true：密码明文显示

    public InputControlView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context, attrs);
    }

    public InputControlView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context, attrs);
    }

    public InputControlView(Context context) {
        super(context);

        init(context, null);
    }

    private void init(Context context, AttributeSet attrs) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_input_control, this);
        mContent = (EditText) view.findViewById(R.id.input_content);
        mClearText = (ImageView) view.findViewById(R.id.cleartext);
        mEyeSwitch = (ImageView) view.findViewById(R.id.eyeswitch);
        setOrientation(LinearLayout.VERTICAL);

        mContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
//                if (mContent.getText().length() > 1) {
//                    if (mClearText.getVisibility() == View.GONE) {
//                        mClearText.setVisibility(View.VISIBLE);
//                    } else {
//                        return;
//                    }
//                }
                if (mContent.getText().length() > 0) {
                    if (mClearText.getVisibility() == View.GONE) {
                        mClearText.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (mClearText.getVisibility() == View.VISIBLE) {
                        mClearText.setVisibility(View.GONE);
                    }
                }
            }
        });

        mClearText.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                mContent.setText("");
                mClearText.setVisibility(View.GONE);
            }
        });

        mEyeSwitch.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                if (!mShowPwd) {
                    mEyeSwitch.setImageResource(R.drawable.openeye);
                    mContent.setInputType(0x90);
                    mContent.setSelection(mContent.getText().length());
                } else {
                    mEyeSwitch.setImageResource(R.drawable.closeeye);
                    mContent.setInputType(0x81);
                    mContent.setSelection(mContent.getText().length());
                }

                mShowPwd = !mShowPwd;
            }
        });


        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.EditType);
            switch (a.getInteger(R.styleable.EditType_type, 0)) {
                case 1:
                    // phone number
                    mContent.setInputType(InputType.TYPE_CLASS_PHONE);
                    String digits = "0123456789";
                    mContent.setKeyListener(DigitsKeyListener.getInstance(digits));
                    mContent.setFilters(new InputFilter[]{new InputFilter.LengthFilter(13)});
                    addDivider(context);
                    break;
                case 2:
                    // pwd
                    mEyeSwitch.setVisibility(View.VISIBLE);
                    mContent.setInputType(0x81);
                    InputFilter[] filters = new InputFilter[2];
                    filters[0] = new MyInputFilter("qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM0123456789");
                    filters[1] = new InputFilter.LengthFilter(20);
                    mContent.setFilters(filters);
                    addDivider(context);
                    break;
                case 3:
                    break;
                case 4:
                    // check code
                    mContent.setInputType(InputType.TYPE_CLASS_PHONE);
                    String digits2 = "0123456789";
                    mContent.setKeyListener(DigitsKeyListener.getInstance(digits2));
                    mContent.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
                    addDivider(context);
                    break;
                case 5:
                    // cash pwd
                    mContent.setInputType(0x81);
                    String digits4 = "0123456789";
                    mContent.setKeyListener(DigitsKeyListener.getInstance(digits4));
                    mContent.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
                    addDivider(context);

                    // black
                    mContent.setTextColor(Color.parseColor("#666666"));
                    mContent.setHintTextColor(Color.parseColor("#999999"));
                    break;

                case 6:
                    // check code
                    mContent.setInputType(InputType.TYPE_CLASS_PHONE);
                    String digits3 = "0123456789";
                    mContent.setKeyListener(DigitsKeyListener.getInstance(digits3));
                    mContent.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
                    addDivider(context);

                    // black
                    mContent.setTextColor(Color.parseColor("#666666"));
                    mContent.setHintTextColor(Color.parseColor("#999999"));
                    break;

                default:
                    break;
            }

            a.recycle();
        }
    }

    private void addDivider(Context context) {
        View line = new View(context);
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, 1);
        params.setMargins(0, DensityUtils.dp2px(context, 8), 0, 0);
        line.setLayoutParams(params);
        line.setBackgroundColor(Color.parseColor("#cccccc"));
        addView(line);
    }

    public EditText getEditText() {
        return mContent;
    }

    public class MyInputFilter extends LoginFilter.UsernameFilterGeneric {
        private String mAllowedDigits;

        public MyInputFilter(String digits) {
            mAllowedDigits = digits;
        }

        @Override
        public boolean isAllowed(char c) {
            if (mAllowedDigits.indexOf(c) != -1) {
                return true;
            }
            return false;
        }
    }

}
