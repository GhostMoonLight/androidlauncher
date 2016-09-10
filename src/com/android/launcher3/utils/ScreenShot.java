package com.android.launcher3.utils;

import java.lang.ref.SoftReference;
import java.lang.reflect.Method;

import com.android.launcher3.Launcher;
import com.android.launcher3.wallpaper.BitmapUtils;

import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;
import android.view.Surface;

/**
 * @author jiang
 * 截屏
 */
public class ScreenShot {
	private static final String TAG = "ScreenShot";
	private static SoftReference<Bitmap> sRefWallpaperBitmap;
	
	private static BroadcastReceiver sWallpaperChangeReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			sRefWallpaperBitmap = null;
			try {
				context.unregisterReceiver(this);
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
	};

	/**
	 * 截屏
	 * @return
	 */
	public static Bitmap getScreenShot(Launcher launcher, int outWidth, int outHeight) {
		int sdkVer = Util.getSdkVersionCode();
		Bitmap screenShot = null;
		
		if (sdkVer >= 18) {
			screenShot = getScreenShotFor18(launcher, outWidth, outHeight);
		} else if (sdkVer >= 14) {
			screenShot = getScreenShotFor14(launcher, outWidth, outHeight);
		}
		
		if (screenShot == null) {
			screenShot = getScreenShotForLowerApi(launcher, outWidth, outHeight);
		}
		
		return screenShot;
	}
	
	public static Bitmap getScreenShotFor18(Launcher launcher, int outWidth, int outHeight) {
		try {
			int scale = 1;
			Class<?> sClass = Class.forName("android.view.SurfaceControl");
			Method sMethod = sClass.getMethod("screenshot", int.class, int.class);
			Object result = sMethod.invoke(sClass, outWidth / scale, outHeight / scale);
			
			if (result != null) {
				Bitmap screenshot = (Bitmap) result;
				return screenshot;
			}
		} catch (Exception e) {
			// TODO: handle exception
			Log.w(TAG, "getScreenShotFor14", e);
		}
		return null;
	}
	
	public static Bitmap getScreenShotFor14(Launcher launcher, int outWidth, int outHeight) {
		try {
			int scale = 1;
			Method sMethod = Surface.class.getMethod("screenshot", int.class, int.class);
			Object result = sMethod.invoke(null, outWidth / scale, outHeight / scale);
			
			if (result != null) {
				Bitmap screenshot = (Bitmap) result;
				return screenshot;
			}
		} catch (Exception e) {
			// TODO: handle exception
			Log.w(TAG, "getScreenShotFor14", e);
		}
		return null;
	}
	public static Bitmap getWallpaperImage(Launcher launcher){
		int scale = 4;
		int displayWidth = launcher.getResources().getDisplayMetrics().widthPixels;
		int displayHeight = launcher.getResources().getDisplayMetrics().heightPixels;
		WallpaperManager sWallpaperManager = WallpaperManager.getInstance(launcher);
		
		int wallpaperWidth = displayWidth;
		int wallpaperHeight = displayHeight;
		Bitmap wallpaperBitmap = sRefWallpaperBitmap == null ? null : sRefWallpaperBitmap.get();
		if (wallpaperBitmap == null) {
			if (sRefWallpaperBitmap == null) {
				launcher.registerReceiver(sWallpaperChangeReceiver, new IntentFilter(Intent.ACTION_WALLPAPER_CHANGED));
			}
			wallpaperBitmap = BitmapUtils.drawableToBitmap(sWallpaperManager.getDrawable(), wallpaperWidth / scale, wallpaperHeight / scale);
			sRefWallpaperBitmap = new SoftReference<Bitmap>(wallpaperBitmap);
			sWallpaperManager.forgetLoadedWallpaper();
		}

		int x = 0;
		
		wallpaperBitmap = Bitmap.createBitmap(wallpaperBitmap, x / scale, 0, displayWidth / scale, displayHeight / scale);
		return wallpaperBitmap;
	}
	public static Bitmap getScreenShotForLowerApi(Launcher launcher, int outWidth, int outHeight) {
		try {
			int scale = 4;
			int displayWidth = launcher.getResources().getDisplayMetrics().widthPixels;
			int displayHeight = launcher.getResources().getDisplayMetrics().heightPixels;
			WallpaperManager sWallpaperManager = WallpaperManager.getInstance(launcher);
			
			int wallpaperWidth = displayWidth;
			int wallpaperHeight = displayHeight;
			Bitmap wallpaperBitmap = sRefWallpaperBitmap == null ? null : sRefWallpaperBitmap.get();
			if (wallpaperBitmap == null) {
				if (sRefWallpaperBitmap == null) {
					launcher.registerReceiver(sWallpaperChangeReceiver, new IntentFilter(Intent.ACTION_WALLPAPER_CHANGED));
				}
				wallpaperBitmap = BitmapUtils.drawableToBitmap(sWallpaperManager.getDrawable(), wallpaperWidth / scale, wallpaperHeight / scale);
				sRefWallpaperBitmap = new SoftReference<Bitmap>(wallpaperBitmap);
				sWallpaperManager.forgetLoadedWallpaper();
			}

			int x = 0;
			
			wallpaperBitmap = Bitmap.createBitmap(wallpaperBitmap, x / scale, 0, displayWidth / scale, displayHeight / scale);
			
			int viewWidth = launcher.getDragLayer().getWidth();
			int viewHeight = launcher.getDragLayer().getHeight();
			Bitmap viewBitmap = BitmapUtils.convertViewToBitmap(launcher, launcher.getDragLayer(), viewWidth / scale, viewHeight / scale);
			
			Bitmap screenshot = null;
			if (viewBitmap != null) {
				screenshot = mergerBitmap(wallpaperBitmap, viewBitmap, true, outWidth / scale, outHeight / scale);
			}
			
			BitmapUtils.recycleBitmap(wallpaperBitmap);
			BitmapUtils.recycleBitmap(viewBitmap);
			return screenshot;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 合并两张图片
	 */
	private static Bitmap mergerBitmap(Bitmap bitmap1, Bitmap bitmap2, boolean crop, int width, int height) {
		try {
			if (width == 0 || height == 0) return null;
			Bitmap bitmap3 = Bitmap.createBitmap(bitmap1.getWidth(), bitmap1.getHeight(), bitmap1.getConfig());  
			Canvas canvas = new Canvas(bitmap3);  
			canvas.drawBitmap(bitmap1, 0, 0, null);  
			canvas.drawBitmap(bitmap2, 0, bitmap1.getHeight() - bitmap2.getHeight(), null);
			
			if (crop) {
				bitmap3 = Bitmap.createBitmap(bitmap3, bitmap3.getWidth() - width, bitmap3.getHeight() - height, width, height);
			}
			return bitmap3;
		} catch (OutOfMemoryError error) {
			Log.w(TAG, "mergerBitmap", error);
		} catch (Exception e) {
			Log.w(TAG, "mergerBitmap", e);
		}
		return null;
	}
}
