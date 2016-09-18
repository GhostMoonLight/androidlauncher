package com.android.launcher3.view;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.BounceInterpolator;

/**
 * 贝塞尔曲线  练习
 */
public class SecondOrderBezier extends View {
	
	private float mAuxiliaryX, mCenterX;
    private float mAuxiliaryY, mCenterY;

    private float mStartPointX;
    private float mStartPointY;

    private float mEndPointX;
    private float mEndPointY;

    private Path mPath = new Path();
    private Paint mPaintBezier;
    AnimatorSet mAnimXY;
	private ValueAnimator valueX;
	private ValueAnimator valueY;

	public SecondOrderBezier(Context context) {
		super(context);
		init();
	}

	public SecondOrderBezier(Context context, AttributeSet attrs) {
		 super(context, attrs);
		 init();
	}
	
	public SecondOrderBezier(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
	
	private void init() {
		mPaintBezier = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintBezier.setStyle(Paint.Style.STROKE);
        mPaintBezier.setStrokeWidth(8);
		mPaintBezier.setStrokeCap(Paint.Cap.ROUND);
        mAnimXY = new AnimatorSet();
        
        valueX = ValueAnimator.ofInt((int)mAuxiliaryX, (int)mCenterX);
    	valueY = ValueAnimator.ofInt((int)mAuxiliaryX, (int)mCenterY);
    	mAnimXY.playTogether(valueX, valueY);
    	valueX.setDuration(500);
    	valueY.setDuration(500);
    	valueX.setInterpolator(new BounceInterpolator());
    	valueY.setInterpolator(new BounceInterpolator());
    	valueX.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				mAuxiliaryX = (Integer) animation.getAnimatedValue();
				invalidate();
			}
		});
    	
    	valueY.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				mAuxiliaryY = (Integer) animation.getAnimatedValue();
				invalidate();
			}
		});
	}
	
	@Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mStartPointX = w / 5;
        mStartPointY = h / 2 - 200;

        mEndPointX = w / 5 * 4;
        mEndPointY = h / 2 - 200;
        
        mCenterX = mAuxiliaryX = (mEndPointX - mStartPointX)/2+mStartPointX;
        mCenterY = mAuxiliaryY = mEndPointY;
    }
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		mPath.reset();
		mPath.moveTo(mStartPointX, mStartPointY);

        mPath.quadTo(mAuxiliaryX, mAuxiliaryY, mEndPointX, mEndPointY);
        canvas.drawPath(mPath, mPaintBezier);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mAnimXY.cancel();
        case MotionEvent.ACTION_MOVE:
			calculateControlPoint(event);
            invalidate();
            break;
        case MotionEvent.ACTION_UP:
			calculateControlPoint(event);
        case MotionEvent.ACTION_CANCEL:
            valueX.setIntValues((int)mAuxiliaryX, (int)mCenterX);
            valueY.setIntValues((int)mAuxiliaryY, (int)mCenterY);
            mAnimXY.start();
            break;
		}
		return true;
	}

	private void calculateControlPoint(MotionEvent event){
		mAuxiliaryX = 2*event.getX()-(mStartPointX+mEndPointX)/2;
		mAuxiliaryY = 2*event.getY()-(mStartPointY+mEndPointY)/2;
	}
}
