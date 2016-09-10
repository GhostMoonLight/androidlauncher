package com.android.launcher3.db;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.text.TextUtils;

import java.util.ArrayList;

public class OtherDBProvider extends ContentProvider {

    private static final String DATABASE_NAME = "other.db";
    private static final String DATABASE_AUTHORITY = "com.cuan.launcher.otherdb";
    public static final Uri CONTENT_URI = Uri.parse("content://" + DATABASE_AUTHORITY);
    private static final int DATABASE_VERSION = 1;

    private OtherDatabaseHelper mOpenHelper;
    public static final String TABLE_RECENT_USER = "recentuseapp";    //最近在使用的应用的表
    private static final String[] TABLE_NAMES = {
            TABLE_RECENT_USER
    };
    private static final UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int BASE_SHIFT = 8; // 8 bits to the base type: 0, 0x100, 0x200, etc

    private static final int RECENT_USER = 0x000;
    private static final int RECENT_USER_ID = RECENT_USER + 1;

    static {
        mUriMatcher.addURI(DATABASE_AUTHORITY, TABLE_RECENT_USER, RECENT_USER);
        mUriMatcher.addURI(DATABASE_AUTHORITY, TABLE_RECENT_USER + "/#", RECENT_USER_ID);
    }

    @Override
    public String getType(Uri uri) {
        switch(mUriMatcher.match(uri)) {
            case RECENT_USER:
                return "vnd.android.cursor.dir/" + TABLE_RECENT_USER;
            case RECENT_USER_ID:
                return "vnd.android.cursor.item/" + TABLE_RECENT_USER;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);

        }
    }


    @Override
    public boolean onCreate() {
        mOpenHelper = new OtherDatabaseHelper(getContext());
        return false;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final int match = mUriMatcher.match(uri);
        int tableIndex = match >> BASE_SHIFT;
        Context sContext = getContext();
        SQLiteDatabase sDatabase = mOpenHelper.getWritableDatabase();
        int rows = -1;

        switch (match) {
            case RECENT_USER:
                rows = sDatabase.delete(TABLE_NAMES[tableIndex], selection, selectionArgs);
                break;

            case RECENT_USER_ID:
                long id = ContentUris.parseId(uri);
                rows = sDatabase.delete(TABLE_NAMES[tableIndex], whereWithId(id, selection), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        if (rows > 0) {
            sendNotify(uri);
        }
        return rows;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final int match = mUriMatcher.match(uri);
        int tableIndex = match >> BASE_SHIFT;
        Context sContext = getContext();
        SQLiteDatabase sDatabase = mOpenHelper.getWritableDatabase();
        long id;
        Uri resultUri;

        switch (match) {
            case RECENT_USER:
                id = sDatabase.insert(TABLE_NAMES[tableIndex], null, values);
                resultUri = ContentUris.withAppendedId(uri, id);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        sendNotify(uri);
        return resultUri;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final int match = mUriMatcher.match(uri);
        int tableIndex = match >> BASE_SHIFT;
        Context sContext = getContext();
        SQLiteDatabase sDatabase = mOpenHelper.getWritableDatabase();
        Cursor sCursor = null;

        switch (match) {
            case RECENT_USER:
                sCursor = sDatabase.query(TABLE_NAMES[tableIndex], projection, selection, selectionArgs, null, null, sortOrder);
                break;

            case RECENT_USER_ID:
                long id = ContentUris.parseId(uri);
                sCursor = sDatabase.query(TABLE_NAMES[tableIndex], projection, whereWithId(id, selection), selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        if ((sCursor != null) && isTemporary()) {
            sCursor.setNotificationUri(sContext.getContentResolver(), CONTENT_URI);
        }
        return sCursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final int match = mUriMatcher.match(uri);
        int tableIndex = match >> BASE_SHIFT;
        Context sContext = getContext();
        SQLiteDatabase sDatabase = mOpenHelper.getWritableDatabase();
        int rows = -1;

        switch (match) {
            case RECENT_USER:
                rows = sDatabase.update(TABLE_NAMES[tableIndex], values, selection, selectionArgs);
                break;

            case RECENT_USER_ID:
                long id = ContentUris.parseId(uri);
                rows = sDatabase.update(TABLE_NAMES[tableIndex], values, whereWithId(id, selection), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        if (rows > 0)
            sendNotify(uri);
        return rows;
    }

    private void sendNotify(Uri uri) {
        String notify = uri.getQueryParameter("notify");
        if (notify == null || "true".equals(notify)) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
    }

    private String whereWithId(long id, String selection) {
        StringBuilder sb = new StringBuilder(256);
        sb.append("_id=");
        sb.append(id);
        if (!TextUtils.isEmpty(selection)) {
            sb.append(" AND (");
            sb.append(selection);
            sb.append(')');
        }
        return sb.toString();
    }

    @Override
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations) throws OperationApplicationException {
        Context sContext = getContext();
        SQLiteDatabase sDatabase = mOpenHelper.getWritableDatabase();
        sDatabase.beginTransaction();// 开启事务
        try {
            ContentProviderResult[] results = super.applyBatch(operations);
            sDatabase.setTransactionSuccessful();// 设置事务标记为successful
            sendNotify(CONTENT_URI);
            return results;
        } finally {
            sDatabase.endTransaction();
        }
    }

    public static class OtherDatabaseHelper extends SQLiteOpenHelper {

        public OtherDatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            createTables(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }

        private void createTables(SQLiteDatabase db) {
            createTableRecentUser(db);
        }

        private void createTableRecentUser(SQLiteDatabase db) {
            String command = "CREATE TABLE IF NOT EXISTS " + TABLE_RECENT_USER + "(" +
                    DBContent.RecentUserAppInfo.RECORD_ID + " INTEGER PRIMARY KEY autoincrement, " +
                    DBContent.RecentUserAppInfo.COLUMN_PCK + " TEXT NOT NULL, " +
                    DBContent.RecentUserAppInfo.COLUMN_TITLE + " TEXT, " +
                    DBContent.RecentUserAppInfo.COLUMN_INTENT + " TEXT, " +
//                    DBContent.RecentUserAppInfo.COLUMN_ICON + " BLOB," +
                    DBContent.RecentUserAppInfo.COLUMN_COUNT + " TEXT default 0)";
            db.execSQL(command);
            createIndex(db, TABLE_RECENT_USER, DBContent.RecentUserAppInfo.COLUMN_INTENT);
        }

        /**
         * 创建索引
         * @param db
         * @param tableName
         * @param columnName
         */
        private void createIndex(SQLiteDatabase db, String tableName, String columnName) {
            String indexSql = "create index " + tableName + '_' + columnName + " on " + tableName + " (" + columnName + ");";

            db.execSQL(indexSql);
        }
    }
}
