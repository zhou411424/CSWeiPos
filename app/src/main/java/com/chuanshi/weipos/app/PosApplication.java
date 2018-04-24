package com.chuanshi.weipos.app;

import android.app.Application;

/**
 * Created by zhouliancheng on 2017/10/19.
 */

public class PosApplication extends Application {

    private static PosApplication application;

    public static PosApplication getInstance() {
        return application;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
    }

}
