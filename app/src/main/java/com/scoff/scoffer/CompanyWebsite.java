package com.scoff.scoffer;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class CompanyWebsite extends Activity {
    private WebView engine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);

        Bundle bundle = this.getIntent().getExtras();
        String loadUrl = bundle.getString("url");

        // initialize the browser object
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressbar);
        engine = (WebView) findViewById(R.id.web_engine);

        engine.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                progressBar.setProgress(progress);
            }
        });

        engine.setWebViewClient(new FixedWebViewClient() {
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                progressBar.setVisibility(View.VISIBLE);
            }

            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed(); // Ignore SSL certificate errors
            }
        });

        engine.setPadding(0, 0, 0, 0);
        engine.setInitialScale(100);
//        engine.setWebContentsDebuggingEnabled(false);
        engine.getSettings().setJavaScriptEnabled(true);
        engine.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        engine.setScrollbarFadingEnabled(false);
//        engine.getSettings().setLoadWithOverviewMode(true);
        engine.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        engine.getSettings().setSupportZoom(true);
        engine.getSettings().setBuiltInZoomControls(true);
        engine.getSettings().setDisplayZoomControls(false);
        engine.getSettings().setUseWideViewPort(true);

        try {
            engine.loadUrl(loadUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class FixedWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(event.getAction() == KeyEvent.ACTION_DOWN){
            switch(keyCode)
            {
                case KeyEvent.KEYCODE_BACK:
                    if(engine.canGoBack()){
                        engine.goBack();
                    }else{
                        finish();
                    }
                    return true;
            }

        }
        return super.onKeyDown(keyCode, event);
    }


}
