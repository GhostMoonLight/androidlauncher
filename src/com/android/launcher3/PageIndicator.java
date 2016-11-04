/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.launcher3;

import android.animation.LayoutTransition;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.android.launcher3.utils.Util;
import com.cuan.launcher.R;

import java.util.ArrayList;
/**
 *	桌面页面指示器
 */
public class PageIndicator extends LinearLayout implements PagedView.OnPageChangeListener{
    @SuppressWarnings("unused")
    private static final String TAG = "PageIndicator";
    // Want this to look good? Keep it odd
    private static final boolean MODULATE_ALPHA_ENABLED = false;

    private LayoutInflater mLayoutInflater;
    private int[] mWindowRange = new int[2];
    private int mMaxWindowSize;

    private ArrayList<PageIndicatorMarker> mMarkers =
            new ArrayList<PageIndicatorMarker>();
    private int mActiveMarkerIndex;  //当前选中的点

    public static class PageMarkerResources {
        int activeId;
        int inactiveId;

        public PageMarkerResources() {
            activeId = R.drawable.indicator_point_selected;
            inactiveId = R.drawable.indicator_point_unselect;
        }
        public PageMarkerResources(int aId, int iaId) {
            activeId = aId;
            inactiveId = iaId;
        }
    }

    public PageIndicator(Context context) {
        this(context, null);
    }

    public PageIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PageIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.PageIndicator, defStyle, 0);
        mMaxWindowSize = a.getInteger(R.styleable.PageIndicator_windowSize, 15);
        mWindowRange[0] = 0;
        mWindowRange[1] = 0;
        mLayoutInflater = LayoutInflater.from(context);
        a.recycle();

        // Set the layout transition properties
        LayoutTransition transition = getLayoutTransition();
        transition.setDuration(175);
    }

    private void enableLayoutTransitions() {
        LayoutTransition transition = getLayoutTransition();
        transition.enableTransitionType(LayoutTransition.APPEARING);
        transition.enableTransitionType(LayoutTransition.DISAPPEARING);
        transition.enableTransitionType(LayoutTransition.CHANGE_APPEARING);
        transition.enableTransitionType(LayoutTransition.CHANGE_DISAPPEARING);
    }

    private void disableLayoutTransitions() {
        LayoutTransition transition = getLayoutTransition();
        transition.disableTransitionType(LayoutTransition.APPEARING);
        transition.disableTransitionType(LayoutTransition.DISAPPEARING);
        transition.disableTransitionType(LayoutTransition.CHANGE_APPEARING);
        transition.disableTransitionType(LayoutTransition.CHANGE_DISAPPEARING);
    }

    void offsetWindowCenterTo(int activeIndex, boolean allowAnimations) {
        if (activeIndex < 0) {
            new Throwable().printStackTrace();
        }
        int windowSize = Math.min(mMarkers.size(), mMaxWindowSize);
        int hWindowSize = (int) windowSize / 2;
        float hfWindowSize = windowSize / 2f;
        int windowStart = Math.max(0, activeIndex - hWindowSize);
        int windowEnd = Math.min(mMarkers.size(), windowStart + mMaxWindowSize);
        windowStart = windowEnd - Math.min(mMarkers.size(), windowSize);
        int windowMid = windowStart + (windowEnd - windowStart) / 2;
        boolean windowAtStart = (windowStart == 0);
        boolean windowAtEnd = (windowEnd == mMarkers.size());
        boolean windowMoved = (mWindowRange[0] != windowStart) || (mWindowRange[1] != windowEnd);

        if (!allowAnimations) {
            disableLayoutTransitions();
        }

        // Remove all the previous children that are no longer in the window
        for (int i = getChildCount() - 1; i >= 0; --i) {
            PageIndicatorMarker marker = (PageIndicatorMarker) getChildAt(i);
            int markerIndex = mMarkers.indexOf(marker);
            if (markerIndex < windowStart || markerIndex >= windowEnd) {
                removeView(marker);
            }
        }

        // Add all the new children that belong in the window
        for (int i = 0; i < mMarkers.size(); ++i) {
            PageIndicatorMarker marker = (PageIndicatorMarker) mMarkers.get(i);
            if (windowStart <= i && i < windowEnd) {
                if (indexOfChild(marker) < 0) {
                    addView(marker, i - windowStart);
                }
                if (i == activeIndex) {
                    marker.activate(windowMoved);
                } else {
                    marker.inactivate(windowMoved);
                }
            } else {
                marker.inactivate(true);
            }

            if (MODULATE_ALPHA_ENABLED) {
                // Update the marker's alpha
                float alpha = 1f;
                if (mMarkers.size() > windowSize) {
                    if ((windowAtStart && i > hWindowSize) ||
                        (windowAtEnd && i < (mMarkers.size() - hWindowSize)) ||
                        (!windowAtStart && !windowAtEnd)) {
                        alpha = 1f - Math.abs((i - windowMid) / hfWindowSize);
                    }
                }
                marker.animate().alpha(alpha).setDuration(500).start();
            }
        }

        if (!allowAnimations) {
            enableLayoutTransitions();
        }

        mWindowRange[0] = windowStart;
        mWindowRange[1] = windowEnd;
    }

    void addMarker(int index, PageMarkerResources marker, boolean allowAnimations) {
        index = Math.max(0, Math.min(index, mMarkers.size()));

        PageIndicatorMarker m = (PageIndicatorMarker) mLayoutInflater.inflate(R.layout.page_indicator_marker, this, false);
        m.setMarkerDrawables(marker.activeId, marker.inactiveId);

        mMarkers.add(index, m);
        offsetWindowCenterTo(mActiveMarkerIndex, allowAnimations);
    }
    void addMarkers(ArrayList<PageMarkerResources> markers, boolean allowAnimations) {
        for (int i = 0; i < markers.size(); ++i) {
            addMarker(Integer.MAX_VALUE, markers.get(i), allowAnimations);
        }
    }

    void updateMarker(int index, PageMarkerResources marker) {
        PageIndicatorMarker m = mMarkers.get(index);
        m.setMarkerDrawables(marker.activeId, marker.inactiveId);
    }

    void removeMarker(int index, boolean allowAnimations) {
        if (mMarkers.size() > 0) {
            index = Math.max(0, Math.min(mMarkers.size() - 1, index));
            mMarkers.remove(index);
            offsetWindowCenterTo(mActiveMarkerIndex, allowAnimations);
        }
    }
    void removeAllMarkers(boolean allowAnimations) {
        while (mMarkers.size() > 0) {
            removeMarker(Integer.MAX_VALUE, allowAnimations);
        }
    }

    void setActiveMarker(int index) {
        // Center the active marker
        mActiveMarkerIndex = index;
        offsetWindowCenterTo(index, false);
    }

    void dumpState(String txt) {
        System.out.println(txt);
        System.out.println("\tmMarkers: " + mMarkers.size());
        for (int i = 0; i < mMarkers.size(); ++i) {
            PageIndicatorMarker m = mMarkers.get(i);
            System.out.println("\t\t(" + i + ") " + m);
        }
        System.out.println("\twindow: [" + mWindowRange[0] + ", " + mWindowRange[1] + "]");
        System.out.println("\tchildren: " + getChildCount());
        for (int i = 0; i < getChildCount(); ++i) {
            PageIndicatorMarker m = (PageIndicatorMarker) getChildAt(i);
            System.out.println("\t\t(" + i + ") " + m);
        }
        System.out.println("\tactive: " + mActiveMarkerIndex);
    }

    private float mFirstCircleX, mFirstCircleY;
    private float mSecondCircleX, mSecondCircleY;
    private float mFirstCircleRadius = 0, mSecondCircleRadius = 0;
    private Path mPath;
    private boolean isUseBezierScrollAnimal = false;
    private Paint mPaint;

    //是否使用页面指示器动画，屏幕滑动的时候页面指示器会显示相应的动画
    public void setUseBezierScrollAnimal(boolean isUseBezierScrollAnimal){
        this.isUseBezierScrollAnimal = isUseBezierScrollAnimal;
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setColor(Color.WHITE);
        mPaint.setAntiAlias(true);

        mPath = new Path();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (getChildCount() >= 3 && isUseBezierScrollAnimal) {

            canvas.drawCircle(mFirstCircleX, mFirstCircleY,
                    mFirstCircleRadius, mPaint);
            canvas.drawCircle(mSecondCircleX, mSecondCircleY,
                    mSecondCircleRadius, mPaint);
            caculate();
            canvas.drawPath(mPath, mPaint);
        }
    }

    private void caculate() {

        float tanX1, tanY1, tanX2, tanY2, tanX3, tanY3, tanX4, tanY4;

        float mControlX = (mFirstCircleX + mSecondCircleX) / 2.0f;
        float mControlY = (mFirstCircleY + mSecondCircleY) / 2.0f;

        float distance = Math.abs(mControlX - mFirstCircleX);
        double a = Math.acos(mFirstCircleRadius / distance);

        //弧度制   Math.Sin(Math.PI*30.0/180.0);   度数＊Math.PI/180.0
        float offsetX1 = (float) (mFirstCircleRadius * Math.cos(a));
        float offsetY1 = (float) (mFirstCircleRadius * Math.sin(a));
        tanX1 = mFirstCircleX + offsetX1;
        tanY1 = mFirstCircleY - offsetY1;

        tanX2 = tanX1;
        tanY2 = mFirstCircleY + offsetY1;

        a = Math.acos(mSecondCircleRadius / distance);
        offsetX1 = (float) (mSecondCircleRadius * Math.cos(a));
        offsetY1 = (float) (mSecondCircleRadius * Math.sin(a));
        tanX3 = mSecondCircleX - offsetX1;
        tanY3 = mSecondCircleY - offsetY1;

        tanX4 = tanX3;
        tanY4 = mSecondCircleY + offsetY1;


        mPath.reset();
        mPath.moveTo(tanX1, tanY1);
        mPath.quadTo(mControlX, mControlY, tanX3, tanY3);
        mPath.lineTo(tanX4, tanY4);
        mPath.quadTo(mControlX, mControlY, tanX2, tanY2);
        mPath.close();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (getChildCount() >= 3) {

            View view = getChildAt(position);
            if (positionOffsetPixels >= 0) {
                mFirstCircleRadius = Util.dip2px(8)/2;
                mSecondCircleRadius = Util.dip2px(6.5f)/2;
                mFirstCircleX = view.getX() + view.getWidth() / 2;
                mFirstCircleY = view.getY() + view.getHeight() / 2;
                if (position < getChildCount() - 1) {
                    view = getChildAt(position + 1);
                }
                mSecondCircleX = view.getLeft() + view.getWidth() / 2;
                mSecondCircleY = view.getTop() + view.getHeight() / 2;

                mSecondCircleX = mFirstCircleX + (Math.abs(mSecondCircleX - mFirstCircleX) * positionOffset);
            }else{
                mFirstCircleRadius = Util.dip2px(6.5f)/2;
                mSecondCircleRadius = Util.dip2px(8)/2;
                mSecondCircleX = view.getX() + view.getWidth() / 2;
                mSecondCircleY = view.getY() + view.getHeight() / 2;
                if (position > 0) {
                    view = getChildAt(position - 1);
                }
                mFirstCircleX = view.getLeft() + view.getWidth() / 2;
                mFirstCircleY = view.getTop() + view.getHeight() / 2;

                mFirstCircleX = mSecondCircleX + (Math.abs(mSecondCircleX - mFirstCircleX) * positionOffset);
            }
            invalidate();
        }
    }

    @Override
    public void onPageSelected(int position) {
        View view = getChildAt(position);
        mSecondCircleX = mFirstCircleX = view.getX() + view.getWidth() / 2;
        mSecondCircleY = mFirstCircleY = view.getY() + view.getHeight() / 2;
        invalidate();
    }
}
