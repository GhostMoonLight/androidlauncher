package com.android.launcher3.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import com.android.launcher3.LauncherProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
/**
 * 获取Assert目录下面packages.db数据库中数据的工具类
 * 这个数据库中保存的各个原生系统中自带的应用比如电话，通讯录，信息等系统软件在
 * 各个系统厂商定制的系统中对应应用的包名
 * @author cuangengxuan
 *
 */
public class PackageDbUtil {
	private static final String TAG = "PackageDbUtil";
	private static String DB_PATH = "/data/data/%s/databases";
	public static final String DATABASE_NAME = "packages.db";
	private static final String TABLE_NAME = "packages";
	
	private static final String COLUMN_PACKAGE = "package";
	private static final String COLUMN_CLASS = "activity";
	private static final String COLUMN_TARGET = "target";
	private static final String COLUMN_SYSTEM = "system";
	private static final String COLUMN_PRIOPRITY = "priority";
	
	private Context mContext;
	private static PackageDbUtil mInstance;
	private SQLiteDatabase db = null;
	private ArrayList<String> systemCompony;
	
	private PackageDbUtil(Context context){
		mContext = context;
		systemCompony = SignatureUtil.getSystemSignatureCompony(mContext);
	}
	
	public static synchronized PackageDbUtil getInstance(Context context){
		if (mInstance == null) {
			mInstance = new PackageDbUtil(context);
		}
		return mInstance;
	}
	
	/**
	 * 根据包名，获取系统应用的intent
	 * @param pkg
	 * @param filterApps  需要过滤掉的应用
	 * @return
	 */
	public Intent getIntentWithPkg(String pkg, String filterApps) {
		if (TextUtils.isEmpty(pkg)) return null;
		Intent sIntent = getIntentWithPkg(pkg, filterApps, true);
		return sIntent;
	}

	private Intent getIntentWithPkg(String pkg, String filterApps, boolean systemApp) {
		if (TextUtils.isEmpty(pkg)) return null;
		PackageManager packageManager = mContext.getPackageManager();
		ResolveInfo resolveInfo = null;
		Intent sIntent = null;
		
		try {
			if ("com.android.settings".equals(pkg)) {
				sIntent = new Intent().setAction(Settings.ACTION_SETTINGS);
				List<ResolveInfo> list = packageManager.queryIntentActivities(sIntent, 0);
				boolean contains = false;
				String pkgName = null;
				String clzName = null;
				
				for (int j = 0; j < list.size(); j++) {
					resolveInfo = list.get(j);
					pkgName = resolveInfo.activityInfo.packageName;
					if ((!PackageUtil.isSystemApp(mContext, resolveInfo.activityInfo.applicationInfo)
							|| (filterApps != null && filterApps.contains(pkgName)))) {
						continue;
					}

					contains = true;
					break;
				}

				if (!contains && list.size() > 0) {
					resolveInfo = list.get(0);
					pkgName = resolveInfo.activityInfo.packageName;
					contains = true;
				}

				if (contains && pkgName != null) {
					clzName = resolveInfo.activityInfo.name;
					pkgName = resolveInfo.activityInfo.packageName;
					sIntent = new Intent();
					sIntent.setComponent(new ComponentName(pkgName, clzName));
				}
			} else {
				ArrayList<PackageDbInfo> listDB = getPackages(pkg);
				
				for (PackageDbInfo packageDbInfo : listDB) {
					if (TextUtils.isEmpty(packageDbInfo.className)) {
						sIntent = packageManager.getLaunchIntentForPackage(packageDbInfo.packageName);
						if (sIntent != null && !isFilterApps(sIntent.getPackage(), filterApps)) break;
					} else {
						sIntent = new Intent();
						sIntent.setAction(Intent.ACTION_MAIN);
						sIntent.addCategory(Intent.CATEGORY_LAUNCHER);
						sIntent.setComponent(new ComponentName(packageDbInfo.packageName, packageDbInfo.className));
						resolveInfo = packageManager.resolveActivity(sIntent, 0);
						if (resolveInfo != null && !isFilterApps(sIntent.getPackage(), filterApps)) {
							if (resolveInfo.activityInfo.exported || (resolveInfo.filter != null && resolveInfo.filter.hasAction(Intent.ACTION_MAIN))) 
								break;
						}
					}
//					sIntent = packageManager.getLaunchIntentForPackage(packageDbInfo.packageName);
//					if (sIntent != null && !isFilterApps(sIntent.getPackage(), filterApps)) break;
					
		 			sIntent = null;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (sIntent == null && !Const.PACKAGE_PHONE.equals(pkg) && !Const.PACKAGE_CONTACT.equals(pkg) && !Const.PACKAGE_SMS.equals(pkg)) {
			sIntent = packageManager.getLaunchIntentForPackage(pkg);
		}
		
		if (sIntent == null) {
			sIntent = getIntentWithAction(pkg, filterApps, systemApp);
		}
		
		return sIntent;
	}
	
	private Intent getIntentWithAction(String pkg, String filterApps, boolean systemApp) {
		HashMap<String, Intent> maps = new HashMap<String, Intent>();
		maps.put("com.cooliris.media", new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI));
		maps.put("com.android.camera", new Intent(MediaStore.ACTION_IMAGE_CAPTURE));
		maps.put("com.android.music", new Intent(MediaStore.INTENT_ACTION_MUSIC_PLAYER));
		maps.put("com.android.settings", new Intent().setAction(Settings.ACTION_SETTINGS));
		maps.put("com.android.phone", new Intent(Intent.ACTION_DIAL, Uri.parse("tel:")));
		maps.put("com.android.contacts", new Intent(Intent.ACTION_VIEW).setType("vnd.android.cursor.dir/contact"));
		maps.put("com.android.mms", new Intent(Intent.ACTION_MAIN).setType("vnd.android-dir/mms-sms"));
		maps.put("com.android.browser", new Intent(Intent.ACTION_VIEW).setData(Uri.parse("http://www.mycheering.com")));
		maps.put("com.android.video", new Intent(Intent.ACTION_VIEW).setType("video/*"));
		
		if (maps.containsKey(pkg)) {
			return getIntentWithAction(pkg, maps.get(pkg), filterApps, systemApp);
		}
		return null;
	}
	
	private Intent getIntentWithAction(String targetPkg, Intent intent, String filterApps, boolean systemApp) {
		try {
			PackageManager packageManager = mContext.getPackageManager();
			List<ResolveInfo> list = packageManager.queryIntentActivities(intent, 0);
			ResolveInfo sResolveInfo = null;
			String pkgName = null;
			String clzName = null;
			boolean contains = false;
			
			for (int j = 0; j < list.size(); j++) {
				sResolveInfo = list.get(j);
				
				if (PackageUtil.isSystemApp(mContext, sResolveInfo.activityInfo.applicationInfo) && !isFilterApps(sResolveInfo.activityInfo.packageName, filterApps)) {
					pkgName = sResolveInfo.activityInfo.packageName;
					
					Intent tempIntent = new Intent();
					tempIntent.setComponent(new ComponentName(pkgName, sResolveInfo.activityInfo.name));
					
					if (sResolveInfo.activityInfo.exported || (sResolveInfo.filter != null && sResolveInfo.filter.hasAction(Intent.ACTION_MAIN))) {
						if (systemApp) {
							if (PackageUtil.isSystemSignatureApp(mContext, systemCompony, pkgName)) {
								contains = true;
								break;	
							}
						} else {
							contains = true;
							break;
						}
					}
				}
			}

			if (!contains && list.size() > 0) {
				sResolveInfo = list.get(0);
				pkgName = sResolveInfo.activityInfo.packageName;
				
				if (systemApp) {
					if (PackageUtil.isSystemApp(mContext, sResolveInfo.activityInfo.applicationInfo) &&
						PackageUtil.isSystemSignatureApp(mContext, systemCompony, pkgName)) {
						contains = true;
					}
				} else { 
					contains = true;
				}
			}

			if (contains && pkgName != null) {
				clzName = sResolveInfo.activityInfo.name;
				pkgName = sResolveInfo.activityInfo.packageName;
				
				Intent sIntent = new Intent();
				sIntent.setAction(Intent.ACTION_MAIN);
				sIntent.addCategory(Intent.CATEGORY_LAUNCHER);
				sIntent.setComponent(new ComponentName(pkgName, clzName));
				return sIntent;
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return null;
	}
	
	private ArrayList<PackageDbInfo> getPackages(String target) {
		Cursor sCursor = null;
		ArrayList<PackageDbInfo> list = new ArrayList<PackageDbInfo>();
		if (TextUtils.isEmpty(target)) return list;
		
		try {
			String selection = COLUMN_TARGET + " = ? ";
			String[] selectionArgs = { target };
			String orderBy = COLUMN_PRIOPRITY + " desc ";
			
			SQLiteDatabase sqLiteDatabase = getDatabase(DATABASE_NAME);
			sCursor = sqLiteDatabase.query(TABLE_NAME, null, selection, selectionArgs, null, null, orderBy);
			PackageDbInfo sInfo;
			String strDeviceName = Util.getDeviceName().toLowerCase(Locale.getDefault());
			String strCompanyName = Util.getCompany().toLowerCase(Locale.getDefault());
			int sdkVersionCode = Util.getSdkVersionCode();
			
			while (sCursor.moveToNext()) {
 				sInfo = new PackageDbInfo();
				sInfo.parser(sCursor);
				if (PackageUtil.isInstalledApk(mContext, sInfo.packageName) && 
						(TextUtils.isEmpty(sInfo.system) ||
								strDeviceName.contains(sInfo.system) ||
								strCompanyName.contains(sInfo.system) ||
								(Util.isNumeric(sInfo.system) && sdkVersionCode >= Integer.valueOf(sInfo.system)))) {

					list.add(sInfo);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			LauncherProvider.closeCursor(sCursor);
			closeDB();
		}
		
		return list;
	}
	
	public String getTargetPackages(String packageName) {
		Cursor sCursor = null;
		String pck = "";
		if (TextUtils.isEmpty(packageName)) return pck;
		
		try {
			String selection = COLUMN_PACKAGE + " = ? ";
			String[] selectionArgs = { packageName };
			
			SQLiteDatabase sqLiteDatabase = getDatabase(DATABASE_NAME);
			sCursor = sqLiteDatabase.query(TABLE_NAME, null, selection, selectionArgs, null, null, null);
			PackageDbInfo sInfo;
			
			while (sCursor.moveToNext()) {
 				sInfo = new PackageDbInfo();
				sInfo.parser(sCursor);
				pck = sInfo.target;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			LauncherProvider.closeCursor(sCursor);
			closeDB();
		}
		
		return pck;
	}
	
	private boolean isFilterApps(String pkg, String filterApps) {
		if (pkg != null && filterApps != null && filterApps.contains(pkg)) return true;
		return false;
	}
	
	public SQLiteDatabase getDatabase(String dbName) {
		if (db != null){
			return db;
		}
		
		if (null == mContext) return null;
		String dbPath = getDatabaseFilePath();
		String dbFile = getDatabaseFile(dbName);

		File file = new File(dbFile);
		SharedPreferences dbsp = mContext.getSharedPreferences(PackageUtil.class.toString(), Context.MODE_PRIVATE);

		boolean flag = dbsp.getBoolean(dbName, false);
		if (!flag || !file.exists()) {
			file = new File(dbPath);
			if (!file.exists() && !file.mkdirs()) {
				Log.i(TAG, "Create \"" + dbPath + "\" failed!");
				return null;
			}
			if (!copyDbToFilesystem(dbName, dbFile)) {
				Log.i(TAG, String.format("Copy %s to %s failed!", dbName, dbFile));
				return null;
			}
			dbsp.edit().putBoolean(dbName, true).commit();
		}
		db = SQLiteDatabase.openDatabase(dbFile, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
		return db;
	}
	
	private String getDatabaseFilePath() {
		return String.format(DB_PATH, mContext.getApplicationInfo().packageName);
	}

	private String getDatabaseFile(String dbName) {
		return getDatabaseFilePath() + "/" + dbName;
	}
	
	private boolean copyDbToFilesystem(String dbSrc, String dbDes) {
		Log.i(TAG, "Copy " + dbSrc + " to " + dbDes);
		InputStream inStream = null;
		OutputStream outStream = null;

		AssetManager am = mContext.getAssets();
		try {
			inStream = am.open(dbSrc);
			byte[] buffer = new byte[1024];
			int count;

			outStream = new FileOutputStream(dbDes);
			while ((count = inStream.read(buffer)) != -1) {
				outStream.write(buffer, 0, count);
			}
			outStream.close();

		} catch (IOException e) {
			e.printStackTrace();
			try {
				if (outStream != null) {
					outStream.close();
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return false;
		}
		return true;
	}
	
	public void closeDB(){
		if (db != null){
			db.close();
			db = null;
		}
	}

	public class PackageDbInfo {
		public String packageName;
		public String className;
		public String target;
		public String system;
		public int prioprity;
		
		public PackageDbInfo parser(Cursor cursor) {
			packageName = cursor.getString(cursor.getColumnIndex(COLUMN_PACKAGE));
			className = cursor.getString(cursor.getColumnIndex(COLUMN_CLASS));
			
			if (!TextUtils.isEmpty(className)) {
				if (className.startsWith("#")) {
					className = className.substring(1);
				} else {
					className = packageName + "." + className;
				}
			}
			
			target = cursor.getString(cursor.getColumnIndex(COLUMN_TARGET));
			system = cursor.getString(cursor.getColumnIndex(COLUMN_SYSTEM));
			prioprity = cursor.getInt(cursor.getColumnIndex(COLUMN_PRIOPRITY));
			
			if (system != null) {
				system = system.trim().toLowerCase(Locale.getDefault());
			}
			if (packageName != null) {
				packageName = packageName.trim();
			}
			if (className != null) {
				className = className.trim();
			}
			return this;
		}
	}
}
