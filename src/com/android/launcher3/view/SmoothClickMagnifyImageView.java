package com.android.launcher3.view;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;

import com.android.launcher3.common.LogUtils;

/**
 * Created by wen on 2016/11/14.
 * 点击图片放大至全屏的ImageView。
 */

public class SmoothClickMagnifyImageView extends ImageView {

    private final int DURATION = 300;

    private int mOriginalWidth, mOriginalHeight;

    private final Matrix mMatrix = new Matrix();
    private ValueAnimator mValueAnimator;
    private TransScale mTransScale;
    private float mScale;    //图片最终缩放的大小
    private boolean isAnimating = true;
    private boolean isOnGlobalLayout = false;
    private Runnable mRunnable;   //如果当前view没有加载出来，就调用setOriginalValues方法时，就把该方法执行的任务，保存到Runnable中
    private Runnable mSetImageRunnable;
    private ArgbEvaluator mArgbEvaluator;

    public SmoothClickMagnifyImageView(Context context) {
        this(context, null);
    }

    public SmoothClickMagnifyImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    public SmoothClickMagnifyImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
        initView();
    }

    public SmoothClickMagnifyImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    private void initView(){
        mArgbEvaluator = new ArgbEvaluator();
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                isOnGlobalLayout = true;
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
                if (mRunnable != null){
                    mRunnable.run();
                }
                mRunnable = null;
            }
        });
    }

    /**
     * 设置初始值     大图是最终显示的图，如果不需要加载大图，此处的largerWidth和largeHeight请根据图片最终显示的大小自行设置，
     * @param bitmap                     小图   显示的缩略图
     * @param originWidth                点击的view的宽
     * @param originHeight               点击的view的高
     * @param originalPositionX          点击的view的X在屏幕中的位置
     * @param originalPositionY          点击的view的Y在屏幕中的位置
     * @param largerWidth                大图的宽
     * @param largeHeight                大图的高
     */
    public void setOriginalValues(final Bitmap bitmap, final int originWidth, final int originHeight, final int originalPositionX, final int originalPositionY,
                                  final int largerWidth, final int largeHeight){
        if (!isOnGlobalLayout){
            mRunnable = new Runnable() {
                @Override
                public void run() {
                    setOriginalValues(bitmap, originWidth, originHeight, originalPositionX, originalPositionY, largerWidth, largeHeight);
                }
            };
            return ;
        }
        postDelayed(new Runnable() {
            @Override
            public void run() {
                setImageBitmap(bitmap);
                mOriginalWidth = originWidth;
                mOriginalHeight = originHeight;

                Drawable d = getDrawable();
                if (d == null)
                    return;
                mTransScale = new TransScale();

                int width = mOriginalWidth;
                int height = mOriginalHeight;
                // 拿到图片的宽和高
                int dw = d.getIntrinsicWidth();
                int dh = d.getIntrinsicHeight();
                float scaleX = width * 1.0f / dw;
                float scaleY = height * 1.0f / dh;

                mTransScale.currentScaleX = mTransScale.startScaleX = scaleX;
                mTransScale.currentScaleY = mTransScale.startScaleY = scaleY;
                mTransScale.currentTransX = mTransScale.startTransX = originalPositionX;
                mTransScale.currentTransY = mTransScale.startTransY = originalPositionY;

                setMatrixValue(mTransScale);

                //计算最终要缩放的大小
                width = getWidth();
                height = getHeight();
                dw = largerWidth;
                dh = largeHeight;
                float scale = 1.0f;
                if (dw > width && dh <= height) {
                    scale = width * 1.0f / dw;
                }
                if (dh > height && dw <= width) {
                    scale = height * 1.0f / dh;
                }
                if (dw > width && dh > height) {
                    scale = Math.min(dw * 1.0f / width, dh * 1.0f / height);
                }
                mScale = scale;

                // 计算当前图片放大到宽为largerWidth * scale高为scale * largeHeight的缩放比
                dw = d.getIntrinsicWidth();
                dh = d.getIntrinsicHeight();

                width = (int)(largerWidth * scale);
                height = (int)(scale * largeHeight);
                scaleX = width * 1.0f / dw;
                scaleY = height * 1.0f / dh;

                int endTransX = (getWidth() - width) / 2;
                int endTransY = (getHeight() - height) / 2;

                mTransScale.endScaleX = scaleX;
                mTransScale.endScaleY = scaleY;
                mTransScale.endTransX = endTransX;
                mTransScale.endTransY = endTransY;

                //开始动画放大移动图片
                PropertyValuesHolder scaleXHolder = PropertyValuesHolder.ofFloat("scaleX", mTransScale.startScaleX, mTransScale.endScaleX);
                PropertyValuesHolder scaleYHolder = PropertyValuesHolder.ofFloat("scaleY", mTransScale.startScaleY, mTransScale.endScaleY);
                PropertyValuesHolder transXHolder = PropertyValuesHolder.ofInt("transX", mTransScale.startTransX, mTransScale.endTransX);
                PropertyValuesHolder transYHolder = PropertyValuesHolder.ofInt("transY", mTransScale.startTransY, mTransScale.endTransY);

                mValueAnimator = new ValueAnimator();
                mValueAnimator.setDuration(DURATION);
                mValueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                mValueAnimator.setValues(scaleXHolder, scaleYHolder, transXHolder, transYHolder);
                mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public synchronized void onAnimationUpdate(ValueAnimator animation) {
                        mTransScale.currentScaleX = (float) animation.getAnimatedValue("scaleX");
                        mTransScale.currentScaleY = (float) animation.getAnimatedValue("scaleY");
                        mTransScale.currentTransX = (int) animation.getAnimatedValue("transX");
                        mTransScale.currentTransY = (int) animation.getAnimatedValue("transY");
                        ((Activity)getContext()).getWindow().getDecorView().setBackgroundColor((int)mArgbEvaluator.evaluate(animation.getAnimatedFraction(), Color.parseColor("#00000000"), Color.parseColor("#FF000000")));
                        setMatrixValue(mTransScale);
                    }
                });
                mValueAnimator.addListener(new SimpleAnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        isAnimating = true;
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        isAnimating = false;
                        if (mSetImageRunnable != null){
                            mSetImageRunnable.run();
                        }
                        mSetImageRunnable = null;
                    }
                });
                mValueAnimator.start();
            }
        }, 200);
    }

    //设置图片的缩放和移动
    public void setMatrixValue(TransScale ts){
        mMatrix.reset();
        mMatrix.postTranslate(ts.currentTransX, ts.currentTransY);
        mMatrix.postScale(ts.currentScaleX, ts.currentScaleY, ts.currentTransX, ts.currentTransY);
        setImageMatrix(mMatrix);
    }

    //设置最终显示的大图
    public void setImageBitmapWithMatrix(final Bitmap bitmap){
        if (isAnimating) {
            mSetImageRunnable = new Runnable() {
                @Override
                public void run() {
                    setImageBitmapWithMatrix(bitmap);
                }
            };
            return ;
        }

        setImageBitmap(bitmap);
        Matrix matrix = new Matrix();
        matrix.postTranslate(mTransScale.currentTransX, mTransScale.currentTransY);
        matrix.postScale(mScale, mScale, mTransScale.currentTransX, mTransScale.currentTransY);
        setImageMatrix(matrix);

    }

    public void onBackPressed(){
        if (mValueAnimator != null && mValueAnimator.isRunning()) return;

        Drawable d = getDrawable();
        //计算最终要缩放的大小
        int dw = d.getIntrinsicWidth();
        int dh = d.getIntrinsicHeight();
        int width = mOriginalWidth;
        int height = mOriginalHeight;
        float scaleX = 1.0f;
        float scaleY = 1.0f;
        scaleX = width * 1.0f / dw;
        scaleY = height * 1.0f / dh;

        PropertyValuesHolder scaleXHolder = PropertyValuesHolder.ofFloat("scaleX", mScale, scaleX);
        PropertyValuesHolder scaleYHolder = PropertyValuesHolder.ofFloat("scaleY", mScale, scaleY);
        PropertyValuesHolder transXHolder = PropertyValuesHolder.ofInt("transX", mTransScale.endTransX, mTransScale.startTransX);
        PropertyValuesHolder transYHolder = PropertyValuesHolder.ofInt("transY", mTransScale.endTransY, mTransScale.startTransY);

        mValueAnimator.setDuration(DURATION);
        mValueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mValueAnimator.setValues(scaleXHolder, scaleYHolder, transXHolder, transYHolder);
        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public synchronized void onAnimationUpdate(ValueAnimator animation) {
                mTransScale.currentScaleX = (float) animation.getAnimatedValue("scaleX");
                mTransScale.currentScaleY = (float) animation.getAnimatedValue("scaleY");
                mTransScale.currentTransX = (int) animation.getAnimatedValue("transX");
                mTransScale.currentTransY = (int) animation.getAnimatedValue("transY");
                ((Activity)getContext()).getWindow().getDecorView().setBackgroundColor((int)mArgbEvaluator.evaluate(animation.getAnimatedFraction(), Color.parseColor("#FF000000"), Color.parseColor("#00000000")));
                setMatrixValue(mTransScale);
            }
        });
        mValueAnimator.addListener(new SimpleAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                setImageBitmap(null);
                ((Activity)getContext()).finish();
                ((Activity)getContext()).overridePendingTransition(0, 0);
            }
        });
        mValueAnimator.start();
    }


    private class TransScale{
        public float startScaleX, startScaleY;
        public int startTransX, startTransY;

        public float endScaleX, endScaleY;
        public int endTransX, endTransY;

        public float currentScaleX, currentScaleY;
        public int currentTransX, currentTransY;
    }

}
