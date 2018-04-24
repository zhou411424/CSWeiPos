package com.chuanshi.weipos.webview;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.chuanshi.weipos.utils.Logger;


/**
 * Created by zhouliancheng on 2017/11/15.
 * 主要辅助WebView处理Javascript的对话框，网站图标，网站title，加载进度等
 */
public class CustomWebChromeClient extends WebChromeClient {

    private static final String TAG = "CustomWebChromeClient";
    private Context mContext;
    public CustomWebChromeClient(Context context) {
        this.mContext = context;
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        Logger.d(TAG, "onProgressChanged....newProgress="+newProgress);
        super.onProgressChanged(view, newProgress);
    }

    @Override
    public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
        Logger.d(TAG, "onJsAlert....url="+url+", message="+message);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
                .setTitle("JsAlert")
                .setMessage(message)
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        result.confirm();
                    }
                });
        builder.create().show();
        return true;
    }

    @Override
    public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
        Logger.d(TAG, "onJsConfirm....url="+url+", message="+message);
        return super.onJsConfirm(view, url, message, result);
    }

    @Override
    public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
        Logger.d(TAG, "onJsPrompt....url="+url+", message="+message+", defaultValue="+defaultValue);
        return super.onJsPrompt(view, url, message, defaultValue, result);
    }

}
