package com.android.launcher3.db;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;

import com.android.launcher3.ItemInfo;

import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * Created by cgx on 16/7/27.
 */
public abstract class DBContent {

    public static final String RECORD_ID = "_id";
    private static final int NOT_SAVED = -1;
    public long mId = NOT_SAVED;

    public abstract ContentValues toContentValues();

    public abstract <T extends DBContent> T restore(Cursor cursor);

    public abstract String[] getContentProjection();

    private static Cursor query(Context context, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        ContentResolver sContentResolver = context.getContentResolver();
        return sContentResolver.query(uri, projection, selection, selectionArgs, sortOrder);
    }

    private static Uri insert(Context context, Uri uri, ContentValues initialValues) {
        return context.getContentResolver().insert(uri, initialValues);
    }

    private static int update(Context context, Uri baseUri, ContentValues contentValues, String selection, String[] selectionArgs) {
        return context.getContentResolver().update(baseUri, contentValues, selection, selectionArgs);
    }

    public static int delete(Context context, Uri uri, String where, String[] selection) {
        return context.getContentResolver().delete(uri, where, selection);
    }

    //最近在使用的app表
    public interface RecentUserAppColumn{
        public static final String COLUMN_PCK = "pck";      //包名
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_INTENT = "intent";
        public static final String COLUMN_COUNT = "count";  //点击次数   目前这个字段没用
    }

    public static class RecentUserAppInfo extends DBContent implements RecentUserAppColumn{
        public static final Uri CONTENT_URI = Uri.parse(OtherDBProvider.CONTENT_URI + "/" + OtherDBProvider.TABLE_RECENT_USER);

        public static final String[] CONTENT_PROJECTION = {
                RECORD_ID,
                COLUMN_PCK,
                COLUMN_TITLE,
                COLUMN_INTENT,
                COLUMN_COUNT,
        };

        public String pck;
        public String title;
        public String intent;
        public Intent mIntent;
        public int count;
        public Drawable icon;

        @Override
        public ContentValues toContentValues() {
            ContentValues values = new ContentValues();
            values.put(COLUMN_PCK, pck);
            values.put(COLUMN_TITLE, title);
            values.put(COLUMN_INTENT, intent);
            values.put(COLUMN_COUNT, count);
            return values;
        }

        @Override
        public RecentUserAppInfo restore(Cursor cursor) {
            mId = cursor.getLong(0);
            pck = cursor.getString(1);
            title = cursor.getString(2);
            intent = cursor.getString(3);
            count = cursor.getInt(4);
            try {
				mIntent = Intent.parseUri(intent, 0);
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
            return this;
        }

        @Override
        public String[] getContentProjection() {
            return CONTENT_PROJECTION;
        }

        public static ArrayList<RecentUserAppInfo> queryData(Context context){
            ArrayList<RecentUserAppInfo> list = new ArrayList<>();

            Cursor cursor = query(context, CONTENT_URI, null, null, null, RECORD_ID+" desc");
            if (cursor != null){
                int count = 0;
                while (cursor.moveToNext()){
                    list.add(new RecentUserAppInfo().restore(cursor));
                    count++;
                    if (count >= 8){
                        break;
                    }
                }
                cursor.close();
            }
            return list;
        }

//        /**
//         * Returns true if the shortcuts already exists in the database.
//         * we identify a shortcut by its title and intent.
//         */
//        private static RecentUserAppInfo isExists(Context context, String title, Intent intent) {
//            Cursor c = null;
//            boolean result = false;
//
//            try {
//                String selection = "title=? and intent=?";
//                String[] selectionArgs = { title, intent.toUri(0) };
//
//                if (!TextUtils.isEmpty(ItemInfo.getPackageName(intent))) {
//                    String pkg = ItemInfo.getPackageName(intent);
//                    String cls ="",cls2="";
//                    if(intent.getComponent()!=null){
//                        cls = intent.getComponent().getClassName();
//                        cls2 = cls.contains(pkg) ? cls.substring(cls.indexOf(pkg) + pkg.length()) : cls;
//                    }
//                    selection = "(title=? and intent=?) or " + "(title like ? and intent like ?) or " + "(title like ? and intent like ?)";
//                    selectionArgs = new String[] { title, intent.toUri(0),
//                            "%" + title + "%", "%" + pkg + "/" + cls + "%",
//                            "%" + title + "%", "%" + pkg + "/" + cls2 + "%"};
//                }
//
//                c = query(context, CONTENT_URI, null, selection, selectionArgs, null);
//                result = c.moveToFirst();
//                if (result){
//                    RecentUserAppInfo info = new RecentUserAppInfo();
//                    return info.restore(c);
//                }
//            } finally {
//                if (c != null)
//                    c.close();
//            }
//
//            return null;
//        }
        
        private static void deleteData(Context context, String title, Intent intent){
        	 try {
                 String selection = "title=? and intent=?";
                 String[] selectionArgs = { title, intent.toUri(0) };

                 if (!TextUtils.isEmpty(ItemInfo.getPackageName(intent))) {
                     String pkg = ItemInfo.getPackageName(intent);
                     String cls ="",cls2="";
                     if(intent.getComponent()!=null){
                         cls = intent.getComponent().getClassName();
                         cls2 = cls.contains(pkg) ? cls.substring(cls.indexOf(pkg) + pkg.length()) : cls;
                     }
                     selection = "(title=? and intent=?) or " + "(title like ? and intent like ?) or " + "(title like ? and intent like ?)";
                     selectionArgs = new String[] { title, intent.toUri(0),
                             "%" + title + "%", "%" + pkg + "/" + cls + "%",
                             "%" + title + "%", "%" + pkg + "/" + cls2 + "%"};
                 }
                 delete(context, CONTENT_URI, selection, selectionArgs);
             } finally {
             }
        }

        public static void updateData(Context context, String pck, String title, String intent){
            //检查该数据是否存在
            try {
//                RecentUserAppInfo info = isExists(context, title, Intent.parseUri(intent, 0));
//                if (info != null){
//                    //更新
//                    ContentValues values = new ContentValues();
//                    values.put(COLUMN_COUNT, info.count+1);
//                    update(context,CONTENT_URI, values, "title=? and intent=?", new String[]{info.title, info.intent});
//                }else{
            	deleteData(context, title, Intent.parseUri(intent, 0));
                //添加
            	RecentUserAppInfo info = new RecentUserAppInfo();
                info.pck = pck;
                info.title = title;
                info.intent = intent;
                info.count = 1;
                insert(context, CONTENT_URI, info.toContentValues());
//                }
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }

    }
}
