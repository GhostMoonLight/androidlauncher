package com.android.launcher3.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by cgx on 16/8/2.
 * 显示加载动画的View
 *
 * PathMeasuer的使用:
 *      相当于Path的计算器，用来计算Path路径上的坐标
 *      两种初始化方式：new PathMeasure() 和 PathMeasure(Path path, boolean forceClosed);
 *      用无参的构造方法创建的对象可通过setPath(Path path, boolean forceClosed);方法将path和PathMeasure进行绑定
 *      forceClosed参数
 * 这个参数——forceClosed，简单的说，就是Path最终是否需要闭合，如果为True的话，则不管关联的Path是否是闭合的，都会被闭合。
 * 但是这个参数对Path和PathMeasure的影响是需要解释下的：
 * forceClosed参数对绑定的Path不会产生任何影响，例如一个折线段的Path，本身是没有闭合的，forceClosed设置为True的时候，PathMeasure计算的Path是闭合的，但Path本身绘制出来是不会闭合的。
 * forceClosed参数对PathMeasure的测量结果有影响，还是例如前面说的一个折线段的Path，本身没有闭合，forceClosed设置为True，PathMeasure的计算就会包含最后一段闭合的路径，与原来的Path不同。
 *
 * 几个常用的API：
 *      getLength();  计算路径的长度
 *      getSegment (float startD, float stopD, Path dst, boolean startWithMoveTo)
 *          这个API用于截取整个Path的片段，通过参数startD和stopD来控制截取的长度，并将截取的Path保存到dst中，
 *          最后一个参数startWithMoveTo表示起始点是否使用moveTo方法，通常为True，保证每次截取的Path片段都是正常的、完整的。
 *          如果startWithMoveTo设置为false，通常是和dst一起使用，因为dst中保存的Path是被不断添加的，而不是每次被覆盖，
 *          设置为false，则新增的片段会从上一次Path终点开始计算，这样可以保存截取的Path片段数组连续起来。
 *      getPosTan (float distance, float[] pos, float[] tan)
 *          用于获取路径上某点的坐标及其切线的坐标
 *
 * 由于硬件加速的问题，PathMeasure中的getSegment在讲Path添加到dst数组中时会被导致一些错误，需要通过mDst.lineTo(0,0)来避免这样一个Bug。
 *
 */
public class LoadingView extends View {

    private Path mPath;
    private Paint mPaint;
    private PathMeasure mPathMeasure;
    private float mAnimatorValue;
    private Path mDst;
    private float mLength;
    private ValueAnimator valueAnimator;

    public LoadingView(Context context) {
        super(context);
        initView();
    }

    public LoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public LoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public LoadingView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    private void initView() {
        mPathMeasure = new PathMeasure();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);   //抗锯齿
        mPaint.setStyle(Paint.Style.STROKE);         //描边
        mPaint.setColor(Color.parseColor("#99FFFFFF"));
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(6);
        mDst = new Path();
    }

    private void startLoadingAnimal(){
        mPath = new Path();
        mPath.addCircle(getMeasuredWidth()/2, getMeasuredHeight()/2, getMeasuredWidth()/2-10, Path.Direction.CW);
        mPathMeasure.setPath(mPath, true);
        mLength = mPathMeasure.getLength();

        if (valueAnimator != null) valueAnimator.reverse();

        valueAnimator = ValueAnimator.ofFloat(0, 1);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mAnimatorValue = (float) valueAnimator.getAnimatedValue();
                invalidate();
            }
        });
        valueAnimator.setDuration(2500);
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator.start();
    }

    private void stopAnim(){
        if (valueAnimator != null){
            valueAnimator.reverse();
            valueAnimator = null;
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (getMeasuredHeight() != 0){
            startLoadingAnimal();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnim();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int height, width;
        if (widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY){
            height = MeasureSpec.getSize(heightMeasureSpec);
            width = MeasureSpec.getSize(widthMeasureSpec);
        } else if (widthMode == MeasureSpec.EXACTLY){
            height = width = MeasureSpec.getSize(widthMeasureSpec);
        } else if (heightMode == MeasureSpec.EXACTLY){
            height = width = MeasureSpec.getSize(heightMeasureSpec);
        } else {
            height = width = 120;
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        startLoadingAnimal();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mDst.reset();
        // 硬件加速的BUG
        mDst.lineTo(0,0);
        float stop = mLength * mAnimatorValue;
//        mPathMeasure.getSegment(0, stop, mDst, true);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0,Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG));

        canvas.save();
        canvas.rotate(360*mAnimatorValue, getMeasuredWidth()/2, getMeasuredHeight()/2);
        float start = (float) (stop - ((0.5 - Math.abs(mAnimatorValue - 0.5)) * mLength));
        mPathMeasure.getSegment(start, stop, mDst, true);
        canvas.drawPath(mDst, mPaint);
        canvas.restore();
    }
}
