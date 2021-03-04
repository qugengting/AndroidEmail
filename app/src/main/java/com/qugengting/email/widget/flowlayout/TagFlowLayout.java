package com.qugengting.email.widget.flowlayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatEditText;

import com.qugengting.email.R;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class TagFlowLayout extends FlowLayout implements TagAdapter.OnDataChangedListener {

    private TagAdapter mTagAdapter;
    private boolean attachLabel = true;
    private final Set<Integer> mSelectedItemPosSet = new HashSet<>();

    private AppCompatEditText editText;
    private String mInputStr;
    private boolean isAdd = false;

    /**
     * 设置是否显示标签，默认显示
     *
     * @param attachLabel 是否显示标签
     */
    public void setAttachLabel(boolean attachLabel) {
        this.attachLabel = attachLabel;
    }

    public TagFlowLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TagFlowLayout);
        ta.recycle();
    }

    public TagFlowLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TagFlowLayout(Context context) {
        this(context, null);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int cCount = getChildCount();
        for (int i = 0; i < cCount; i++) {
            TagView tagView = (TagView) getChildAt(i);
            if (tagView.getVisibility() == View.GONE) {
                continue;
            }
            if (tagView.getTagView().getVisibility() == View.GONE) {
                tagView.setVisibility(View.GONE);
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setAdapter(TagAdapter adapter) {
        mTagAdapter = adapter;
        mTagAdapter.setOnDataChangedListener(this);
        mSelectedItemPosSet.clear();
        changeAdapter();
    }

    private void removeItem(int position) {
        mTagAdapter.remove(attachLabel ? position - 1 : position);
        mSelectedItemPosSet.clear();
//        changeAdapter();
        if (attachLabel) {
            removeViewAt(position + 1);
        } else {
            removeViewAt(position);
        }
    }

    public void addItem(final String s) {
        mTagAdapter.add(s);
//        mSelectedItemPosSet.clear();
//        changeAdapter();

        View tagView = mTagAdapter.getView(this, mTagAdapter.getCount() - 1, s);

        TagView tagViewContainer = new TagView(getContext());
        tagView.setDuplicateParentStateEnabled(true);
        if (tagView.getLayoutParams() != null) {
            tagViewContainer.setLayoutParams(tagView.getLayoutParams());
        } else {
            MarginLayoutParams lp = new MarginLayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT);
            lp.setMargins(dip2px(getContext(), 8),
                    dip2px(getContext(), 4),
                    dip2px(getContext(), 8),
                    dip2px(getContext(), 4));
            tagViewContainer.setLayoutParams(lp);
        }
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        tagView.setLayoutParams(lp);
        tagViewContainer.addView(tagView);
        addView(tagViewContainer, getChildCount() - 1);

        tagView.setClickable(false);
        tagViewContainer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setCursorVisible(false);
                String content = editText.getText().toString();
                if (!TextUtils.isEmpty(content)) {
                    editText.getText().clear();
                    mInputStr = "";
                    if (mTagAdapter.getDatas().contains(content)) {
                        return;
                    }
                    addItem(content);
                }
                int position = mTagAdapter.getDatas().indexOf(s);
                doSelect(attachLabel ? position + 1 : position);
            }
        });
    }

    private void changeAdapter() {
        removeAllViews();
        final TagAdapter adapter = mTagAdapter;
        TagView tagViewContainer;
        if (attachLabel) {
            View labelView = adapter.getLabelView(this);
            tagViewContainer = new TagView(getContext());
            labelView.setDuplicateParentStateEnabled(true);
            if (labelView.getLayoutParams() != null) {
                tagViewContainer.setLayoutParams(labelView.getLayoutParams());
            } else {
                MarginLayoutParams lp = new MarginLayoutParams(
                        LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT);
                lp.setMargins(dip2px(getContext(), 8),
                        dip2px(getContext(), 4),
                        dip2px(getContext(), 8),
                        dip2px(getContext(), 4));
                tagViewContainer.setLayoutParams(lp);
            }
            LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            labelView.setLayoutParams(lp);
            tagViewContainer.addView(labelView);
            addView(tagViewContainer);
        }
        for (int i = 0; i < adapter.getCount(); i++) {
            final String content = adapter.getItem(i);
            View tagView = adapter.getView(this, i, content);

            final TagView tagViewC = new TagView(getContext());
            tagView.setDuplicateParentStateEnabled(true);
            if (tagView.getLayoutParams() != null) {
                tagViewC.setLayoutParams(tagView.getLayoutParams());
            } else {
                MarginLayoutParams lp = new MarginLayoutParams(
                        LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT);
                lp.setMargins(dip2px(getContext(), 8),
                        dip2px(getContext(), 4),
                        dip2px(getContext(), 8),
                        dip2px(getContext(), 4));
                tagViewC.setLayoutParams(lp);
            }
            LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            tagView.setLayoutParams(lp);
            tagViewC.addView(tagView);
            tagViewC.setTag(content);
            addView(tagViewC);

            tagView.setClickable(false);
            tagViewC.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    editText.setCursorVisible(false);
                    String content = editText.getText().toString();
                    if (!TextUtils.isEmpty(content)) {
                        editText.getText().clear();
                        if (mTagAdapter.getDatas().contains(content)) {
                            return;
                        }
                        addItem(content);
                    }
                    int position = mTagAdapter.getDatas().indexOf(tagViewC.getTag());
                    doSelect(attachLabel ? position + 1 : position);
                }
            });
        }
        editText = (AppCompatEditText) adapter.getInputView(this);
        tagViewContainer = new TagView(getContext());
        editText.setDuplicateParentStateEnabled(true);
        if (editText.getLayoutParams() != null) {
            tagViewContainer.setLayoutParams(editText.getLayoutParams());
        } else {
            MarginLayoutParams lp = new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            lp.setMargins(dip2px(getContext(), 8),
                    dip2px(getContext(), 4),
                    dip2px(getContext(), 8),
                    dip2px(getContext(), 4));
            tagViewContainer.setLayoutParams(lp);
        }
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        editText.setLayoutParams(lp);
        tagViewContainer.addView(editText);
        addView(tagViewContainer);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isSetItem(s.toString())) {
                    isAdd = true;
                    String ss = s.toString().substring(0, s.length() - 1);
                    if (mTagAdapter.getDatas().contains(ss)) {
                        Toast.makeText(getContext(), getContext().getString(R.string.input_error), Toast.LENGTH_SHORT).show();
                        editText.setText(ss);
                        editText.setSelection(ss.length());
                        return;
                    }
                    addItem(ss);
                    editText.getText().clear();
                    editText.requestFocus();
                } else {
                    isAdd = false;
                    mInputStr = s.toString();
                }
            }
        });
        editText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                //失去焦点，自动将输入内容添加到TAG列表
                if (hasFocus) {
                    editText.setCursorVisible(true);
                } else {
                    if (!TextUtils.isEmpty(mInputStr) && !isAdd) {
                        if (mTagAdapter.getDatas().contains(mInputStr)) {
                            editText.getText().clear();
                            return;
                        }
                        addItem(mInputStr);
                        editText.getText().clear();
                    }
                    editText.setCursorVisible(false);
                }
            }
        });
        editText.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //执行了两次因为onkey事件包含了down和up事件，所以只需要加入其中一个即可
                if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_UP) {
                    if (attachLabel && getChildCount() <= 2) {
                        return false;
                    }
                    if (!attachLabel && getChildCount() <= 1) {
                        return false;
                    }
                    if (isSelect) {
                        removeItem(selectedIndex);
                        isSelect = false;
                        editText.requestFocus();
                        editText.setCursorVisible(true);
                    } else {
                        if (editText.length() == 0) {
                            editText.setCursorVisible(false);
                            doSelect(getChildCount() - 2);
                        }
                    }
                }
                return false;
            }
        });
        editText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setCursorVisible(true);
                if (isSelect) {
                    doSelect(selectedIndex);
                }
            }
        });
    }

    private boolean isSetItem(String s) {
        return s.contains(" ") || s.contains(";") || s.contains("；") || s.contains(",") || s.contains("，") && s.length() > 1;
    }

    private void showInput(final EditText et) {
        et.requestFocus();
        et.setCursorVisible(false);
        new Handler().post(new Runnable() {

            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);
            }
        });
    }

    private void setChildChecked(TagView view) {
        view.setChecked(true);
    }

    private void setChildUnChecked(TagView view) {
        view.setChecked(false);
    }

    private int selectedIndex = -1;
    private boolean isSelect = false;

    private void doSelect(int position) {
        TagView child = (TagView) getChildAt(position);
        if (!child.isChecked()) {
            if (mSelectedItemPosSet.size() == 1) {
                Iterator<Integer> iterator = mSelectedItemPosSet.iterator();
                Integer preIndex = iterator.next();
                TagView pre = (TagView) getChildAt(preIndex);
                setChildUnChecked(pre);
                setChildChecked(child);

                mSelectedItemPosSet.remove(preIndex);
            } else {
                if (mSelectedItemPosSet.size() >= 1) {
                    return;
                }
                setChildChecked(child);
            }
            mSelectedItemPosSet.add(position);
            showInput(editText);
            selectedIndex = position;
            isSelect = true;
        } else {
            setChildUnChecked(child);
            mSelectedItemPosSet.remove(position);
            isSelect = false;
        }
    }

    @Override
    public void onChanged() {
        mSelectedItemPosSet.clear();
        changeAdapter();
    }

    public int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
