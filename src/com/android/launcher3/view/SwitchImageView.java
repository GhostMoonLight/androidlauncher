package com.android.launcher3.view;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;

/**
 * 上下滚动切换图片的view 设置图片不能使用setBackargound
 */
public class SwitchImageView extends View {

	private ValueAnimator mFlipAnimator;
	private BitmapDrawable mNextDrawable = null, mCurrentDrawbale;
	private float mFlipFraction = 0f;
	private int mMaxWidth = Integer.MAX_VALUE;
	private int mMaxHeight = Integer.MAX_VALUE;
	private Matrix matrix = new Matrix();
	private Paint paint = new Paint();

	public SwitchImageView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	public SwitchImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public SwitchImageView(Context context) {
		super(context);
		init();
	}

	private void init() {
		mFlipAnimator = ValueAnimator.ofFloat(0f, 1f).setDuration(500);
		mFlipAnimator.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(final ValueAnimator animation) {
				mFlipFraction = (Float) animation.getAnimatedValue();
				invalidate();
			}
		});
		mFlipAnimator.addListener(new AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {
			}
			@Override
			public void onAnimationRepeat(Animator animation) {
			}
			@Override
			public void onAnimationEnd(Animator animation) {
				mCurrentDrawbale = mNextDrawable;
				mNextDrawable = null;
				invalidate();
			}
			@Override
			public void onAnimationCancel(Animator animation) {
			}
		});
	}

	public void setNextDrawable(BitmapDrawable drawable) {
		if (mCurrentDrawbale == null) {
			mCurrentDrawbale = drawable;
			requestLayout();
			return;
		}

		mNextDrawable = drawable;
		switchDrawable();
	}

	private void switchDrawable() {
		if (!mFlipAnimator.isRunning()){
			mFlipAnimator.start();
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int w;
		int h;

		final int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
		final int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
		
		// Desired aspect ratio of the view's contents (not including padding)
		float desiredAspect = 0.0f;

		// We are allowed to change the view's width
		boolean resizeWidth = false;

		// We are allowed to change the view's height
		boolean resizeHeight = false;

		if (mCurrentDrawbale == null) {
			w = h = 0;
		} else {
			w = mCurrentDrawbale.getBitmap().getWidth();
			h = mCurrentDrawbale.getBitmap().getHeight();
			if (w <= 0)
				w = 1;
			if (h <= 0)
				h = 1;

			resizeWidth = widthSpecMode != MeasureSpec.EXACTLY;
			resizeHeight = heightSpecMode != MeasureSpec.EXACTLY;
			
			desiredAspect = (float) w / (float) h;
		}

		int pleft = getPaddingLeft();
		int pright = getPaddingRight();
		int ptop = getPaddingTop();
		int pbottom = getPaddingBottom();

		int widthSize;
		int heightSize;

		if (resizeWidth || resizeHeight) {
			
            widthSize = resolveAdjustedSize(w + pleft + pright, mMaxWidth, widthMeasureSpec);
            heightSize = resolveAdjustedSize(h + ptop + pbottom, mMaxHeight, heightMeasureSpec);
            
         // See what our actual aspect ratio is
            float actualAspect = (float)(widthSize - pleft - pright) /
                                    (heightSize - ptop - pbottom);
            
            if (Math.abs(actualAspect - desiredAspect) > 0.0000001) {
                
                boolean done = false;
                
                // Try adjusting width to be proportional to height
                if (resizeWidth) {
                    int newWidth = (int)(desiredAspect * (heightSize - ptop - pbottom)) +
                            pleft + pright;

                    // Allow the width to outgrow its original estimate if height is fixed.
                    if (!resizeHeight) {
                        widthSize = resolveAdjustedSize(newWidth, mMaxWidth, widthMeasureSpec);
                    }

                    if (newWidth <= widthSize) {
                        widthSize = newWidth;
                        done = true;
                    } 
                }
                
                // Try adjusting height to be proportional to width
                if (!done && resizeHeight) {
                    int newHeight = (int)((widthSize - pleft - pright) / desiredAspect) +
                            ptop + pbottom;

                    // Allow the height to outgrow its original estimate if width is fixed.
                    if (!resizeWidth) {
                        heightSize = resolveAdjustedSize(newHeight, mMaxHeight,
                                heightMeasureSpec);
                    }

                    if (newHeight <= heightSize) {
                        heightSize = newHeight;
                    }
                }
            }
		} else {
			w += pleft + pright;
			h += ptop + pbottom;

			w = Math.max(w, getSuggestedMinimumWidth());
			h = Math.max(h, getSuggestedMinimumHeight());

			widthSize = resolveSizeAndState(w, widthMeasureSpec, 0);
			heightSize = resolveSizeAndState(h, heightMeasureSpec, 0);
		}
		setMeasuredDimension(widthSize, heightSize);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (mNextDrawable == null) {
			if(mCurrentDrawbale != null){
				canvas.translate(getPaddingLeft(), getPaddingTop());
				canvas.drawBitmap(mCurrentDrawbale.getBitmap(), matrix, paint);
				canvas.restore();
			}
			return;
		}

		int height = getHeight() - getPaddingTop();

		canvas.save();
		canvas.translate(getPaddingLeft(), getPaddingTop() + height * mFlipFraction - height);
		canvas.drawBitmap(mNextDrawable.getBitmap(), matrix, paint);
		canvas.restore();
		canvas.save();
		
		canvas.translate(getPaddingLeft(), getPaddingTop() + height * mFlipFraction);
		canvas.drawBitmap(mCurrentDrawbale.getBitmap(), matrix, paint);
		canvas.restore();
	}

	private int resolveAdjustedSize(int desiredSize, int maxSize,
			int measureSpec) {
		int result = desiredSize;
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);
		switch (specMode) {
		case MeasureSpec.UNSPECIFIED:
			result = Math.min(desiredSize, maxSize);
			break;
		case MeasureSpec.AT_MOST:
			result = Math.min(Math.min(desiredSize, specSize), maxSize);
			break;
		case MeasureSpec.EXACTLY:
			result = specSize;
			break;
		}
		return result;
	}

}
