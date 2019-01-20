package com.kunfei.bookshelf.help;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.widget.FrameLayout;
import android.widget.VideoView;

public class VideoEnabledWebChromeClient extends WebChromeClient implements OnPreparedListener, OnCompletionListener, OnErrorListener {
    public interface ToggledFullscreenCallback {
        void toggledFullscreen(boolean fullscreen);
    }

    private View activityNonVideoView;
    private ViewGroup activityVideoView;
    private View loadingView;
    private VideoEnabledWebView webView;
    private boolean isVideoFullscreen;
    private FrameLayout videoViewContainer;
    private CustomViewCallback videoViewCallback;
    private ToggledFullscreenCallback toggledFullscreenCallback;
    private Activity mContext;

    public VideoEnabledWebChromeClient() {
    }

    public VideoEnabledWebChromeClient(Activity activity, View activityNonVideoView, ViewGroup activityVideoView) {
        this.mContext = activity;
        this.activityNonVideoView = activityNonVideoView;
        this.activityVideoView = activityVideoView;
        this.loadingView = null;
        this.webView = null;
        this.isVideoFullscreen = false;
    }

    public VideoEnabledWebChromeClient(Activity activity,View activityNonVideoView, ViewGroup activityVideoView, View loadingView) {
        this.mContext = activity;
        this.activityNonVideoView = activityNonVideoView;
        this.activityVideoView = activityVideoView;
        this.loadingView = loadingView;
        this.webView = null;
        this.isVideoFullscreen = false;
    }

    public VideoEnabledWebChromeClient(Activity activity,View activityNonVideoView, ViewGroup activityVideoView, View loadingView, VideoEnabledWebView webView) {
        this.mContext = activity;
        this.activityNonVideoView = activityNonVideoView;
        this.activityVideoView = activityVideoView;
        this.loadingView = loadingView;
        this.webView = webView;
        this.isVideoFullscreen = false;
    }

    public boolean isVideoFullscreen() {
        return isVideoFullscreen;
    }

    public void setOnToggledFullscreen(ToggledFullscreenCallback callback) {
        this.toggledFullscreenCallback = callback;
    }

    @Override
    public void onShowCustomView(View view, CustomViewCallback callback) {
        if (view instanceof FrameLayout) {
            // A video wants to be shown
            FrameLayout frameLayout = (FrameLayout) view;
            View focusedChild = frameLayout.getFocusedChild();
            // Save video related variables
            this.isVideoFullscreen = true;
            this.videoViewContainer = frameLayout;
            this.videoViewCallback = callback;
            // Hide the non-video view, add the video view, and show it
            activityNonVideoView.setVisibility(View.GONE);
            activityVideoView.addView(videoViewContainer, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            activityVideoView.setVisibility(View.VISIBLE);
            if (focusedChild instanceof VideoView) {
                // VideoView (typically API level <11)
                VideoView videoView = (VideoView) focusedChild;
                // Handle all the required events
                videoView.setOnPreparedListener(this);
                videoView.setOnCompletionListener(this);
                videoView.setOnErrorListener(this);
            } else // Usually android.webkit.HTML5VideoFullScreen$VideoSurfaceView, sometimes android.webkit.HTML5VideoFullScreen$VideoTextureView
            {
                // HTML5VideoFullScreen (typically API level 11+)
                // Handle HTML5 video ended event
                if (webView != null && webView.getSettings().getJavaScriptEnabled()) {
                    // Run javascript code that detects the video end and notifies the interface
                    String js = "javascript:";
                    js += "_ytrp_html5_video = document.getElementsByTagName('video')[0];";
                    js += "if (_ytrp_html5_video !== undefined) {";
                    {
                        js += "function _ytrp_html5_video_ended() {";
                        {
                            js += "_ytrp_html5_video.removeEventListener('ended', _ytrp_html5_video_ended);";
                            js += "_VideoEnabledWebView.notifyVideoEnd();"; // Must match Javascript interface name and method of VideoEnableWebView
                        }
                        js += "}";
                        js += "_ytrp_html5_video.addEventListener('ended', _ytrp_html5_video_ended);";
                    }
                    js += "}";
                    webView.loadUrl(js);
                }
            }

            // Notify full-screen change
            if (toggledFullscreenCallback != null) {
                toggledFullscreenCallback.toggledFullscreen(true);
            }

            setFullscreen(true);
        }
    }

    @Override  @SuppressWarnings("deprecation")
    public void onShowCustomView(View view, int requestedOrientation, CustomViewCallback callback) // Only available in API level 14+
    {
        onShowCustomView(view, callback);
    }

    @Override
    public void onHideCustomView() {
        if (isVideoFullscreen) {
            // Hide the video view, remove it, and show the non-video view
            activityVideoView.setVisibility(View.GONE);//播放视频的
            activityVideoView.removeView(videoViewContainer);
            activityNonVideoView.setVisibility(View.VISIBLE);

            // Call back
            if (videoViewCallback != null) videoViewCallback.onCustomViewHidden();

            // Reset video related variables
            isVideoFullscreen = false;
            videoViewContainer = null;
            videoViewCallback = null;

            // Notify full-screen change
            if (toggledFullscreenCallback != null) {
                toggledFullscreenCallback.toggledFullscreen(false);
            }
            setFullscreen(false);
        }
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
    public void onPrepared(MediaPlayer mp)
    {
        if (loadingView != null) {
            loadingView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp)
    {
        onHideCustomView();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra)
    {
        return false;
    }

    public boolean onBackPressed() {
        if (isVideoFullscreen) {
            onHideCustomView();
            return true;
        } else {
            return false;
        }
    }

    private void setFullscreen(boolean enable) {
        if (enable) { //show status bar
            WindowManager.LayoutParams lp = mContext.getWindow().getAttributes();
            lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            mContext.getWindow().setAttributes(lp);
            mContext.getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        } else { //hide status bar
            WindowManager.LayoutParams lp = mContext.getWindow().getAttributes();
            lp.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            mContext.getWindow().setAttributes(lp);
            mContext.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }
}