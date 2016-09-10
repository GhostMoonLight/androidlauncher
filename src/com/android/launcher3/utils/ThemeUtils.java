package com.android.launcher3.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;

public class ThemeUtils {
	/**
	 * 静态内部类的单例模式
	 */
	private static class Singleton{
		private static ThemeUtils INSTANCE = new ThemeUtils();
	}
	
	private ThemeUtils(){};
	
	public static ThemeUtils getInstance(){
		return Singleton.INSTANCE;
	}

	/**
	 * 生成主题风格图片
	 */
	public Bitmap createThemeStyleBitmap(Bitmap bitmap, Bitmap bgBitmap) {
		return addThemeBackground(bitmap,bgBitmap);
	}
	/**
	 * 添加主题背景
	 * @param bitmap
	 * @param bgBitmap
	 * @return
	 */
	private Bitmap addThemeBackground(Bitmap bitmap,Bitmap bgBitmap){
		if(bgBitmap==null){
			return bitmap;
		}
		try{
			int width,height;
			width=bgBitmap.getWidth();
			height=bgBitmap.getHeight();
			Bitmap themeIcon = Bitmap.createBitmap(width, height,
					Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas();
			canvas.setBitmap(themeIcon);
			
			BitmapDrawable bgDrawable = new BitmapDrawable(bgBitmap);
			bgDrawable.setBounds(0, 0, bgBitmap.getWidth(), bgBitmap.getHeight());
			bgDrawable.draw(canvas);
			
			BitmapDrawable iconDrawable = new BitmapDrawable(bitmap);
			int x=width/11;
			int y=height/11;
			iconDrawable.setBounds(x, y, bitmap.getWidth()-x, bitmap.getHeight()-y);
			iconDrawable.draw(canvas);
			
			
			
			return themeIcon;
		}catch(Exception e){
			e.printStackTrace();
		}
		return bitmap;
		
	}
}
