package com.android.launcher3.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.android.launcher3.LauncherAppState;
import com.android.launcher3.db.DBContent.RecentUserAppInfo;
import com.android.launcher3.utils.Util;
import com.cuan.launcher.R;

import java.util.ArrayList;

/**
 * Created by cgx on 16/7/28.
 * 最近使用app的界面  最多放置8个View，上下两行
 */
public class RecentUseView extends FrameLayout{

    private int viewHeight;
    private int realHeight;
    

    public RecentUseView(Context context) {
        super(context);
        init();
    }

    public RecentUseView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RecentUseView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    //初始化
    private void init() {
    	realHeight = viewHeight = 2*getResources().getDimensionPixelOffset(R.dimen.folder_cell_height)+ Util.dip2px(7);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int height = viewHeight;
        if (heightMode == MeasureSpec.EXACTLY){	
            height = MeasureSpec.getSize(heightMeasureSpec);
        }
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSpecSize/4, MeasureSpec.EXACTLY);
            int childheightMeasureSpec = MeasureSpec.makeMeasureSpec(viewHeight/2, MeasureSpec.EXACTLY);
            child.measure(childWidthMeasureSpec, childheightMeasureSpec);
        }
        setMeasuredDimension(widthSpecSize, height);
    }
    
    public void setViewHeight(int height){
    	ViewGroup.LayoutParams params = getLayoutParams();
    	realHeight = params.height = height;
    	setLayoutParams(params);
    }
    
    public int getViewHeight(){
    	return realHeight;
    }
    
    public int getOriginalHeight(){
    	return viewHeight;
    }
    
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = getChildCount();
        int left = 0;
        int right = 0;
        int top = 0;
        int bottom = viewHeight/2;
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            right += child.getMeasuredWidth();
            child.layout(left, top, right, bottom);
            left = right;
            if (i ==  3){
                top = bottom;
                bottom *= 2;
                left = 0;
                right = 0;
            }
        }
    }
    
    public void refreshData(ArrayList<RecentUserAppInfo> infos){
    	removeAllViews();
    	for (RecentUserAppInfo info: infos){
    		getView(info);
    	}
    }

    private void getView(RecentUserAppInfo info){
        RecentUseAppView view = (RecentUseAppView) View.inflate(getContext(), R.layout.layout_recent_app, null);
        view.setData(info);
        view.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				LauncherAppState.getInstance().getLauncher().startAppShortcutOrInfoActivity(v);
			}
		});
        addView(view);
    }
}
