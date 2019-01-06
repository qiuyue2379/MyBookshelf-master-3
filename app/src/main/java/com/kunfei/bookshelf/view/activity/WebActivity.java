package com.kunfei.bookshelf.view.activity;

import android.content.pm.ActivityInfo;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.WindowManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.KeyEvent;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.graphics.Color;
import android.os.Build;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.os.Handler;

import com.kunfei.bookshelf.R;

import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;

public class WebActivity extends AppCompatActivity{
    private WebView mWVmhtml;
    private FrameLayout fullVideo;
    private View customView = null;
    private ProgressBar progressBar;
    private WaveSwipeRefreshLayout mWaveSwipeRefreshLayout;
    private Handler handler = new Handler();

    public static void startThis(Context context) {
        Intent intent = new Intent(context, WebActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
        mWaveSwipeRefreshLayout = findViewById(R.id.wave);

        initWebView();
    }

    private void initWebView() {
        mWVmhtml.getSettings().setJavaScriptEnabled(true);
        mWVmhtml.getSettings().setMediaPlaybackRequiresUserGesture(false);
        mWVmhtml.getSettings().setDefaultTextEncodingName("utf-8");
        //访问首页
        mWVmhtml.loadUrl("http://qiuyue.vicp.net:86/");
        //设置在当前WebView继续加载网页
        mWVmhtml.setWebViewClient(new MyWebViewClient());
        mWVmhtml.setWebChromeClient(new MyWebChromeClient());

        //设置中间小圆从白色到黑色
        mWaveSwipeRefreshLayout.setColorSchemeColors(Color.WHITE, Color.BLACK);
        //设置整体的颜色
        mWaveSwipeRefreshLayout.setWaveColor(Color.argb(255,74, 134, 232));
        //下拉刷新
        mWaveSwipeRefreshLayout.setOnRefreshListener(new WaveSwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mWVmhtml.reload();
                        //三秒后停止刷新
                        mWaveSwipeRefreshLayout.setRefreshing(false);
                    }
                },3000);
            }
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