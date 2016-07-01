package com.youtu.acb.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.youtu.acb.R;
import com.youtu.acb.common.Common;
import com.youtu.acb.common.Settings;
import com.youtu.acb.util.OnSingleClickListener;


/*
 * dialog
 */
public class DialogActivity extends BaseActivity {

	private RelativeLayout mRoot;
	private TextView mNote;
	private Button mIknowBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.dialog_indicate);
		
		LinearLayout content = (LinearLayout) findViewById(R.id.content_layout);
		content.getLayoutParams().width = Settings.DISPLAY_WIDTH * 11 / 15;
		mIknowBtn = (Button) findViewById(R.id.jiyibi_note_btn);

		mIknowBtn.setOnClickListener(new OnSingleClickListener() {
			@Override
			public void doOnClick(View v) {
				onBackPressed();
			}
		});
		
	}

	@Override
	public void onBackPressed() {
		getSharedPreferences(Common.sharedPrefName, MODE_PRIVATE).edit().putInt("jiyibi" , 1).commit();

		super.onBackPressed();
	}
}
