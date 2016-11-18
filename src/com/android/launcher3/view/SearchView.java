package com.android.launcher3.view;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.android.launcher3.Insettable;
import com.android.launcher3.Launcher;
import com.android.launcher3.utils.Util;

/**
 * Created by cgx on 2016/11/18.
 * 搜索界面
 */
public class SearchView extends LinearLayout implements Insettable {

    private Launcher mLauncher;

    public SearchView(Context context) {
        this(context, null);
    }

    public SearchView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        mLauncher = (Launcher) context;
        setPadding(0, Util.getStatusBarHeight(mLauncher), 0, 0);
    }

    @Override
    public void setInsets(Rect insets) {

    }
}
