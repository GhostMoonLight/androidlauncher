package com.android.launcher3.view;

import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;

import com.android.launcher3.utils.Util;

/**
 * Created by cgx on 2016/11/20.
 * SearchView滑动的帮助类
 */

public class SearchViewHelper {

    private SearchView mSearchView;
    private float mXDown;
    private float mYDown;
    private VelocityTracker mVelocityTracker;    //速度跟踪器
    private float mDownLastX, mMoveX;
    private float mDownLastY, mMoveY;
    private boolean isUpDowning = false;
    private int mHieghtScreen;
    private int mTouchSlop;

    public SearchViewHelper(SearchView searchView){
        mSearchView = searchView;
        mHieghtScreen = Util.getScreenH();
        final ViewConfiguration configuration = ViewConfiguration.get(mSearchView.getContext());
        mTouchSlop = configuration.getScaledPagingTouchSlop();
    }

    public boolean onInterceptTouchEvent(MotionEvent ev){

        if (mSearchView.isAnimatorRuning()) return false;
        mMoveX = ev.getX();
        mMoveY = ev.getY();

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mXDown = ev.getX();
                mYDown = ev.getY();
                isUpDowning = false;

                initOrResetVelocityTracker();
                mVelocityTracker.addMovement(ev);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                recycleVelocityTracker();
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = Math.abs(mXDown - mMoveX);
                float dy = Math.abs(mYDown - mMoveY);
                if (dy > mTouchSlop && dx < mTouchSlop){
                    isUpDowning = true;
                }

                mDownLastX = mMoveX;
                mDownLastY = mMoveY;
        }
        return isUpDowning;
    }

    public boolean onTouchEvent(MotionEvent ev) {
        initVelocityTrackerIfNotExists();
        mVelocityTracker.addMovement(ev);

        mMoveX = ev.getX();
        mMoveY = ev.getY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:

                float dy = mMoveY - mDownLastY;
                if (isUpDowning) {
                    mSearchView.setHeightOffset(dy);
                }

                mDownLastX = mMoveX;
                mDownLastY = mMoveY;
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (isUpDowning) {
                    if (mMoveY - mYDown > mHieghtScreen * 0.25f || getYVelocity() > 2500) {
                        mSearchView.animatorExpand(mSearchView.getHeight(), mSearchView.getContentHeight());
                    } else {
                        mSearchView.animatorRetraction();
                    }
                }
                isUpDowning = false;
                recycleVelocityTracker();
                break;
        }
        return isUpDowning;
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

    //获取Y方向上的滑动速度
    private int getYVelocity(){
        mVelocityTracker.computeCurrentVelocity(1000);
        return (int) mVelocityTracker.getYVelocity(0);
    }
}
