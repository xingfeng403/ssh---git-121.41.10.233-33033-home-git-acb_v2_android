package com.youtu.acb.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.youtu.acb.R;
import com.youtu.acb.Views.Titlebar;
import com.youtu.acb.common.Settings;
import com.youtu.acb.entity.GoldInfo;
import com.youtu.acb.entity.PlatSelInfo;
import com.youtu.acb.util.CommonUtil;
import com.youtu.acb.util.DaoUtil;
import com.youtu.acb.util.OnSingleClickListener;

import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PlatSelActivity extends BaseActivity {

    private Titlebar mTitleBar;
    private ListView mListView;
    private ArrayList<PlatSelInfo> mInfos = new ArrayList();
    private PlatListAdapter mAdapter;
    private PlatSelInfo mSelInfo;
    private EditText mNameEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plat_sel);

        mTitleBar = (Titlebar) findViewById(R.id.plat_sel_titlebar);
        mNameEdit = (EditText) findViewById(R.id.plat_rep_name);
        mTitleBar.getLayoutParams().height = Settings.TITLEBAR_HEIGHT;
        mTitleBar.setTitle("平台选择");
        mTitleBar.getmLeftPart().setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                PlatSelActivity.this.finish();
            }
        });

        mListView = (ListView) findViewById(R.id.plat_list);
        mAdapter = new PlatListAdapter();
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mSelInfo = mInfos.get(i);
                onBackPressed();
            }
        });

        getPlats();
    }


    class PlatListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mInfos.size();
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
            view = getLayoutInflater().inflate(R.layout.item_plat_sel, null);
            PlatSelInfo info = mInfos.get(i);

            TextView name = (TextView) view.findViewById(R.id.plat_sel_item_name);
            name.setText(info.name == null ? "" : info.name);

            return view;
        }
    }


    private String errMsg;

    private void getPlats() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(Settings.BASE_URL + "report").addHeader("CLIENT", "android")
                        .addHeader("TOKEN", CommonUtil.getMobileUniqueId(PlatSelActivity.this))
                        .addHeader("authorization", DaoUtil.getAuthorization(PlatSelActivity.this))
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    JSONObject result = JSON.parseObject(response.body().string());

                    if (result.getIntValue("code") == 0) {
                        JSONArray array = result.getJSONArray("list");
                        if (array != null && array.size() > 0) {

                            int length = array.size();
                            for (int i = 0; i < length; i++) {
                                PlatSelInfo info = JSON.toJavaObject(array.getJSONObject(i), PlatSelInfo.class);

                                mInfos.add(info);
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                    } else {
                        errMsg = result.getString("msg");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(PlatSelActivity.this, errMsg, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (Exception e) {
                }
            }
        }).start();
    }


    @Override
    public void onBackPressed() {
        if (mSelInfo != null) {
            Intent result = new Intent();
            result.putExtra("id", mSelInfo.id);
            result.putExtra("name", mSelInfo.name);
            setResult(10004, result);
        } else if (mNameEdit.getText().toString().replaceAll(" ", "").length() > 0) {
            Intent result = new Intent();
            result.putExtra("name",mNameEdit.getText().toString());
            setResult(10004, result);
        }

        super.onBackPressed();
    }
}
