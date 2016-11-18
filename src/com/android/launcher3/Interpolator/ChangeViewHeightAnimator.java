package com.android.launcher3.Interpolator;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by cgx on 16/11/19.
 * 改变高度的Animator
 */

public class ChangeViewHeightAnimator extends ValueAnimator implements ValueAnimator.AnimatorUpdateListener {

    private View mTarget;

    public ChangeViewHeightAnimator(View target, float start, float end){
        this.mTarget = target;
        setIntValues((int)start, (int)end);
        addUpdateListener(this);
    }

    @Override
    public long getStartDelay() {
        return 0;
    }

    @Override
    public void setStartDelay(long startDelay) {

    }

    @Override
    public ValueAnimator setDuration(long duration) {
        return null;
    }

    @Override
    public long getDuration() {
        return 0;
    }

    @Override
    public void setInterpolator(TimeInterpolator value) {

    }

    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        ViewGroup.LayoutParams lp = mTarget.getLayoutParams();
        lp.height = (int)animation.getAnimatedValue();
        mTarget.setLayoutParams(lp);
    }
}
