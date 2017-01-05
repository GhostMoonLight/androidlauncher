package com.android.launcher3.view;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.android.launcher3.FolderInfo;
import com.android.launcher3.ItemInfo;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.LauncherModel;
import com.android.launcher3.LauncherSettings;
import com.android.launcher3.ShortcutInfo;
import com.android.launcher3.db.DBContent.RecentUserAppInfo;
import com.android.launcher3.utils.Util;
import com.cuan.launcher.R;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by cgx on 16/7/28.
 * 最近使用app的界面  最多放置8个View，上下两行
 */
public class RecentUseView extends FrameLayout{

    private int viewHeight;
    private int realHeight;
    private Context mContext;

    private boolean isNotUseRXjava = false;

    private Future mAnimationFuture = null;
    private static ExecutorService sThreadPool = Executors.newFixedThreadPool(1);
    private RecentUserAppObserver observer;

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
        mContext = getContext();
        observer = new RecentUserAppObserver(new Handler());
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

    //刷新界面
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

    //遍历sBgWorkspaceItems给最近使用的app设置icon
    public void setRecentUserAppInfoIcon(ArrayList<RecentUserAppInfo> infos) {
        for (ItemInfo item: LauncherModel.sBgWorkspaceItems){
            if (item instanceof ShortcutInfo){
                ShortcutInfo sInfo = (ShortcutInfo)item;
                if (item.itemType != LauncherSettings.BaseLauncherColumns.ITEM_TYPE_SHORTCUT
                        || (mContext.getPackageName().equals(sInfo.packName))){
                    for (RecentUserAppInfo info: infos){
                        if (info.title.equals(sInfo.title) && info.pck.equals(sInfo.packName)){
                            if (sInfo.iconBg != null){
                                //只有时钟和日历的ShortcutInfo对象该字段有值
                                info.icon = sInfo.iconBg;
                            }else{
                                info.icon = sInfo.themeDrawable;
                            }
                        }
                    }
                }
            } else if (item instanceof FolderInfo){
                FolderInfo fi = (FolderInfo)item;
                for (ShortcutInfo sInfo: fi.contents){
                    if (sInfo.itemType != LauncherSettings.BaseLauncherColumns.ITEM_TYPE_SHORTCUT
                            || (mContext.getPackageName().equals(sInfo.packName))) {
                        for (RecentUserAppInfo info : infos) {
                            if (info.title.equals(sInfo.title) && info.pck.equals(sInfo.packName)) {
                                if (sInfo.iconBg != null) {
                                    //只有时钟和日历的ShortcutInfo对象该字段有值
                                    info.icon = sInfo.iconBg;
                                } else {
                                    info.icon = sInfo.themeDrawable;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    //加载最近打开的app
    public void loadRecentUseData(){
        if (isNotUseRXjava) {
            if (mAnimationFuture != null && !mAnimationFuture.isCancelled()) {
                mAnimationFuture.cancel(true);
            }
            final ArrayList<RecentUserAppInfo> infos = RecentUserAppInfo.queryData(mContext);
            mAnimationFuture = sThreadPool.submit(new Runnable() {
                @Override
                public void run() {
                    setRecentUserAppInfoIcon(infos);

                    post(new Runnable() {
                        @Override
                        public void run() {
                            refreshData(infos);
                        }
                    });
                }
            });
        }else {
            Observable.create(new ObservableOnSubscribe<ArrayList<RecentUserAppInfo>>() {
                @Override
                public void subscribe(ObservableEmitter<ArrayList<RecentUserAppInfo>> e) throws Exception {
                    ArrayList<RecentUserAppInfo> infos = RecentUserAppInfo.queryData(mContext);
                    setRecentUserAppInfoIcon(infos);
                    e.onNext(infos);
                    e.onComplete();
                }
            })
            //在处理下一个事件之前要做的事  可以输出Log什么的
            .doOnNext(new Consumer<ArrayList<RecentUserAppInfo>>() {
                @Override
                public void accept(ArrayList<RecentUserAppInfo> recentUserAppInfos) throws Exception {
                }
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<ArrayList<RecentUserAppInfo>>() {
                        @Override
                        public void accept(ArrayList<RecentUserAppInfo> recentUserAppInfos) throws Exception {
                            refreshData(recentUserAppInfos);
                        }
                    });
        }
    }

    //最近使用app表变化的监听
    private class RecentUserAppObserver extends ContentObserver {
        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public RecentUserAppObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            loadRecentUseData();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getContext().getContentResolver().registerContentObserver(RecentUserAppInfo.CONTENT_URI, true, observer);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getContext().getContentResolver().unregisterContentObserver(observer);
    }
}
