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
import android.os.Build;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.kunfei.bookshelf.R;

import androidx.appcompat.app.AppCompatActivity;

public class WebViewActivity extends AppCompatActivity {
    //声明引用
    private WebView mWVmhtml;
    private FrameLayout fullVideo;
    private View customView = null;
    private ProgressBar progressBar;

    public static void startThis(Context context) {
        Intent intent = new Intent(context, WebViewActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        //获取控件对象
        mWVmhtml=findViewById(R.id.WV_Id);
        fullVideo=findViewById(R.id.full_video);
        progressBar=findViewById(R.id.progress);

        //加载本地html文件
        // mWVmhtml.loadUrl("file:///android_asset/hello.html");
        //加载网络URL
        //mWVmhtml.loadUrl("https://blog.csdn.net/qq_36243942/article/details/82187204");
        //设置JavaScrip
        mWVmhtml.getSettings().setJavaScriptEnabled(true);
        mWVmhtml.getSettings().setMediaPlaybackRequiresUserGesture(false);
        //访问百度首页
        mWVmhtml.loadUrl("http://qiuyue.vicp.net:86/");
        //设置在当前WebView继续加载网页
        mWVmhtml.setWebViewClient(new MyWebViewClient());

        mWVmhtml.setWebChromeClient(new MyWebChromeClient());
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
                return true;
            }
            return super.shouldOverrideUrlLoading(view, request);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            progressBar.setVisibility(View.VISIBLE);
            Log.d("WebView","开始访问网页");
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            progressBar.setVisibility(View.GONE);
            Log.d("WebView","访问网页结束");
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