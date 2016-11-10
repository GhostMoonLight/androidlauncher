package com.android.launcher3.demoactivity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.launcher3.bean.WallpaperOnline;
import com.android.launcher3.net.HttpController;
import com.android.launcher3.net.ResultCallBack;
import com.android.launcher3.utils.Util;
import com.cuan.launcher.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Random;

/**
 * 瀑布流的壁纸界面
 * RecyclerView.canScrollVertically(1)的值表示是否能向下滚动，false表示已经滚动到底部
 * RecyclerView.canScrollVertically(-1)的值表示是否能向上滚动，false表示已经滚动到顶部
 */
public class MainActivity extends Activity implements SwipeRefreshLayout.OnRefreshListener {

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch(msg.what){
            }
            return true;
        }
    };

    private RecyclerView mRecycleView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private Handler mHandler = new WeakRefHandler(mCallback);
    private ArrayList<WallpaperOnline.WallpaperOnlineInfo> mWallpaperList;
    private WaterfallAdapter mAdapter;
    private DisplayImageOptions imageOptions;
    private Random mRandom;
    private boolean isLoading; //是否正在加载
    private boolean isLoadMore; //是否是加载更多
    private int pageIndex=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        setContentView(R.layout.activity_main);

        mRecycleView = (RecyclerView) findViewById(R.id.recycleview);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiprefreshlayout);

        final StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(3 , StaggeredGridLayoutManager.VERTICAL);
        mRecycleView.setLayoutManager(layoutManager);
        mAdapter = new WaterfallAdapter();

        mWallpaperList = new ArrayList<>();
        mRecycleView.setAdapter(mAdapter);

        //设置界面显示出来的时候就显示加载进度
        mSwipeRefreshLayout.setProgressViewOffset(true, 0, Util.dip2px(32));
        mSwipeRefreshLayout.setSize(SwipeRefreshLayout.DEFAULT);  //进度圈的大小  只有DEFAULT和LARGE两个值
        //mSwipeRefreshLayout.setProgressBackgroundColor(android.R.color.holo_blue_dark);  //设置进度圈的背景颜色
        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_green_dark,
                android.R.color.holo_blue_dark, android.R.color.holo_orange_dark);
        mSwipeRefreshLayout.setRefreshing(true);

        mSwipeRefreshLayout.setOnRefreshListener(this);
        mRecycleView.setItemAnimator(new NoAlphaDefaultItemAnimator());

        mRandom = new Random();
        imageOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .showImageOnLoading(R.drawable.default_loading_img)
                .delayBeforeLoading(100)//载入图片前稍做延时可以提高整体滑动的流畅度
                .imageScaleType(ImageScaleType.EXACTLY_STRETCHED)//设置图片以如何的编码方式显示
                .displayer(new FadeInBitmapDisplayer(600))//是否图片加载好后渐入的动画时间
                .build();

        mRecycleView.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE
                        && !mRecycleView.canScrollVertically(1)) {
                    mSwipeRefreshLayout.setRefreshing(true);
                    loadMore();
                }
            }
        });
        onRefresh();
    }

    //请求网络数据   type=1 下啦刷新，  type＝2加载更多
    private void loadWallpaperOnline(int type){
        if (isLoading || isLoadMore) return;
        if (type == 1){
            isLoading = true;
        } else if (type == 2){
            isLoadMore = true;
        } else {
            throw new IllegalArgumentException("type只能为1或者2");
        }
        HttpController.getInstance().getWallpaperOnline(new ResultCallBack<WallpaperOnline>() {
            @Override
            public void onError(Exception e) {
                isLoadMore = isLoading = false;
                mSwipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onResponse(WallpaperOnline response) {
                mSwipeRefreshLayout.setRefreshing(false);
                Log.e("AAAAA", "size:"+response.getResults().size());
                if(response.getResults().size() > 0) {
                    int index = mWallpaperList.size();
                    if (isLoading) {
                        mWallpaperList.addAll(0, response.getResults());
                        mAdapter.notifyDataSetChanged();
                    } else {
                        mWallpaperList.addAll(response.getResults());
                        mAdapter.notifyItemRangeChanged(index, mWallpaperList.size());
                    }
                    pageIndex++;
                }
                isLoadMore = isLoading = false;
            }
        }, pageIndex);
    }

    //下拉刷新
    @Override
    public void onRefresh() {
        loadWallpaperOnline(1);
    }

    public void loadMore(){
        loadWallpaperOnline(2);
    }

    private class WaterfallAdapter extends RecyclerView.Adapter<WaterfallAdapter.WaterfallHolder>{

        @Override
        public WaterfallHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.wallpaper_item, parent, false);

            WaterfallHolder holder = new WaterfallHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(WaterfallHolder holder, final int position) {
            WallpaperOnline.WallpaperOnlineInfo info = mWallpaperList.get(position);
            if (!info.getCover().equals(holder.mImageView.getTag())) {
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.mImageView.getLayoutParams();
                if (info.mHeight == 0) {
                    info.mHeight = (int) (Util.getScreenW() / 3 + info.getWeight() * Util.dip2px(7));
                }
                params.height = info.mHeight;
                holder.mImageView.setImageBitmap(null);
                holder.mImageView.setTag(info.getCover());
                ImageLoader.getInstance().displayImage(info.getCover(), holder.mImageView, imageOptions);

                holder.mImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(MainActivity.this, "position:"+position, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return mWallpaperList.size();
        }

        public class WaterfallHolder extends RecyclerView.ViewHolder{
            public ImageView mImageView;

            public WaterfallHolder(View itemView) {
                super(itemView);

                mImageView = (ImageView) itemView.findViewById(R.id.wallpaper_img);
            }
        }
    }

    /**
     * 实现回调弱引用的Handler
     * 防止由于内部持有导致的内存泄露
     *
     * PS：
     * 1、传入的Callback不能使用匿名实现的变量，必须与使用这个Handle的对象的生命周期一致，否则会被立即释放掉了
     *
     * @author brian512
     */
    public class WeakRefHandler extends Handler {
        private WeakReference<Callback> mWeakReference;

        public WeakRefHandler(Callback callback) {
            mWeakReference = new WeakReference<>(callback);
        }

        public WeakRefHandler(Callback callback, Looper looper) {
            super(looper);
            mWeakReference = new WeakReference<>(callback);
        }

        @Override
        public void handleMessage(Message msg) {
            if (mWeakReference != null && mWeakReference.get() != null) {
                Callback callback = mWeakReference.get();
                callback.handleMessage(msg);
            }
        }
    }
}
