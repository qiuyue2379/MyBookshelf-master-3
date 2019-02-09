package com.kunfei.bookshelf.view.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.kunfei.basemvplib.impl.IPresenter;
import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.base.MBaseActivity;
import com.kunfei.bookshelf.help.X5WebView;
import com.kunfei.bookshelf.utils.Theme.ThemeStore;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;
import com.tencent.smtt.export.external.interfaces.IX5WebChromeClient.CustomViewCallback;

import androidx.appcompat.app.ActionBar;
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
    @BindView(R.id.frame_web_video)
    FrameLayout video_fullView;

    private SearchView.SearchAutoComplete mSearchAutoComplete;
    private RelativeLayout error;
    private MyWebChromeClient mWebChromeClient;
    private View loadingView;
    private View xCustomView;
    private CustomViewCallback xCustomViewCallback;

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
        error = ll_control_error.findViewById(R.id.online_error_btn_retry);
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

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView webView, String s) {
                webView.loadUrl(s);
                return true;
            }
        });

    }

    @Override
    public void onBackPressed() {
        /** 回退键 事件处理 优先级:视频播放全屏-网页回退-关闭页面 */
        if (inCustomView()) {
            hideCustomView();
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
                webView.reload();
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
        /**
         * 全屏播放配置
         */
        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            super.onShowCustomView(view, callback);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            webView.setVisibility(View.INVISIBLE);
            // 如果一个视图已经存在，那么立刻终止并新建一个
            if (xCustomView != null) {
                callback.onCustomViewHidden();
                return;
            }
            view.setVisibility(View.VISIBLE);
            video_fullView.addView(view);
            xCustomView = view;
            xCustomView.setVisibility(View.VISIBLE);
            xCustomViewCallback = callback;
            video_fullView.setVisibility(View.VISIBLE);
        }

        @Override
        public void onHideCustomView() {
            super.onHideCustomView();
            if (xCustomView == null){
                // 不是全屏播放状态
                return;
            }
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            xCustomView.setVisibility(View.GONE);
            video_fullView.removeView(xCustomView);
            xCustomView = null;
            video_fullView.setVisibility(View.GONE);
            xCustomViewCallback.onCustomViewHidden();
            webView.setVisibility(View.VISIBLE);
        }

        //视频加载添加默认图标
        @Override
        public Bitmap getDefaultVideoPoster() {
            final Bitmap bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawARGB(255, 0, 0, 0);
            return bitmap;
        }

        @Override
        public View getVideoLoadingProgressView()
        {
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

    /**
     * 判断是否是全屏
     *
     * @return
     */
    public boolean inCustomView() {
        return (xCustomView != null);
    }

    /**
     * 全屏时按返加键执行退出全屏方法
     */
    public void hideCustomView() {
        mWebChromeClient.onHideCustomView();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
}