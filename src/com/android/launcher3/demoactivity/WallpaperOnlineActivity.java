package com.android.launcher3.demoactivity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.launcher3.bean.WallpaperClassify;
import com.android.launcher3.bean.WallpaperOnline;
import com.android.launcher3.net.HttpController;
import com.android.launcher3.net.ResultCallBack;
import com.android.launcher3.pageindicator.PageIndicatorView;
import com.android.launcher3.swipe.HeaderAndFooterWrapper;
import com.android.launcher3.swipe.NoAlphaDefaultItemAnimator;
import com.android.launcher3.swipe.OutlineContainer;
import com.android.launcher3.swipe.SwipeRecyclerView;
import com.android.launcher3.swipe.SwitchViewPager;
import com.android.launcher3.utils.Util;
import com.cuan.launcher.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by cgx on 16/11/10.
 * 壁纸界面
 */

public class WallpaperOnlineActivity extends Activity implements SwipeRecyclerView.OnSwipeRecyclerViewListener {

    private SwipeRecyclerView mSwipeRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private int pageIndex=1;
    private ArrayList<WallpaperOnline.WallpaperOnlineInfo> mWallpaperList;
    private WaterfallAdapter mAdapter;   //瀑布流的适配器
    private HeaderAndFooterWrapper mHeaderAndFooterWrapper;  //可以添加Head和Foot的包装者适配器，需要传入一个适配器
    private DisplayImageOptions imageOptions;
    private SwitchViewPager mViewPager;
    private ArrayList<WallpaperClassify.WallpaperClassifyInfo> mWallpaperClassifyList;
    private WallpaperClassifyAdapter mClassifyAdapter;
    private PageIndicatorView mPageIndicatorView;
    private View mWallpaperHeadView;
    private String tagCode;   //当前加载的壁纸类别
    private boolean isSwitchWallpaperClsaaify;   //是不是切换壁纸类别

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
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

        mWallpaperHeadView = View.inflate(this, R.layout.wallpaper_headview, null);
        mWallpaperClassifyList = new ArrayList<>();
        mViewPager = (SwitchViewPager) mWallpaperHeadView.findViewById(R.id.viewpager);
        mClassifyAdapter = new WallpaperClassifyAdapter();
        mViewPager.setAdapter(mClassifyAdapter);
        mPageIndicatorView = (PageIndicatorView) mWallpaperHeadView.findViewById(R.id.pageindicatorview);
        mPageIndicatorView.setViewPager(mViewPager);

        mHeaderAndFooterWrapper = new HeaderAndFooterWrapper(mAdapter);
        mHeaderAndFooterWrapper.addHeaderView(mWallpaperHeadView);
        mRecyclerView.setAdapter(mHeaderAndFooterWrapper);

        imageOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .showImageOnLoading(R.drawable.default_loading_img)
                .delayBeforeLoading(100)//载入图片前稍做延时可以提高整体滑动的流畅度
                .imageScaleType(ImageScaleType.EXACTLY_STRETCHED)//设置图片以如何的编码方式显示
                .displayer(new FadeInBitmapDisplayer(600))//是否图片加载好后渐入的动画时间
                .build();
        loadWallpaperClassify();
    }

    private void refreshWallpaperData(){
        isSwitchWallpaperClsaaify = true;
        pageIndex = 1;
        mSwipeRecyclerView.loadData();
    }

    //获取壁纸分类信息
    private void loadWallpaperClassify(){
        HttpController.getInstance().getWallpaperClassify(new ResultCallBack<WallpaperClassify>() {
            @Override
            public void onError(Exception e) {

            }

            @Override
            public void onResponse(WallpaperClassify response) {
                if (response != null && response.getResults() != null && response.getResults().size() > 0){
                    mWallpaperClassifyList.addAll(response.getResults());
                    mClassifyAdapter.notifyDataSetChanged();
                    //设置指示器点的个数
                    mPageIndicatorView.setCount(response.getResults().size());
                    mPageIndicatorView.setSelectIndicator(0);
                    tagCode = response.getResults().get(0).getCode();
                    refreshWallpaperData();
                }
            }
        });
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
                if(response !=null && response.getResults() != null && response.getResults().size() > 0) {
                    if (isSwitchWallpaperClsaaify) {
                        mWallpaperList.clear();
                    }
                    isSwitchWallpaperClsaaify = false;
                    int index = mWallpaperList.size();
                    if (type == 1){
                        mWallpaperList.addAll(0, response.getResults());
                        mHeaderAndFooterWrapper.notifyDataSetChanged();
                    }else{
                        mWallpaperList.addAll(response.getResults());
                        mHeaderAndFooterWrapper.notifyItemRangeChanged(mHeaderAndFooterWrapper.getHeadersCount()+index, mWallpaperList.size());
                    }
                    pageIndex++;
                }
                mSwipeRecyclerView.onLoadFinish();
            }
        }, pageIndex, tagCode);
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
        public void onBindViewHolder(final WaterfallAdapter.WaterfallHolder holder, final int position) {
            final WallpaperOnline.WallpaperOnlineInfo info = mWallpaperList.get(position);
            if (!info.getCover().equals(holder.mImageView.getTag())) {
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.mImageView.getLayoutParams();
                if (info.mHeight == 0) {
                    info.mHeight = (int) (Util.getScreenW() / 3 + info.getWeight() * Util.dip2px(7));
                }
                params.height = info.mHeight;
                holder.mImageView.setImageBitmap(null);
                holder.mImageView.setTag(info.getCover());
                holder.mImageView.setOnClickListener(null);
                ImageLoader.getInstance().displayImage(info.getCover(), holder.mImageView, imageOptions, new SimpleImageLoadingListener(){
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        //图片显示出来以后在设置点击事件
                        holder.mImageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startImageActivity(holder.mImageView, info);
                            }
                        });
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

    private class WallpaperClassifyAdapter extends PagerAdapter {

        private LinkedList<View> mCaches = new LinkedList<>();

        public WallpaperClassifyAdapter(){
        }

        private View createItem(WallpaperClassifyHolder holder){
            View view = View.inflate(WallpaperOnlineActivity.this, R.layout.wallpaper_classify_item, null);
            holder.imageView = (ImageView) view.findViewById(R.id.classify_img);
            holder.textView = (TextView) view.findViewById(R.id.classify_name);
            view.setTag(holder);
            return view;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {

            WallpaperClassifyHolder holder;
            View view;
            final WallpaperClassify.WallpaperClassifyInfo classifyInfo = mWallpaperClassifyList.get(position);
            if (mCaches.size() == 0){
                holder = new WallpaperClassifyHolder();
                view = createItem(holder);
            }else{
                view = mCaches.removeFirst();
                holder = (WallpaperClassifyHolder) view.getTag();
            }
//            view.setScaleX(1); view.setScaleY(1);
//            view.setTranslationX(0);
            ImageLoader.getInstance().displayImage(classifyInfo.getCover(), holder.imageView, imageOptions);
            holder.textView.setText(classifyInfo.getName());

            container.addView(view, ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            mViewPager.setObjectForPosition(view, position);

            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!TextUtils.isEmpty(classifyInfo.getCode()) && !TextUtils.equals(tagCode, classifyInfo.getCode())){
                        tagCode = classifyInfo.getCode();
                        refreshWallpaperData();
                        mPageIndicatorView.setSelectIndicator(position);
                    }
                }
            });

            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object obj) {
            mViewPager.findViewFromObject(position);
            container.removeView((View)obj);
//            mCaches.clear();
//            mCaches.add((View)obj);
        }

        @Override
        public int getCount() {
            return mWallpaperClassifyList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            if (view instanceof OutlineContainer) {
                return ((OutlineContainer) view).getChildAt(0) == obj;
            } else {
                return view == obj;
            }
        }

        private class WallpaperClassifyHolder {
            public ImageView imageView;
            public TextView textView;
        }
    }

    private void startImageActivity(View view, WallpaperOnline.WallpaperOnlineInfo info){
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        //显示大图图片的宽和高，服务器返回的数据要带这个图片的宽高数据，这里已经知道壁纸数据的宽和高，就是屏幕高乘以2
        int largeWidth = info.getFileWidth();
        int largeHeight = info.getFileHeight();
        ImageActivity.actionActivity(WallpaperOnlineActivity.this, info.getCover(), info.getFile(),
                view.getWidth(), view.getHeight(), location[0], location[1], largeWidth, largeHeight);
    }
}
