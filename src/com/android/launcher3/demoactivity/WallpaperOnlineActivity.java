package com.android.launcher3.demoactivity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.launcher3.bean.WallpaperOnline;
import com.android.launcher3.net.HttpController;
import com.android.launcher3.net.ResultCallBack;
import com.android.launcher3.swipe.NoAlphaDefaultItemAnimator;
import com.android.launcher3.swipe.SwipeRecyclerView;
import com.android.launcher3.utils.Util;
import com.cuan.launcher.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.util.ArrayList;

/**
 * Created by cgx on 16/11/10.
 */

public class WallpaperOnlineActivity extends Activity implements SwipeRecyclerView.OnSwipeRecyclerViewListener {

    private SwipeRecyclerView mSwipeRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private int pageIndex=1;
    private ArrayList<WallpaperOnline.WallpaperOnlineInfo> mWallpaperList;
    private WaterfallAdapter mAdapter;
    private DisplayImageOptions imageOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSwipeRecyclerView = (SwipeRecyclerView) findViewById(R.id.swiperecyclerview);
        mRecyclerView = mSwipeRecyclerView.getRecyclerView();
        mSwipeRecyclerView.setItemAnimator(new NoAlphaDefaultItemAnimator());
        mSwipeRefreshLayout = mSwipeRecyclerView.getSwipeRefreshLayout();
        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_green_dark,
                android.R.color.holo_blue_dark, android.R.color.holo_orange_dark);

        mSwipeRecyclerView.setOnSwipeRecyclerViewListener(this);
        final StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(3 , StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new WaterfallAdapter();
        mWallpaperList = new ArrayList<>();
        mRecyclerView.setAdapter(mAdapter);

        imageOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .showImageOnLoading(R.drawable.default_loading_img)
                .delayBeforeLoading(100)//载入图片前稍做延时可以提高整体滑动的流畅度
                .imageScaleType(ImageScaleType.EXACTLY_STRETCHED)//设置图片以如何的编码方式显示
                .displayer(new FadeInBitmapDisplayer(600))//是否图片加载好后渐入的动画时间
                .build();
        mSwipeRecyclerView.loadData();
    }

    //请求网络数据   type=1 下啦刷新，  type＝2加载更多
    private void loadWallpaperOnline(final int type){
        HttpController.getInstance().getWallpaperOnline(new ResultCallBack<WallpaperOnline>() {
            @Override
            public void onError(Exception e) {
                mSwipeRecyclerView.onLoadFinish();
            }

            @Override
            public void onResponse(WallpaperOnline response) {
                if(response.getResults().size() > 0) {
                    int index = mWallpaperList.size();
                    if (type == 1){
                        mWallpaperList.addAll(0, response.getResults());
                        mAdapter.notifyDataSetChanged();
                    }else{
                        mWallpaperList.addAll(response.getResults());
                        mAdapter.notifyItemRangeChanged(index, mWallpaperList.size());
                    }
                    pageIndex++;
                }
                mSwipeRecyclerView.onLoadFinish();
            }
        }, pageIndex);
    }

    //刷新
    @Override
    public void onRefresh() {
        loadWallpaperOnline(1);
    }
    //加载更多
    @Override
    public void onLoadNext() {
        loadWallpaperOnline(2);
    }

    private class WaterfallAdapter extends RecyclerView.Adapter<WaterfallAdapter.WaterfallHolder>{

        @Override
        public WaterfallAdapter.WaterfallHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.wallpaper_item, parent, false);

            WaterfallAdapter.WaterfallHolder holder = new WaterfallAdapter.WaterfallHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(WaterfallAdapter.WaterfallHolder holder, final int position) {
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
                        Toast.makeText(WallpaperOnlineActivity.this, "position:"+position, Toast.LENGTH_SHORT).show();
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
}
