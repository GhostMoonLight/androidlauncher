package com.android.launcher3.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.database.ContentObserver;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.launcher3.FolderInfo;
import com.android.launcher3.IconCache;
import com.android.launcher3.Insettable;
import com.android.launcher3.ItemInfo;
import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherModel;
import com.android.launcher3.LauncherSettings;
import com.android.launcher3.ShortcutInfo;
import com.android.launcher3.db.DBContent.RecentUserAppInfo;
import com.cuan.launcher.R;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import rx.Observable;
import rx.functions.Action1;

/**
 * Created by cgx on 16/7/21.
 * 左屏的内容控件
 */
public class CustomContentPage extends LinearLayout implements Insettable{

    private RecentUserAppObserver observer;
    private TextView mTextView, mExpandView;
    private RecentUseView mRecentView;
    private ObjectAnimator oa;  //应用推荐界面展开，折叠的动画
    private Launcher mLauncher;
    private IconCache mIconCache;
    
    private Future mAnimationFuture = null;
    private static ExecutorService sThreadPool = Executors.newFixedThreadPool(1);

    public CustomContentPage(Context context){
        this(context, null);
    }

    public CustomContentPage(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomContentPage(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mLauncher = (Launcher)context;
        observer = new RecentUserAppObserver(new Handler());

    }

    @Override
    public void setInsets(Rect insets) {
        setPadding(insets.left+getPaddingLeft(), insets.top, insets.right+getPaddingRight(), insets.bottom);
        requestLayout();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mTextView = (TextView) findViewById(R.id.recent_use);

        Observable.just("最近使用").subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                mTextView.setText(s);
            }
        });

        mExpandView = (TextView) findViewById(R.id.expand);
        mRecentView = (RecentUseView) findViewById(R.id.recentview);
        setExpandViewClickListener();
    }

    /**
     * 设置折叠展开按钮的点击事件
     */
	private void setExpandViewClickListener() {
		mExpandView.setTag(true);  //true表示已经展开了
        mExpandView.setOnClickListener(new OnClickListener(){
			@Override
            public void onClick(View v) {
                boolean isExpand = (boolean) v.getTag();
                if (isExpand){
                	v.setTag(false);
                	((TextView)v).setText(R.string.expand);
                	if(oa != null && oa.isRunning())
                		oa.reverse();  //流畅的取消动画
                	oa = ObjectAnimator.ofInt(mRecentView, "viewHeight", mRecentView.getOriginalHeight()/2);
                } else {
                	v.setTag(true);
                	((TextView)v).setText(R.string.folded);
                	if(oa != null && oa.isRunning())
                		oa.reverse(); 
                	oa = ObjectAnimator.ofInt(mRecentView, "viewHeight", mRecentView.getOriginalHeight());
                }
                oa.setInterpolator(new DecelerateInterpolator());
                oa.start();
            }
        });
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

    //最近使用app表变化的监听
    private class RecentUserAppObserver extends ContentObserver{
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
            loadRecentUseApp();
        }
    }
    
    public void loadRecentUseApp() {
    	 loadRecentUseData(RecentUserAppInfo.queryData(mLauncher));
	}
    
    private void loadRecentUseData(final ArrayList<RecentUserAppInfo> infos){
    	if (mAnimationFuture != null && !mAnimationFuture.isCancelled()) {
    		mAnimationFuture.cancel(true );
    	}
    	mAnimationFuture = sThreadPool.submit(new Runnable(){
	   		@Override
	   		public void run() {
	   			for (ItemInfo item: LauncherModel.sBgWorkspaceItems){
	   				if (item instanceof ShortcutInfo){
	   					ShortcutInfo sInfo = (ShortcutInfo)item;
	   					if (item.itemType != LauncherSettings.BaseLauncherColumns.ITEM_TYPE_SHORTCUT){
	   						for (RecentUserAppInfo info: infos){
	   							if (info.title.equals(sInfo.title) && info.pck.equals(sInfo.packName)){
	   								info.icon = sInfo.themeDrawable;
	   							}
	   			   			}
	   					} 
	   				} else if (item instanceof FolderInfo){
	   					FolderInfo fi = (FolderInfo)item;
	   					for (ShortcutInfo sInfo: fi.contents){
	   						for (RecentUserAppInfo info: infos){
	   							if (info.title.equals(sInfo.title) && info.pck.equals(sInfo.packName)){
	   								info.icon = sInfo.themeDrawable;
	   							}
	   			   			}
	   					}
	   				}
	   			}
	   			
	   			CustomContentPage.this.post(new Runnable() {
					@Override
					public void run() {
						mRecentView.refreshData(infos);
					}
				});
	   		}
	    });
   }

	public void setIconCache(IconCache mIconCache) {
		this.mIconCache = mIconCache;
	}
}

