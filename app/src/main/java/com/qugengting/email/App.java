package com.qugengting.email;

import android.app.Application;

import com.qugengting.email.utils.DateUtils;

import org.litepal.LitePal;

/**
 * Created by xuruibin on 2018/5/7.
 * 描述：
 */

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        LitePal.initialize(this);
        DateUtils.initContext(this);
    }
}
