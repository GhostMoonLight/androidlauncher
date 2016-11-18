package com.android.launcher3.view;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;

import com.android.launcher3.Insettable;
import com.android.launcher3.Interpolator.ChangeViewHeightAnimator;
import com.android.launcher3.Launcher;
import com.android.launcher3.utils.Util;

/**
 * Created by cgx on 2016/11/18.
 * 搜索界面
 */
public class SearchView extends LinearLayout implements Insettable, View.OnClickListener {

    private Launcher mLauncher;
    private ChangeViewHeightAnimator mChangeViewHeightAnimator;
    private boolean isExpand;   //是否展开

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
        setOnClickListener(this);
    }

    @Override
    public void setInsets(Rect insets) {

    }

    //SearchView展开动画   从0展开
    public void animatorExpand(){
        animatorExpand(0, getHeight());
    }

    //SearchView展开动画
    public void animatorExpand(float start, float end){
        isExpand = true;
        if (mChangeViewHeightAnimator != null) mChangeViewHeightAnimator.cancel();
        mChangeViewHeightAnimator = new ChangeViewHeightAnimator(this, start, end);
        mChangeViewHeightAnimator.setInterpolator(new DecelerateInterpolator());
        mChangeViewHeightAnimator.cancel();
        mChangeViewHeightAnimator.setDuration(600);
        mChangeViewHeightAnimator.start();
    }

    //SearchView收回动画
    public void animatorRetraction(){
        isExpand = false;
        if (mChangeViewHeightAnimator != null) mChangeViewHeightAnimator.cancel();
        mChangeViewHeightAnimator = new ChangeViewHeightAnimator(this, getHeight(), 0);
        mChangeViewHeightAnimator.setInterpolator(new DecelerateInterpolator());
        mChangeViewHeightAnimator.setDuration(600);
        mChangeViewHeightAnimator.start();

    }

    //动画时候正在运行
    public boolean isAnimatorRuning(){
        return mChangeViewHeightAnimator != null && mChangeViewHeightAnimator.isRunning();
    }

    //高度的改变量
    public void setHeightOffset(float offset){
        ViewGroup.LayoutParams lp = getLayoutParams();
        lp.height += offset;
        if (lp.height <= 0){
            lp.height = 0;
        }
        setLayoutParams(lp);
    }

    public boolean isExpand(){
        return isExpand;
    }

    @Override
    public void onClick(View v) {

    }
}

