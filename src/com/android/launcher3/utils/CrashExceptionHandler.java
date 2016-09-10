package com.android.launcher3.utils;

import java.lang.Thread.UncaughtExceptionHandler;

import com.cuan.launcher.R;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Looper;
import android.widget.Toast;

/**
 * 捕获UncaughtException
 */
public class CrashExceptionHandler implements UncaughtExceptionHandler {
	private static String TAG = "CrashExceptionHandler";
    private Thread.UncaughtExceptionHandler mDefaultHandler;  
    private static CrashExceptionHandler singletonInstance = new CrashExceptionHandler();  
    private Context mContext;
	
	private CrashExceptionHandler() {}

	public static CrashExceptionHandler getInstance() {
        return singletonInstance;  
    }  
    
	public void init(Context context) {
		mContext = context;
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);  
	}
	
	@Override
	public void uncaughtException(Thread thread, Throwable throwable) {
		try {
			
			if (!handleException(throwable) && mDefaultHandler != null) {
				// 如果用户没有处理则让系统默认的异常处理器来处理
				mDefaultHandler.uncaughtException(thread, throwable);
			} else {
				// Sleep一会后结束程序
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					Log.e(TAG, e.toString());
				}
				Util.restartApplication();
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	private boolean handleException(Throwable throwable) {
		if (throwable == null) {
			return true;
		}
		Log.e(TAG, collectCrashDeviceInfo(), throwable);
		
		if(true/*Const.DEBUG*/){
		    //使用Toast来显示异常信息  
	        new Thread() {  
	            @Override  
	            public void run() {
	            	try {
		                Looper.prepare();
		                Toast.makeText(mContext, R.string.crash_exception, Toast.LENGTH_LONG).show();  
		                Looper.loop();
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
					}
	            }  
	        }.start();  
		}
		return true;
	}
	
	
	/**
	 * 收集设备信息
	 */
	public String collectCrashDeviceInfo() {
		StringBuilder builder = new StringBuilder();
		String appVersion = getAppVersion();
		builder.append("VERSION=").append(appVersion).append('\n');
		
		try {
			
			builder.append("SDK=").append(Util.getSdkVersion()).append('\n');
			builder.append("Modle=").append(Util.getDeviceName()).append('\n');
			builder.append("Release=").append(android.os.Build.VERSION.RELEASE).append('\n');
			builder.append("Display=").append(android.os.Build.DISPLAY).append('\n');
			builder.append("Fingerprint=").append(android.os.Build.FINGERPRINT).append('\n');
			builder.append("Manufacturer=").append(android.os.Build.MANUFACTURER).append('\n');
			builder.append("Type=").append(android.os.Build.TYPE).append('\n');
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		return builder.toString();
	}	 
	
	private String getAppVersion() {
		try {
			PackageManager manager = mContext.getPackageManager();
			PackageInfo info = manager.getPackageInfo(mContext.getPackageName(), 0);
			String sVersion = String.valueOf(info.versionName);
			return sVersion;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "1.0";
	}

}
