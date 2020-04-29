package com.rzm.downloadmodle.down.db;

import android.database.sqlite.SQLiteDatabase;

import java.util.List;

/**
 * Created by rzm on 2017/8/22.
 */
public interface IDaoSupport<T> {

    void init(SQLiteDatabase sqLiteDatabase, Class<T> clazz);

    long insert(T t);

    void insert(List<T> data);

    QuerySupport<T> querySupport();

    int delete(String whereClause, String... whereArgs);

    int update(T obj, String whereClause, String... whereArgs);

    int deleteAll();
}
