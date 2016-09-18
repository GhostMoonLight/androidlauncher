package com.android.launcher3.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.android.launcher3.utils.Util;

/**
 * Created by wen on 2016/9/18.
 */
public class BezierMetaBall extends View {

    private int mDefaultWidth, mDefaultHeight;
    private int mFirstCircleX, mFirstCircleY;
    private int mSecondCircleX, mSecondCircleY;
    private int mFirstCircleRadius, mSecondCircleRadius;
    private Paint mPaint;

    public BezierMetaBall(Context context) {
        super(context);
        init();
    }

    public BezierMetaBall(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public BezierMetaBall(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public BezierMetaBall(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init(){
        mDefaultWidth = Util.dip2px(getContext(), 30);
        mDefaultHeight = Util.dip2px(getContext(), 30);
        mFirstCircleRadius = Util.dip2px(getContext(), 10);
        mSecondCircleRadius = Util.dip2px(getContext(), 20);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int w,h;

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthMode == MeasureSpec.EXACTLY){
            w = MeasureSpec.getSize(widthMeasureSpec);
        } else {
            w = mDefaultWidth;
            if (widthMode == MeasureSpec.AT_MOST){
                w = Math.min(w, widthSize);
            }
        }

        if (heightMode == MeasureSpec.EXACTLY){
            h = MeasureSpec.getSize(heightMeasureSpec);
        } else {
            h = mDefaultHeight;
            if(heightMode == MeasureSpec.AT_MOST){
                Math.min(h, heightSize);
            }
        }

        setMeasuredDimension(w, h);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mSecondCircleX = mFirstCircleX = getWidth()/2;
        mSecondCircleY = mFirstCircleY = getHeight()/2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }
}
