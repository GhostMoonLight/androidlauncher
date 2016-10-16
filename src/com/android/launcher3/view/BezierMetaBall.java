package com.android.launcher3.view;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.android.launcher3.Interpolator.SpringInterpolator;
import com.android.launcher3.utils.Util;

/**
 * Created by wen on 2016/9/18.
 * 杯赛尔曲线拟合，模仿QQ消息的拖拽
 */
public class BezierMetaBall extends View {

    private int mDefaultWidth, mDefaultHeight;
    private float mFirstCircleX, mFirstCircleY;
    private float mSecondCircleX, mSecondCircleY;
    private float mFirstCircleRadius, mSecondCircleRadius;
    private int mTouchSlop;
    private Paint mPaint;
    private Path mPath;
    AnimatorSet mAnimXY;
    private ValueAnimator valueX;
    private ValueAnimator valueY;

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
        mDefaultWidth = Util.dip2px(getContext(), 40);
        mDefaultHeight = Util.dip2px(getContext(), 40);
        mFirstCircleRadius = Util.dip2px(getContext(), 11);
        mSecondCircleRadius = Util.dip2px(getContext(), 12);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setColor(Color.RED);
        mPath = new Path();

        mTouchSlop = ViewConfiguration.getTouchSlop();

        mAnimXY = new AnimatorSet();
        valueX = ValueAnimator.ofFloat(mSecondCircleX, mFirstCircleX);
        valueY = ValueAnimator.ofFloat(mSecondCircleY, mFirstCircleY);
        mAnimXY.playTogether(valueX, valueY);
        valueX.setDuration(500);
        valueY.setDuration(500);
        valueX.setInterpolator(new SpringInterpolator(0.5f));
        valueY.setInterpolator(new SpringInterpolator(0.5f));
        valueX.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mSecondCircleX = (Float) animation.getAnimatedValue();
                caculate();
                invalidate();
            }
        });

        valueY.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mSecondCircleY = (Float) animation.getAnimatedValue();
                caculate();
                invalidate();
            }
        });
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
                mAnimXY.cancel();
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
                    mSecondCircleX = event.getX();
                    mSecondCircleY = event.getY();
                    caculate();
                    invalidate();
                }

                mLastDownX = event.getX();
                mLastDownX = event.getY();
                result = isIntercept;
                break;
            case MotionEvent.ACTION_UP:
                isIntercept = false;
//
//                mSecondCircleX = mFirstCircleX;
//                mSecondCircleY = mFirstCircleY;
//                invalidate();
                valueX.setFloatValues(mSecondCircleX, mFirstCircleX);
                valueY.setFloatValues(mSecondCircleY, mFirstCircleY);
                mAnimXY.start();
                break;
        }

        return true;
    }

    private void caculate() {

        boolean flag = (mSecondCircleY - mFirstCircleY) * (mSecondCircleX - mFirstCircleY) >= 0;
        float tanX1, tanY1, tanX2, tanY2, tanX3, tanY3, tanX4, tanY4;

        float mControlX = (mFirstCircleX + mSecondCircleX) / 2.0f;
        float mControlY = (mFirstCircleY + mSecondCircleY) / 2.0f;

        float distance = (float) Math.sqrt((mControlX - mFirstCircleX) * (mControlX - mFirstCircleX) + (mControlY - mFirstCircleY) * (mControlY - mFirstCircleY));

        double a = Math.acos(mFirstCircleRadius / distance);
        double angle;
        /**
         *       |
         *   3   |   2
         *       |
         * ---------------
         *       |
         *   4   |   1
         *       |
         *  自定义象限划分
         */

        if (flag) {  //1或3象限
            if (mSecondCircleY - mFirstCircleY >= 0) {  //1象限
                double b = Math.acos(Math.abs(mControlX - mFirstCircleX) / distance);
                float offsetX1 = (float) (mFirstCircleRadius * Math.cos(a - b));
                float offsetY1 = (float) (mFirstCircleRadius * Math.sin(a - b));
                tanX1 = mFirstCircleX + offsetX1;
                tanY1 = mFirstCircleY - offsetY1;

                double c = Math.acos(Math.abs(mControlY - mFirstCircleY) / distance);
                float offsetX2 = (float) (mFirstCircleRadius * Math.sin(a - c));
                float offsetY2 = (float) (mFirstCircleRadius * Math.cos(a - c));
                tanX2 = mFirstCircleX - offsetX2;
                tanY2 = mFirstCircleY + offsetY2;

                a = Math.acos(mSecondCircleRadius / distance);
                double d = Math.acos(Math.abs(mSecondCircleY - mControlY) / distance);
                float offsetX3 = (float) (mSecondCircleRadius * Math.sin(a - d));
                float offsetY3 = (float) (mSecondCircleRadius * Math.cos(a - d));
                tanX3 = mSecondCircleX + offsetX3;
                tanY3 = mSecondCircleY - offsetY3;

                double e = Math.acos(Math.abs(mSecondCircleX - mControlX) / distance);
                float offsetX4 = (float) (mSecondCircleRadius * Math.cos(a - e));
                float offsetY4 = (float) (mSecondCircleRadius * Math.sin(a - e));
                tanX4 = mSecondCircleX - offsetX4;
                tanY4 = mSecondCircleY + offsetY4;

            }else{//3象限

                double b = Math.acos(Math.abs(mControlX - mFirstCircleX) / distance);
                float offsetX1 = (float) (mFirstCircleRadius * Math.cos(a - b));
                float offsetY1 = (float) (mFirstCircleRadius * Math.sin(a - b));
                tanX1 = mFirstCircleX - offsetX1;
                tanY1 = mFirstCircleY + offsetY1;

                double c = Math.acos(Math.abs(mControlY - mFirstCircleY) / distance);
                float offsetX2 = (float) (mFirstCircleRadius * Math.sin(a - c));
                float offsetY2 = (float) (mFirstCircleRadius * Math.cos(a - c));
                tanX2 = mFirstCircleX + offsetX2;
                tanY2 = mFirstCircleY - offsetY2;

                a = Math.acos(mSecondCircleRadius / distance);
                double d = Math.acos(Math.abs(mSecondCircleY - mControlY) / distance);
                float offsetX3 = (float) (mSecondCircleRadius * Math.sin(a - d));
                float offsetY3 = (float) (mSecondCircleRadius * Math.cos(a - d));
                tanX3 = mSecondCircleX - offsetX3;
                tanY3 = mSecondCircleY + offsetY3;

                double e = Math.acos(Math.abs(mSecondCircleX - mControlX) / distance);
                float offsetX4 = (float) (mSecondCircleRadius * Math.cos(a - e));
                float offsetY4 = (float) (mSecondCircleRadius * Math.sin(a - e));
                tanX4 = mSecondCircleX + offsetX4;
                tanY4 = mSecondCircleY - offsetY4;
            }
        }else{ //2或4象限

            if (mSecondCircleY - mFirstCircleY <= 0){   //2象限
                double c = Math.acos(Math.abs(mControlY - mFirstCircleY) / distance);
                float offsetX1 = (float) (mFirstCircleRadius * Math.sin(a - c));
                float offsetY1 = (float) (mFirstCircleRadius * Math.cos(a - c));
                tanX1 = mFirstCircleX - offsetX1;
                tanY1 = mFirstCircleY - offsetY1;

                double b = Math.acos(Math.abs(mControlX - mFirstCircleX) / distance);
                float offsetX2 = (float) (mFirstCircleRadius * Math.cos(a - b));
                float offsetY2 = (float) (mFirstCircleRadius * Math.sin(a - b));
                tanX2 = mFirstCircleX + offsetX2;
                tanY2 = mFirstCircleY + offsetY2;

                a = Math.acos(mSecondCircleRadius / distance);
                double e = Math.acos(Math.abs(mSecondCircleX - mControlX) / distance);
                angle = a - e;
                float offsetX3 = (float) (mSecondCircleRadius * Math.cos(angle));
                float offsetY3 = (float) (mSecondCircleRadius * Math.sin(angle));
                tanX3 = mSecondCircleX - offsetX3;
                tanY3 = mSecondCircleY - offsetY3;

                double d = Math.acos(Math.abs(mSecondCircleY - mControlY) / distance);
                angle = a - d;
                float offsetX4 = (float) (mSecondCircleRadius * Math.sin(angle));
                float offsetY4 = (float) (mSecondCircleRadius * Math.cos(angle));
                tanX4 = mSecondCircleX + offsetX4;
                tanY4 = mSecondCircleY + offsetY4;

            } else {//4象限
                double b = Math.acos(Math.abs(mControlX - mFirstCircleX) / distance);
                float offsetX1 = (float) (mFirstCircleRadius * Math.cos(a - b));
                float offsetY1 = (float) (mFirstCircleRadius * Math.sin(a - b));
                tanX1 = mFirstCircleX - offsetX1;
                tanY1 = mFirstCircleY - offsetY1;

                double c = Math.acos(Math.abs(mControlY - mFirstCircleY) / distance);
                float offsetX2 = (float) (mFirstCircleRadius * Math.sin(a - c));
                float offsetY2 = (float) (mFirstCircleRadius * Math.cos(a - c));
                tanX2 = mFirstCircleX + offsetX2;
                tanY2 = mFirstCircleY + offsetY2;

                a = Math.acos(mSecondCircleRadius / distance);
                double d = Math.acos(Math.abs(mSecondCircleY - mControlY) / distance);
                float offsetX3 = (float) (mSecondCircleRadius * Math.sin(a - d));
                float offsetY3 = (float) (mSecondCircleRadius * Math.cos(a - d));
                tanX3 = mSecondCircleX - offsetX3;
                tanY3 = mSecondCircleY - offsetY3;

                double e = Math.acos(Math.abs(mSecondCircleX - mControlX) / distance);
                float offsetX4 = (float) (mSecondCircleRadius * Math.cos(a - e));
                float offsetY4 = (float) (mSecondCircleRadius * Math.sin(a - e));
                tanX4 = mSecondCircleX + offsetX4;
                tanY4 = mSecondCircleY + offsetY4;
            }
        }

        mPath.reset();
        mPath.moveTo(tanX1, tanY1);
        mPath.quadTo(mControlX, mControlY, tanX3, tanY3);
        mPath.lineTo(tanX4, tanY4);
        mPath.quadTo(mControlX, mControlY, tanX2, tanY2);
        mPath.close();
    }
}
