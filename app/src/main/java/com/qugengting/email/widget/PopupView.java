package com.qugengting.email.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import androidx.core.graphics.drawable.DrawableCompat;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qugengting.email.R;

/**
 * Created by xuruibin on 2018/5/22.
 */

public class PopupView extends LinearLayout {

    private final LinearLayout layoutAll;
    private final LinearLayout layoutUnread;
    private final LinearLayout layoutRead;
    private final ImageButton ibBack;
    private final TextView tvTitle;
    public static final String TAG_0 = "back";
    public static final String TAG_I = "unread";
    public static final String TAG_II = "read";
    public static final String TAG_III = "all";

    public PopupView(Context context) {
        super(context, null);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_receiver, this);
        layoutAll = view.findViewById(R.id.lyAll);
        layoutUnread = view.findViewById(R.id.lyUnread);
        layoutRead = view.findViewById(R.id.lyRead);
        tvTitle = view.findViewById(R.id.tvDialogTitle);

        layoutUnread.setTag(TAG_I);
        layoutRead.setTag(TAG_II);
        layoutAll.setTag(TAG_III);

        ibBack = view.findViewById(R.id.btn_dialog_back);
        ibBack.setTag(TAG_0);
    }

    public void setTitle(String title) {
        tvTitle.setText(title);
    }

    public void setOnClickListener1(OnClickListener listener) {
        layoutUnread.setOnClickListener(listener);
        layoutRead.setOnClickListener(listener);
        layoutAll.setOnClickListener(listener);
        ibBack.setOnClickListener(listener);
    }
}
