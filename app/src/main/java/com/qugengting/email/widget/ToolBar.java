package com.qugengting.email.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qugengting.email.R;

/**
 * Created by xuruibin on 2017/11/10.
 * 描述：自定义标题栏
 */

public class ToolBar extends LinearLayout {
    private Boolean isLeftBtnVisible;
    private int leftResId;

    private Boolean isLeftTvVisible;
    private String leftTvText;

    private Boolean isRightBtnVisible;
    private int rightResId;

    private Boolean isRightTvVisible;
    private String rightTvText;

    private Boolean isRightTv1Visible;
    private String rightTv1Text;

    private Boolean isTitleVisible;
    private String titleText;

    private ImageButton leftBtn;
    private Button rightBtn;

    private TextView leftTv;
    private TextView rightTv;
    private TextView titleTv;

    public TextView getRightTv1() {
        return rightTv1;
    }

    private TextView rightTv1;

    private int backgroundResId;
    private RelativeLayout layout;
    private Boolean isTitleIcon;
    private ImageView titleIv;
    private LinearLayout titleLayout;

    public ToolBar(Context context) {
        this(context, null);
    }

    public ToolBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ToolBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    /**
     * 初始化属性
     *
     * @param attrs
     */
    public void initView(Context context, AttributeSet attrs) {

        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.CustomToolBar);
        /**-------------获取左边按钮属性------------*/
        isLeftBtnVisible = typedArray.getBoolean(R.styleable.CustomToolBar_left_btn_visible, false);
        leftResId = typedArray.getResourceId(R.styleable.CustomToolBar_left_btn_src, -1);
        /**-------------获取左边文本属性------------*/
        isLeftTvVisible = typedArray.getBoolean(R.styleable.CustomToolBar_left_tv_visible, false);
        if (typedArray.hasValue(R.styleable.CustomToolBar_left_tv_text)) {
            leftTvText = typedArray.getString(R.styleable.CustomToolBar_left_tv_text);
        }
        /**-------------获取右边按钮属性------------*/
        isRightBtnVisible = typedArray.getBoolean(R.styleable.CustomToolBar_right_btn_visible, false);
        rightResId = typedArray.getResourceId(R.styleable.CustomToolBar_right_btn_src, -1);
        /**-------------获取右边文本属性------------*/
        isRightTvVisible = typedArray.getBoolean(R.styleable.CustomToolBar_right_tv_visible, false);
        if (typedArray.hasValue(R.styleable.CustomToolBar_right_tv_text)) {
            rightTvText = typedArray.getString(R.styleable.CustomToolBar_right_tv_text);
        }
        /**-------------获取右边文本1属性------------*/
        isRightTv1Visible = typedArray.getBoolean(R.styleable.CustomToolBar_right1_tv_visible, false);
        if (typedArray.hasValue(R.styleable.CustomToolBar_right1_tv_text)) {
            rightTv1Text = typedArray.getString(R.styleable.CustomToolBar_right1_tv_text);
        }
        /**-------------获取标题属性------------*/
        isTitleVisible = typedArray.getBoolean(R.styleable.CustomToolBar_title_visible, false);
        if (typedArray.hasValue(R.styleable.CustomToolBar_title_text)) {
            titleText = typedArray.getString(R.styleable.CustomToolBar_title_text);
        }
        /**-------------背景颜色------------*/
        backgroundResId = typedArray.getResourceId(R.styleable.CustomToolBar_barBackground, -1);
        /**-------------标题icon------------*/
        isTitleIcon = typedArray.getBoolean(R.styleable.CustomToolBar_titleIcon, false);

        typedArray.recycle();

        /**-------------设置内容------------*/
        View barLayoutView = LayoutInflater.from(context).inflate(R.layout.layout_common_toolbar, this);
        layout = barLayoutView.findViewById(R.id.toolbar_content_rlyt);
        leftBtn = barLayoutView.findViewById(R.id.toolbar_left_btn);
        leftTv = barLayoutView.findViewById(R.id.toolbar_left_tv);
        titleTv = barLayoutView.findViewById(R.id.toolbar_title_tv);
        titleIv = barLayoutView.findViewById(R.id.toolbar_title_iv);
        titleLayout = barLayoutView.findViewById(R.id.toolbar_title_layout);
        rightBtn = barLayoutView.findViewById(R.id.toolbar_right_btn);
        rightTv = barLayoutView.findViewById(R.id.toolbar_right_tv);
        rightTv1 = barLayoutView.findViewById(R.id.toolbar_right1_tv);

        if (isLeftBtnVisible) {
            leftBtn.setVisibility(VISIBLE);
        } else {
            leftBtn.setVisibility(INVISIBLE);
        }
        if (isLeftTvVisible) {
            leftTv.setVisibility(VISIBLE);
        }
        if (isRightBtnVisible) {
            rightBtn.setVisibility(VISIBLE);
        }
        if (isRightTvVisible) {
            rightTv.setVisibility(VISIBLE);
        }
        if (isRightTv1Visible) {
            rightTv1.setVisibility(VISIBLE);
        }
        if (isTitleVisible) {
            titleTv.setVisibility(VISIBLE);
        }
        leftTv.setText(leftTvText);
        rightTv.setText(rightTvText);
        rightTv1.setText(rightTv1Text);
        titleTv.setText(titleText);

        if (leftResId != -1) {
            leftBtn.setBackgroundResource(leftResId);
        }
        if (rightResId != -1) {
            rightBtn.setBackgroundResource(rightResId);
        }
        if (backgroundResId != -1) {
            layout.setBackgroundResource(backgroundResId);
            barLayoutView.setBackgroundResource(backgroundResId);
        }

        if (isTitleIcon) {
            titleIv.setVisibility(VISIBLE);
        }
    }

    public void setTitleLayoutClickListener(OnClickListener listener) {
        if (titleLayout != null) {
            titleLayout.setOnClickListener(listener);
        }
    }

    public void setTitleImageDrawableDown(boolean isDown) {
        if (isDown) {
            titleIv.setImageResource(R.drawable.ic_down);
        } else {
            titleIv.setImageResource(R.drawable.ic_up);
        }
    }

    public void setLeftBtnSrc(int resId) {
        leftBtn.setImageResource(resId);
    }

    public void setLeftBtnClickListener(OnClickListener listener) {
        if (leftBtn != null) {
            leftBtn.setOnClickListener(listener);
        }
    }

    public void setRightBtnClickListener(OnClickListener listener) {
        if (rightBtn != null) {
            rightBtn.setOnClickListener(listener);
        }
    }

    public void setLeftTextClickListener(OnClickListener listener) {
        if (leftTv != null) {
            leftTv.setOnClickListener(listener);
        }
    }

    public void setRightTextClickListener(OnClickListener listener) {
        if (rightTv != null) {
            rightTv.setOnClickListener(listener);
        }
    }

    public void setRightText1ClickListener(OnClickListener listener) {
        if (rightTv1 != null) {
            rightTv1.setOnClickListener(listener);
        }
    }

    public void setRight1Text(String text) {
        if (rightTv1 != null) {
            rightTv1.setText(text);
        }
    }

    public void setRightTextVisible(boolean visible) {
        if (visible) {
            rightTv.setVisibility(VISIBLE);
            rightTv1.setVisibility(VISIBLE);
        } else {
            rightTv.setVisibility(GONE);
            rightTv1.setVisibility(GONE);
        }
    }

    public void setTitle(String title) {
        titleTv.setText(title);
    }

    public void setRightBtnBackground(Drawable drawable) {
        rightBtn.setBackground(drawable);
    }
}
