package com.qugengting.email.fragment;

import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.Fragment;

/**
 * Created by xuruibin on 2018/2/1.
 */
public class BaseFragment extends Fragment {
    private String fragmentTitle = "";
    public View view = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (getUserVisibleHint()) {
            onVisible();
        } else {
            onInvisible();
        }
    }

    /**
     * 可见
     */
    public void onVisible() {
    }

    /**
     * 不可见
     */
    public void onInvisible() {
    }

    public String getFragmentTitle() {
        return fragmentTitle;
    }

    public void setFragmentTitle(String fragmentTitle) {
        this.fragmentTitle = fragmentTitle;
    }

}
