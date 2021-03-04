package com.qugengting.email.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.widget.Toast;

import com.qugengting.email.widget.AlertDialogEx;

/**
 * Created by xuruibin on 2017/11/10.
 * 描述：公共工具类
 */

public class Utils {
    /**
     * 弹出AlertDialog
     *
     * @param context  -- 上下文
     * @param title -- 标题
     * @param content --内容
     * @param confirmListenner  -- 确认回调
     * @param cancelListenner  -- 取消回调
     * @param type -- 显示类型，0=默认触摸空闲区域会消失dialog；1=触摸空闲区域不会消失dialog
     */
    public static void showDialog(Context context, String title, String content, String confirmLab,
                                  DialogInterface.OnClickListener confirmListenner, String cancelLab,
                                  DialogInterface.OnClickListener cancelListenner, int type) {

        AlertDialogEx.Builder builder = new AlertDialogEx.Builder(context);
        if (!TextUtils.isEmpty(title)) {
            builder.setTitle(title);
        }
        if (!TextUtils.isEmpty(content)) {
            builder.setMessage(content);
        }
        if (!TextUtils.isEmpty(confirmLab)) {
            builder.setPositiveButton(confirmLab, confirmListenner);
        }
        if (!TextUtils.isEmpty(cancelLab)) {
            builder.setNegativeButton(cancelLab, cancelListenner);
        }
        AlertDialogEx alertDialog = builder.create();
        if (type == 1) {
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.setCancelable(false);
        }
        alertDialog.show();

    }

    /**
     * 提示消息
     *
     * @param context
     * @param msg     信息
     */
    public static void makeText(Context context, CharSequence msg) {
        if (toast == null) {
            toast = Toast.makeText(context,
                    msg,
                    Toast.LENGTH_SHORT);
        } else {
            toast.setText(msg);
        }
        toast.show();
    }

    private static Toast toast;

    /**
     * 提示消息
     *
     * @param context
     * @param resId   id
     */
    public static void makeText(Context context, int resId) {
        if (toast == null) {
            toast = Toast.makeText(context,
                    resId,
                    Toast.LENGTH_LONG);
        } else {
            toast.setText(resId);
        }
        toast.show();
    }

    public static void closeToast() {
        if (toast != null) {
            toast.cancel();
            toast = null;
        }
    }
}
