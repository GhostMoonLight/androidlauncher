package com.android.launcher3.appfilter;

import com.android.launcher3.LauncherAppState;
import com.android.launcher3.LauncherApplication;
import com.cuan.launcher.R;

import android.content.ComponentName;
/**
 * 用来隐藏(不显示)指定的应用
 * config.xml中的app_filter_class字段指定过滤的完整类名
 */
public class CustomFilter extends AppFilter {
	
	private String[] appFilterList;
	
	public CustomFilter() {
		appFilterList = LauncherApplication.getInstance().getResources().getStringArray(R.array.hidden_pkg);
	}

	@Override
	public boolean shouldShowApp(ComponentName app) {
		
		if (app != null && isFilter(app)){
			return false;
		}
		
		return true;
	}
	
	private boolean isFilter(ComponentName app) {
		if (appFilterList != null) {
			for (String string : appFilterList) {
				if (app.getPackageName().equals(string) 
						|| (app.getPackageName() + "/" + app.getClassName()).equals(string)
						|| (string.endsWith("*") && app.getPackageName().startsWith(string.substring(0, string.indexOf("*"))))) {
					return true;
				}
			}
		}
		
		return false;
	}

}
