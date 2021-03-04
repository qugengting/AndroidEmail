package com.qugengting.email.widget.flowlayout;

import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class TagAdapter {
    private List<String> mTagDatas;
    private OnDataChangedListener mOnDataChangedListener;

    public TagAdapter(List<String> data) {
        mTagDatas = data;
    }

    public void remove(int position) {
        mTagDatas.remove(position);
    }

    public void add(String s) {
        mTagDatas.add(s);
    }

    public void addAll(List<String> data) {
        mTagDatas.addAll(data);
        notifyDataChanged();
    }

    public TagAdapter(String[] datas) {
        mTagDatas = new ArrayList<>(Arrays.asList(datas));
    }

    public List<String> getDatas() {
        return mTagDatas;
    }

    interface OnDataChangedListener {
        void onChanged();
    }

    void setOnDataChangedListener(OnDataChangedListener listener) {
        mOnDataChangedListener = listener;
    }

    public int getCount() {
        return mTagDatas == null ? 0 : mTagDatas.size();
    }

    public void resetData(List<String> data) {
        this.mTagDatas = data;
        notifyDataChanged();
    }

    public void notifyDataChanged() {
        if (mOnDataChangedListener != null)
            mOnDataChangedListener.onChanged();
    }

    public String getItem(int position) {
        return mTagDatas.get(position);
    }

    public abstract View getView(FlowLayout parent, int position, String s);

    public abstract View getLabelView(FlowLayout parent);

    public abstract View getInputView(FlowLayout parent);

}
