package com.android.launcher3.bean;

import com.android.launcher3.ShortcutInfo;

import java.util.ArrayList;

/**
 * 所有App界面中的每行显示数据的信息
 */
public class AppSortEntity extends ShortcutInfo {

	public String name;
	public ArrayList<ShortcutInfo> mDatas = new ArrayList<>();
	public boolean isFirst;
}
