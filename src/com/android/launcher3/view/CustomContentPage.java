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

import com.android.launcher3.IconCache;
import com.android.launcher3.Insettable;
import com.android.launcher3.Launcher;
import com.android.launcher3.db.DBContent.RecentUserAppInfo;
import com.cuan.launcher.R;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

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
//        mTextView.setText("最近使用");

        mExpandView = (TextView) findViewById(R.id.expand);
        mRecentView = (RecentUseView) findViewById(R.id.recentview);
        setExpandViewClickListener();

        useRxJava();
    }

    //RxJava使用样例
    private void useRxJava(){
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                e.onNext("最近使用");
                e.onComplete();
            }
        }).subscribeOn(Schedulers.newThread())//指定事件发出的线程
          .observeOn(AndroidSchedulers.mainThread())//指定接受事件的线程
          .subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                mTextView.setText(s);
            }
        });

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

    //加载最近在用的app
    public void loadRecentUseApp() {
    	 mRecentView.loadRecentUseData();
	}
    


    public void setIconCache(IconCache mIconCache) {
		this.mIconCache = mIconCache;
	}
}

