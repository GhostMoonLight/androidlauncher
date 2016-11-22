package com.android.launcher3.demoactivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.android.launcher3.BaseActivity;
import com.android.launcher3.utils.Util;
import com.android.launcher3.view.SmoothClickMagnifyImageView;
import com.android.launcher3.wallpaper.BitmapUtils;
import com.cuan.launcher.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class ImageActivity extends BaseActivity implements View.OnClickListener {

    public static void actionActivity(Activity activity, String imageUrl, String imageUrlLarger,
                                        int originWidth, int originHeigth, int originPositionX, int originPositionY,
                                        int largeWidth, int largeHeight) {
        Intent intent = new Intent(activity, ImageActivity.class);
        intent.putExtra("imageUrl", imageUrl);
        intent.putExtra("imageUrlLarger", imageUrlLarger);
        intent.putExtra("originWidth", originWidth);
        intent.putExtra("originHeigth", originHeigth);
        intent.putExtra("originPositionX", originPositionX);
        intent.putExtra("originPositionY", originPositionY);
        intent.putExtra("largeWidth", largeWidth);
        intent.putExtra("largeHeight", largeHeight);
        activity.startActivity(intent);
        activity.overridePendingTransition(0, 0);
    }

    private SmoothClickMagnifyImageView mImageView;
    private String mUrl, mLargeUrl;
    private int mOriginalWidth, mOriginalHeight, mLargeWidth, mLargeHeight;
    private int mOriginalPositionX, mOriginalPositionY;
    private TextView mSetWallpaper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        Intent intent = getIntent();
        if (intent == null){
            finish();
        }

        mUrl = intent.getStringExtra("imageUrl");
        mLargeUrl = intent.getStringExtra("imageUrlLarger");
        mOriginalWidth = intent.getIntExtra("originWidth", 0);
        mOriginalHeight = intent.getIntExtra("originHeigth", 0);
        mOriginalPositionX = intent.getIntExtra("originPositionX", 0);
        mOriginalPositionY = intent.getIntExtra("originPositionY", 0);
        mLargeWidth = intent.getIntExtra("largeWidth", 0);
        mLargeHeight = intent.getIntExtra("largeHeight", 0);
        if(Util.getScreenW() == 800) {
            mLargeWidth = 1440;
        }

        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .imageScaleType(ImageScaleType.NONE)   //加载原图 不经过ImageLoader处理
                .build();

        setContentView(R.layout.activity_image);
        mImageView = (SmoothClickMagnifyImageView) findViewById(R.id.image_view);
        ImageLoader.getInstance().loadImage(mUrl, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                mImageView.setOriginalValues(bitmap, mOriginalWidth, mOriginalHeight, mOriginalPositionX, mOriginalPositionY, mLargeWidth, mLargeHeight);
            }
        });
        ImageLoader.getInstance().loadImage(mLargeUrl, defaultOptions, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                mImageView.setImageBitmapWithMatrix(bitmap);
            }
        });

        mSetWallpaper = (TextView) findViewById(R.id.wallpaper_set);
        mSetWallpaper.setOnClickListener(this);
        mSetWallpaper.setAlpha(0);
        mSetWallpaper.animate().alpha(1).setDuration(1500).start();
    }

    @Override
    public void onBackPressed() {
        if(!mImageView.isMagnifyFull())
            mSetWallpaper.animate().alpha(0).setDuration(200).start();
        mImageView.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.wallpaper_set:
                setWallpaper();
                break;
        }
    }

    private boolean isWallpaperSetting;
    public void setWallpaper(){
        if (mImageView.getBitmap() != null) {
            if (!isWallpaperSetting) {
                isWallpaperSetting = true;
                showTip("开始设置壁纸...");
                new Thread() {
                    @Override
                    public void run() {

                        BitmapUtils.setThemeWallpaper(ImageActivity.this, mImageView.getBitmap());
                        showTip("壁纸设置成功");
                        isWallpaperSetting = false;
                    }
                }.start();
            } else {
                showTip("壁纸设置中...");
            }
        }
    }

    private void showTip(final String str){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ImageActivity.this, str, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
