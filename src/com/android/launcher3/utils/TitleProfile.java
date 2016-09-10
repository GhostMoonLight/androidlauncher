package com.android.launcher3.utils;

import java.util.HashMap;

import com.cuan.launcher.R;

import android.content.Context;

/**
 * 默认标题配置
 */
public class TitleProfile {

	private static HashMap<String, Integer> titleMappings = new HashMap<String, Integer>();
	private static HashMap<Long, Integer> folderTitleMappings = new HashMap<Long, Integer>();
	
	
	static {
		folderTitleMappings.put(Const.TYPE_FOLDER_SYSTEM, R.string.folder_system);
		folderTitleMappings.put((long) 1, R.string.folder_classify_1);
		folderTitleMappings.put((long) 2, R.string.folder_classify_2);
		folderTitleMappings.put((long) 3, R.string.folder_classify_3);
		folderTitleMappings.put((long) 4, R.string.folder_classify_4);
		folderTitleMappings.put((long) 5, R.string.folder_classify_5);
		folderTitleMappings.put((long) 6, R.string.folder_classify_6);
		folderTitleMappings.put((long) 7, R.string.folder_classify_7);
		folderTitleMappings.put((long) 8, R.string.folder_classify_8);
		folderTitleMappings.put((long) 9, R.string.folder_classify_9);
		folderTitleMappings.put((long) 10, R.string.folder_classify_10);
		folderTitleMappings.put((long) 11, R.string.folder_classify_11);
		folderTitleMappings.put((long) 12, R.string.folder_classify_12);
		folderTitleMappings.put((long) 13, R.string.folder_classify_13);
		folderTitleMappings.put((long) 14, R.string.folder_classify_14);
		folderTitleMappings.put((long) 1609, R.string.folder_classify_1609);
		folderTitleMappings.put((long) 10027, R.string.folder_classify_10027);
	}
	
	public static boolean hasDefaultTitle(Context context, String pkg) {
		return titleMappings.containsKey(pkg);
	}
	
	public static String getDefaultTitle(Context context, String pkg) {
		if (hasDefaultTitle(context, pkg)) {
			return context.getString(titleMappings.get(pkg));
		} else {
			return null;
		}
	}
	
	public static boolean hasDefaultFolderTitle(Context context, long extendId) {
		return folderTitleMappings.containsKey(extendId);
	}
	
	public static String getDefaultFolderTitle(Context context, long extendId) {
		if (hasDefaultFolderTitle(context, extendId)) {
			return context.getString(folderTitleMappings.get(extendId));
		} else {
			return null;
		}
	}
	
	public static int getDefaultFolderTitleId(Context context, long extendId) {
		if (hasDefaultFolderTitle(context, extendId)) {
			return folderTitleMappings.get(extendId);
		} else {
			return -1;
		}
	}
}
