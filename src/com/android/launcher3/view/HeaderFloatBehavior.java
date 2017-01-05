package com.android.launcher3.view;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.content.res.Resources;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.CoordinatorLayout.Behavior;
import android.util.AttributeSet;
import android.view.View;

import com.cuan.launcher.R;

/**
 * Created by cgx on 2016/12/28.
 *
 */
public class HeaderFloatBehavior extends Behavior<View> {
    private ArgbEvaluator argbEvaluator;
    private Context mContext;

    public HeaderFloatBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        argbEvaluator = new ArgbEvaluator();
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        if (dependency != null && dependency.getId() == R.id.scrolling_header) {
            return true;
        }
        return false;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        Resources resources = mContext.getResources();
        final float progress = 1.f -
                Math.abs(dependency.getTranslationY() / (dependency.getHeight() - resources.getDimension(R.dimen.collapsed_header_height)));

        // Translation
        final float collapsedOffset = resources.getDimension(R.dimen.collapsed_float_offset_y);
        final float initOffset = resources.getDimension(R.dimen.init_float_offset_y);
        final float translateY = collapsedOffset + (initOffset - collapsedOffset) * progress;
        child.setTranslationY(translateY);

        // Background
        child.setBackgroundColor((int) argbEvaluator.evaluate(
                progress,
                resources.getColor(R.color.colorCollapsedFloatBackground),
                resources.getColor(R.color.colorInitFloatBackground)));

        // Margins
        final float collapsedMargin = resources.getDimension(R.dimen.collapsed_float_margin);
        final float initMargin = resources.getDimension(R.dimen.init_float_margin);
        final int margin = (int) (collapsedMargin + (initMargin - collapsedMargin) * progress);
        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
        lp.setMargins(margin, 0, margin, 0);
        child.setLayoutParams(lp);

        return true;
    }
}
