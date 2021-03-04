package com.qugengting.email.activity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by xuruibin on 2018/5/7.
 * 描述：
 */

public class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);// 设置默认键盘不弹出
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // 设置设备为竖屏模式
    }

    /**
     * 隐藏键盘
     */
    protected void hideInput() {
        new Handler().post(new Runnable() {

            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                View v = getCurrentFocus();
                if (null != v) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        hideInput();
        super.onDestroy();
    }
}
