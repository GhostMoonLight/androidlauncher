package com.android.launcher3.view;

import com.cuan.launcher.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
/**
 * 放置三个view的水平布局，自动平分间隔,view的大小一样大
 */
public class ThreeView extends ViewGroup {
	
	private int mCellWidth;
	private int mCellHeight;
	private int cellX = 3;
	
	public ThreeView(Context context) {
		this(context, null);
	}
	
	public ThreeView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ThreeView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		mCellWidth = getResources().getDimensionPixelOffset(R.dimen.folder_cell_width);
		mCellHeight = getResources().getDimensionPixelOffset(R.dimen.folder_cell_height);
	}
	
	public void setCellX(int cellX){
		this.cellX = cellX;
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			View child = getChildAt(i);
			int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(mCellWidth, MeasureSpec.EXACTLY);
			int childheightMeasureSpec = MeasureSpec.makeMeasureSpec(mCellHeight, MeasureSpec.EXACTLY);
			child.measure(childWidthMeasureSpec, childheightMeasureSpec);
		}
		int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSpecSize = mCellHeight+10;
		setMeasuredDimension(widthSpecSize, heightSpecSize);
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int count = getChildCount();
		int sapce = (getWidth()-getPaddingLeft()-getPaddingRight()-mCellWidth*cellX)/(cellX+1);
		//距左边边缘间隔近一点，取space的一半，中间间隔大一点取space的1.5倍
		int leftDis = (int) (sapce*0.5);
		int midleDis = sapce + (2*sapce - 2*leftDis)/(cellX-1);
		int left = 0;
		int right = 0;
		for (int i = 0; i < count; i++) {
			View child = getChildAt(i);
			left = getPaddingLeft()+i*(mCellWidth+midleDis) + leftDis;
			right = left+mCellWidth;
			child.layout(left, getPaddingTop(), right, getPaddingTop()+mCellHeight);
		}
	}

}
