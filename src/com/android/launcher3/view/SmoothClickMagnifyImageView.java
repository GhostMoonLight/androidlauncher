package com.android.launcher3.view;

import android.animation.Animator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;

/**
 * Created by wen on 2016/11/14.
 * 点击图片放大至全屏的ImageView。
 */

public class SmoothClickMagnifyImageView extends ImageView {

    private final int DURATION = 600;

    private int mOriginalWidth, mOriginalHeight;
    private int mOriginalPositionX, mOriginalPositionY;

    private final Matrix mMatrix = new Matrix();
    private ValueAnimator mValueAnimator;
    private TransScale mTransScale;
    private float mScale;
    private Bitmap mBitmap;
    private boolean isAnimating = true;

    public SmoothClickMagnifyImageView(Context context) {
        this(context, null);
    }

    public SmoothClickMagnifyImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    public SmoothClickMagnifyImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SmoothClickMagnifyImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public void setOriginalValues(int originWidth, int originHeight, int originalPositionX, int originalPositionY,
                                    int largerWidth, int largeHeight){
        mOriginalWidth = originWidth;
        mOriginalHeight = originHeight;
        mOriginalPositionX = originalPositionX;
        mOriginalPositionY = originalPositionY;

        Drawable d = getDrawable();
        if (d == null)
            return;
        mTransScale = new TransScale();

        int width = mOriginalWidth;
        int height = mOriginalHeight;
        // 拿到图片的宽和高
        int dw = d.getIntrinsicWidth();
        int dh = d.getIntrinsicHeight();
        float scaleX = 1.0f;
        float scaleY = 1.0f;
        scaleX = width * 1.0f / dw;
        scaleY = height * 1.0f / dh;

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

        int endTransX = 0;
        int endTransY = (int)((getHeight() - height) / 2);

        mTransScale.endScaleX = scaleX;
        mTransScale.endScaleY = scaleY;
        mTransScale.endTransX = endTransX;
        mTransScale.endTransY = endTransY;

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
                Log.e("AAAAA", "currentScaleX:"+mTransScale.currentScaleX+" currentScaleY"+mTransScale.currentScaleY+
                        " currentTransX:"+mTransScale.currentTransX+" currentTransY:"+mTransScale.currentTransY);
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
                if (mBitmap != null){
                    setImageBitmapWithMatrix(mBitmap);
                }
            }
        });
        mValueAnimator.start();
    }

    public void setMatrixValue(TransScale ts){
        mMatrix.reset();
        mMatrix.postTranslate(ts.currentTransX, ts.currentTransY);
        mMatrix.postScale(ts.currentScaleX, ts.currentScaleY, ts.currentTransX, ts.currentTransY);
        setImageMatrix(mMatrix);
    }

    public void setImageBitmapWithMatrix(Bitmap bitmap){
        mBitmap = bitmap;
        if (!isAnimating) {
            setImageBitmap(bitmap);
            Log.e("AAAAA", "bw:"+bitmap.getWidth()+" bh:"+bitmap.getHeight()+" mScale:"+mScale);
            Matrix matrix = new Matrix();
            matrix.postTranslate(mTransScale.currentTransX, mTransScale.currentTransY);
            setImageMatrix(matrix);
        }
    }

    public void onBackPressed(){

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

        PropertyValuesHolder scaleXHolder = PropertyValuesHolder.ofFloat("scaleX", 1f, scaleX);
        PropertyValuesHolder scaleYHolder = PropertyValuesHolder.ofFloat("scaleY", 1f, scaleY);
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
