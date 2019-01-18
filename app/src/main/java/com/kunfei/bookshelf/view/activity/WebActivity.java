package com.kunfei.bookshelf.view.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.help.VideoEnabledWebChromeClient;
import com.kunfei.bookshelf.help.VideoEnabledWebView;
import com.kunfei.bookshelf.utils.Theme.ThemeStore;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class WebActivity extends AppCompatActivity
{
    private VideoEnabledWebView webView;
    private VideoEnabledWebChromeClient webChromeClient;
    private SwipeRefreshLayout refreshLayout;
    private ProgressBar progressBar;
    private View mErrorView;

    private boolean isSuccess = false;
    private boolean isError = false;
    private LinearLayout ll_control_error;
    private RelativeLayout error;

    public static void startThis(Context context) {
        Intent intent = new Intent(context, WebActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progress);
        refreshLayout = findViewById(R.id.refresh_layout);

        //View ll_control = getLayoutInflater().inflate(R.layout.activity_error, null);
        ll_control_error =  findViewById(R.id.ll_control_error);
        RelativeLayout error =  ll_control_error.findViewById(R.id.online_error_btn_retry);

        View nonVideoLayout = findViewById(R.id.nonVideoLayout);
        ViewGroup videoLayout = findViewById(R.id.videoLayout);
        View loadingView = getLayoutInflater().inflate(R.layout.view_loading_video, null);

        webChromeClient = new VideoEnabledWebChromeClient(nonVideoLayout, videoLayout, loadingView, webView)
        {
            @Override //监听加载进度
            public void onProgressChanged(WebView view, int progress)
            {
                progressBar.setProgress(progress);
                if(progress >= 100){
                    progressBar.setVisibility(View.GONE);
                }
                else{
                    progressBar.setVisibility(View.VISIBLE);//开始加载网页时显示进度条
                    progressBar.setProgress(progress);//设置进度值
                }
                super.onProgressChanged(view, progress);
            }
        };
        webChromeClient.setOnToggledFullscreen(new VideoEnabledWebChromeClient.ToggledFullscreenCallback()
        {
            @Override
            public void toggledFullscreen(boolean fullscreen)
            {
                if (fullscreen)
                {
                    WindowManager.LayoutParams attrs = getWindow().getAttributes();
                    attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
                    attrs.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    getWindow().setAttributes(attrs);
                    if (android.os.Build.VERSION.SDK_INT >= 19)
                    {
                        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
                    }
                }
                else
                {
                    WindowManager.LayoutParams attrs = getWindow().getAttributes();
                    attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
                    attrs.flags &= ~WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    getWindow().setAttributes(attrs);
                    if (android.os.Build.VERSION.SDK_INT >= 19)
                    {
                        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                    }
                }

            }
        });
        webView.setWebChromeClient(webChromeClient);
        webView.setWebViewClient(new InsideWebViewClient());
        webView.loadUrl("http://qiuyue.vicp.net:86/");

        refreshLayout.setColorSchemeColors(ThemeStore.accentColor(MApplication.getInstance()));
        refreshLayout.setOnRefreshListener(() -> {
            webView.reload();
            refreshLayout.setRefreshing(false);
        });

        error.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ll_control_error.setVisibility(View.GONE);
                webView.reload();
            }
        });

    }

    private class InsideWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            //super.onPageStarted(view, url, favicon);
            //Log.d("WebView","开始访问网页");
            if (!isError) {
                //webView.setVisibility(View.VISIBLE);
                ll_control_error.setVisibility(View.GONE);
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (!isError) {
                isSuccess = true;
                //回调成功后的相关操作
                ll_control_error.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
            } else {
                isError = false;
                ll_control_error.setVisibility(View.VISIBLE);
            }
        }

        @SuppressWarnings("deprecation")
        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            // 加载网页失败时处理 如：提示失败，或显示新的界面
            //if (view != null) {
            //    view.loadUrl("file:///android_asset/not_found.html");
            //}
            //Log.d("WebView","访问网页失败");
            isError = true;
            isSuccess = false;
            webView.setVisibility(View.GONE);
            ll_control_error.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed()
    {
        if (!webChromeClient.onBackPressed())
        {
            if (webView.canGoBack())
            {
                webView.goBack();
            }
            else
            {
                super.onBackPressed();
            }
        }
    }

}