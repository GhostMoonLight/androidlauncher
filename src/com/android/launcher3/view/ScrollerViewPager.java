package com.android.launcher3.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.DecelerateInterpolator;
import java.lang.reflect.Field;

/**
 * Created by cgx on 16/11/1.
 * 自定义ViewPager，改变ViewPager的滑动速度
 */
public class ScrollerViewPager  extends ViewPager {

    private static final String TAG = ScrollerViewPager.class.getSimpleName();

    private int duration = 1000;

    public ScrollerViewPager(Context context) {
        super(context);
        fixScrollSpeed();
    }

    public ScrollerViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        fixScrollSpeed();
    }


    public void fixScrollSpeed(){
        fixScrollSpeed(duration);
    }

    public void fixScrollSpeed(int duration){
        this.duration = duration;
        setScrollSpeedUsingRefection(duration);
    }


    private void setScrollSpeedUsingRefection(int duration) {
        try {
            Field localField = ViewPager.class.getDeclaredField("mScroller");
            localField.setAccessible(true);
            FixedSpeedScroller scroller = new FixedSpeedScroller(getContext(), new DecelerateInterpolator(1.5F));
            scroller.setDuration(duration);
            localField.set(this, scroller);
            return;
        } catch (Exception e) {
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
