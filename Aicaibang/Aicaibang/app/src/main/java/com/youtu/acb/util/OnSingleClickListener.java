package com.youtu.acb.util;

import android.view.View;
import android.view.View.OnClickListener;

import java.util.Timer;
import java.util.TimerTask;

public abstract class OnSingleClickListener implements OnClickListener {

	public boolean mEnable = true;

	public static final int mDelay = 300;

	@Override
	public void onClick(View v) {
		if (mEnable) {
			mEnable = false;
			doOnClick(v);
			new Timer().schedule(new TimerTask() {

				@Override
				public void run() {
					mEnable = true;
				}
			}, mDelay);
		}
	}

	public abstract void doOnClick(View v);
}
