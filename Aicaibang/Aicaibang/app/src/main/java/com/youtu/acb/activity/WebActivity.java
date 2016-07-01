package com.youtu.acb.activity;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.youtu.acb.R;
import com.youtu.acb.common.Settings;
import com.youtu.acb.util.OnSingleClickListener;

/**
 * Created by xingf on 16/6/21.
 */
public class WebActivity extends BaseActivity {

    private RelativeLayout mTitleBar;
    private FrameLayout mBack;
    private LinearLayout mRoot;
    private TextView mWebTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_web);

        mTitleBar = (RelativeLayout) findViewById(R.id.web_titlebar);
        mTitleBar.getLayoutParams().height = Settings.TITLEBAR_HEIGHT;
        mBack = (FrameLayout) findViewById(R.id.web_back);

        mBack.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void doOnClick(View v) {
                onBackPressed();
            }
        });

        WebView webView = new WebView(WebActivity.this);
        webView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        mRoot = (LinearLayout) findViewById(R.id.web_root);
        mWebTitle = (TextView) findViewById(R.id.web_title_tv);

        mWebTitle.setText(getIntent().getStringExtra("title"));

        String url = getIntent().getStringExtra("url");
        if (url == null) {
            url = "http://www.youtuker.com";
        }
        if (url.startsWith("http://") || url.startsWith("https://")) {

        } else {
            url = "http://" + url;
        }
        webView.loadUrl(url);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setSupportZoom(true);
        settings.setAllowFileAccess(true);
//        settings.setBuiltInZoomControls(true);
        settings.setAppCacheEnabled(false);
        settings.setUseWideViewPort(true);

        webView.setInitialScale(100);

        webView.removeJavascriptInterface("searchBoxJavaBredge_");

        mRoot.addView(webView);

        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return super.shouldOverrideUrlLoading(view, url);
            }
        });

    }
}
