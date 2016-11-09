package com.android.launcher3.demoactivity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

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

public class MainActivity extends Activity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        setContentView(R.layout.activity_main);

        mRecycleView = (RecyclerView) findViewById(R.id.recycleview);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiprefreshlayout);

        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(3 , StaggeredGridLayoutManager.VERTICAL);
        mRecycleView.setLayoutManager(layoutManager);
        mAdapter = new WaterfallAdapter();

        mWallpaperList = new ArrayList<>();
        mRecycleView.setAdapter(mAdapter);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //下拉刷新
                HttpController.getInstance().getWallpaperOnline(new ResultCallBack<WallpaperOnline>() {
                    @Override
                    public void onError(Exception e) {

                    }

                    @Override
                    public void onResponse(WallpaperOnline response) {
                        mSwipeRefreshLayout.setRefreshing(false);
                        mWallpaperList.addAll(response.getResults());
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        });

        HttpController.getInstance().getWallpaperOnline(new ResultCallBack<WallpaperOnline>() {
            @Override
            public void onError(Exception e) {

            }

            @Override
            public void onResponse(WallpaperOnline response) {
                mWallpaperList.addAll(response.getResults());
                mAdapter.notifyDataSetChanged();
            }
        });

        mRandom = new Random();
        imageOptions = new DisplayImageOptions.Builder()
                .imageScaleType(ImageScaleType.EXACTLY_STRETCHED)//设置图片以如何的编码方式显示
                .displayer(new FadeInBitmapDisplayer(800))//是否图片加载好后渐入的动画时间
                .build();
    }

    private class WaterfallAdapter extends RecyclerView.Adapter<WaterfallAdapter.WaterfallHolder>{


        @Override
        public WaterfallHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.wallpaper_item, parent, false);

            WaterfallHolder holder = new WaterfallHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(WaterfallHolder holder, int position) {
            WallpaperOnline.WallpaperOnlineInfo info = mWallpaperList.get(position);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.mImageView.getLayoutParams();
            if (info.mHeight == 0){
                info.mHeight = (int) (Util.getScreenW()/3+ info.getWeight()*Util.dip2px(7));
            }
            params.height = info.mHeight;
            holder.mImageView.setImageBitmap(null);
            ImageLoader.getInstance().displayImage(info.getCover(), holder.mImageView, imageOptions);
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
