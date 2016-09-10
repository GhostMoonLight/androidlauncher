package com.android.launcher3.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class AssetsDatabaseManager {
	private static final String TAG = "AssetsDatabaseManager";
	private static String DB_PATH = "/data/data/%s/databases";
	private Map<String, SQLiteDatabase> databases = new HashMap<String, SQLiteDatabase>();
	private Context mContext = null;
	private static AssetsDatabaseManager mInstance = null;

	private AssetsDatabaseManager(Context context) {
		this.mContext = context;
	}

	public static AssetsDatabaseManager getAssetsDatabaseManager(Context context) {
		if (null == mInstance) {
			mInstance = new AssetsDatabaseManager(context);
		}
		return mInstance;
	}

	public SQLiteDatabase getDatabase(String dbName) {
		if (databases.get(dbName) != null) {
			Log.i(TAG, String.format("Return a database copy of %s.", dbName));
			return databases.get(dbName);
		}
		if (null == mContext) return null;
		Log.i(TAG, String.format("Create database %s.", dbName));
		String dbPath = getDatabaseFilePath();
		String dbFile = getDatabaseFile(dbName);

		File file = new File(dbFile);
		SharedPreferences dbsp = mContext.getSharedPreferences(AssetsDatabaseManager.class.toString(), Context.MODE_PRIVATE);

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
		SQLiteDatabase db = SQLiteDatabase.openDatabase(dbFile, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
		if (null != db) databases.put(dbName, db);
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

	public boolean closeDatabase(String dbfile) {
		if (databases.get(dbfile) != null) {
			SQLiteDatabase db = databases.get(dbfile);
			db.close();
			databases.remove(dbfile);
			return true;
		}
		return false;
	}

	public static void closeAllDatabase() {
		Log.i(TAG, "closeAllDatabases.");
		if (mInstance != null) {
			for (int i = 0; i < mInstance.databases.size(); i++) {
				if (mInstance.databases.get(i) != null) mInstance.databases.get(i).close();
			}
			mInstance.databases.clear();
		}
	}
	
	public void deleteDatabase(String dbName) {
		try {
			String path = getDatabaseFile(dbName);
			File file = new File(path);
			if (file != null && file.exists()) {
				file.delete();
			}
			databases.remove(dbName);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
}
