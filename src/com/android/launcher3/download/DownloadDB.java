package com.android.launcher3.download;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.android.launcher3.LauncherApplication;

import java.io.File;
import java.util.ArrayList;

public class DownloadDB {
	
	private SQLiteDatabase mDatabase;
	private static DownloadDB instance;
	
	public static synchronized DownloadDB getInstance(){
		if (instance == null){
			instance = new DownloadDB();
		}
		return instance;
	}
	
	public synchronized SQLiteDatabase openDatabase() {
		mDatabase = new DownloadDBHelper(LauncherApplication.getInstance()).getWritableDatabase();
		return mDatabase;
	}
	
	//插入未完成的
	public synchronized void insertThemeUnfinished(DownloadTaskInfo info){
		SQLiteDatabase db = openDatabase();
		ContentValues values = new ContentValues();
		values.put(DownloadDBHelper.COLUMN_ID, info.id);
		values.put(DownloadDBHelper.COLUMN_NAME, info.name);
		values.put(DownloadDBHelper.COLUMN_SIZE, info.size);
		values.put(DownloadDBHelper.COLUMN_CURRENTSIZE, info.currentSize);
		values.put(DownloadDBHelper.COLUMN_URL, info.url);
		db.insert(DownloadDBHelper.TABLE_THEME_UNFINISHED, null, values);
		db.close();
	}
	//更新未完成的
	public synchronized void updateThemeUnfinished(DownloadTaskInfo info){
		SQLiteDatabase db = openDatabase();
		ContentValues values = new ContentValues();
		values.put(DownloadDBHelper.COLUMN_ID, info.id);
		values.put(DownloadDBHelper.COLUMN_NAME, info.name);
		values.put(DownloadDBHelper.COLUMN_SIZE, info.size);
		values.put(DownloadDBHelper.COLUMN_CURRENTSIZE, info.currentSize);
		values.put(DownloadDBHelper.COLUMN_URL, info.url);
		db.update(DownloadDBHelper.TABLE_THEME_UNFINISHED, values, DownloadDBHelper.COLUMN_ID+"=?", new String[]{info.id+""});
		db.close();
	}
	//删除未完成的
	public synchronized void deleteThemeUnfinished(String name){
		SQLiteDatabase db = openDatabase();
		db.delete(DownloadDBHelper.TABLE_THEME_UNFINISHED, DownloadDBHelper.COLUMN_NAME+"=?", new String[]{name});
		db.close();
	}

	//查询未完成的
	public ArrayList<DownloadTaskInfo> queryAllThemeUnfinished(){
		ArrayList<DownloadTaskInfo> list = new ArrayList<DownloadTaskInfo>();
		SQLiteDatabase db = openDatabase();
		Cursor cursor = db.query(DownloadDBHelper.TABLE_THEME_UNFINISHED, null, null, null, null, null, null);
		if (cursor != null && cursor.getCount() > 0){
			while(cursor.moveToNext()){
				DownloadTaskInfo info = new DownloadTaskInfo();
				info.id = (cursor.getInt(cursor.getColumnIndex(DownloadDBHelper.COLUMN_ID)));
				info.name = (cursor.getString(cursor.getColumnIndex(DownloadDBHelper.COLUMN_NAME)));
				info.size = (Long.valueOf(cursor.getString(cursor.getColumnIndex(DownloadDBHelper.COLUMN_SIZE))));
				info.currentSize = (Long.valueOf(cursor.getString(cursor.getColumnIndex(DownloadDBHelper.COLUMN_CURRENTSIZE))));
				info.url = (cursor.getString(cursor.getColumnIndex("url")));
				info.downloadState = DownloadManager.STATE_PAUSED;
				list.add(info);
			}
		}
		closeCursor(cursor);
		db.close();
		return list;
	}
	
	//插入完成的
	public void insertThemeFinished(DownloadTaskInfo info){
		SQLiteDatabase db = openDatabase();
		ContentValues values = new ContentValues();
		values.put(DownloadDBHelper.COLUMN_ID, info.id);
		values.put(DownloadDBHelper.COLUMN_NAME, info.name);
		values.put(DownloadDBHelper.COLUMN_SIZE, info.size);
		values.put(DownloadDBHelper.COLUMN_URL, info.url);
		db.insert(DownloadDBHelper.TABLE_THEME_FINISHED,null, values);
		db.close();
	}
	//删除完成的
	public synchronized void deleteThemeFinished(String themename){
		SQLiteDatabase db = openDatabase();
		db.delete(DownloadDBHelper.TABLE_THEME_FINISHED, DownloadDBHelper.COLUMN_NAME+"=?", new String[]{themename});
		db.close();
	}
	//查询完成的
	public ArrayList<DownloadTaskInfo> queryAllThemeFinished(){
		ArrayList<DownloadTaskInfo> list = new ArrayList<DownloadTaskInfo>();;
		SQLiteDatabase db = openDatabase();
		Cursor cursor = db.query(DownloadDBHelper.TABLE_THEME_FINISHED, null, null, null, null, null, null);
		if (cursor != null && cursor.getCount() > 0){
			while(cursor.moveToNext()){
				DownloadTaskInfo info = new DownloadTaskInfo();
				info.id = cursor.getInt(cursor.getColumnIndex(DownloadDBHelper.COLUMN_ID));
				info.name = (cursor.getString(cursor.getColumnIndex(DownloadDBHelper.COLUMN_NAME)));
				info.size = (Long.valueOf(cursor.getString(cursor.getColumnIndex(DownloadDBHelper.COLUMN_SIZE))));
				info.url = (cursor.getString(cursor.getColumnIndex(DownloadDBHelper.COLUMN_URL)));
				info.downloadState = (DownloadManager.STATE_DOWNLOADED);
				if (new File(DownloadTaskInfo.getPath(info.name)).exists()) {
					list.add(info);
				}
			}
		}
		closeCursor(cursor);
		db.close();
		return list;
	}
	
	public static void closeCursor(Cursor cursor) {
		if (cursor != null) {
			cursor.close();
			cursor = null;
		}
	}
}
