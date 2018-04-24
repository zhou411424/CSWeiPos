package com.chuanshi.weipos.webview;

import android.graphics.Bitmap;
import android.net.http.SslError;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.chuanshi.weipos.utils.Logger;


/**
 * Created by zhouliancheng on 2017/11/15.
 * 通知app加载当前网页时的各种时机状态
 */
public class CustomWebViewClient extends WebViewClient {

    private static final String TAG = "CustomWebViewClient";

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Logger.d(TAG, "shouldOverrideUrlLoading...");
        // 使用自己的WebView组件来响应Url加载事件，而不是使用默认浏览器器加载页面
        view.loadUrl(url);
        // 返回true，消耗掉此事件，不再传递
        return true;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        Logger.d(TAG, "onPageStarted...");
//        ((CustomWebView) view).notifyPageStarted();
        super.onPageStarted(view, url, favicon);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        Logger.d(TAG, "onPageFinished...");
//        ((CustomWebView) view).notifyPageFinished();

        //4.4以上系统在onPageFinished时再恢复图片加载时,如果存在多张图片引用的是相同的src时，
        // 会只有一个image标签得到加载，因而对于这样的系统我们就先直接加载
        if(!view.getSettings().getLoadsImagesAutomatically()) {
            view.getSettings().setLoadsImagesAutomatically(true);
        }

        super.onPageFinished(view, url);
    }

    @Override
    public void onLoadResource(WebView view, String url) {
        super.onLoadResource(view, url);
        Logger.d(TAG, "onLoadResource...");
    }

    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);
        Logger.d(TAG, "onReceivedError...");
    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        Logger.d(TAG, "onReceivedSslError...");
//        super.onReceivedSslError(view, handler, error);
        handler.proceed();  // 接受所有网站的证书
    }
}
