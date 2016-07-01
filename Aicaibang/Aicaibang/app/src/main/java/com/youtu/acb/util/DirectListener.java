package com.youtu.acb.util;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * Created by xingf on 16/5/30.
 */
public class DirectListener extends OnSingleClickListener {
    private Context mOrigin;
    private Class mTarget;
    private Bundle bundle;

    public DirectListener(Context originActivity, Class targetActivity) {
        mOrigin = originActivity;
        mTarget = targetActivity;
    }

    public DirectListener(Context originActivity, Class targetActivity, Bundle bd) {
        mOrigin = originActivity;
        mTarget = targetActivity;
        bundle = bd;
    }

    @Override
    public void doOnClick(View v) {

        if (bundle != null) {
            mOrigin.startActivity(new Intent(mOrigin, mTarget).putExtras(bundle));
        } else {
            mOrigin.startActivity(new Intent(mOrigin, mTarget));
        }
    }
}
