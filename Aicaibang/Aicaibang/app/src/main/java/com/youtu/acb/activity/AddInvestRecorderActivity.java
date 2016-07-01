package com.youtu.acb.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
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
import com.youtu.acb.Views.Titlebar;
import com.youtu.acb.Views.wheelview.LoopView;
import com.youtu.acb.common.Settings;
import com.youtu.acb.entity.RepayTypeInfo;
import com.youtu.acb.interfaces.DialogSelectedListener;
import com.youtu.acb.util.CommonUtil;
import com.youtu.acb.util.DaoUtil;
import com.youtu.acb.util.OnSingleClickListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by xingf on 16/6/30.
 */
public class AddInvestRecorderActivity extends AppCompatActivity implements OnDateSetListener {

    private Titlebar mTitleBar;
    private TextView mPlat;
    private EditText mInvestNum;
    private TextView mSdate;
    private TextView mEdate;
    private TextView mRepayType;
    private Button mSaveBtn;
    private boolean isSetStartDate;

    private boolean hasPlat, hasNum, hasSdate, hasEdate, hasRepay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ar);

        mTitleBar = (Titlebar) findViewById(R.id.add_invest_record);
        mTitleBar.getLayoutParams().height = Settings.TITLEBAR_HEIGHT;
        mTitleBar.getmLeftPart().setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                onBackPressed();
            }
        });

        mTitleBar.setTitle("添加投资记录");

        mSaveBtn = (Button) findViewById(R.id.add_invest_rec_btn);
        mPlat = (TextView) findViewById(R.id.add_record_plat);
        mInvestNum = (EditText) findViewById(R.id.add_record_amount);
        mSdate = (TextView) findViewById(R.id.add_record_start_date);
        mEdate = (TextView) findViewById(R.id.add_record_end_date);
        mRepayType = (TextView) findViewById(R.id.add_record_repay);

        mPlat.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                startActivityForResult(new Intent(AddInvestRecorderActivity.this, PlatSelActivity.class), 10231);
            }
        });

        mSdate.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                isSetStartDate = true;
                showTimePicker();
            }
        });

        mEdate.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                isSetStartDate = false;
                showTimePicker();
            }
        });

        mRepayType.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                showSelAccountDialog(AddInvestRecorderActivity.this, mTypes, new DialogSelectedListener() {
                    @Override
                    public void getSeletedItem(String reuslt) {
                        mRepayType.setText(reuslt);
                    }
                });
            }
        });


        mInvestNum.addTextChangedListener(new TextWatcher() {
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
                    mInvestNum.setText("0.");
                } else if (str.length() == 2 && str.startsWith("0") && !str.startsWith("0.")) {  // 0.
                    mInvestNum.setText("0." + str.substring(1, 2));
                    mInvestNum.setSelection(3);
                } else if (hasDot && str.lastIndexOf(".") == str.length() - 1) { // double dot
                    mInvestNum.setText(str.substring(0, str.length() - 1));
                    mInvestNum.setSelection(mInvestNum.getText().length());
                } else if (hasDot && str.length() - str.indexOf(".") == 4) { // more than 2 digits after dot
                    mInvestNum.setText(str.toString().substring(0, str.length() - 1));
                    mInvestNum.setSelection(str.length() - 1);
                } else if (str.endsWith(".0")) {
                    mInvestNum.setText(str + "0");
                    mInvestNum.setSelection(str.length());
                }

                if (editable.length() > 0) {
                    hasNum = true;
                } else {
                    hasNum = false;
                }
                checkState();
            }
        });

        mSaveBtn.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                doSubmit();
            }
        });

        getRepayType();
    }


    TimePickerDialog mDialogAll;

    private void showTimePicker() {
        mDialogAll = new TimePickerDialog.Builder()
                .setType(Type.YEAR_MONTH_DAY)
                .setCallBack(this)
                .build();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDialogAll.show(AddInvestRecorderActivity.this.getSupportFragmentManager(), "year_month_day");
            }
        });

    }

    /**
     * 检查按钮状态
     */
    private void checkState() {
        if (hasPlat && hasNum && hasSdate && hasEdate && hasRepay) {
            mSaveBtn.setEnabled(true);
        } else {
            mSaveBtn.setEnabled(false);
        }
    }

    String id, name;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 10004 && data != null) {
            // 返回选中平台名
            name = data.getStringExtra("name");
            if (!TextUtils.isEmpty(name)) {
                mPlat.setText(name);
                mPlat.setTextColor(Color.parseColor("#333333"));
                hasPlat = true;

                id = data.getStringExtra("id");
                if (id == null) {
                    id = "0";
                }
                checkState();
            } else {
                hasPlat = false;
            }
        }
    }


    private String expire_date;
    private String invest_date;

    @Override
    public void onDateSet(TimePickerDialog timePickerView, long millseconds) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        if (isSetStartDate) {
            invest_date = sdf.format(new Date(millseconds));
            mSdate.setText(invest_date + "");
            hasSdate = true;
            checkState();
        } else {
            expire_date = sdf.format(new Date(millseconds));
            mEdate.setText(expire_date + "");

            hasEdate = true;
            checkState();
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
                        .addHeader("TOKEN", CommonUtil.getMobileUniqueId(AddInvestRecorderActivity.this))
                        .addHeader("authorization", DaoUtil.getAuthorization(AddInvestRecorderActivity.this))
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
                                Toast.makeText(AddInvestRecorderActivity.this, repayMsg, Toast.LENGTH_SHORT).show();
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
    String type_id;

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


        ArrayList<String> strList = new ArrayList<>();
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
                hasRepay = true;
                checkState();
            }
        });
    }

    private void doSubmit() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();

                FormBody body = new FormBody.Builder()
                        .add("pid", id)
                        .add("name", name)
                        .add("amount", mInvestNum.getText().toString())
                        .add("type_id", type_id)
                        .add("expire_date", expire_date)
                        .add("invest_date", invest_date)
                        .build();

                Request request = new Request.Builder().url(Settings.BASE_URL + "investRecord")
                        .addHeader("TOKEN", CommonUtil.getMobileUniqueId(AddInvestRecorderActivity.this))
                        .addHeader("authorization", DaoUtil.getAuthorization(AddInvestRecorderActivity.this))
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
                                Toast.makeText(AddInvestRecorderActivity.this, "记录成功", Toast.LENGTH_SHORT).show();
                                AddInvestRecorderActivity.this.finish();
                            }
                        });

                    } else {
                        repayMsg = result.getString("msg");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (repayMsg == null)
                                    return;
                                Toast.makeText(AddInvestRecorderActivity.this, repayMsg, Toast.LENGTH_SHORT).show();
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
