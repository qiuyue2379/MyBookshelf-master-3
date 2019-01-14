package com.kunfei.bookshelf.view.activity;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.WindowManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.os.Build;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.utils.Theme.ThemeStore;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;

public class WebActivity extends AppCompatActivity{
    private WebView mWVmhtml;
    private FrameLayout fullVideo;
    private View customView = null;
    private ProgressBar progressBar;
    private SwipeRefreshLayout refreshLayout;
    private Bitmap xdefaltvideo;
    private View xprogressvideo;

    public static void startThis(Context context) {
        Intent intent = new Intent(context, WebActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        //获取控件对象
        mWVmhtml=findViewById(R.id.WebView);
        fullVideo=findViewById(R.id.full_video);
        progressBar=findViewById(R.id.progress);
        refreshLayout = findViewById(R.id.refresh_layout);
        initWebView();
    }

    /**
     * 数据初始化
     */
    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView() {
        WebSettings settings = mWVmhtml.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setDefaultTextEncodingName("utf-8");
        //访问首页
        mWVmhtml.loadUrl("http://qiuyue.vicp.net:86/");
        //设置在当前WebView继续加载网页
        mWVmhtml.setWebViewClient(new MyWebViewClient());
        mWVmhtml.setWebChromeClient(new MyWebChromeClient());

        refreshLayout.setColorSchemeColors(ThemeStore.accentColor(MApplication.getInstance()));
        refreshLayout.setOnRefreshListener(() -> {
            mWVmhtml.reload();
            refreshLayout.setRefreshing(false);
        });
    }

    class MyWebViewClient extends WebViewClient{

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {  //过时的方法用于sdk版本小于21的
            view.loadUrl(url);
            return super.shouldOverrideUrlLoading(view, url);
        }

        @Override  //WebView代表是当前的WebView
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            //表示在当前的WebView继续打开网页
            if(Build.VERSION.SDK_INT>Build.VERSION_CODES.LOLLIPOP) {  //当Sdk版本大于21时才能使用此方法
                view.loadUrl(request.getUrl().toString());
                return false;
            }
            return super.shouldOverrideUrlLoading(view, request);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            Log.d("WebView","开始访问网页");
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            Log.d("WebView","访问网页结束");
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            // 加载网页失败时处理 如：提示失败，或显示新的界面
            Log.d("WebView","访问网页失败");
        }
    }

    class MyWebChromeClient extends WebChromeClient{
        @Override
        public void onHideCustomView() {

            if (customView == null){
                return;
            }
            fullVideo.removeView(customView);
            fullVideo.setVisibility(View.GONE);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);//清除全屏

        }

        //视频加载添加默认图标
        @Override
        public Bitmap getDefaultVideoPoster() {
            //Log.i(LOGTAG, "here in on getDefaultVideoPoster");
            if (xdefaltvideo == null) {
                xdefaltvideo = BitmapFactory.decodeResource(
                        getResources(), R.drawable.videoicon);
            }
            return xdefaltvideo;
        }
        //视频加载时进程loading
        @Override
        public View getVideoLoadingProgressView() {
            //Log.i(LOGTAG, "here in on getVideoLoadingPregressView");
            if (xprogressvideo == null) {
                LayoutInflater inflater = LayoutInflater.from(WebActivity.this);
                xprogressvideo = inflater.inflate(R.layout.video_loading_progress, null);
            }
            return xprogressvideo;
        }

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            customView = view;
            fullVideo.setVisibility(View.VISIBLE);
            fullVideo.addView(customView);
            fullVideo.bringToFront();
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏
        }

        @Override //监听加载进度
        public void onProgressChanged(WebView view, int newProgress) {
            progressBar.setProgress(newProgress);
            if(newProgress >= 100){
                progressBar.setVisibility(View.GONE);
            }
            else{
                progressBar.setVisibility(View.VISIBLE);//开始加载网页时显示进度条
                progressBar.setProgress(newProgress);//设置进度值
            }
            super.onProgressChanged(view, newProgress);
        }
        @Override//接受网页标题
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            //把当前的Title设置到Activity的title上显示
            setTitle(title);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //如果按返回键，此时WebView网页可以后退
        if (keyCode==KeyEvent.KEYCODE_BACK&&mWVmhtml.canGoBack()){
            mWVmhtml.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}