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
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Surface;

/**
 * 截屏
 */
public class ScreenShot {

	private static final String TAG = "ScreenShot";
	private static Bitmap mWallpaperBitmap;

	/**
	 * 更新壁纸缓存
	 * @param context
	 */
	public static void updateWallpaperBitmap(final Context context) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				int scale = 16;
				WallpaperManager sWallpaperManager = WallpaperManager.getInstance(context);
				int wallpaperWidth = Util.getScreenW();
				int wallpaperHeight = Util.getScreenH();
				mWallpaperBitmap = BitmapUtils.drawableToBitmap(sWallpaperManager.getDrawable(), wallpaperWidth / scale, wallpaperHeight / scale);
				sWallpaperManager.forgetLoadedWallpaper();
			}
		}).start();
	}

	/**
	 * 清除壁纸缓存
	 */
	public static void clearWallpaperBitmap() {
		if (mWallpaperBitmap != null) {
			mWallpaperBitmap.recycle();
			mWallpaperBitmap = null;
		}
	}

	/**
	 * 截屏
	 * @return
	 */
	public static Bitmap getScreenShot(Launcher launcher, int outWidth, int outHeight) {
		int sdkVer = Util.getSdkVersionCode();
		Bitmap screenShot = null;

//		if (sdkVer >= 18) {
//			screenShot = getScreenShotFor18(launcher, outWidth, outHeight);
//		} else if (sdkVer >= 14) {
//			screenShot = getScreenShotFor14(launcher, outWidth, outHeight);
//		}

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
		int scale = 16;
		int displayWidth = launcher.getResources().getDisplayMetrics().widthPixels;
		int displayHeight = launcher.getResources().getDisplayMetrics().heightPixels;
		WallpaperManager sWallpaperManager = WallpaperManager.getInstance(launcher);

        Drawable wallpaperDrawable = sWallpaperManager.getDrawable();
		int wallpaperWidth = wallpaperDrawable.getIntrinsicWidth();
		int wallpaperHeight = wallpaperDrawable.getIntrinsicHeight();
		if (mWallpaperBitmap == null) {
			mWallpaperBitmap = BitmapUtils.drawableToBitmap(sWallpaperManager.getDrawable(), wallpaperWidth / scale, wallpaperHeight / scale);
			sWallpaperManager.forgetLoadedWallpaper();
		}

		int x = (wallpaperWidth - displayWidth) / (launcher.getWorkspace().getChildCount() - 2) * launcher.getWorkspace().getCurrentPage();
		if (x <= 0) x = 0;

		Bitmap wallpaperBitmap = Bitmap.createBitmap(mWallpaperBitmap, x / scale, 0, displayWidth / scale, displayHeight / scale);
		return wallpaperBitmap;
	}
	public static Bitmap getScreenShotForLowerApi(Launcher launcher, int outWidth, int outHeight) {
		try {
			int scale = 16;
			int displayWidth = launcher.getResources().getDisplayMetrics().widthPixels;
			int displayHeight = launcher.getResources().getDisplayMetrics().heightPixels;
			WallpaperManager sWallpaperManager = WallpaperManager.getInstance(launcher);

			Drawable wallpaperDrawable = sWallpaperManager.getDrawable();
			int wallpaperWidth = wallpaperDrawable.getIntrinsicWidth();
			int wallpaperHeight = wallpaperDrawable.getIntrinsicHeight();
			if (mWallpaperBitmap == null) {
				mWallpaperBitmap = BitmapUtils.drawableToBitmap(wallpaperDrawable, wallpaperWidth / scale, wallpaperHeight / scale);
				sWallpaperManager.forgetLoadedWallpaper();
			}

			int x = (wallpaperWidth - displayWidth) / (launcher.getWorkspace().getChildCount() - 1) * launcher.getWorkspace().getCurrentPage();
			if (x <= 0) x = 0;

			Bitmap wallpaperBitmap = Bitmap.createBitmap(mWallpaperBitmap, x / scale, 0, displayWidth / scale, displayHeight / scale);

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
