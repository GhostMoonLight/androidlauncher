package com.android.launcher3.view;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Scroller;

import com.cuan.launcher.R;

import java.lang.ref.WeakReference;

/**
 * Created by cgx on 2016/12/28.
 *
 * Behavior只有是CoordinatorLayout的直接子View才有意义
 */
public class HeaderScrollingBehavior extends CoordinatorLayout.Behavior<NestedScrollView> {
    private boolean isExpanded = false;
    private boolean isScrolling = false;
    private Context mContext;

    private WeakReference<View> dependentView;
    private Scroller scroller;
    private Handler handler;

    /**
     * 一定要重写该构造函数，CoordinatorLayout中parseBehavior() 直接反射调用这个构造函数
     * @param context
     * @param attrs
     */
    public HeaderScrollingBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        scroller = new Scroller(context);
        handler = new Handler();
    }

    //判断Header当前是否展开
    public boolean isExpanded() {
        return isExpanded;
    }

    /**
     * child是否要依赖dependency
     * @param parent     CoordinatorLayout
     * @param child      该Behavior对应的那个View
     * @param dependency 要检查的View(child是否要依赖这个dependency)
     * @return true 依赖, false 不依赖
     */
    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, NestedScrollView child, View dependency) {
        if (dependency != null && dependency.getId() == R.id.scrolling_header) {
            dependentView = new WeakReference<>(dependency);
            return true;
        }
        return false;
    }

    //对child进行布局,只有CoordinatorLayout被创建的时候才会被多次调用
    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, NestedScrollView child, int layoutDirection) {
        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
        if (lp.height == CoordinatorLayout.LayoutParams.MATCH_PARENT) {
            child.layout(0, 0, parent.getWidth(), (int) (parent.getHeight() - getDependentViewCollapsedHeight()));
            return true;
        }
        return super.onLayoutChild(parent, child, layoutDirection);
    }

    /**
     * 在layoutDependsOn返回true的基础上之后，及时报告dependency的状态变化
     * @param parent     CoordinatorLayout
     * @param child      该Behavior对应的那个View
     * @param dependency child依赖dependency
     * @return true 处理了, false  没处理
     */
    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, NestedScrollView child, View dependency) {
        Resources resources = getDependentView().getResources();
        final float progress = 1.f -
                Math.abs(dependency.getTranslationY() / (dependency.getHeight() - resources.getDimension(R.dimen.collapsed_header_height)));

        child.setTranslationY(dependency.getHeight() + dependency.getTranslationY());

        float scale = 1 + 0.4f * (1.f - progress);
        dependency.setScaleX(scale);
        dependency.setScaleY(scale);

        dependency.setAlpha(progress);

        return true;
    }

    /**
     * 在layoutDependsOn返回true的基础上之后，及时报告dependency的状态变化
     * @param parent     CoordinatorLayout
     * @param child      该Behavior对应的那个View
     * @param dependency child依赖dependency
     * @return true 处理了, false  没处理
     */
    @Override
    public void onDependentViewRemoved(CoordinatorLayout parent, NestedScrollView child, View dependency) {
        super.onDependentViewRemoved(parent, child, dependency);
    }

    //以下方法，只有该Behavior对应的View实现了NestedScrollingChild接口的类，才会被回调执行
    //系统的SwipeRefreshLayout，NestedScrollView，RecyclerView实现了NestedScrollingChild接口
    //onStartNestedScroll
    //onNestedScrollAccepted
    //onNestedPreScroll
    //onNestedScroll
    //onNestedPreFling
    //onNestedFling
    //onStopNestedScroll
    //回调过程：实现了NestedScrollingChild接口的类调用自己的相应方法去回调CoordinatorLayout中的相应方法，
    //        再去遍历执行CoordinatorLayout中所有孩子的Behavior中相对应的方法

    /**
     * 有嵌套滑动到来了，问下该Behavior是否接受嵌套滑动
     *
     * @param coordinatorLayout CoordinatorLayout
     * @param child             该Behavior对应的View
     * @param directTargetChild 嵌套滑动对应的父类的子类(因为嵌套滑动对于的父View不一定是一级就能找到的，可能挑了两级父View的父View， directTargetChild>=target)
     * @param target            具体嵌套滑动的那个子类
     * @param nestedScrollAxes  支持嵌套滚动轴。水平方向，垂直方向，或者不指定
     * @return 是否接受该嵌套滑动
     */
    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, NestedScrollView child, View directTargetChild, View target, int nestedScrollAxes) {
        return (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    /**
     * Behavior接受了嵌套滑动的请求该函数调用。onStartNestedScroll返回true该函数会被调用。 参数和onStartNestedScroll一样
     */
    @Override
    public void onNestedScrollAccepted(CoordinatorLayout coordinatorLayout, NestedScrollView child, View directTargetChild, View target, int nestedScrollAxes) {
        scroller.abortAnimation();
        isScrolling = false;

        super.onNestedScrollAccepted(coordinatorLayout, child, directTargetChild, target, nestedScrollAxes);
    }

    /**
     * 在嵌套滑动的子View未滑动之前告诉过来的准备滑动的情况
     *
     * @param coordinatorLayout CoordinatorLayout
     * @param child             该Behavior对应的View
     * @param target            具体嵌套滑动的那个子类
     * @param dx                水平方向嵌套滑动的子View想要变化的距离
     * @param dy                垂直方向嵌套滑动的子View想要变化的距离
     * @param consumed          这个参数要我们在实现这个函数的时候指定，回头告诉子View当前父View消耗的距离 consumed[0] 水平消耗的距离，consumed[1] 垂直消耗的距离 好让子view做出相应的调整
     */
    @Override
    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, NestedScrollView child, View target, int dx, int dy, int[] consumed) {
//        //防止滑到顶部后再向上滑，抖动的情况
//        if (dy < 0 && dy >= -3) {
//            dy=0;
//        }
//
//        View dependentView = getDependentView();
//        float newTranslateY = dependentView.getTranslationY() - dy;
//        float minHeaderTranslate = -(dependentView.getHeight() - getDependentViewCollapsedHeight());
//        final float maxHeaderTranslate = 0;
//
//        if (newTranslateY < minHeaderTranslate){
//            newTranslateY = minHeaderTranslate;
//        } else if (newTranslateY > maxHeaderTranslate){
//            newTranslateY = maxHeaderTranslate;
//        } else {
//            consumed[1] = dy; //告诉子View 父View把当前滑动的dy消耗了
//        }
//        dependentView.setTranslationY(newTranslateY);

        if (dy < 0) {
            return;
        }
        View dependentView = getDependentView();
        float newTranslateY = dependentView.getTranslationY() - dy;
        float minHeaderTranslate = -(dependentView.getHeight() - getDependentViewCollapsedHeight());
        if (newTranslateY > minHeaderTranslate) {
            dependentView.setTranslationY(newTranslateY);
            consumed[1] = dy;
        }
    }

    /**
     * 嵌套滑动的子View在滑动之后报告过来的滑动情况
     *
     * @param coordinatorLayout CoordinatorLayout
     * @param child             该Behavior对应的View
     * @param target            具体嵌套滑动的那个子类
     * @param dxConsumed        水平方向嵌套滑动的子View滑动的距离(消耗的距离)
     * @param dyConsumed        垂直方向嵌套滑动的子View滑动的距离(消耗的距离)
     * @param dxUnconsumed      水平方向嵌套滑动的子View未滑动的距离(未消耗的距离)
     * @param dyUnconsumed      垂直方向嵌套滑动的子View未滑动的距离(未消耗的距离)
     *
     *     Unconsumed:child有没有把滑动的距离消耗掉。0说明消耗掉了；其他值说明child滑到头了，滑动的距离没消耗掉
     */
    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, NestedScrollView child, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        if (dyUnconsumed > 0) {
            return;
        }
        View dependentView = getDependentView();
        float newTranslateY = dependentView.getTranslationY() - dyUnconsumed;
        final float maxHeaderTranslate = 0;
        if (newTranslateY < maxHeaderTranslate) {
            dependentView.setTranslationY(newTranslateY);
        }
    }

    /**
     * 在嵌套滑动的子View未fling之前告诉过来的准备fling的情况
     *
     * @param coordinatorLayout CoordinatorLayout
     * @param child             该Behavior对应的View
     * @param target            具体嵌套滑动的那个子类
     * @param velocityX         水平方向速度
     * @param velocityY         垂直方向速度
     * @return true Behavior是否消耗了fling
     */
    @Override
    public boolean onNestedPreFling(CoordinatorLayout coordinatorLayout, NestedScrollView child,
                                    View target, float velocityX, float velocityY) {
        return super.onNestedPreFling(coordinatorLayout, child, target, velocityX, velocityY);
    }

    /**
     * 嵌套滑动的子View在fling之后报告过来的fling情况
     *
     * @param coordinatorLayout CoordinatorLayout
     * @param child             该Behavior对应的View
     * @param target            具体嵌套滑动的那个子类
     * @param velocityX         水平方向速度
     * @param velocityY         垂直方向速度
     * @param consumed          子view是否fling了
     * @return true Behavior是否消耗了fling
     */
    @Override
    public boolean onNestedFling(CoordinatorLayout coordinatorLayout, NestedScrollView child, View target, float velocityX, float velocityY, boolean consumed) {
        return onUserStopDragging(velocityY);
    }

    /**
     * 停止嵌套滑动
     *
     * @param coordinatorLayout CoordinatorLayout
     * @param child             该Behavior对应的View
     * @param target            具体嵌套滑动的那个子类
     */
    @Override
    public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, NestedScrollView child, View target) {
        if (!isScrolling) {
            onUserStopDragging(100);
        }
    }

    private boolean onUserStopDragging(float velocity) {
        View dependentView = getDependentView();
        float translateY = dependentView.getTranslationY();
        float minHeaderTranslate = -(dependentView.getHeight() - getDependentViewCollapsedHeight());

        if (translateY == 0 || translateY == minHeaderTranslate) {
            return false;
        }

        boolean targetState; // Flag indicates whether to expand the content.
        if (Math.abs(velocity) <= 1000) {
            targetState = Math.abs(translateY - minHeaderTranslate) < Math.abs(minHeaderTranslate/2);
            velocity = 1300; // Limit velocity's minimum value.
        } else {
            targetState = (velocity > 0);
        }

        float targetTranslateY = targetState ? minHeaderTranslate : 0;

        scroller.startScroll(0, (int) translateY, 0, (int) (targetTranslateY - translateY), (int) (800000 / Math.abs(velocity)));
        handler.post(flingRunnable);
        isScrolling = true;
        return true;
    }

    //获取Header折叠的高度
    private float getDependentViewCollapsedHeight() {
        return mContext.getResources().getDimension(R.dimen.collapsed_header_height);
    }

    private View getDependentView() {
        return dependentView.get();
    }

    private Runnable flingRunnable = new Runnable() {
        @Override
        public void run() {
            if (scroller.computeScrollOffset()) {
                getDependentView().setTranslationY(scroller.getCurrY());
                handler.post(this);
            } else {
                isExpanded = getDependentView().getTranslationY() != 0;
                isScrolling = false;
            }
        }
    };
}
