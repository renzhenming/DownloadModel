package com.rzm.downloadlibrary.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import com.rzm.downloadlibrary.download.DownloadInfo;
import com.rzm.downloadlibrary.download.DownloadManager;
import com.rzm.downloadlibrary.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by renzm on 2020/6/15.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    private static DatabaseHelper instance;

    private static final String DATABASE_NAME = "download_asset.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "download";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_SIZE = "size";
    public static final String COLUMN_UNIQUE_KEY = "uniqueKey";
    public static final String COLUMN_PKG = "packageName";
    public static final String COLUMN_URL = "downloadUrl";

    public static final String COLUMN_STATE = "currentState";
    public static final String COLUMN_POSITION = "currentPos";
    public static final String COLUMN_PATH = "path";

    public static DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            synchronized (DatabaseHelper.class) {
                if (instance == null) {
                    instance = new DatabaseHelper(context);
                }
            }
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table if not exists "
                + TABLE_NAME + "("
                + COLUMN_ID + " text primary key,"
                + COLUMN_NAME + " text,"
                + COLUMN_SIZE + " integer,"
                + COLUMN_UNIQUE_KEY + " text,"
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
        List<DownloadInfo> downloadInfo = queryByKey(info.getUniqueKey());
        SQLiteDatabase db = getWritableDatabase();

        long insert = -1;
        //如果不存在，则新增
        if (downloadInfo == null || downloadInfo.size() == 0) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_ID, info.getId());
            values.put(COLUMN_NAME, info.getName());
            values.put(COLUMN_SIZE, info.getSize());
            values.put(COLUMN_UNIQUE_KEY, info.getUniqueKey());
            values.put(COLUMN_PKG, info.getPackageName());
            values.put(COLUMN_URL, info.getDownloadUrl());
            values.put(COLUMN_STATE, info.getCurrentState());
            values.put(COLUMN_POSITION, info.getCurrentPos());
            values.put(COLUMN_PATH, info.getPath());
            insert = db.insert(TABLE_NAME, null, values);
        } else {
            //已存在，则更新
            insert = update(info.getUniqueKey(), info);
        }
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


    public synchronized int update(String uniqueKey, DownloadInfo info) {
        if (TextUtils.isEmpty(uniqueKey) || info == null) {
            return 0;
        }
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, info.getId());
        values.put(COLUMN_NAME, info.getName());
        values.put(COLUMN_SIZE, info.getSize());
        values.put(COLUMN_UNIQUE_KEY, info.getUniqueKey());
        values.put(COLUMN_PKG, info.getPackageName());
        values.put(COLUMN_URL, info.getDownloadUrl());
        values.put(COLUMN_STATE, info.getCurrentState());
        values.put(COLUMN_POSITION, info.getCurrentPos());
        values.put(COLUMN_PATH, info.getPath());
        int update = db.update(TABLE_NAME, values, COLUMN_UNIQUE_KEY + "=?", new String[]{uniqueKey});
        db.close();
        return update;
    }

    public synchronized List<DownloadInfo> queryByKey(String uniqueKey) {
        return queryByKey(COLUMN_UNIQUE_KEY + "=?", uniqueKey);
    }

    public synchronized List<DownloadInfo> queryByPkgName(String packageName) {
        return queryByKey(COLUMN_PKG + "=?", packageName);
    }

    private synchronized List<DownloadInfo> queryByKey(String column, String value) {
        long start = System.currentTimeMillis();
        if (TextUtils.isEmpty(value)) return null;
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, column, new String[]{value}, null, null, null);
        List<DownloadInfo> downloadInfo = new ArrayList<>();
        while (cursor.moveToNext()) {
            int idIndex = cursor.getColumnIndex(COLUMN_ID);
            int nameIndex = cursor.getColumnIndex(COLUMN_NAME);
            int sizeIndex = cursor.getColumnIndex(COLUMN_SIZE);

            int urlIndex = cursor.getColumnIndex(COLUMN_URL);
            int stateIndex = cursor.getColumnIndex(COLUMN_STATE);
            int positionIndex = cursor.getColumnIndex(COLUMN_POSITION);
            int pathIndex = cursor.getColumnIndex(COLUMN_PATH);
            int uniqueKeyIndex = cursor.getColumnIndex(COLUMN_UNIQUE_KEY);
            int pkgIndex = cursor.getColumnIndex(COLUMN_PKG);

            String id = cursor.getString(idIndex);
            String name = cursor.getString(nameIndex);
            int size = cursor.getInt(sizeIndex);
            String url = cursor.getString(urlIndex);
            int state = cursor.getInt(stateIndex);
            int position = cursor.getInt(positionIndex);
            String path = cursor.getString(pathIndex);
            String uniqueKey = cursor.getString(uniqueKeyIndex);
            String pkg = cursor.getString(pkgIndex);

            DownloadInfo info = new DownloadInfo.Builder().build();
            info.setId(id);
            info.setName(name);
            info.setSize(size);
            info.setDownloadUrl(url);
            info.setCurrentState(state);
            info.setCurrentPos(position);
            info.setPath(path);
            info.setUniqueKey(uniqueKey);
            info.setPackageName(pkg);
            downloadInfo.add(info);
        }
        db.close();
        long end = System.currentTimeMillis();
        LogUtils.d("查询一次消耗的时间是：" + (end - start));
        return downloadInfo;
    }

    public synchronized List<DownloadInfo> queryAll() {
        return queryByKey(null, null);
    }
}
