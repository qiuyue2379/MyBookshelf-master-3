package com.kunfei.bookshelf.view.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.kunfei.basemvplib.impl.IPresenter;
import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.base.MBaseActivity;
import com.kunfei.bookshelf.help.WebViewJavaScriptFunction;
import com.kunfei.bookshelf.help.X5WebView;
import com.kunfei.bookshelf.utils.Theme.ThemeStore;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import butterknife.ButterKnife;

public class x5webActivity extends MBaseActivity {
    @BindView(R.id.searchView)
    SearchView searchView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout refreshLayout;
    @BindView(R.id.root)
    ViewGroup rootView;
    @BindView(R.id.web_filechooser)
    X5WebView webView;
    @BindView(R.id.ll_control_error)
    LinearLayout ll_control_error;
    @BindView(R.id.progress)
    ProgressBar progressBar;

    private SearchView.SearchAutoComplete mSearchAutoComplete;
    private RelativeLayout error;
    private String searchKey;
    private boolean showHistory;
    private MyWebChromeClient mWebChromeClient;
    private View customView = null;
    private View loadingView;
    boolean isFullScrenn = false;
    Handler handler=new Handler(Looper.getMainLooper());

    public static void startThis(Context context) {
        Intent intent = new Intent(context, x5webActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected IPresenter initInjector() {
        return null;
    }

    @Override
    protected void onCreateActivity() {
        getWindow().getDecorView().setBackgroundColor(ThemeStore.backgroundColor(this));
        setContentView(R.layout.x5);
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void bindView() {
        ButterKnife.bind(this);
        initSearchView();
        this.setSupportActionBar(toolbar);
        setupActionBar();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RelativeLayout error =  ll_control_error.findViewById(R.id.online_error_btn_retry);
        loadingView = getLayoutInflater().inflate(R.layout.view_loading_video, null);

        mWebChromeClient = new MyWebChromeClient();
        webView.setWebChromeClient(mWebChromeClient);

        webView.loadUrl("http://qiuyue.vicp.net:86/");

        refreshLayout.setColorSchemeColors(ThemeStore.accentColor(MApplication.getInstance()));
        refreshLayout.setOnRefreshListener(() -> {
            webView.reload();
            refreshLayout.setRefreshing(false);
        });

        error.setOnClickListener(View -> {
            ll_control_error.setVisibility(View.GONE);
            webView.reload();
        });

        getWindow().setFormat(PixelFormat.TRANSLUCENT);

        webView.getView().setOverScrollMode(View.OVER_SCROLL_ALWAYS);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        try {
            super.onConfigurationChanged(newConfig);
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                Log.d("ppp", "onConfigurationChanged: " + "ORIENTATION_LANDSCAPE");
            } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                Log.d("ppp", "onConfigurationChanged: " + "ORIENTATION_PORTRAIT");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    FullscreenHolder fullscreenHolder;
    protected static final FrameLayout.LayoutParams COVER_SCREEN_PARAMS = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

    // 向webview发出信息
    private void enterFullScreenOnMainThread(){
        if (Looper.myLooper()!=Looper.getMainLooper()){
            handler.post(() -> enterFullScreen());
        }else {
            enterFullScreen();
        }
    }

    private void enterFullScreen() {
        FrameLayout decorView = (FrameLayout) getWindow().getDecorView();
        if (null != webView && null != rootView) {
            rootView.removeView(webView);
        }
        fullscreenHolder = new FullscreenHolder(x5webActivity.this);
        fullscreenHolder.addView(webView, COVER_SCREEN_PARAMS);
        decorView.addView(fullscreenHolder, COVER_SCREEN_PARAMS);
        //横屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setStatusBarVisibility(false);
        isFullScrenn=true;
    }

    private void exitFullScreenOnMainThread(){
        if (Looper.myLooper()!=Looper.getMainLooper()){
            handler.post(() -> exitFullScreen());
        }else {
            exitFullScreen();
        }
    }

    private void exitFullScreen() {
        FrameLayout decorView = (FrameLayout) getWindow().getDecorView();
        fullscreenHolder.removeAllViews();
        decorView.removeView(fullscreenHolder);
        fullscreenHolder = null;
        rootView.addView(webView);
        //竖屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setStatusBarVisibility(true);
        isFullScrenn=false;
    }

    /**
     * 全屏容器界面
     */
    static class FullscreenHolder extends FrameLayout {

        public FullscreenHolder(Context ctx) {
            super(ctx);
            setBackgroundColor(ctx.getResources().getColor(android.R.color.holo_red_dark));
        }

        @Override
        public boolean onTouchEvent(MotionEvent evt) {
            return true;
        }
    }

    private void setStatusBarVisibility(boolean visible) {
        int flag = visible ? 0 : WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setFlags(flag, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    public void onBackPressed() {
        /** 回退键 事件处理 优先级:视频播放全屏-网页回退-关闭页面 */
        if (isFullScrenn) {
            exitFullScreenOnMainThread();
        } else if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    //设置ToolBar
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.vod);
        }
    }

    // 添加菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_web_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //菜单
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_book_source_manage:
                BookSourceActivity.startThis(this);
                break;
            case R.id.clear_cookie:
                clearWebViewCache();
                showSnackBar(toolbar, "成功为您清除Cookie，请刷新网页!");
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void clearWebViewCache() {
        // 清除cookie即可彻底清除缓存
        CookieSyncManager.createInstance(this);
        CookieManager.getInstance().removeAllCookie();
        CookieManager.getInstance().removeSessionCookie();
        CookieSyncManager.getInstance().sync();
        CookieSyncManager.getInstance().startSync();
    }

    private void initSearchView() {
        mSearchAutoComplete = searchView.findViewById(R.id.search_src_text);
        searchView.setQueryHint(getString(R.string.vod));
        mSearchAutoComplete.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        mSearchAutoComplete.setPadding(15, 0, 0, 0);
    }

    class MyWebChromeClient extends WebChromeClient{

        @Override
        public void onHideCustomView() {
            exitFullScreenOnMainThread();
        }

        //视频加载添加默认图标
        @Override
        public Bitmap getDefaultVideoPoster() {
            final Bitmap bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawARGB(255, 0, 0, 0);
            return bitmap;
        }

        //视频加载时进程loading
        @Override
        public View getVideoLoadingProgressView() {
            if (loadingView != null) {
                loadingView.setVisibility(View.VISIBLE);
                return loadingView;
            } else {
                return super.getVideoLoadingProgressView();
            }
        }

        @Override
        public void onProgressChanged(WebView webView, int newProgress) {
            if (newProgress >= 100) {
                progressBar.setVisibility(View.GONE);
            } else {
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(newProgress);
            }
            super.onProgressChanged(webView, newProgress);
        }

        @Override
        public void onReceivedTitle(WebView webView, String s) {
            super.onReceivedTitle(webView, s);
            searchView.setQueryHint(s);
        }
    };
}