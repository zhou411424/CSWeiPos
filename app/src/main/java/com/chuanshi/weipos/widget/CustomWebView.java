package com.chuanshi.weipos.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.chuanshi.weipos.BuildConfig;
import com.chuanshi.weipos.utils.Logger;

import java.lang.reflect.Field;

/**
 * Created by zhouliancheng on 2017/11/15.
 */
@SuppressLint("SetJavaScriptEnabled")
public class CustomWebView extends WebView {
    private static final String TAG = "CustomWebView";
    private boolean mIsLoading = false;

    public CustomWebView(Context context) {
        this(context, null);
    }

    public CustomWebView(Context context, AttributeSet attrs) {
        //传入webViewStyle防止webview打开网页时调不出输入法
        this(context, attrs, android.R.attr.webViewStyle);
    }

    public CustomWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setDrawingCacheEnabled(true);
        // 开启硬件加速后，WebView渲染页面更加快速，拖动也更加顺滑，
        // 但是容易会出现页面加载白块同时界面闪烁现象,解决方法暂时关闭WebView硬件加速
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        // 防止闪黑屏，如果直接设置"#00000000"，可能在某些机型上还会出问题
//        setBackgroundColor(Color.parseColor("#00000000"));
        setBackgroundColor(getResources().getColor(android.R.color.transparent));
        WebSettings settings = getSettings();
        // 调用js时设置字符编码
        settings.setDefaultTextEncodingName("UTF-8");
//        setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        settings.setJavaScriptEnabled(true);
        // 允许文件访问，如Assets and resources
        settings.setAllowFileAccess(true);
        settings.setPluginState(WebSettings.PluginState.ON);
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
//        settings.setUserAgentString("Mozilla/5.0 (Linux; U; Android 2.0; en-us; Droid Build/ESD20) AppleWebKit/530.17 (KHTML, like Gecko) Version/4.0 Mobile Safari/530.17");

        // 如果为true，可任意比例缩放
        settings.setUseWideViewPort(false);
        settings.setLoadWithOverviewMode(false);
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
//        settings.setNeedInitialFocus(false);

        // sdk4.4一下有效，让webview只显示一列，自适应页面大小，不能左右滑动
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);


        // 提高html页面加载完成速度，即WebView先不要自动加载图片，等页面finish后再发起图片加载
        if(Build.VERSION.SDK_INT >= 19) {
            settings.setLoadsImagesAutomatically(true);
        } else {
            settings.setLoadsImagesAutomatically(false);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        // 设置h5缓存
        settings.setAppCacheEnabled(true);
        // appCaceDir=/data/data/com.xingyun.main/cache/webviewCache
        String appCaceDir = context.getCacheDir().getAbsolutePath() + "/webviewCache";
        settings.setAppCachePath(appCaceDir);
        // 建议缓存策略为，判断是否有网络，有的话，使用LOAD_DEFAULT,无网络时，使用LOAD_CACHE_ELSE_NETWORK
        settings.setCacheMode(WebSettings.LOAD_DEFAULT/*LOAD_CACHE_ELSE_NETWORK*/);
        settings.setAppCacheMaxSize(1024 * 1024 * 10);// 设置缓冲大小，10M
        //开启 database storage 功能
        settings.setDatabaseEnabled(true);
        // database也缓存到/data/data/com.xingyun.main/cache/webviewCache路径下
        settings.setDatabasePath(appCaceDir);

        //开启 dom storage 功能
        settings.setDomStorageEnabled(true);

//        setWebChromeClient(new CustomWebChromeClient(context));
//        setWebViewClient(new CustomWebViewClient());
    }

    // 除关闭硬件加速外，还可重写onMeasure来防止webview闪烁
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Logger.d(TAG, "onMeasure...");
        invalidate();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * 使WebView不可滚动
     * */
    @Override
    public void scrollTo(int x, int y){
        super.scrollTo(0,0);
    }

    /**
     * 页面加载开始
     */
    public void notifyPageStarted() {
        mIsLoading = true;
    }

    /**
     * 页面加载完成
     */
    public void notifyPageFinished() {
        mIsLoading = false;
    }

    public boolean isLoading() {
        return mIsLoading;
    }

    /**
     * onPause时，webview加载html的一些Js资源，如anim,如果webview挂在后台，就需要将js释放，
     */
    @Override
    public void onPause() {
        Logger.d(TAG, "onStopPlay...");
//        getSettings().setJavaScriptEnabled(false);
        super.onPause();
    }

    /**
     * onResume时，webview需要重新加载html里的js
     */
    @Override
    public void onResume() {
        Logger.d(TAG, "onResume...");
//        getSettings().setJavaScriptEnabled(true);
        super.onResume();
    }

    /**
     * 释放所有的资源
     */
    @Override
    public void destroy() {
        Logger.d(TAG, "destroy...");
        releaseAllWebViewCallback();
        super.destroy();
    }

    /**
     * 使用cookie机制，可以加快app加载速度，在网页更新cookie以后，不刷新页面即可生效
     * @param cookie
     * @param cookieValue
     */
    public void updateCookies(String cookie, String cookieValue) {
        // use cookies to remember a Loggerged in status
        final CookieManager cookieManager = CookieManager.getInstance();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            final CookieSyncManager cookieSyncManager = CookieSyncManager.createInstance(getContext().getApplicationContext());
            cookieManager.removeAllCookie();
            cookieManager.setAcceptCookie(true);

            cookieManager.setCookie(cookie, cookieValue);
            cookieSyncManager.sync();
        } else {
            // API 21及以上
            cookieManager.removeAllCookies(new ValueCallback<Boolean>() {
                @Override
                public void onReceiveValue(Boolean value) {

                }
            });
            cookieManager.setAcceptThirdPartyCookies(this, true);
            cookieManager.setCookie(cookie, cookieValue);
            cookieManager.flush();
        }
    }

    /**
     * 释放所有的资源，防止android4.4以下的基于webkit内核的webview出现内存泄漏
     */
    public void releaseAllWebViewCallback() {
        Log.d(TAG, "releaseAllWebViewCallback==>");
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {//android4.1
            try {
                Field field = WebView.class.getDeclaredField("mWebViewCore");
                field = field.getType().getDeclaredField("mBrowserFrame");
                field = field.getType().getDeclaredField("sConfigCallback");
                field.setAccessible(true);
                field.set(null, null);
            } catch (NoSuchFieldException e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
            } catch (IllegalAccessException e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
            }
        } else {
            try {
                Field sConfigCallback = Class.forName("android.webkit.BrowserFrame").getDeclaredField("sConfigCallback");
                if (sConfigCallback != null) {
                    sConfigCallback.setAccessible(true);
                    sConfigCallback.set(null, null);
                }
            } catch (NoSuchFieldException e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
            } catch (ClassNotFoundException e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
            } catch (IllegalAccessException e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
            }
        }
    }
}
