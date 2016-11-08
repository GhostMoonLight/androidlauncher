/*
 * Copyright (C) 2013 The Android Open Source Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.android.launcher3;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import com.android.launcher3.utils.CrashExceptionHandler;
import com.android.launcher3.utils.Util;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import java.io.File;

/**
 * 每个进程都会对应一个application实例
 */
public class LauncherApplication extends Application {

    private final static String TAG = "LauncherApplication";
    private static LauncherApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.w(TAG, "LauncherApplication#onCreate");
        instance = this;
		String procressName = getCurProcessName(this);
		if ("com.cuan.launcher".equals(procressName)) {
			LauncherAppState.setApplicationContext(this);
			LauncherAppState.getInstance();
			CrashExceptionHandler.getInstance().init(getApplicationContext());
			initImageLoader(this);
		}
    }
    
    public static LauncherApplication getInstance(){
    	return instance;
    }
    
    /**
	 * 初始化ImageLoader
	 */
	private void initImageLoader(Context context) {
		// This configuration tuning is custom. You can tune every option, you may tune some of them,
		// or you can create default configuration by
		// ImageLoaderConfiguration.createDefault(this);
		// method.
		DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
				.cacheInMemory(true)
				.cacheOnDisk(true)
				.build();
		
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
				.threadPriority(Thread.NORM_PRIORITY - 2)
				.denyCacheImageMultipleSizesInMemory()
				.diskCacheFileNameGenerator(new Md5FileNameGenerator())
				.diskCacheSize(50 * 1024 * 1024) // 50 Mb
				.tasksProcessingOrder(QueueProcessingType.LIFO)
				.defaultDisplayImageOptions(defaultOptions)
				.writeDebugLogs() // Remove for release app
				.build();
		// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(config);
	}

	//当系统内存不足的时候，会调用该方法，完成对运行环境的清理工作
    @Override
    public void onTerminate() {
        super.onTerminate();
        LauncherAppState.getInstance().onTerminate();
    }
    
    public Handler getHandler() {
		return mHandler;
	}
	
	Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			
		};
	};
	
	/**
	 * log存储
	 */
	public String getLogCacheDir() {
		String strCacheDir;
		File cacheDir;
		File cacheFile;
		
		if (Util.isSDCardAvailable()) {
			cacheDir = getExternalCacheDir();
			
			if (cacheDir == null) {
				cacheDir = new File(Environment.getExternalStorageDirectory().toString() + "/Android/data/" + getPackageName() + "/cache");
				cacheDir.mkdirs();
			}
		} else {
			cacheDir = getCacheDir();
			
		}
		cacheFile = new File(cacheDir, "log");
		cacheFile.mkdirs();
		strCacheDir = cacheFile.getAbsolutePath();
		return strCacheDir;
	}

	/**
	 * urk数据存储
	 */
	public String getUrlCacheDir() {
		String strCacheDir;
		File cacheDir;
		File cacheFile;

		if (Util.isSDCardAvailable()) {
			cacheDir = getExternalCacheDir();

			if (cacheDir == null) {
				cacheDir = new File(Environment.getExternalStorageDirectory().toString() + "/Android/data/" + getPackageName() + "/url");
				cacheDir.mkdirs();
			}
		} else {
			cacheDir = getCacheDir();

		}
		cacheFile = new File(cacheDir, "url");
		cacheFile.mkdirs();
		strCacheDir = cacheFile.getAbsolutePath();
		return strCacheDir;
	}

	private static String getCurProcessName(Context context) {
		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context. ACTIVITY_SERVICE);
		for (ActivityManager.RunningAppProcessInfo appProcess : activityManager.getRunningAppProcesses()) {
			if ( appProcess. pid == android.os.Process. myPid()) {
				return appProcess. processName;
			}
		}
		return "";
	}
}
