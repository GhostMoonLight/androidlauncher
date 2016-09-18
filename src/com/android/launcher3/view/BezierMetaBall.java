package com.android.launcher3.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.android.launcher3.utils.Util;

/**
 * Created by wen on 2016/9/18.
 */
public class BezierMetaBall extends View {

    private int mDefaultWidth, mDefaultHeight;
    private float mFirstCircleX, mFirstCircleY;
    private float mSecondCircleX, mSecondCircleY;
    private int mFirstCircleRadius, mSecondCircleRadius;
    private int mTouchSlop;
    private Paint mPaint;
    private Path mPath;

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
        mSecondCircleRadius = Util.dip2px(getContext(), 15);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setColor(Color.RED);

        mPath = new Path();

        mTouchSlop = ViewConfiguration.getTouchSlop();
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
        Log.e("AAAAA", "mSecondCircleX:"+mSecondCircleX+" mSecondCircleY:"+mSecondCircleY);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawCircle(mFirstCircleX, mFirstCircleY, mFirstCircleRadius, mPaint);
        canvas.drawCircle(mSecondCircleX, mSecondCircleY, mSecondCircleRadius, mPaint);
        canvas.drawPath(mPath, mPaint);
    }

    private float mStartDownX, mStartDwonY, mLastDownX, mLastDwonY;
    private boolean isIntercept;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = false;
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                mLastDownX = mStartDownX = event.getX();
                mLastDwonY = mStartDwonY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float dx, dy;
                dx = Math.abs(event.getX() - mStartDownX);
                dy = Math.abs(event.getY() - mStartDwonY);
                if (dx > mTouchSlop || dy > mTouchSlop){
                    getParent().requestDisallowInterceptTouchEvent(true);
                    isIntercept = true;
                }

                if (isIntercept){
                    mSecondCircleX = (int)event.getX();
                    mSecondCircleY = (int)event.getY();
                    caculate();
                    invalidate();
                }

                mLastDownX = event.getX();
                mLastDownX = event.getY();
                result = isIntercept;
                break;
            case MotionEvent.ACTION_UP:
                isIntercept = false;

                mSecondCircleX = mFirstCircleX;
                mSecondCircleY = mFirstCircleY;
                invalidate();
                break;
        }

        return true;
    }

    private void caculate() {
        float mControlX = (mFirstCircleX + mSecondCircleX)/2.0f;
        float mControlY = (mFirstCircleY + mSecondCircleY)/2.0f;

        float distance = (float) Math.sqrt((mControlX - mFirstCircleX) * (mControlX - mFirstCircleX) + (mControlY - mFirstCircleY) * (mControlY - mFirstCircleY));
        double a = Math.acos(mFirstCircleRadius / distance);

        double b = Math.acos((mControlX - mFirstCircleX) / distance);
        float offsetX1 = (float) (mFirstCircleRadius * Math.cos(a - b));
        float offsetY1 = (float) (mFirstCircleRadius * Math.sin(a - b));
        float tanX1 = mFirstCircleX + offsetX1;
        float tanY1 = mFirstCircleY - offsetY1;

        double c = Math.acos((mControlY - mFirstCircleY) / distance);
        float offsetX2 = (float) (mFirstCircleRadius * Math.sin(a - c));
        float offsetY2 = (float) (mFirstCircleRadius * Math.cos(a - c));
        float tanX2 = mFirstCircleX - offsetX2;
        float tanY2 = mFirstCircleY + offsetY2;

        double d = Math.acos((mSecondCircleY - mControlY) / distance);
        float offsetX3 = (float) (mSecondCircleRadius * Math.sin(a - d));
        float offsetY3 = (float) (mSecondCircleRadius * Math.cos(a - d));
        float tanX3 = mSecondCircleX + offsetX3;
        float tanY3 = mSecondCircleY - offsetY3;

        double e = Math.acos((mSecondCircleX - mControlX) / distance);
        float offsetX4 = (float) (mSecondCircleRadius * Math.cos(a - e));
        float offsetY4 = (float) (mSecondCircleRadius * Math.sin(a - e));
        float tanX4 = mSecondCircleX - offsetX4;
        float tanY4 = mSecondCircleY + offsetY4;

        mPath.reset();
        mPath.moveTo(tanX1, tanY1);
        mPath.quadTo(mControlX, mControlY, tanX3, tanY3);
        mPath.lineTo(tanX4, tanY4);
        mPath.quadTo(mControlX, mControlY, tanX2, tanY2);
    }
}
