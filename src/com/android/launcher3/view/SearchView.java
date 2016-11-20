package com.android.launcher3.view;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;

import com.android.launcher3.Insettable;
import com.android.launcher3.Interpolator.ChangeViewHeightAnimator;
import com.android.launcher3.Launcher;
import com.android.launcher3.utils.Util;
import com.android.launcher3.wallpaper.BitmapUtils;
import com.cuan.launcher.R;

/**
 * Created by cgx on 2016/11/18.
 * 搜索界面
 */
public class SearchView extends LinearLayout implements Insettable, View.OnClickListener {

    private Launcher mLauncher;
    private ChangeViewHeightAnimator mChangeViewHeightAnimator;
    private boolean isExpand;   //是否展开
    private View mBackground;
    private int mContentHeight;   //该控件的高度

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
        setOnClickListener(this);
    }

    @Override
    protected void onFinishInflate() {
        mBackground = findViewById(R.id.background);
        mBackground.setPadding(0, Util.getStatusBarHeight(mLauncher), 0, 0);
        mBackground.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mBackground.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                LinearLayout.LayoutParams layoutParams = (LayoutParams) mBackground.getLayoutParams();
                layoutParams.height = mLauncher.getDragLayer().getHeight();
                mContentHeight = layoutParams.height;
                mBackground.setLayoutParams(layoutParams);
            }
        });
    }

    public int getContentHeight(){
        return mContentHeight;
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
        setBuleBackground();
        if (mChangeViewHeightAnimator != null) mChangeViewHeightAnimator.cancel();
        mChangeViewHeightAnimator = new ChangeViewHeightAnimator(this, start, end);
        mChangeViewHeightAnimator.setInterpolator(new DecelerateInterpolator());
        mChangeViewHeightAnimator.cancel();
        mChangeViewHeightAnimator.setDuration(1000);
        mChangeViewHeightAnimator.start();
    }

    //SearchView收回动画
    public void animatorRetraction(){
        isExpand = false;
        if (mChangeViewHeightAnimator != null) mChangeViewHeightAnimator.cancel();
        mChangeViewHeightAnimator = new ChangeViewHeightAnimator(this, getHeight(), 0);
        mChangeViewHeightAnimator.setInterpolator(new DecelerateInterpolator());
        mChangeViewHeightAnimator.setDuration(600);
        mChangeViewHeightAnimator.addListener(new SimpleAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mBackground.setBackgroundDrawable(null);
            }
        });
        mChangeViewHeightAnimator.start();

    }

    private void setBuleBackground(){
        if (mBackground.getBackground() == null) {
            Bitmap bluredBitmap = BitmapUtils.getBluredBackgroundImage((Launcher) getContext());
            mBackground.setBackgroundDrawable(new BitmapDrawable(getResources(), bluredBitmap));
        }
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
            mBackground.setBackgroundDrawable(null);
        }else{
            setBuleBackground();
        }
        setLayoutParams(lp);
    }

    public boolean isExpand(){
        return isExpand;
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
    }
}

