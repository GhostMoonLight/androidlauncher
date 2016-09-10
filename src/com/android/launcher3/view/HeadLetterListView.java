package com.android.launcher3.view;

import com.cuan.launcher.R;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
/**
 * 显示一个头部的ListView
 */
public class HeadLetterListView extends ListView implements OnScrollListener{
	
	private View mHeaderView; //覆盖在顶部的view
	private int mHeaderWidth;
	private int mHeaderHeight;
	private boolean isTopVisible = false; //是否显示
	
	private OnScrollListener mScrollListener;
	private OnHeaderUpdateListener mHeaderUpdateListener;

	public HeadLetterListView(Context context) {
		this(context, null);
	}

	public HeadLetterListView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public HeadLetterListView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initView();
	}
	
	private void initView() {
        setFadingEdgeLength(0);
        setOnScrollListener(this);
    }
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		if (mHeaderView == null) {
            return;
        }
		measureChild(mHeaderView, widthMeasureSpec, heightMeasureSpec);
        mHeaderWidth = mHeaderView.getMeasuredWidth();
        mHeaderHeight = mHeaderView.getMeasuredHeight();
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (mHeaderView == null) {
            return;
        }
        int left = getPaddingLeft();
        mHeaderView.layout(left, 0, left+mHeaderWidth, mHeaderHeight);
	}
	
	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		if (mHeaderView != null && isTopVisible) {
            drawChild(canvas, mHeaderView, getDrawingTime());
        }
	}
	
	private void refreshHeader() {
		if (mHeaderView == null) {
            return;
        }
		int firstVisiblePos = getFirstVisiblePosition();  //返回屏幕中第一个显示的条目位置（在集合中的位置）
		
		 final View view = getChildAt(0);
		 int top1;
		 if (firstVisiblePos == 0 && view.getTop() >= 0){
			 isTopVisible = false;
		 }else{
			 isTopVisible = true;
			 final View view1 = getChildAt(1);
			 top1 = view1.getTop();
			 if ((Boolean)(view1.getTag(R.id.tag_is_first)) && top1 < mHeaderHeight && top1 >= 0){
				 int delta = mHeaderHeight - top1;
	             mHeaderView.layout(getPaddingLeft(), -delta, getPaddingLeft()+mHeaderWidth, mHeaderHeight - delta);
			 } else{
				 mHeaderView.layout(getPaddingLeft(), 0, getPaddingLeft()+mHeaderWidth, mHeaderHeight);
			 }
		 }
		
		if (mHeaderUpdateListener != null) {
            mHeaderUpdateListener.updatePinnedHeader(mHeaderView, firstVisiblePos);
        }
	}
	
	public void setOnHeaderUpdateListener(OnHeaderUpdateListener listener) {
        mHeaderUpdateListener = listener;
        if (listener == null) {
            mHeaderView = null;
            mHeaderWidth = mHeaderHeight = 0;
            return;
        }
        mHeaderView = listener.getPinnedHeader();
        int firstVisiblePos = getFirstVisiblePosition();
        listener.updatePinnedHeader(mHeaderView, firstVisiblePos);
        requestLayout();
        postInvalidate();
    }
	
	@Override
    public void setOnScrollListener(OnScrollListener l) {
        if (l != this) {
            mScrollListener = l;
        } else {
            mScrollListener = null;
        }
        super.setOnScrollListener(this);
    }


	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		if (totalItemCount > 0) {
            refreshHeader();
        }
        if (mScrollListener != null) {
            mScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (mScrollListener != null) {
            mScrollListener.onScrollStateChanged(view, scrollState);
        }
	}
	
	public interface OnHeaderUpdateListener {
		/**
	     * 返回一个view对象即可
	     * 注意：view必须要有LayoutParams
	     */
	    public View getPinnedHeader();

	    public void updatePinnedHeader(View headerView, int firstVisiblePosition);
	}
}
