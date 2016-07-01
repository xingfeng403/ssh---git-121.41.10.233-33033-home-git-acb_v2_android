package com.youtu.acb.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jzxiang.pickerview.TimePickerDialog;
import com.jzxiang.pickerview.data.Type;
import com.jzxiang.pickerview.listener.OnDateSetListener;
import com.youtu.acb.R;
import com.youtu.acb.Views.wheelview.LoopView;
import com.youtu.acb.common.Settings;
import com.youtu.acb.entity.RepayTypeInfo;
import com.youtu.acb.interfaces.DialogSelectedListener;
import com.youtu.acb.util.CommonUtil;
import com.youtu.acb.util.DaoUtil;
import com.youtu.acb.util.OnSingleClickListener;
import com.youtu.acb.util.ToastUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by xingf on 16/6/29.
 */
public class JiyibiActivity extends AppCompatActivity implements OnDateSetListener {

    private TextView mStartDate, mDeadDate;
    private EditText mNum;
    private LinearLayout face, back;
    private boolean isStart;
    private TextView mRepayType;
    private String id;
    private String type;
    private String type_id;
    private String expire_date;
    private String invest_date;
    private String amount;
    private Button mSubmit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dialog_jyb_layout);

        id = getIntent().getStringExtra("actid");
        type = getIntent().getStringExtra("acttype");


        face = (LinearLayout) findViewById(R.id.face_side);
        back = (LinearLayout) findViewById(R.id.back_side);
        face.getLayoutParams().width = Settings.DISPLAY_WIDTH * 11 / 15;
        back.getLayoutParams().width = Settings.DISPLAY_WIDTH * 11 / 15;
        ImageView cancel = (ImageView) findViewById(R.id.jiyibi_dialog_cancel);
        Button backToJyb = (Button) findViewById(R.id.back_jiyibi_note_btn);
        LinearLayout why = (LinearLayout) face.findViewById(R.id.jiyibi_why);
        mStartDate = (TextView) face.findViewById(R.id.face_start_date);
        mDeadDate = (TextView) face.findViewById(R.id.face_dead_date);
        mNum = (EditText) face.findViewById(R.id.jiyibi_dialog_edittext);
        mRepayType = (TextView) face.findViewById(R.id.shape_jiyibi_repay_type);
        mSubmit = (Button) face.findViewById(R.id.jiyibi_submit);

        mNum.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    CommonUtil.showKeyboard(JiyibiActivity.this, mNum);
                } else {
                    CommonUtil.closeKeyboard(JiyibiActivity.this, mNum);
                }
            }
        });

        mNum.addTextChangedListener(new TextWatcher() {
            boolean hasDot = false;

            @Override
            public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {
                if (s.toString().contains(".")) {
                    hasDot = true;
                } else {
                    hasDot = false;
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String str = editable.toString();

                if (str.startsWith(".")) {
                    mNum.setText("0.");
                } else if (str.length() == 2 && str.startsWith("0") && !str.startsWith("0.")) {  // 0.
                    mNum.setText("0." + str.substring(1, 2));
                    mNum.setSelection(3);
                } else if (hasDot && str.lastIndexOf(".") == str.length() - 1) { // double dot
                    mNum.setText(str.substring(0, str.length() - 1));
                    mNum.setSelection(mNum.getText().length());
                } else if (hasDot && str.length() - str.indexOf(".") == 4) { // more than 2 digits after dot
                    mNum.setText(str.toString().substring(0, str.length() - 1));
                    mNum.setSelection(str.length() - 1);
                } else if (str.endsWith(".0")) {
                    mNum.setText(str + "0");
                    mNum.setSelection(str.length());
                }

//                if (mNum.getText().length() == 0) {
//                    mGetCashBtn.setEnabled(false);
//                } else {
//                    mGetCashBtn.setEnabled(true);
//                }
            }
        });


        cancel.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                onBackPressed();
            }
        });

        why.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                //翻转
                face.setVisibility(View.INVISIBLE);
                back.setVisibility(View.VISIBLE);
            }
        });

        backToJyb.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                face.setVisibility(View.VISIBLE);
                back.setVisibility(View.INVISIBLE);
            }
        });

        mStartDate.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                isStart = true;
                showTimePicker();
            }
        });

        mDeadDate.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                isStart = false;
                showTimePicker();
            }
        });

        mRepayType.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                showSelAccountDialog(JiyibiActivity.this, mTypes, new DialogSelectedListener() {
                    @Override
                    public void getSeletedItem(String reuslt) {
                        mRepayType.setText(reuslt);
                    }
                });
            }
        });

        mSubmit.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                amount = mNum.getText().toString().replaceAll(" ", "");
                if (mNum.getText().length() == 0) {
                    ToastUtil.show(JiyibiActivity.this, "请输入投资金额");
                    return;
                } else if(invest_date == null) {
                    ToastUtil.show(JiyibiActivity.this, "请选择开始日期");
                    return;
                } else if (expire_date == null) {
                    ToastUtil.show(JiyibiActivity.this, "请选择结束日期");
                    return;
                } else if (type_id == null){
                    ToastUtil.show(JiyibiActivity.this, "请选择回款方式");
                    return;
                }

                doSubmit();
            }
        });

        getRepayType();
    }


    /**
     * 显示日期选择器
     */
    TimePickerDialog mDialogAll;

    private void showTimePicker() {
//        mDialogAll = new TimePickerDialog.Builder()
//                .setCallBack(ActDetailActivity.this)
//                .setCancelStringId("cancel")
//                .setSureStringId("sure")
//                .setTitleStringId("TimePicker")
//                .setCyclic(false)
//                .setMinMillseconds(System.currentTimeMillis())
//                .setSelectorMillseconds(System.currentTimeMillis())
//                .setThemeColor(getResources().getColor(R.color.timepicker_dialog_bg))
//                .setType(Type.ALL)
//                .setWheelItemTextNormalColorId(R.color.timetimepicker_default_text_color)
//                .setWheelItemTextSelectorColorId(R.color.timepicker_toolbar_bg)
//                .setWheelItemTextSize(12)
//                .build();

        mDialogAll = new TimePickerDialog.Builder()
                .setType(Type.YEAR_MONTH_DAY)
                .setCallBack(this)
                .build();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDialogAll.show(JiyibiActivity.this.getSupportFragmentManager(), "year_month_day");
            }
        });

    }


    @Override
    public void onDateSet(TimePickerDialog timePickerView, long millseconds) {
        SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd");

        if (isStart) {
            if (mStartDate != null) {
                invest_date = sdf.format(new Date(millseconds));
                mStartDate.setText(invest_date + "");
            }
        } else {
            if (mDeadDate != null) {
                expire_date = sdf.format(new Date(millseconds));
                mDeadDate.setText(expire_date + "");
            }
        }

    }


    private String repayMsg;
    private ArrayList<RepayTypeInfo> mTypes = new ArrayList<>();

    private void getRepayType() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(Settings.BASE_URL + "repayType")
                        .addHeader("TOKEN", CommonUtil.getMobileUniqueId(JiyibiActivity.this))
                        .addHeader("authorization", DaoUtil.getAuthorization(JiyibiActivity.this))
                        .addHeader("CLIENT", "android")
                        .get()
                        .build();

                try {
                    Response response = client.newCall(request).execute();
                    JSONObject result = JSON.parseObject(response.body().string());

                    if (result.getIntValue("code") == 0) {
                        JSONArray array = result.getJSONArray("list");
                        mTypes.clear();
                        int length = array.size();
                        for (int i = 0; i < length; i++) {
                            RepayTypeInfo info = JSON.toJavaObject(array.getJSONObject(i), RepayTypeInfo.class);
                            mTypes.add(info);
                        }

                        // initDialog

                    } else {
                        repayMsg = result.getString("msg");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (repayMsg == null)
                                    return;
                                Toast.makeText(JiyibiActivity.this, repayMsg, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }


    /**
     * 选择对话框
     */
    public void showSelAccountDialog(Context context, ArrayList<RepayTypeInfo> list, final DialogSelectedListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final Dialog mDialog = builder.create();
        mDialog.show();
        mDialog.setContentView(R.layout.dialog_sel_account);
        mDialog.setCanceledOnTouchOutside(true);
        mDialog.setCancelable(true);
        mDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        WindowManager.LayoutParams params = mDialog.getWindow().getAttributes();
        params.width = Settings.DISPLAY_WIDTH;
        params.height = (int) (Settings.RATIO_HEIGHT * 360);
        params.gravity = Gravity.BOTTOM;
        mDialog.getWindow().setAttributes(params);

        final LoopView view = new LoopView(context);
        LinearLayout rootView = (LinearLayout) mDialog.findViewById(R.id.rootview);

//        if (list.size() <= 1) {
        view.setNotLoop();
//        }


        ArrayList<String>strList = new ArrayList<>();
        for (RepayTypeInfo info : list) {
            strList.add(info.name);
        }

        view.setItems(strList);
        view.setTextSize(20);
        view.setInitPosition(0);
        LinearLayout.LayoutParams loopparams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (Settings.RATIO_HEIGHT * 170));
        loopparams.gravity = Gravity.CENTER_HORIZONTAL;
        rootView.addView(view, loopparams);

        TextView cancel = (TextView) mDialog.findViewById(R.id.dialog_sel_account_cancel);
        TextView sure = (TextView) mDialog.findViewById(R.id.dialog_sel_account_sure);

        cancel.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                mDialog.dismiss();
            }
        });

        sure.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                mDialog.dismiss();
                type_id = view.getSelectedItem() + "";
                listener.getSeletedItem(mTypes.get(view.getSelectedItem()).name + "");
            }
        });
    }

    private void doSubmit() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();

                FormBody body = new FormBody.Builder()
                        .add("id", id)
                        .add("type", type)
                        .add("amount", amount)
                        .add("type_id", type_id)
                        .add("expire_date", expire_date)
                        .add("invest_date", invest_date)
                        .build();

                Request request = new Request.Builder().url(Settings.BASE_URL + "record")
                        .addHeader("TOKEN", CommonUtil.getMobileUniqueId(JiyibiActivity.this))
                        .addHeader("authorization", DaoUtil.getAuthorization(JiyibiActivity.this))
                        .addHeader("CLIENT", "android")
                        .post(body)
                        .build();

                try {
                    Response response = client.newCall(request).execute();
                    JSONObject result = JSON.parseObject(response.body().string());

                    if (result.getIntValue("code") == 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(JiyibiActivity.this, "记录成功", Toast.LENGTH_SHORT).show();
                                JiyibiActivity.this.finish();
                            }
                        });

                    } else {
                        repayMsg = result.getString("msg");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (repayMsg == null)
                                    return;
                                Toast.makeText(JiyibiActivity.this, repayMsg, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }
}
