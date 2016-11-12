package com.android.launcher3.swipe;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.launcher3.view.MaterialProgressDrawable;
import com.cuan.launcher.R;

/**
 * Created by cgx on 16/11/10.
 * RecyclerView和SwipeRefreshLayout组合的下拉刷新和上拉加载控件
 */
public class SwipeRecyclerView extends FrameLayout {

    private static final float OFFSET_RATIO = 0.3f;

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private TextView tvLoadingText;
    //x上次保存的
    private int mLastMotionX;
    //y上次保存的
    private int mLastMotionY;
    //滑动状态
    private int mPullState;
    private int PULL_UP_STATE = 2;  //手指上滑
    private int PULL_FINISH_STATE = 0;
    //当前滑动的距离
    private int curTransY;
    private int footerHeight;
    private View footerView;
    //是否上拉加载更多
    private boolean isLoadNext = false;
    //是否在加载中
    private boolean isLoading = false;
    private boolean isAnimatoring = false; //动画是否正在执行，footerView消失的动画是否正在执行
    private ObjectAnimator objectAnimator;

    private OnSwipeRecyclerViewListener onSwipeRecyclerViewListener;

    private boolean isCancelLoadNext = false;   //是否取消上拉加载
    private MaterialProgressDrawable mProgress;
    private ImageView mProgressView;

    public SwipeRecyclerView(Context context) {
        this(context, null);
    }

    public SwipeRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public RecyclerView getRecyclerView(){
        return recyclerView;
    }

    public SwipeRefreshLayout getSwipeRefreshLayout(){
        return swipeRefreshLayout;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swiperefreshlayout);
        recyclerView = (RecyclerView)findViewById(R.id.recyclerview);
        footerView = LayoutInflater.from(getContext()).inflate(R.layout.swiperecyclerview_footerview,null);
        tvLoadingText = (TextView)footerView.findViewById(R.id.loading_text);
        mProgressView = (ImageView) footerView.findViewById(R.id.progressview);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!isLoading && !isLoadNext) {
                    isLoading = true;
                    swipeRefreshLayout.setRefreshing(true);
                    if (onSwipeRecyclerViewListener != null) {
                        onSwipeRecyclerViewListener.onRefresh();
                    }else{
                        onLoadFinish();
                    }
                }else{
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });

        FrameLayout.LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.BOTTOM;
        addView(footerView, params);

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int height = getHeight();
                if (height != 0) {
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    ViewGroup.LayoutParams footerParams = tvLoadingText.getLayoutParams();
                    footerHeight = footerParams.height;
                    curTransY = footerHeight;
                    footerView.setTranslationY(curTransY);
                    mProgress.start();
                }
            }
        });
        addLoadingProgress();
    }

    //添加加载动画
    private void addLoadingProgress() {
        mProgress = new MaterialProgressDrawable(getContext(), this);
        mProgress.updateSizes(MaterialProgressDrawable.DEFAULT);
        mProgress.setBackgroundColor(0xFFFAFAFA);
        mProgress.setAlpha(255);
        mProgress.setStartEndTrim(0, 0.8f);
        mProgress.setColorSchemeColors(Color.parseColor("#006633"));
        mProgressView.setImageDrawable(mProgress);
    }

    public void setItemAnimator(RecyclerView.ItemAnimator animator){
        recyclerView.setItemAnimator(animator);
    }

    public void setSwipeRefreshColor(int color){
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(color));
    }

    //是否支持上拉加载
    public void isCancelLoadNext(Boolean isCancelLoadNext){
        this.isCancelLoadNext = isCancelLoadNext;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int x = (int)ev.getX();
        int y = (int)ev.getY();
        int deltaY = 0;
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                mLastMotionX = x;
                mLastMotionY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                int deltaX = x - mLastMotionX;
                deltaY = y - mLastMotionY;
                if(Math.abs(deltaX) < Math.abs(deltaY) && Math.abs(deltaY) > 10){
                    if(isRefreshViewScroll(deltaY)){
                        return true;
                    }
                }
                break;
        }
        return super.onInterceptTouchEvent(ev) || !isCanRefresh(deltaY);
    }

    //当前是否可以刷新
    private boolean isCanRefresh(int deltaY){
        return !(deltaY>0 && !recyclerView.canScrollVertically(-1) && isLoadNext);
    }

    /**
     * RecyclerView.canScrollVertically(1)的值表示是否能向下滚动，false表示已经滚动到底部
     * RecyclerView.canScrollVertically(-1)的值表示是否能向上滚动，false表示已经滚动到顶部
     * @param deltaY
     * @return
     */
    private boolean isRefreshViewScroll(int deltaY) {
        if(deltaY < 0 && !recyclerView.canScrollVertically(1) && !isLoading && !isCancelLoadNext){
            mPullState = PULL_UP_STATE;
            isLoading = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int y = (int)event.getY();
        switch (event.getAction()){
            case MotionEvent.ACTION_MOVE:
                float deltaY = y - mLastMotionY;
                if(mPullState == PULL_UP_STATE){
                    curTransY += deltaY;
                    if (curTransY < 0) {
                        curTransY = 0;
                    }
                    if (objectAnimator != null){
                        objectAnimator.cancel();
                    }
                    footerView.setTranslationY(curTransY);
                    if(Math.abs(curTransY) == 0){
                        isLoadNext = true;
                    }else {
                        isLoadNext = false;
                    }
                }
                mLastMotionY = y;
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                //此时是否滑到了底部
                if (!recyclerView.canScrollVertically(1)) {
                    if (isLoadNext) {
                        changeFooterState(isLoadNext);
                        mPullState = PULL_FINISH_STATE;
                        if (onSwipeRecyclerViewListener != null) {
                            onSwipeRecyclerViewListener.onLoadNext();
                        } else {
                            hideTranslationY();
                            isLoading = false;
                            isLoadNext = false;
                        }
                    } else {
                        hideTranslationY();
                        isLoading = false;
                    }
                    return true;
                }else{
                    hideTranslationY();
                    isLoading = false;
                }
        }

        return super.onTouchEvent(event);
    }

    public void onLoadFinish(){
        swipeRefreshLayout.setRefreshing(false);
        isLoading = false;
        isLoadNext = false;
        if(curTransY == footerHeight){
            return;
        }
        hideTranslationY();
    }

    private void hideTranslationY() {
        objectAnimator = ObjectAnimator.ofFloat(footerView, "translationY",curTransY, footerHeight).setDuration(800);
        objectAnimator.setInterpolator(new DecelerateInterpolator());
        objectAnimator.start();

        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                curTransY = footerHeight;
                changeFooterState(false);
            }
        });
    }

    private void changeFooterState(boolean loading){
        if(loading){
            tvLoadingText.setText("正在努力的加载中...");
        }else{
            tvLoadingText.setText("松手加载更多");
        }
    }

    public void setOnSwipeRecyclerViewListener(OnSwipeRecyclerViewListener onSwipeRecyclerViewListener){
        this.onSwipeRecyclerViewListener = onSwipeRecyclerViewListener;
    }

    //第一次显示时，显示加载进度条，获取数据
    public void loadData(){
        if (onSwipeRecyclerViewListener != null) {
            isLoading = true;
            swipeRefreshLayout.setRefreshing(true);
            onSwipeRecyclerViewListener.onRefresh();
        }
    }

    public interface OnSwipeRecyclerViewListener{

        public void onRefresh();

        public void onLoadNext();

    }
}