package com.kunfei.bookshelf.view.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.graphics.Bitmap;
import android.os.Handler;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceError;
import android.widget.ProgressBar;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.net.http.SslError;
import android.os.Build;

import com.kunfei.basemvplib.impl.IPresenter;
import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.base.MBaseActivity;
import com.kunfei.bookshelf.help.VideoEnabledWebChromeClient;
import com.kunfei.bookshelf.help.VideoEnabledWebView;
import com.kunfei.bookshelf.utils.Theme.ThemeStore;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import butterknife.ButterKnife;

public class WebActivity extends MBaseActivity {
    @BindView(R.id.searchView)
    SearchView searchView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.webView)
    VideoEnabledWebView webView;
    @BindView(R.id.progress)
    ProgressBar progressBar;
    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout refreshLayout;
    @BindView(R.id.ll_control_error)
    LinearLayout ll_control_error;
    @BindView(R.id.nonVideoLayout)
    View nonVideoLayout;
    @BindView(R.id.videoLayout)
    ViewGroup videoLayout;

    private VideoEnabledWebChromeClient webChromeClient;
    private SearchView.SearchAutoComplete mSearchAutoComplete;
    private boolean isSuccess = false;
    private boolean isError = false;
    private RelativeLayout error;
    private String searchKey;
    private boolean showHistory;

    public static void startThis(Context context) {
        Intent intent = new Intent(context, WebActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected IPresenter initInjector() {
        return null;
    }

    @Override
    protected void onCreateActivity() {
        getWindow().getDecorView().setBackgroundColor(ThemeStore.backgroundColor(this));
        setContentView(R.layout.activity_web_view);
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
        View loadingView = getLayoutInflater().inflate(R.layout.view_loading_video, null);

        webChromeClient = new VideoEnabledWebChromeClient(this,nonVideoLayout, videoLayout, loadingView, webView) {
            @Override //监听加载进度
            public void onProgressChanged(WebView view, int progress) {
                progressBar.setProgress(progress);
                if(progress >= 100){
                    progressBar.setVisibility(View.GONE);
                }else{
                    progressBar.setVisibility(View.VISIBLE);//开始加载网页时显示进度条
                    progressBar.setProgress(progress);//设置进度值
                }
                super.onProgressChanged(view, progress);
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                searchView.setQueryHint(title);
                if (title.contains("404")){
                    isError = true;
                    isSuccess = false;
                    webView.setVisibility(View.GONE);
                    ll_control_error.setVisibility(View.VISIBLE);
                }
            }
        };

        webChromeClient.setOnToggledFullscreen(fullscreen -> {
                if (fullscreen) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                } else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
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

        error.setOnClickListener(View -> {
                ll_control_error.setVisibility(View.GONE);
                webView.reload();
        });

    }

    private class InsideWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override  //WebView代表是当前的WebView
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            //表示在当前的WebView继续打开网页
            if(Build.VERSION.SDK_INT>Build.VERSION_CODES.LOLLIPOP) {  //当Sdk版本大于21时才能使用此方法
                view.loadUrl(request.getUrl().toString());
                return false;
            }
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            if (!isError) {
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
            isError = true;
            isSuccess = false;
            webView.setVisibility(View.GONE);
            ll_control_error.setVisibility(View.VISIBLE);
        }

        //处理网页加载失败时
        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            isError = true;
            isSuccess = false;
            webView.setVisibility(View.GONE);
            ll_control_error.setVisibility(View.VISIBLE);
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                webView.getSettings()
                        .setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            }
            handler.proceed();// 接受所有网站的证书
        }
    }

    @Override
    public void onBackPressed() {
        if (!webChromeClient.onBackPressed()) {
            if (webView.canGoBack()) {
                webView.goBack();
            }
            else
            {
                super.onBackPressed();
            }
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
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //在文字改变的时候回调，query是改变之后的文字
                if (TextUtils.isEmpty(query))
                    return false;
                searchKey = query.trim();
                if (!searchKey.toLowerCase().startsWith("http:")) {
                    toSearch();
                    searchView.clearFocus();
                    return false;
                } else {
                    finish();
                    return false;
                }
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //文字提交的时候哦回调，newText是最后提交搜索的文字
                if (!newText.toLowerCase().startsWith("http")) {

                } else {
                    //webView.loadUrl(newText);
                }
                return false;
            }
        });

        searchView.setOnQueryTextFocusChangeListener((view, b) -> {
            showHistory = b;
            if (!b && searchView.getQuery().toString().trim().equals("")) {
                finish();
            }
            if (showHistory) {
                webView.loadUrl(searchView.getQuery().toString().trim());
            }
        });
    }

    private void toSearch() {
        if (!TextUtils.isEmpty(searchKey)) {
            //执行搜索请求
            new Handler().postDelayed(() -> {
                if (!searchKey.toLowerCase().startsWith("http")) {
                    webView.loadUrl("http://" + searchKey);
                }else{
                    webView.loadUrl(searchKey);
                }
            }, 300);
        }
    }
}