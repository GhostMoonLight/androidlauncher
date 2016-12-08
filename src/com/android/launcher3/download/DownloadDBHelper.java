package com.android.launcher3.download;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
/**
 * 下载数据库
 */
public class DownloadDBHelper extends SQLiteOpenHelper {

	public static String COLUMN_ID = "id";
	public static String COLUMN_NAME = "name";
	public static String COLUMN_SIZE = "size";
	public static String COLUMN_CURRENTSIZE = "currentsize";
	public static String COLUMN_URL = "url";
	
	public static String TABLE_THEME_UNFINISHED = "download_unfinished";
	public static String TABLE_THEME_FINISHED = "download_finished";
	private static int VERSION = 1;

	public DownloadDBHelper(Context context) {
		super(context, "download.db", null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		//下载未完成的表
		db.execSQL("create table "+TABLE_THEME_UNFINISHED+"(_id integer PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_ID + " integer, " +
                COLUMN_NAME + " char, " +
                COLUMN_SIZE +" char, " +
                COLUMN_CURRENTSIZE + " char," +
                COLUMN_URL + " char)");
		//下载完成的表
		db.execSQL("create table "+TABLE_THEME_FINISHED+"(_id integer PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_ID + " integer, " +
                COLUMN_NAME + " char, " +
                COLUMN_SIZE +" char, " +
                COLUMN_URL + " char)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

}
