package com.android.launcher3.utils;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.android.launcher3.LauncherAppState;
import com.android.launcher3.ShortcutInfo;
import com.cuan.launcher.R;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.util.DisplayMetrics;

public class PackageUtil {
	
	private final static String TAG = "PackageUtil";
	
	/**
	 * 卸载应用
	 */
	public static void uninstallApp(Context context, String packageName) {
		LauncherAppState.getInstance().getLauncher().exitFullScreen();
		try {
			if (isInstalledApk(context, packageName)) {
				Uri packageUri = Uri.parse("package:" + packageName);
				Intent intent = new Intent(Intent.ACTION_DELETE, packageUri);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	/**
	 * 判断是否本机已安装了packageName指定的应用
	 * @param packageName
	 * @return
	 */
	public static boolean isInstalledApk(Context context, String packageName) {
		try {
			if (TextUtils.isEmpty(packageName)) {
				return false;
			}

			PackageInfo sPackageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
			
			if(sPackageInfo != null && sPackageInfo.applicationInfo.enabled) {
				return true;
			} else {
				return false;
			}
		} catch (NameNotFoundException e) {
		} catch (Exception e) {
		}
		return false;
	}
	
	/**
	 * 是否可以卸载（有的系统应用可以卸载，恢复到出厂时该应用的状态）
	 * @param context
	 * @param info
	 * @return
	 */
	public static boolean canUninstall(Context context, ShortcutInfo info){
		try {
			LogUtil.d(TAG, " isSystemApp："+info.getPckName()+" "+isSystemApp(context, info));
			if (isSystemApp(context, info)) {
				LogUtil.d(TAG, " isSystemUpdateApp："+info.getPckName()+" "+isSystemUpdateApp(context, info));
				if (isSystemUpdateApp(context, info)) {
					return true;
				}
				
				PackageManager sPackageManager = context.getPackageManager();
				ApplicationInfo appInfo = sPackageManager.getApplicationInfo(info.getPckName(), 0);
				if (appInfo == null) return false;

				if (appInfo.sourceDir != null && (appInfo.sourceDir.contains("/system/delapp") || appInfo.sourceDir.startsWith("/data/"))) {
					return true;
				} else {
					return false;
				}
			} else {
				return true;
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return true;
		}
	}
	
	/**
	 * 是否是系统分区应用
	 */
	public static boolean isSystemApp(Context context, ShortcutInfo info) {
		try {
			if ((info.flags & ApplicationInfo.FLAG_SYSTEM) > 0 ||
					(info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) > 0) {
				return true;
			} else {
				PackageManager pm = context.getPackageManager();
				PackageInfo packageInfo = pm.getPackageInfo(info.getPckName(), 0);
				
				if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0 ||
						(packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) > 0) {
					return true;
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		return false;
	}
	
	/**
	 * 是否是系统分区升级应用
	 */
	public static boolean isSystemUpdateApp(Context context, ShortcutInfo info) {
		try {
			if ((info.flags & ApplicationInfo.FLAG_SYSTEM) > 0 &&
					(info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) > 0) {
				return true;
			} else {
				PackageManager pm = context.getPackageManager();
				PackageInfo packageInfo = pm.getPackageInfo(info.getPckName(), 0);
				
				if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0 &&
						(packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) > 0) {
					return true;
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		return false;
	}
	
	/**
	 * 是否可以卸载（有的系统应用可以卸载，恢复到出厂时该应用的状态）
	 * @param context
	 * @param info
	 * @return
	 */
	public static boolean canUninstall(Context context, String pck){
		try {
			if (isSystemApp(context, pck)) {
				if (isSystemUpdateApp(context, pck)) {
					return true;
				}
				
				PackageManager sPackageManager = context.getPackageManager();
				ApplicationInfo appInfo = sPackageManager.getApplicationInfo(pck, 0);
				if (appInfo == null) return false;

				if (appInfo.sourceDir != null && (appInfo.sourceDir.contains("/system/delapp") || appInfo.sourceDir.startsWith("/data/"))) {
					return true;
				} else {
					return false;
				}
			} else {
				return true;
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return true;
		}
	}
	
	/**
	 * 是否是系统分区应用
	 */
	public static boolean isSystemApp(Context context, String pck) {
		try {
			PackageManager pm = context.getPackageManager();
			PackageInfo packageInfo = pm.getPackageInfo(pck, 0);
			
			if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0 ||
					(packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) > 0) {
				return true;
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		return false;
	}
	
	/**
	 * 是否是系统分区升级应用
	 */
	public static boolean isSystemUpdateApp(Context context, String pck) {
		try {
			PackageManager pm = context.getPackageManager();
			PackageInfo packageInfo = pm.getPackageInfo(pck, 0);
			
			if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0 &&
					(packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) > 0) {
				return true;
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		return false;
	}
	
	/**
	 * 是否是系统分区应用
	 */
	public static boolean isSystemApp(Context context, ApplicationInfo applicationInfo) {
		try {
			if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0 ||
					(applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) > 0) {
				return true;
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		return false;
	}
	
	/**
	 * 是否是系统签名应用
	 */
	public static boolean isSystemSignatureApp(Context context, ArrayList<String> systemSignatureCompony, String packageName) {
		boolean isSystemApp = false;

		try {
			if (systemSignatureCompony != null) {
				PackageManager pm = context.getPackageManager();
				PackageInfo packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
				
				if (PackageUtil.isSystemApp(context, packageInfo.applicationInfo)) {
					Signature currApkSign = SignatureUtil.getApkSignatureWithPackageInfo(context, packageInfo);
					String strSignatureCompony = SignatureUtil.getSignatureCompony(currApkSign);
					LogUtil.d(TAG, "pkg=" + packageName + ", compony-->" + strSignatureCompony);
					
					if (systemSignatureCompony.contains(strSignatureCompony) || 
							Util.isArrayContainsString(strSignatureCompony, context.getResources().getStringArray(R.array.signature_compony), false)) {
						isSystemApp = true;
					}
				}
			}
		} catch (NameNotFoundException e) {
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		return isSystemApp;
	}
	/**
	 * 获取某个应用的启动界面Intent
	 */
	public static Intent getLaunchIntentForPackage(Context context, String packageName) {
		try {
			if (isInstalledApk(context, packageName)) {
				PackageManager packageManager = context.getPackageManager();
				Intent intent = packageManager.getLaunchIntentForPackage(packageName);
				return intent;
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		return null;
	}
	
	static public class AppSnippet {
		public CharSequence label;
        public Drawable icon;
        public String packageName;
        public String versionName;
		int versionCode;
    }
	
	/**
	 * 获取安装包的App信息
	 * @param context
	 * @param apkPath
	 * @return
	 */
	public static AppSnippet getAppSnippet(Context context, String apkPath) {
		AppSnippet sAppSnippet = null;
		
		try {
			PackageManager pm = context.getPackageManager();
			PackageInfo packageInfo = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
//			Resources sResources = getUninstalledApkResources(context, apkPath);
			
			// 读取apk文件的信息
			if (packageInfo != null) {
				sAppSnippet = new AppSnippet();
//				sAppSnippet.icon = sResources.getDrawable(packageInfo.applicationInfo.icon);// 图标
				ApplicationInfo appInfo = packageInfo.applicationInfo;
	            appInfo.sourceDir = apkPath;
	            appInfo.publicSourceDir = apkPath;
				sAppSnippet.icon = appInfo.loadIcon(pm);
				sAppSnippet.packageName = packageInfo.packageName;		// 包名
				sAppSnippet.versionName = packageInfo.versionName;		// 版本号
				sAppSnippet.versionCode = packageInfo.versionCode;		// 版本码
//				sAppSnippet.permissions = packageInfo.requestedPermissions;
				
				try {
//					sAppSnippet.label = (String) sResources.getText(packageInfo.applicationInfo.labelRes);// 名字
					sAppSnippet.label = appInfo.loadLabel(pm);
				} catch (Exception e) {
					e.printStackTrace();
					try {
						sAppSnippet.label = pm.getApplicationLabel(packageInfo.applicationInfo);
					} catch (Exception e2) {
						e2.printStackTrace();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sAppSnippet;
	}
	
	/**
	 * 获取安装包的Resources
	 * @param context
	 * @param apkPath
	 * @return
	 */
	public static Resources getUninstalledApkResources(Context context, String apkPath) {
		if (TextUtils.isEmpty(apkPath)) {
			return context.getResources();
		}
		
		File apkFile = new File(apkPath);
		if (!apkFile.exists()) {
			return context.getResources();
		}

		// AppInfoData appInfoData;
		String PATH_AssetManager = "android.content.res.AssetManager";
		
		try {
			// 反射得到pkgParserCls对象并实例化,有参数
			Class<?>[] typeArgs = { String.class };
			Object[] valueArgs = { apkPath };

			// 反射得到assetMagCls对象并实例化,无参
			Class<?> assetMagCls = Class.forName(PATH_AssetManager);
			Object assetMag = assetMagCls.newInstance();
			// 从assetMagCls类得到addAssetPath方法
			typeArgs = new Class[1];
			typeArgs[0] = String.class;
			Method assetMag_addAssetPathMtd = assetMagCls.getDeclaredMethod("addAssetPath", typeArgs);
			valueArgs = new Object[1];
			valueArgs[0] = apkPath;
			// 执行assetMag_addAssetPathMtd方法
			assetMag_addAssetPathMtd.invoke(assetMag, valueArgs);

			// 得到Resources对象并实例化,有参数
			Resources res = context.getResources();
			typeArgs = new Class[3];
			typeArgs[0] = assetMag.getClass();
			typeArgs[1] = res.getDisplayMetrics().getClass();
			typeArgs[2] = res.getConfiguration().getClass();
			Constructor<Resources> resCt = Resources.class.getConstructor(typeArgs);
			valueArgs = new Object[3];
			valueArgs[0] = assetMag;
			valueArgs[1] = res.getDisplayMetrics();
			valueArgs[2] = res.getConfiguration();
			
			res = (Resources) resCt.newInstance(valueArgs);
			return res;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return context.getResources();
	}
	
	/**
	 * 获取安装包的ApplicationInfo
	 * @param context
	 * @param apkPath
	 * @return
	 */
	public static ApplicationInfo getUninstalledApkInfo(Context context, String apkPath) {
		try {
			// AppInfoData appInfoData;
			String PATH_PackageParser = "android.content.pm.PackageParser";

			// 反射得到pkgParserCls对象并实例化,有参数
			Class<?> pkgParserCls = Class.forName(PATH_PackageParser);
			Class<?>[] typeArgs = { String.class };
			Constructor<?> pkgParserCt = pkgParserCls.getConstructor(typeArgs);
			Object[] valueArgs = { apkPath };
			Object pkgParser = pkgParserCt.newInstance(valueArgs);

			// 从pkgParserCls类得到parsePackage方法
			DisplayMetrics metrics = new DisplayMetrics();
			metrics.setToDefaults();// 这个是与显示有关的, 这边使用默认
			typeArgs = new Class<?>[] { File.class, String.class, DisplayMetrics.class, int.class };
			Method pkgParser_parsePackageMtd = pkgParserCls.getDeclaredMethod("parsePackage", typeArgs);

			valueArgs = new Object[] { new File(apkPath), apkPath, metrics, 0 };

			// 执行pkgParser_parsePackageMtd方法并返回
			Object pkgParserPkg = pkgParser_parsePackageMtd.invoke(pkgParser, valueArgs);

			// 从返回的对象得到名为"applicationInfo"的字段对象
			Field appInfoFld = pkgParserPkg.getClass().getDeclaredField("applicationInfo");

			// 从对象"pkgParserPkg"得到字段"appInfoFld"的值
			ApplicationInfo info = (ApplicationInfo) appInfoFld.get(pkgParserPkg);
			return info;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	//获取安装的桌面应用
	public static ArrayList<String> getLauncherApps(Context context){
		ArrayList<String> apps = new ArrayList<String>();
		
		PackageManager sPackageManager = context.getPackageManager();
		Intent homeIntent = new Intent(Intent.ACTION_MAIN);
		homeIntent.addCategory(Intent.CATEGORY_HOME);
		homeIntent.addCategory(Intent.CATEGORY_DEFAULT);
		List<ResolveInfo> sResolveInfos = sPackageManager.queryIntentActivities(homeIntent, 0);
		if (sResolveInfos != null && sResolveInfos.size() > 0) {
			for (ResolveInfo resolveInfo : sResolveInfos) {
				if (!apps.contains(resolveInfo.activityInfo.packageName)) {
					apps.add(resolveInfo.activityInfo.packageName);
				}
			}
		}
		return apps;
	}
}
