package com.android.launcher3.plugin;

import java.lang.ref.WeakReference;
import java.util.Map;

import dalvik.system.DexClassLoader;
import android.content.Context;
import android.util.ArrayMap;

public class PluginUtil {

	/**
	 * 动态加载apk包
	 * @param context
	 * @param apkPath     apk路径
	 * @param outDir      dex输出目录
	 */
	public static void initPlugin(Context context, String apkPath, String outDir){
		try {
			Object currentActivityThread = ReflexUtil.executeMethodReflect("android.app.ActivityThread",  "currentActivityThread", new Class[] {}, new Object[] {});
			//ArrayMap<String, WeakReference<LoadedApk>>
			Map<?, ?> mPackages = (Map<?, ?>) ReflexUtil.getField("android.app.ActivityThread", "mPackages", currentActivityThread);
			WeakReference<?> weakRef = (WeakReference<?>) mPackages.get(context.getPackageName());
			ClassLoader mClassLoader = (ClassLoader) ReflexUtil.getField("android.app.LoadedApk", "mClassLoader", weakRef.get());
			DexClassLoader dexClassLoader = new DexClassLoader(apkPath, outDir, null, mClassLoader);
			ReflexUtil.setFieldValue("android.app.LoadedApk", "mClassLoader", weakRef.get(), dexClassLoader);
		} catch (Exception e) {
		    e.printStackTrace();
		}
	}

}
