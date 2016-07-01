package com.youtu.acb.util;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.youtu.acb.encode.Base64;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by xingf on 15/12/28.
 */
public class CommonUtil {
    public static String authorizeStr(String id, String token) {
        return "Basic " + new String(Base64.encode((id + ":" + token).getBytes(), Base64.NO_WRAP));
    }

    /*
     * close keyboard
	 */
    public static void closeKeyboard(Context context, View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /*
     * show keyboard
     */
    public static void showKeyboard(Context context, View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        // inputMethodManager.showSoftInputFromInputMethod(view.getWindowToken(),
        // 0);
        inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_FORCED);
    }


    public static final String ARG_REVEAL_START_LOCATION = "reveal_start_location";

    public static void startUserProfileFromLocation(int[] startingLocation, Activity startingActivity, Class target, String storeId) {
        Intent intent = new Intent(startingActivity, target);
        intent.putExtra(ARG_REVEAL_START_LOCATION, startingLocation);
        intent.putExtra("storeid", storeId);
        startingActivity.startActivity(intent);
    }


    public static String getMobileUniqueId(Context context) {
        String identifier = null;
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm != null)
            identifier = tm.getDeviceId();
        if (identifier == null || identifier.length() == 0 || identifier.equals("000000000000000"))
            identifier = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        return identifier;
    }

    public static String getChannelCodeThroughActivityInfo(Activity context) {
        String code = getMetaDataThroughActivityInfo(context, "CHANNEL");
        if (code != null) {
            return code;
        }
        return "C_000";
    }

    private static String getMetaDataThroughActivityInfo(Activity context, String key) {
        try {
            ActivityInfo ai = context.getPackageManager().getActivityInfo(context.getComponentName(),
                    PackageManager.GET_META_DATA);
            Object value = ai.metaData.get(key);
            if (value != null) {
                return value.toString();
            }
        } catch (Exception e) {
            //
        }
        return null;
    }

    public static String getChannelCode(Context context) {
        String code = getMetaData(context, "CHANNEL");
        if (code != null) {
            return code;
        }
        return "C_000";
    }

    private static String getMetaData(Context context, String key) {
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(),
                    PackageManager.GET_META_DATA);
            Object value = ai.metaData.get(key);
            if (value != null) {
                return value.toString();
            }
        } catch (Exception e) {
            //
        }
        return null;
    }


    //版本名
    public static String getVersionName(Context context) {
        return getPackageInfo(context).versionName;
    }

    //版本号
    public static int getVersionCode(Context context) {
        return getPackageInfo(context).versionCode;
    }

    private static PackageInfo getPackageInfo(Context context) {
        PackageInfo pi = null;

        try {
            PackageManager pm = context.getPackageManager();
            pi = pm.getPackageInfo(context.getPackageName(),
                    PackageManager.GET_CONFIGURATIONS);

            return pi;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return pi;
    }


    public static void setPricePoint(final EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                if (s.toString().contains(".")) {
                    if (s.length() - 1 - s.toString().indexOf(".") > 2) {
                        s = s.toString().subSequence(0,
                                s.toString().indexOf(".") + 3);
                        editText.setText(s);
                        editText.setSelection(s.length());
                    }
                }
                if (s.toString().trim().substring(0).equals(".")) {
                    s = "0" + s;
                    editText.setText(s);
                    editText.setSelection(2);
                }

                if (s.toString().startsWith("0")
                        && s.toString().trim().length() > 1) {
                    if (!s.toString().substring(1, 2).equals(".")) {
                        editText.setText(s.subSequence(0, 1));
                        editText.setSelection(1);
                        return;
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub

            }

        });

    }

    public static boolean isMobileNO(String mobiles) {
        Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");
        Matcher m = p.matcher(mobiles);

        return m.matches();
    }
}
