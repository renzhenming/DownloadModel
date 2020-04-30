package com.rzm.downloadlibrary.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import java.io.File;

/**
 * Created by rzm on 2017/8/22.
 */
public class  DaoSupportFactory {

    private static final String DB_DIR = "update";
    private static final String DB_NAME = "user.db";
    private static volatile DaoSupportFactory mFactory;
    private static SQLiteDatabase mSqliteDatabase;

    //持有外部数据库的引用
    private DaoSupportFactory(Context context){
        File dbRoot = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator+DB_DIR/*+File.separator+"database"*/);
        //File dbRoot = new File(context.getFilesDir()+File.separator+DB_DIR+File.separator+"database");
        if (!dbRoot.exists()){
            dbRoot.mkdirs();
        }
        File dbFile = new File(dbRoot,DB_NAME);
        //打开或者创建数据库
        mSqliteDatabase = SQLiteDatabase.openOrCreateDatabase(dbFile, null);
    }

    public static DaoSupportFactory getFactory(Context context){
        if (mFactory == null){
            synchronized (DaoSupportFactory.class){
                if (mFactory == null){
                    mFactory = new DaoSupportFactory(context);
                }
            }
        }
        return mFactory;
    }

    public static<T> IDaoSupport<T> getDao(Class clazz){
        IDaoSupport<T> daoSupport = new DaoSupport<T>();
        daoSupport.init(mSqliteDatabase,clazz);
        return daoSupport;
    }
}
