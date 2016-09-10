package com.android.launcher3.view;

import com.android.launcher3.FolderBase;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.OverScroller;

/**
 * 可以滑半屏的view 该布局最好是屏幕宽高  类似ScrollView
 */
public class SingleHandedView extends FrameLayout {
	
	private static final String TAG = "SingleHandedView";
	
	private static final int ANIMATION_DURATION = 1000;
	
	/**
     * Sentinel value for no current active pointer.
     * Used by {@link #mActivePointerId}.
     */
    private static final int INVALID_POINTER = -1;

	private OverScroller mScroller;// 滑动
	private Context mContext;
	private int mLastMotionY;
	private boolean isHalfing = false; // 是否是滑半屏状态
	private int mTouchSlop;
	private int mMinimumVelocity; // 最小速度
	private int mMaximumVelocity; // 最大速度
	private int mOverscrollDistance;
	private int mOverflingDistance;
	/**
     * True if the user is currently dragging this ScrollView around. This is
     * not the same as 'is being flinged', which can be checked by
     * mScroller.isFinished() (flinging begins when the user lifts his finger).
     */
	private boolean mIsBeingDragged = false;
	private VelocityTracker mVelocityTracker;
	/**
     * ID of the active pointer. This is used to retain consistency during
     * drags/flings if multiple pointers are used.
     */
	private int mActivePointerId = INVALID_POINTER;

	public SingleHandedView(Context context) {
		this(context, null);
	}

	public SingleHandedView(Context context, AttributeSet attrs) {
		//显示ScrollBar的系统样式
		this(context, attrs, android.R.attr.scrollViewStyle);
	}

	public SingleHandedView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		init();
	}
	
	private void init() {
		mScroller = new OverScroller(mContext);
		setFocusable(true);
		/**
		 * FOCUS_BEFORE_DESCENDANTS ViewGroup本身先对焦点进行处理，如果没有处理则分发给child View进行处理
		 * FOCUS_AFTER_DESCENDANTS 先分发给Child View进行处理，如果所有的Child View都没有处理，则自己再处理
		 * FOCUS_BLOCK_DESCENDANTS ViewGroup本身进行处理，不管是否处理成功，都不会分发给ChildView进行处理
		 */
		setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
		setWillNotDraw(false);
		final ViewConfiguration configuration = ViewConfiguration
				.get(getContext());
		mTouchSlop = configuration.getScaledTouchSlop();
		mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
		mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
		mOverscrollDistance = configuration.getScaledOverscrollDistance();
		mOverflingDistance = configuration.getScaledOverflingDistance();
	}
	
	@Override
    protected void measureChild(View child, int parentWidthMeasureSpec, int parentHeightMeasureSpec) {
        ViewGroup.LayoutParams lp = child.getLayoutParams();

        int childWidthMeasureSpec;
        int childHeightMeasureSpec;

        childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec, getPaddingLeft()
                + getPaddingRight(), lp.width);

        childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);

        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    @Override
    protected void measureChildWithMargins(View child, int parentWidthMeasureSpec, int widthUsed,
            int parentHeightMeasureSpec, int heightUsed) {
        final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

        final int childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec,
        		getPaddingLeft() + getPaddingRight() + lp.leftMargin + lp.rightMargin
                        + widthUsed, lp.width);
        final int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                lp.topMargin + lp.bottomMargin, MeasureSpec.UNSPECIFIED);

        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }
    
    /**
     * @return Returns true this ScrollView can be scrolled
     */
    private boolean canScroll() {
        View child = getChildAt(0);
        if (child != null) {
            int childHeight = child.getHeight();
            return getHeight() < childHeight + getPaddingTop() + getPaddingBottom();
        }
        return false;
    }

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		/*
         * This method JUST determines whether we want to intercept the motion.
         * If we return true, onMotionEvent will be called and we do the actual
         * scrolling there.
         */

        /*
        * Shortcut the most recurring case: the user is in the dragging
        * state and he is moving his finger.  We want to intercept this
        * motion.
        */
		final int action = ev.getAction();
        if ((action == MotionEvent.ACTION_MOVE) && (mIsBeingDragged)) {
            return true;
        }
        
		switch (ev.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:{
			final int y = (int) ev.getY();
            if (!inChild((int) ev.getX(), (int) y)) {
                mIsBeingDragged = false;
                recycleVelocityTracker();
                break;
            }
            /*
             * Remember location of down touch.
             * ACTION_DOWN always refers to pointer index 0.
             */
            mLastMotionY = y;
            mActivePointerId = ev.getPointerId(0);

            initOrResetVelocityTracker();
            mVelocityTracker.addMovement(ev);
            /*
            * If being flinged and user touches the screen, initiate drag;
            * otherwise don't.  mScroller.isFinished should be false when
            * being flinged.
            */
            mIsBeingDragged = !mScroller.isFinished();
			break;
		}
		case MotionEvent.ACTION_MOVE:{
			final int activePointerId = mActivePointerId;
            if (activePointerId == INVALID_POINTER) {
                // If we don't have a valid id, the touch down wasn't on content.
                break;
            }
            final int pointerIndex = ev.findPointerIndex(activePointerId);
            if (pointerIndex == -1) {
                Log.e(TAG, "Invalid pointerId=" + activePointerId
                        + " in onInterceptTouchEvent");
                break;
            }
            final int y = (int) ev.getY(pointerIndex);
            final int yDiff = Math.abs(y - mLastMotionY);
            if (yDiff > mTouchSlop) {
                mIsBeingDragged = true;
                mLastMotionY = y;
                initVelocityTrackerIfNotExists();
                mVelocityTracker.addMovement(ev);
                final ViewParent parent = getParent();
                if (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(true);
                }
                if (getScrollY() < 0){
                	isHalfing = true;
                }
            }
			break;
		}
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			/* Release the drag */
            mIsBeingDragged = false;
            mActivePointerId = INVALID_POINTER;
            recycleVelocityTracker();
            if (getScrollY() > 0){
	            if (mScroller.springBack(getScrollX(), getScrollY(), 0, 0, 0, getScrollRange())) {
	                postInvalidateOnAnimation();
	            }
            }
            break;
        case MotionEvent.ACTION_POINTER_UP:
            onSecondaryPointerUp(ev);
            break;		}
		return false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
        initVelocityTrackerIfNotExists();
        mVelocityTracker.addMovement(ev);
        
        final int action = ev.getAction();
		switch (action & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:{
			if (getChildCount() == 0) {
                return false;
            }
            if ((mIsBeingDragged = !mScroller.isFinished())) {
                final ViewParent parent = getParent();
                if (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(true);
                }
            }

            /*
             * If being flinged and user touches, stop the fling. isFinished
             * will be false if being flinged.
             */
            if (!mScroller.isFinished()) {
                mScroller.abortAnimation();
            }

            // Remember where the motion event started
            mLastMotionY = (int) ev.getY();
            mActivePointerId = ev.getPointerId(0);
			break;
		}
		case MotionEvent.ACTION_MOVE:{
			final int activePointerIndex = ev.findPointerIndex(mActivePointerId);
            if (activePointerIndex == -1) {
                Log.e(TAG, "Invalid pointerId=" + mActivePointerId + " in onTouchEvent");
                break;
            }

            final int y = (int) ev.getY(activePointerIndex);
            int deltaY = mLastMotionY - y;
            if (!mIsBeingDragged && Math.abs(deltaY) > mTouchSlop) {
                final ViewParent parent = getParent();
                if (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(true);
                }
                mIsBeingDragged = true;
                if (deltaY > 0) {
                    deltaY -= mTouchSlop;
                } else {
                    deltaY += mTouchSlop;
                }
            }
            if (mIsBeingDragged) {
                // Scroll to follow the motion event
                mLastMotionY = y;

                final int range = getScrollRange();

                // Calling overScrollBy will call onOverScrolled, which
                // calls onScrollChanged if applicable.
                if (isHalfing || (getScrollY() == 0 && deltaY < 0) || getScrollY() < 0){
                	isHalfing = true;
                	if (getScrollY() > 50 && deltaY>0){
                		deltaY = 0;
                	}
                	if (getScrollY() < -getHeight()/4*3){
                		deltaY = 0;
                	}
                	scrollBy(0, deltaY);
                }else{
	                if (overScrollBy(0, deltaY, 0, getScrollY(),
	                        0, range, 0, mOverscrollDistance, true)) {
	                    // Break our velocity if we hit a scroll barrier.
	                    mVelocityTracker.clear();
	                }
                }
            }
			break;
		}
		case MotionEvent.ACTION_UP:{
			if (mIsBeingDragged) {
                final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                int initialVelocity = (int) velocityTracker.getYVelocity(mActivePointerId);
                if (!isHalfing){
	                if (getChildCount() > 0) {
	                    if ((Math.abs(initialVelocity) > mMinimumVelocity)) {
	                        fling(-initialVelocity);
	                    } else {
	                        if (mScroller.springBack(getScrollX(), getScrollY(), 0, 0, 0,
	                                getScrollRange())) {
	                            postInvalidateOnAnimation();
	                        }
	                    }
	                }
                }else{
                	int duration = ANIMATION_DURATION;
                	int half = (int) (getHeight()*0.4f);
                	if (initialVelocity > mMinimumVelocity*20){
                		//向下滑动
                		if (Math.abs(-(half+getScrollY())) < 200){
                			duration /= 2;
                		}
                		mScroller.startScroll(0, getScrollY(), 0, -(half+getScrollY()), duration);
                	}else if (initialVelocity < -mMinimumVelocity*20){
                		//向上滑动
                		if (Math.abs(-getScrollY()) < 200){
                			duration /= 2;
                		}
                		mScroller.startScroll(0, getScrollY(), 0, -getScrollY(), duration);
                	} else if (getScrollY() < -(getHeight()/3)){
                		if (Math.abs(-(half+getScrollY())) < 200){
                			duration /= 2;
                		}
                		//向下滑动
                		mScroller.startScroll(0, getScrollY(), 0, -(half+getScrollY()), duration);
                	} else if (getScrollY() > -(getHeight()/3)){
                		if (Math.abs(-getScrollY()) < 200){
                			duration /= 2;
                		}
                		//向上滑动
                		mScroller.startScroll(0, getScrollY(), 0, -getScrollY(), duration);
                	}
                	invalidate();  
                }

                mActivePointerId = INVALID_POINTER;
                endDrag();
            }else{
            	//如果点击的区域在mContainer之外
            	if (folder != null){
            		folder.closeFolder((int)ev.getRawX(), (int)ev.getRawY());
            	}
            }
            break;
		}
        case MotionEvent.ACTION_CANCEL:{
            if (getScrollY() > 0 && mIsBeingDragged && getChildCount() > 0) {
                if (mScroller.springBack(getScrollX(), getScrollY(), 0, 0, 0, getScrollRange())) {
                    postInvalidateOnAnimation();
                }
                mActivePointerId = INVALID_POINTER;
                endDrag();
            }
            break;
        }
        case MotionEvent.ACTION_POINTER_DOWN: {
            final int index = ev.getActionIndex();
            mLastMotionY = (int) ev.getY(index);
            mActivePointerId = ev.getPointerId(index);
            break;
        }
        case MotionEvent.ACTION_POINTER_UP:
            onSecondaryPointerUp(ev);
            mLastMotionY = (int) ev.getY(ev.findPointerIndex(mActivePointerId));
            break;
		}

		return true;
	}
	
	private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >>
                MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            // TODO: Make this decision more intelligent.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mLastMotionY = (int) ev.getY(newPointerIndex);
            mActivePointerId = ev.getPointerId(newPointerIndex);
            if (mVelocityTracker != null) {
                mVelocityTracker.clear();
            }
        }
    }
	
	@Override
    protected void onOverScrolled(int scrollX, int scrollY,
            boolean clampedX, boolean clampedY) {
        // Treat animating scrolls differently; see #computeScroll() for why.
        if (!mScroller.isFinished()) {
            final int oldX = getScrollX();
            final int oldY = getScrollY();
            setScrollX(scrollX);
            setScrollY(scrollY);
            onScrollChanged(getScrollX(), getScrollY(), oldX, oldY);
            if (clampedY) {
                mScroller.springBack(getScrollX(), getScrollY(), 0, 0, 0, getScrollRange());
            }
        } else {
            super.scrollTo(scrollX, scrollY);
        }

        awakenScrollBars();
    }
	
	//滑动范围
	private int getScrollRange() {
        int scrollRange = 0;
        if (getChildCount() > 0) {
            View child = getChildAt(0);
            scrollRange = Math.max(0,
                    child.getHeight() - (getHeight() - getPaddingBottom() - getPaddingTop()));
        }
        return scrollRange;
    }

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			
			int oldX = getScrollX();
            int oldY = getScrollY();
            int x = mScroller.getCurrX();
            int y = mScroller.getCurrY();

            if ((oldX != x || oldY != y) && !isHalfing) {
                final int range = getScrollRange();
                overScrollBy(x - oldX, y - oldY, oldX, oldY, 0, range,
                        0, mOverflingDistance, false);
                onScrollChanged(getScrollX(), getScrollY(), oldX, oldY);
            }
			if (isHalfing){
				scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			}
			if (mScroller.isFinished()){
				isHalfing = false;
			}
			invalidate();  
			if (!awakenScrollBars()) {
                // Keep on drawing until the animation has finished.
                postInvalidateOnAnimation();
            }
		}
	}

	private void initOrResetVelocityTracker() {
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		} else {
			mVelocityTracker.clear();
		}
	}

	private void initVelocityTrackerIfNotExists() {
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
	}

	private void recycleVelocityTracker() {
		if (mVelocityTracker != null) {
			mVelocityTracker.recycle();
			mVelocityTracker = null;
		}
	}
	
	private boolean inChild(int x, int y) {
        if (getChildCount() > 0) {
            final int scrollY = getScrollY();
            final View child = getChildAt(0);
            return !(y < child.getTop() - scrollY
                    || y >= child.getBottom() - scrollY
                    || x < child.getLeft()
                    || x >= child.getRight());
        }
        return false;
    }

	@Override
	public void addView(View child) {
		if (getChildCount() > 0) {
			throw new IllegalStateException(
					"ScrollView can host only one direct child");
		}

		super.addView(child);
	}

	@Override
	public void addView(View child, int index) {
		if (getChildCount() > 0) {
			throw new IllegalStateException(
					"ScrollView can host only one direct child");
		}

		super.addView(child, index);
	}

	@Override
	public void addView(View child, ViewGroup.LayoutParams params) {
		if (getChildCount() > 0) {
			throw new IllegalStateException(
					"ScrollView can host only one direct child");
		}

		super.addView(child, params);
	}

	@Override
	public void addView(View child, int index, ViewGroup.LayoutParams params) {
		if (getChildCount() > 0) {
			throw new IllegalStateException(
					"ScrollView can host only one direct child");
		}

		super.addView(child, index, params);
	}
	
	/**
     * Fling the scroll view
     *
     * @param velocityY The initial velocity in the Y direction. Positive
     *                  numbers mean that the finger/cursor is moving down the screen,
     *                  which means we want to scroll towards the top.
     */
    public void fling(int velocityY) {
        if (getChildCount() > 0) {
            int height = getHeight() - getPaddingBottom() - getPaddingTop();
            int bottom = getChildAt(0).getHeight();

            mScroller.fling(getScrollX(), getScrollY(), 0, velocityY, 0, 0, 0,
                    Math.max(0, bottom - height), 0, height/2);
            postInvalidateOnAnimation();
        }
    }

    private void endDrag() {
        mIsBeingDragged = false;

        recycleVelocityTracker();
    }
    
    /**
     * <p>The scroll range of a scroll view is the overall height of all of its
     * children.</p>
     */
    @Override
    protected int computeVerticalScrollRange() {
        final int count = getChildCount();
        final int contentHeight = getHeight() - getPaddingBottom() - getPaddingTop();
        if (count == 0) {
            return contentHeight;
        }

        int scrollRange = getChildAt(0).getBottom();
        final int scrollY = getScrollY();
        final int overscrollBottom = Math.max(0, scrollRange - contentHeight);
        if (scrollY < 0) {
            scrollRange -= scrollY;
        } else if (scrollY > overscrollBottom) {
            scrollRange += scrollY - overscrollBottom;
        }

        return scrollRange;
    }
    
    @Override
    protected int computeVerticalScrollOffset() {
        return Math.max(0, super.computeVerticalScrollOffset());
    }
    private FolderBase folder;
    public void setFolder(FolderBase folder){
    	this.folder = folder;
    }
}
