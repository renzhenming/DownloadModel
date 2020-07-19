package com.rzm.downloadlibrary.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import com.rzm.downloadlibrary.download.DownloadInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by renzm on 2020/6/15.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "wifi_sdk.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "download";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_SIZE = "size";
    public static final String COLUMN_MD5 = "downloadMd5";
    public static final String COLUMN_PKG = "packageName";
    public static final String COLUMN_URL = "downloadUrl";

    public static final String COLUMN_STATE = "currentState";
    public static final String COLUMN_POSITION = "currentPos";
    public static final String COLUMN_PATH = "path";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table if not exists "
                + TABLE_NAME + "("
                + COLUMN_ID + " text primary key,"
                + COLUMN_NAME + " text,"
                + COLUMN_SIZE + " integer,"
                + COLUMN_MD5 + " text,"
                + COLUMN_PKG + " text,"
                + COLUMN_URL + " text,"
                + COLUMN_STATE + " integer,"
                + COLUMN_POSITION + " integer,"
                + COLUMN_PATH + " text);";

        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


    public synchronized long insert(DownloadInfo info) {
        if (info == null) return -1;
        List<DownloadInfo> downloadInfos = queryById(info.id);
        SQLiteDatabase db = getWritableDatabase();

        long insert = -1;
        //如果不存在，则新增
        if (downloadInfos == null || downloadInfos.size() == 0) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_ID, info.id);
            values.put(COLUMN_NAME, info.name);
            values.put(COLUMN_SIZE, info.size);
            values.put(COLUMN_MD5, info.downloadMd5);
            values.put(COLUMN_PKG, info.packageName);
            values.put(COLUMN_URL, info.downloadUrl);
            values.put(COLUMN_STATE, info.currentState);
            values.put(COLUMN_POSITION, info.currentPos);
            values.put(COLUMN_PATH, info.path);
            insert = db.insert(TABLE_NAME, null, values);
        } else {
            //已存在，则更新
            insert = update(info.id, info);
        }
        //多线程操作数据库会导致问题，所以每次操作之后不关闭
        db.close();
        return insert;
    }

    public synchronized int delete(String id) {
        if (TextUtils.isEmpty(id)) return 0;
        SQLiteDatabase db = getWritableDatabase();
        int delete = db.delete(TABLE_NAME, COLUMN_ID + "=?", new String[]{id});
        db.close();
        return delete;
    }


    public synchronized int update(String id, DownloadInfo info) {
        if (TextUtils.isEmpty(id) || info == null) {
            return 0;
        }
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, info.id);
        values.put(COLUMN_NAME, info.name);
        values.put(COLUMN_SIZE, info.size);
        values.put(COLUMN_MD5, info.downloadMd5);
        values.put(COLUMN_PKG, info.packageName);
        values.put(COLUMN_URL, info.downloadUrl);
        values.put(COLUMN_STATE, info.currentState);
        values.put(COLUMN_POSITION, info.currentPos);
        values.put(COLUMN_PATH, info.path);
        int update = db.update(TABLE_NAME, values, COLUMN_ID + "=?", new String[]{id});
        db.close();
        return update;
    }

    public synchronized List<DownloadInfo> queryById(String downloadId) {
        if (TextUtils.isEmpty(downloadId)) return null;
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, COLUMN_ID + "=?", new String[]{downloadId}, null, null, null);
        List<DownloadInfo> downloadInfos = new ArrayList<>();
        while (cursor.moveToNext()) {
            int idIndex = cursor.getColumnIndex(COLUMN_ID);
            int nameIndex = cursor.getColumnIndex(COLUMN_NAME);
            int sizeIndex = cursor.getColumnIndex(COLUMN_SIZE);

            int urlIndex = cursor.getColumnIndex(COLUMN_URL);
            int stateIndex = cursor.getColumnIndex(COLUMN_STATE);
            int positionIndex = cursor.getColumnIndex(COLUMN_POSITION);
            int pathIndex = cursor.getColumnIndex(COLUMN_PATH);
            int md5Index = cursor.getColumnIndex(COLUMN_MD5);
            int pkgIndex = cursor.getColumnIndex(COLUMN_PKG);

            String id = cursor.getString(idIndex);
            String name = cursor.getString(nameIndex);
            int size = cursor.getInt(sizeIndex);
            String url = cursor.getString(urlIndex);
            int state = cursor.getInt(stateIndex);
            int position = cursor.getInt(positionIndex);
            String path = cursor.getString(pathIndex);
            String md5 = cursor.getString(md5Index);
            String pkg = cursor.getString(pkgIndex);

            DownloadInfo info = new DownloadInfo();
            info.id = id;
            info.name = name;
            info.size = size;
            info.downloadUrl = url;
            info.currentState = state;
            info.currentPos = position;
            info.path = path;
            info.downloadMd5 = md5;
            info.packageName = pkg;
            downloadInfos.add(info);
        }
        db.close();
        return downloadInfos;
    }

    public List<DownloadInfo> queryAll() {
        return new ArrayList<>();
    }
}
