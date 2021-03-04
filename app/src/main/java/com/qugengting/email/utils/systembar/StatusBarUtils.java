package com.qugengting.email.utils.systembar;

/**
 * @author:xuruibin
 * @date:2020/8/6 Description:
 */

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.qugengting.email.R;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 状态栏工具类
 * 作者： JairusTse
 * 日期： 17/12/19
 */
public class StatusBarUtils {

    /**
     * 设置状态栏为透明
     *
     * @param activity
     */
    @TargetApi(19)
    public static void setTranslucentStatus(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = activity.getWindow();
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    /**
     * 修改状态栏颜色，支持4.4以上版本
     *
     * @param activity
     * @param colorId
     */
    public static void setStatusBarColor(Activity activity, int colorId) {
        //Android6.0（API 23）以上，系统方法
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.setStatusBarColor(activity.getResources().getColor(colorId));
        } else {
            //使用SystemBarTint库使4.4版本状态栏变色，需要先将状态栏设置为透明
            setTranslucentStatus(activity);
            //设置状态栏颜色
            SystemBarTintManager tintManager = new SystemBarTintManager(activity);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setStatusBarTintResource(colorId);
        }
    }

    public static void setDefault(Activity activity) {
        setStatusBarMode(activity, true, R.color.white);
    }

    public static void setBlack(Activity activity) {
        setStatusBarMode(activity, true, R.color.black);
    }

    /**
     * 设置状态栏模式
     *
     * @param activity
     * @param isTextDark 文字、图标是否为黑色 （false为默认的白色）
     * @param colorId    状态栏颜色
     * @return
     */
    public static void setStatusBarMode(Activity activity, boolean isTextDark, int colorId) {
        if (!isTextDark) {
            //文字、图标颜色不变，只修改状态栏颜色
            setStatusBarColor(activity, colorId);
        } else {
            //修改状态栏颜色和文字图标颜色
            setStatusBarColor(activity, colorId);
            if (OSUtil.isMIUI()) {
                //小米MIUI系统
                setMIUIStatusBarTextMode(activity, true);
            } else if (OSUtil.isFlyme()) {
                //魅族flyme系统
                setFlymeStatusBarTextMode(activity, true);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //6.0以上，调用系统方法
                Window window = activity.getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            } else {
                //4.4以上6.0以下的其他系统，暂时没有修改状态栏的文字图标颜色的方法，有可以加上
            }
        }
    }

    public static void setStatusBarTextWhite(Activity activity) {
        //4.4以上才可以改文字图标颜色
        if (OSUtil.isMIUI()) {
            //小米MIUI系统
            setMIUIStatusBarTextMode(activity, false);
        } else if (OSUtil.isFlyme()) {
            //魅族flyme系统
            setFlymeStatusBarTextMode(activity, false);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //6.0以上，调用系统方法
            Window window = activity.getWindow();
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        } else {
            //4.4以上6.0以下的其他系统，暂时没有修改状态栏的文字图标颜色的方法，有可以加上
        }
    }

    /**
     * 设置Flyme系统状态栏的文字图标颜色
     *
     * @param activity
     * @param isDark   状态栏文字及图标是否为深色
     * @return
     */
    public static boolean setFlymeStatusBarTextMode(Activity activity, boolean isDark) {
        Window window = activity.getWindow();
        boolean result = false;
        if (window != null) {
            try {
                WindowManager.LayoutParams lp = window.getAttributes();
                Field darkFlag = WindowManager.LayoutParams.class
                        .getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
                Field meizuFlags = WindowManager.LayoutParams.class
                        .getDeclaredField("meizuFlags");
                darkFlag.setAccessible(true);
                meizuFlags.setAccessible(true);
                int bit = darkFlag.getInt(null);
                int value = meizuFlags.getInt(lp);
                if (isDark) {
                    value |= bit;
                } else {
                    value &= ~bit;
                }
                meizuFlags.setInt(lp, value);
                window.setAttributes(lp);
                result = true;
            } catch (Exception e) {

            }
        }
        return result;
    }

    /**
     * 设置MIUI系统状态栏的文字图标颜色
     * 参考：https://dev.mi.com/console/doc/detail?pId=1159
     *
     * @param activity 界面
     * @param isDark   状态栏文字及图标是否为深色
     */
    public static void setMIUIStatusBarTextMode(Activity activity, boolean isDark) {
        //Android6.0（API 23）以上
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = activity.getWindow();
            if (isDark) {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            } else {
                int flag = window.getDecorView().getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                window.getDecorView().setSystemUiVisibility(flag);
            }
        } else {
            Class<? extends Window> clazz = activity.getWindow().getClass();
            try {
                int darkModeFlag;
                Class<?> layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
                Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
                darkModeFlag = field.getInt(layoutParams);
                Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
                extraFlagField.invoke(activity.getWindow(), isDark ? darkModeFlag : 0, darkModeFlag);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

