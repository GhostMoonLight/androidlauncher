package com.android.launcher3.utils;

import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingInfo {
	private static final String TAG = "SettingInfo";
	public static final String PREF_NAME = "com.cuan.launcher.prefs";
	private SharedPreferences mPreferences;
	private static SettingInfo mInstance;
	private Context mContext;
	
	private static final String PREF_SCREEN_ROUND = "screenRound";    //屏幕循环

	private SettingInfo(Context context) {
		mContext = context.getApplicationContext();
		mPreferences = mContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
	}
	
	public synchronized static SettingInfo getInstance(Context context) {
		if(mInstance == null) {
			mInstance = new SettingInfo(context);
		}
		return mInstance;
	}
	
	public void setScreenRound(boolean round) {
		putBoolean(PREF_SCREEN_ROUND, round);
	}
	
	public boolean getScreenRount() {
		boolean screenRound = getBoolean(PREF_SCREEN_ROUND, false);
		return screenRound;
	}
	
	public void putInt(String key, int value) {
		SharedPreferences.Editor sEditor = mPreferences.edit();
		sEditor.putInt(key, value);
		sEditor.commit();
	}
	
	public int getInt(String key, int defValue) {
		int value = mPreferences.getInt(key, defValue);
		return value;
	}
	
	public void putLong(String key, long value) {
		SharedPreferences.Editor sEditor = mPreferences.edit();
		sEditor.putLong(key, value);
		sEditor.commit();
	}
	
	public long getLong(String key, long defValue) {
		long value = mPreferences.getLong(key, defValue);
		return value;
	}
	
	public void putString(String key, String value) {
		SharedPreferences.Editor sEditor = mPreferences.edit();
		sEditor.putString(key, value);
		sEditor.commit();
	}
	
	public String getString(String key, String defValue) {
		String value = mPreferences.getString(key, defValue);
		return value;
	}
	
	public void putBoolean(String key, boolean value) {
		SharedPreferences.Editor sEditor = mPreferences.edit();
		sEditor.putBoolean(key, value);
		sEditor.commit();
	}
	
	public boolean getBoolean(String key, boolean defValue) {
		boolean value = mPreferences.getBoolean(key, defValue);
		return value;
	}
	
	public void putStringSet(String key, Set<String> values) {
		SharedPreferences.Editor sEditor = mPreferences.edit();
		sEditor.putStringSet(key, values);
		sEditor.commit();
	}
	
	public Set<String> getStringSet(String key, Set<String> defValues) {
		return mPreferences.getStringSet(key, defValues);
	}
}
