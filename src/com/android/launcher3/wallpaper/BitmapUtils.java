/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.launcher3.wallpaper;

import android.annotation.SuppressLint;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.RSIllegalArgumentException;
import android.renderscript.RenderScript;
import android.renderscript.RenderScript.RSMessageHandler;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.FloatMath;
import android.util.Log;
import android.view.View;

import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.utils.ScreenShot;
import com.android.launcher3.utils.Util;
import com.cuan.launcher.R;

import net.qiujuer.genius.blur.StackBlur;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class BitmapUtils {
    private static final String TAG = "BitmapUtils";
    private static final int DEFAULT_JPEG_QUALITY = 90;
    public static final int UNCONSTRAINED = -1;
    private static int sIconWidth = -1;
    private static int sIconHeight = -1;
    public static int sIconTextureWidth = -1;
    public static int sIconTextureHeight = -1;
    private static Bitmap bgBitmap=null;

    private BitmapUtils(){}

    /*
     * Compute the sample size as a function of minSideLength
     * and maxNumOfPixels.
     * minSideLength is used to specify that minimal width or height of a
     * bitmap.
     * maxNumOfPixels is used to specify the maximal size in pixels that is
     * tolerable in terms of memory usage.
     *
     * The function returns a sample size based on the constraints.
     * Both size and minSideLength can be passed in as UNCONSTRAINED,
     * which indicates no care of the corresponding constraint.
     * The functions prefers returning a sample size that
     * generates a smaller bitmap, unless minSideLength = UNCONSTRAINED.
     *
     * Also, the function rounds up the sample size to a power of 2 or multiple
     * of 8 because BitmapFactory only honors sample size this way.
     * For example, BitmapFactory downsamples an image by 2 even though the
     * request is 3. So we round up the sample size to avoid OOM.
     */
    public static int computeSampleSize(int width, int height,
            int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(
                width, height, minSideLength, maxNumOfPixels);

        return initialSize <= 8
                ? Utils.nextPowerOf2(initialSize)
                : (initialSize + 7) / 8 * 8;
    }

    private static int computeInitialSampleSize(int w, int h,
            int minSideLength, int maxNumOfPixels) {
        if (maxNumOfPixels == UNCONSTRAINED
                && minSideLength == UNCONSTRAINED) return 1;

        int lowerBound = (maxNumOfPixels == UNCONSTRAINED) ? 1 :
                (int) FloatMath.ceil(FloatMath.sqrt((float) (w * h) / maxNumOfPixels));

        if (minSideLength == UNCONSTRAINED) {
            return lowerBound;
        } else {
            int sampleSize = Math.min(w / minSideLength, h / minSideLength);
            return Math.max(sampleSize, lowerBound);
        }
    }

    // This computes a sample size which makes the longer side at least
    // minSideLength long. If that's not possible, return 1.
    public static int computeSampleSizeLarger(int w, int h,
            int minSideLength) {
        int initialSize = Math.max(w / minSideLength, h / minSideLength);
        if (initialSize <= 1) return 1;

        return initialSize <= 8
                ? Utils.prevPowerOf2(initialSize)
                : initialSize / 8 * 8;
    }

    // Find the min x that 1 / x >= scale
    public static int computeSampleSizeLarger(float scale) {
        int initialSize = (int) FloatMath.floor(1f / scale);
        if (initialSize <= 1) return 1;

        return initialSize <= 8
                ? Utils.prevPowerOf2(initialSize)
                : initialSize / 8 * 8;
    }

    // Find the max x that 1 / x <= scale.
    public static int computeSampleSize(float scale) {
        Utils.assertTrue(scale > 0);
        int initialSize = Math.max(1, (int) FloatMath.ceil(1 / scale));
        return initialSize <= 8
                ? Utils.nextPowerOf2(initialSize)
                : (initialSize + 7) / 8 * 8;
    }

    public static Bitmap resizeBitmapByScale(
            Bitmap bitmap, float scale, boolean recycle) {
        int width = Math.round(bitmap.getWidth() * scale);
        int height = Math.round(bitmap.getHeight() * scale);
        if (width == bitmap.getWidth()
                && height == bitmap.getHeight()) return bitmap;
        Bitmap target = Bitmap.createBitmap(width, height, getConfig(bitmap));
        Canvas canvas = new Canvas(target);
        canvas.scale(scale, scale);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        if (recycle) bitmap.recycle();
        return target;
    }

    private static Bitmap.Config getConfig(Bitmap bitmap) {
        Bitmap.Config config = bitmap.getConfig();
        if (config == null) {
            config = Bitmap.Config.ARGB_8888;
        }
        return config;
    }

    public static Bitmap resizeDownBySideLength(
            Bitmap bitmap, int maxLength, boolean recycle) {
        int srcWidth = bitmap.getWidth();
        int srcHeight = bitmap.getHeight();
        float scale = Math.min(
                (float) maxLength / srcWidth, (float) maxLength / srcHeight);
        if (scale >= 1.0f) return bitmap;
        return resizeBitmapByScale(bitmap, scale, recycle);
    }

    public static Bitmap resizeAndCropCenter(Bitmap bitmap, int size, boolean recycle) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        if (w == size && h == size) return bitmap;

        // scale the image so that the shorter side equals to the target;
        // the longer side will be center-cropped.
        float scale = (float) size / Math.min(w,  h);

        Bitmap target = Bitmap.createBitmap(size, size, getConfig(bitmap));
        int width = Math.round(scale * bitmap.getWidth());
        int height = Math.round(scale * bitmap.getHeight());
        Canvas canvas = new Canvas(target);
        canvas.translate((size - width) / 2f, (size - height) / 2f);
        canvas.scale(scale, scale);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        if (recycle) bitmap.recycle();
        return target;
    }

    public static void recycleSilently(Bitmap bitmap) {
        if (bitmap == null) return;
        try {
            bitmap.recycle();
        } catch (Throwable t) {
            Log.w(TAG, "unable recycle bitmap", t);
        }
    }

    public static Bitmap rotateBitmap(Bitmap source, int rotation, boolean recycle) {
        if (rotation == 0) return source;
        int w = source.getWidth();
        int h = source.getHeight();
        Matrix m = new Matrix();
        m.postRotate(rotation);
        Bitmap bitmap = Bitmap.createBitmap(source, 0, 0, w, h, m, true);
        if (recycle) source.recycle();
        return bitmap;
    }

    public static Bitmap createVideoThumbnail(String filePath) {
        // MediaMetadataRetriever is available on API Level 8
        // but is hidden until API Level 10
        Class<?> clazz = null;
        Object instance = null;
        try {
            clazz = Class.forName("android.media.MediaMetadataRetriever");
            instance = clazz.newInstance();

            Method method = clazz.getMethod("setDataSource", String.class);
            method.invoke(instance, filePath);

            // The method name changes between API Level 9 and 10.
            if (Build.VERSION.SDK_INT <= 9) {
                return (Bitmap) clazz.getMethod("captureFrame").invoke(instance);
            } else {
                byte[] data = (byte[]) clazz.getMethod("getEmbeddedPicture").invoke(instance);
                if (data != null) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    if (bitmap != null) return bitmap;
                }
                return (Bitmap) clazz.getMethod("getFrameAtTime").invoke(instance);
            }
        } catch (IllegalArgumentException ex) {
            // Assume this is a corrupt video file
        } catch (RuntimeException ex) {
            // Assume this is a corrupt video file.
        } catch (InstantiationException e) {
            Log.e(TAG, "createVideoThumbnail", e);
        } catch (InvocationTargetException e) {
            Log.e(TAG, "createVideoThumbnail", e);
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "createVideoThumbnail", e);
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "createVideoThumbnail", e);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "createVideoThumbnail", e);
        } finally {
            try {
                if (instance != null) {
                    clazz.getMethod("release").invoke(instance);
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    public static byte[] compressToBytes(Bitmap bitmap) {
        return compressToBytes(bitmap, DEFAULT_JPEG_QUALITY);
    }

    public static byte[] compressToBytes(Bitmap bitmap, int quality) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(65536);
        bitmap.compress(CompressFormat.JPEG, quality, baos);
        return baos.toByteArray();
    }

    public static boolean isSupportedByRegionDecoder(String mimeType) {
        if (mimeType == null) return false;
        mimeType = mimeType.toLowerCase();
        return mimeType.startsWith("image/") &&
                (!mimeType.equals("image/gif") && !mimeType.endsWith("bmp"));
    }

    public static boolean isRotationSupported(String mimeType) {
        if (mimeType == null) return false;
        mimeType = mimeType.toLowerCase();
        return mimeType.equals("image/jpeg");
    }
    
    private static void initStatics(Context context) {
        final Resources resources = context.getResources();
        sIconWidth = sIconHeight = (int) resources.getDimension(R.dimen.app_icon_size);
        sIconTextureWidth = sIconTextureHeight = sIconWidth;
        BitmapDrawable bd = (BitmapDrawable)context.getResources().getDrawable(R.drawable.bg_icon);
		bgBitmap=bd.getBitmap();
    }

    public static void setIconSize(int widthPx) {
        sIconWidth = sIconHeight = widthPx;
        sIconTextureWidth = sIconTextureHeight = widthPx;
    }
    
    /**
	 * Returns a bitmap suitable for the all apps view.
	 */
	public static Bitmap createIconBitmap(Drawable icon, Context context) {
		if (sIconWidth == -1) {
			initStatics(context);
		}
		
		if (icon == null) {
			icon = LauncherAppState.getInstance().getIconCache().getFullResDefaultActivityIcon();
		}
		
		int iconWidth = icon.getIntrinsicWidth();
		int iconHeight = icon.getIntrinsicHeight();
		int newIconWidth = sIconTextureWidth;
		int newIconHeight = sIconTextureHeight;
		
		Bitmap bitmap = Bitmap.createBitmap(newIconWidth, newIconHeight, icon.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.ARGB_4444);
		Canvas canvas = new Canvas();
		canvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG, Paint.FILTER_BITMAP_FLAG));
		canvas.setBitmap(bitmap);
		
		
		float scale = (float) newIconWidth / iconWidth;
		canvas.scale(scale, scale);
		icon.setBounds(0, 0, iconWidth, iconHeight);
		icon.draw(canvas);
		return getRoundedCornerBitmap(context, bitmap);
	}
	
	/**
	 * 生成圆角图片
	 */
	public static Bitmap getRoundedCornerBitmap(Context context, Bitmap bitmap) {
		try {
			if(bgBitmap==null){
				BitmapDrawable bd = (BitmapDrawable)context.getResources().getDrawable(R.drawable.bg_icon);
				bgBitmap=bd.getBitmap();
			}
			Paint p = new Paint();
			p.setAntiAlias(true); //去锯齿
			p.setColor(Color.BLACK);
			p.setStyle(Paint.Style.FILL);
			Canvas canvas = new Canvas(bitmap);  	//bitmap就是我们原来的图,比如头像
			p.setXfermode(new PorterDuffXfermode(Mode.DST_IN));  //因为我们先画了图所以DST_IN
			
			float scaleX = (float) bitmap.getWidth() / bgBitmap.getWidth();
			float scaleY = (float) bitmap.getHeight() / bgBitmap.getHeight();
			Matrix matrix = new Matrix();
			matrix.postScale(scaleX, scaleY); 	// 长和宽放大缩小的比例
			bgBitmap = Bitmap.createBitmap(bgBitmap, 0, 0, bgBitmap.getWidth(), bgBitmap.getHeight(), matrix, true);
			
			Rect src = new Rect(0, 0, bgBitmap.getWidth(), bgBitmap.getHeight());
			canvas.drawBitmap(bgBitmap, src, src, p);
			
			return bitmap;
		} catch (Exception e) {
			e.printStackTrace();
			if(bgBitmap!=null)
			bgBitmap.recycle();
			return bitmap;
		}
	}
	
	/**
	 * 只用于获取应用icon
	 * @param iconName
	 * @return
	 */
	public static Bitmap getBitmap(String iconName) {
		if(iconName.equals("")){
			return null;
		}
		Drawable drawable=getResourceDrawable(iconName);
		Bitmap icon=null;
		if(drawable!=null){
			icon=createIconBitmap(drawable, LauncherAppState.getInstance().getContext());
		}
		
		return icon;
	}
	
	/**
	 * @param name
	 * @return
	 */
	public static Drawable getResourceDrawable(String name){
		if (name.equals("")) {
			return null;
		}
		Drawable value=null;
		try{
			Context mContext = LauncherAppState.getInstance().getContext();
			int id=LauncherAppState.getInstance().getContext().getResources().getIdentifier(name, "drawable", mContext.getPackageName());
			Resources resources = mContext.getApplicationContext().getResources();
			if(id!=0){
//					value = resources.getDrawable(id);
				value = getDrawableByResId(resources, id);
			}
		}catch(Exception e){
			value=null;
		}
		return value;
	}
	
	public static Drawable getDrawableByResId(Resources res,int resId){
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inPreferredConfig = Bitmap.Config.RGB_565;
		opt.inPurgeable = true;
		opt.inInputShareable = true;
		//获取资源图片
		InputStream is = res.openRawResource(resId);
		Bitmap bitmap = BitmapFactory.decodeStream(is,null, opt);
		try {
			is.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new BitmapDrawable(res,bitmap);
	}
	
	/**
     * Draw the view into a bitmap.
     */
    public static Bitmap getViewBitmap(View v) {
        v.clearFocus();
        v.setPressed(false);

        boolean willNotCache = v.willNotCacheDrawing();
        v.setWillNotCacheDrawing(false);

        // Reset the drawing cache background color to fully transparent
        // for the duration of this operation
        int color = v.getDrawingCacheBackgroundColor();
        v.setDrawingCacheBackgroundColor(0);
        float alpha = v.getAlpha();
        v.setAlpha(1.0f);

        if (color != 0) {
            v.destroyDrawingCache();
        }
        v.buildDrawingCache();
        Bitmap cacheBitmap = v.getDrawingCache();
        if (cacheBitmap == null) {
            Log.e(TAG, "failed getViewBitmap(" + v + ")", new RuntimeException());
            return null;
        }

        Bitmap bitmap = Bitmap.createBitmap(cacheBitmap);

        // Restore the view
        v.destroyDrawingCache();
        v.setAlpha(alpha);
        v.setWillNotCacheDrawing(willNotCache);
        v.setDrawingCacheBackgroundColor(color);

        return bitmap;
    }
    
    /**
	 * @param view
	 * @return
	 */
	public static Bitmap getBitmap(View view) {
		if (view.getWidth() <= 0) {
			return null;
		}

		Bitmap screenshot;
		screenshot = Bitmap.createBitmap(view.getWidth(), view.getHeight(),
				Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(screenshot);
		view.draw(c);
		return screenshot;
	}
	
	/**
	 * Drawable → Bitmap
	 */
	public static Bitmap drawableToBitmap(Drawable drawable, int width, int height) {
		try {
			if (width <= 0 || height <= 0) {
				width = drawable.getIntrinsicWidth();
				height = drawable.getIntrinsicHeight();
			}
			
			Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
			Canvas canvas = new Canvas(bitmap);
			// canvas.setBitmap(bitmap);
			drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
			float x = (float) width / (float) drawable.getIntrinsicWidth();
			float y = (float) height / (float) drawable.getIntrinsicHeight();
			canvas.scale(x, y);
			drawable.draw(canvas);
			return bitmap;
		} catch (OutOfMemoryError error) {
			Log.w(TAG, "drawableToBitmap", error);
		} catch (Exception e) {
			Log.w(TAG, "drawableToBitmap", e);
		}
		return null;
	}
	
	public static Bitmap convertViewToBitmap(Context context, View view, int width, int height) {
		Bitmap bitmap = null;
		try {
			bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
		} catch (OutOfMemoryError error) {
			error.printStackTrace();
			bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
		}
		final Canvas c = new Canvas(bitmap);
		float x = (float) width / (float) view.getWidth();
		float y = (float) height / (float) view.getHeight();
		c.scale(x, y);
		view.draw(c);
		return bitmap;
	}
	

	/**
	 * 释放图片
	 */
	public static void recycleBitmap(Bitmap bitmap) {
		if (bitmap != null && !bitmap.isRecycled()) {
			bitmap.recycle();
		}
	}
	
	public static Bitmap getBluredBackgroundImage(Launcher launcher) {
		int width = launcher.getResources().getDisplayMetrics().widthPixels;
		int height = launcher.getResources().getDisplayMetrics().heightPixels;
		int statusBarHeight=Util.getStatusBarHeight(launcher);
		if (launcher.isFullScreen()) {
			statusBarHeight=0;
		}
		return getBluredBackgroundImage(launcher, width, height-statusBarHeight);
	}
	
	/**
	 * 获取模糊壁纸背景
	 */
	public static Bitmap getBluredWallpaperImage(Launcher launcher, int outWidth, int outHeight) {
		try {
			Bitmap screenShot = ScreenShot.getWallpaperImage(launcher);
			if (screenShot == null) return null;
			
			Bitmap bluredBitmap = null;
			
			if (Util.getSdkVersionCode() >= 17) {
				bluredBitmap = doBlur(launcher, screenShot, 1, 25);
			}
			
			if (bluredBitmap == null) {
				bluredBitmap = doBlur(screenShot, 16, true);
			}

			if (screenShot != bluredBitmap) {
				recycleBitmap(screenShot);
			}
			return bluredBitmap;
		} catch (OutOfMemoryError error) {
			Log.w(TAG, "getBluredBackgroundImage", error);
		} catch (Exception e) {
			Log.w(TAG, "getBluredBackgroundImage", e);
		}
		return null;
	}

	public static Bitmap getBackgroundImage(Launcher launcher) {
		int width = launcher.getResources().getDisplayMetrics().widthPixels;
		int height = launcher.getResources().getDisplayMetrics().heightPixels;
		int statusBarHeight=Util.getStatusBarHeight(launcher);
		if (launcher.isFullScreen()) {
			statusBarHeight=0;
		}
		return getBackgroundImage(launcher, width, height-statusBarHeight);
	}

	public static Bitmap getBackgroundImage(Launcher launcher, int outWidth, int outHeight){
		return ScreenShot.getScreenShot(launcher, outWidth, outHeight);
	}

	public static Bitmap getBlueIamge(Context context, Bitmap bitmap){
		try {
			Bitmap bluredBitmap = null;

			if (Util.getSdkVersionCode() >= 17) {
				bluredBitmap = doBlur(context, bitmap, 1, 25);
			}

			if (bluredBitmap == null) {
				bluredBitmap = doBlur(bitmap, 16, true);
			}

			if (bitmap != bluredBitmap) {
				recycleBitmap(bitmap);
			}
			return bluredBitmap;
		} catch (OutOfMemoryError error) {
			Log.w(TAG, "getBluredBackgroundImage", error);
		} catch (Exception e) {
			Log.w(TAG, "getBluredBackgroundImage", e);
		}
		return null;
	}

	/**
	 * 获取模糊背景
	 */
	public static Bitmap getBluredBackgroundImage(Launcher launcher, int outWidth, int outHeight) {
		try {
			long time = System.currentTimeMillis();
			Bitmap screenShot = ScreenShot.getScreenShot(launcher, outWidth, outHeight);
			if (screenShot == null) return null;
			
			Bitmap bluredBitmap = null;
            bluredBitmap = StackBlur.blurNatively(screenShot, 5, true);
//			bluredBitmap = StackBlur.blur(screenShot, 5, true);
//			if (Util.getSdkVersionCode() >= 30) {
//				bluredBitmap = doBlur(launcher, screenShot, 1, 7);
//			}
//			if (bluredBitmap == null) {
//				bluredBitmap = doBlur(screenShot, 16, true);
//			}

			if (screenShot != bluredBitmap) {
				recycleBitmap(screenShot);
			}
			return bluredBitmap;
		} catch (OutOfMemoryError error) {
			Log.w(TAG, "getBluredBackgroundImage", error);
		} catch (Exception e) {
			Log.w(TAG, "getBluredBackgroundImage", e);
		}
		return null;
	}
	
	/**
	 * 图片模糊
	 */
	public static Bitmap doBlur(Bitmap sentBitmap, int radius, boolean canReuseInBitmap) {
		try {
			if (sentBitmap == null) return null;
			Bitmap bitmap;
			if (canReuseInBitmap) {
				bitmap = sentBitmap;
			} else {
				bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);
			}

			if (radius < 1) {
				return (null);
			}

			int w = bitmap.getWidth();
			int h = bitmap.getHeight();

			int[] pix = new int[w * h];
			bitmap.getPixels(pix, 0, w, 0, 0, w, h);

			int wm = w - 1;
			int hm = h - 1;
			int wh = w * h;
			int div = radius + radius + 1;

			int r[] = new int[wh];
			int g[] = new int[wh];
			int b[] = new int[wh];
			int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
			int vmin[] = new int[Math.max(w, h)];

			int divsum = (div + 1) >> 1;
			divsum *= divsum;
			int dv[] = new int[256 * divsum];
			for (i = 0; i < 256 * divsum; i++) {
				dv[i] = (i / divsum);
			}

			yw = yi = 0;

			int[][] stack = new int[div][3];
			int stackpointer;
			int stackstart;
			int[] sir;
			int rbs;
			int r1 = radius + 1;
			int routsum, goutsum, boutsum;
			int rinsum, ginsum, binsum;

			for (y = 0; y < h; y++) {
				rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
				for (i = -radius; i <= radius; i++) {
					p = pix[yi + Math.min(wm, Math.max(i, 0))];
					sir = stack[i + radius];
					sir[0] = (p & 0xff0000) >> 16;
					sir[1] = (p & 0x00ff00) >> 8;
					sir[2] = (p & 0x0000ff);
					rbs = r1 - Math.abs(i);
					rsum += sir[0] * rbs;
					gsum += sir[1] * rbs;
					bsum += sir[2] * rbs;
					if (i > 0) {
						rinsum += sir[0];
						ginsum += sir[1];
						binsum += sir[2];
					} else {
						routsum += sir[0];
						goutsum += sir[1];
						boutsum += sir[2];
					}
				}
				stackpointer = radius;

				for (x = 0; x < w; x++) {

					r[yi] = dv[rsum];
					g[yi] = dv[gsum];
					b[yi] = dv[bsum];

					rsum -= routsum;
					gsum -= goutsum;
					bsum -= boutsum;

					stackstart = stackpointer - radius + div;
					sir = stack[stackstart % div];

					routsum -= sir[0];
					goutsum -= sir[1];
					boutsum -= sir[2];

					if (y == 0) {
						vmin[x] = Math.min(x + radius + 1, wm);
					}
					p = pix[yw + vmin[x]];

					sir[0] = (p & 0xff0000) >> 16;
					sir[1] = (p & 0x00ff00) >> 8;
					sir[2] = (p & 0x0000ff);

					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];

					rsum += rinsum;
					gsum += ginsum;
					bsum += binsum;

					stackpointer = (stackpointer + 1) % div;
					sir = stack[(stackpointer) % div];

					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];

					rinsum -= sir[0];
					ginsum -= sir[1];
					binsum -= sir[2];

					yi++;
				}
				yw += w;
			}
			for (x = 0; x < w; x++) {
				rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
				yp = -radius * w;
				for (i = -radius; i <= radius; i++) {
					yi = Math.max(0, yp) + x;

					sir = stack[i + radius];

					sir[0] = r[yi];
					sir[1] = g[yi];
					sir[2] = b[yi];

					rbs = r1 - Math.abs(i);

					rsum += r[yi] * rbs;
					gsum += g[yi] * rbs;
					bsum += b[yi] * rbs;

					if (i > 0) {
						rinsum += sir[0];
						ginsum += sir[1];
						binsum += sir[2];
					} else {
						routsum += sir[0];
						goutsum += sir[1];
						boutsum += sir[2];
					}

					if (i < hm) {
						yp += w;
					}
				}
				yi = x;
				stackpointer = radius;
				for (y = 0; y < h; y++) {
					// Preserve alpha channel: ( 0xff000000 & pix[yi] )
					pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];

					rsum -= routsum;
					gsum -= goutsum;
					bsum -= boutsum;

					stackstart = stackpointer - radius + div;
					sir = stack[stackstart % div];

					routsum -= sir[0];
					goutsum -= sir[1];
					boutsum -= sir[2];

					if (x == 0) {
						vmin[y] = Math.min(y + r1, hm) * w;
					}
					p = x + vmin[y];

					sir[0] = r[p];
					sir[1] = g[p];
					sir[2] = b[p];

					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];

					rsum += rinsum;
					gsum += ginsum;
					bsum += binsum;

					stackpointer = (stackpointer + 1) % div;
					sir = stack[stackpointer];

					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];

					rinsum -= sir[0];
					ginsum -= sir[1];
					binsum -= sir[2];

					yi += w;
				}
			}

			bitmap.setPixels(pix, 0, w, 0, 0, w, h);
			return bitmap;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return sentBitmap;
	}
	
	@SuppressLint("NewApi") 
	public static Bitmap doBlur(Context context, Bitmap bmpIn, float scale, int degree) {
		RenderScript rs = null;
		Allocation inputAllocation = null;
		Allocation outputAllocation = null;
		
		try {
			if (bmpIn == null) return null;
			
			if (scale <= 0.0f) {
				scale = 0.0625f;
			}
			
			if (degree <= 0 || degree > 25) {
				degree = 5;
			}
			
			int width = Math.round(bmpIn.getWidth() * scale);
			int height = Math.round(bmpIn.getHeight() * scale);
			Bitmap.Config config = Bitmap.Config.ARGB_4444;
			Bitmap target = Bitmap.createBitmap(width, height, config);
			Canvas canvas = new Canvas(target);
			canvas.scale(scale, scale);
			Paint paint = new Paint();
			canvas.drawBitmap(bmpIn, 0, 0, paint);
			rs = RenderScript.create(context);
			rs.setMessageHandler(new RSMessageHandler());
			inputAllocation = Allocation.createFromBitmap(rs, target, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);
			outputAllocation = Allocation.createTyped(rs, inputAllocation.getType());
			ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, inputAllocation.getElement());
			script.setInput(inputAllocation);
			script.setRadius(degree);
			script.forEach(outputAllocation);
			
			Bitmap bitmap = Bitmap.createBitmap(width, height, config);
			outputAllocation.copyTo(bitmap);
            recycleBitmap(target);
			return bitmap;
		} catch (OutOfMemoryError error) {
			Log.w(TAG, "doBlur", error);
		} catch (RSIllegalArgumentException e) {
		} catch (Exception e) {
			Log.w(TAG, "doBlur", e);
		} finally {
			try {
				rs.destroy();
				inputAllocation.destroy();
				outputAllocation.destroy();
			} catch (Exception e2) {}
		}
		return null;
	}
	
	public static void setThemeWallpaper(Context c, Bitmap image){
		int wallpaperWidth = 0, wallpaperHeight = 0;

		final int screenWidth = LauncherAppState.getInstance().getScreenWidth();
		final int screenHeight = LauncherAppState.getInstance().getScreenHeight();
		if (image == null)
			image = BitmapFactory.decodeResource(c.getResources(), R.drawable.wallpaper_default);
		final WallpaperManager mWallpaperManager = WallpaperManager.getInstance(c);
		try {
			if (image.getWidth() < image.getHeight()) {
				float xx = (float) screenWidth / image.getWidth();
				float yy = (float) screenHeight / image.getHeight();
				Matrix matrix = new Matrix();
				matrix.postScale(xx, yy); // 长和宽放大缩小的比例
				image = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);
				wallpaperWidth = screenWidth;
				wallpaperHeight = screenHeight;
			} else {
				wallpaperWidth = image.getWidth();
				wallpaperHeight = screenHeight;
				int x = wallpaperWidth / 2 - screenWidth / 2;
				int y = 0;
				image = Bitmap.createBitmap(image, x, y, screenWidth, screenHeight);

			}
			mWallpaperManager.setBitmap(image);

		} catch (Exception e) {
			e.printStackTrace();
			try {
				mWallpaperManager.setResource(R.drawable.wallpaper_default);
			} catch (Exception ex) {
			}
		}			
	}

	//计算适当的缩放比例
	private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// 获得内存中图片的宽高
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// 计算出一个数值，必须符合为2的幂（1，2，4，8，tec），赋值给inSampleSize
			// 图片宽高应大于期望的宽高的时候，才进行计算
			while ((halfHeight / inSampleSize) > reqHeight
					&& (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}

	//从资源文件夹中加载需要宽高的图片
	public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
														 int reqWidth, int reqHeight) {

		// 第一次解析 inJustDecodeBounds=true 只是用来获取bitmap在内存中的尺寸和类型，系统并不会为其分配内存，
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, resId, options);

		// 计算出一个数值
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

		// 根据inSampleSize 数值来解析bitmap
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeResource(res, resId, options);
	}

}
