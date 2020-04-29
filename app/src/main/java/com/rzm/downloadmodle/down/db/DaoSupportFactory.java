package com.rzm.downloadmodle.down.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import java.io.File;

/**
 * Created by rzm on 2017/8/22.
 *
 * 1.为什么使用Factory目前的数据是在内存卡中 有时候我们需要放到系统目录 data/data/xxx/database中
 * 获取不同的Factory，可以达到写入位置不同的效果，只需创建另一个Factory
 *
 * 2.面向接口编程，获取IDaoSupport 那么不需要关心实现，目前的实现是我们自己写的，方便以后使用第三方的
 *
 * 3.为了高扩展
 */

public class  DaoSupportFactory {

    private static final String DB_DIR = "update";
    private static final String DB_NAME = "user.db";
    private static volatile DaoSupportFactory mFactory;
    private static SQLiteDatabase mSqliteDabase;

    //持有外部数据库的引用
    private DaoSupportFactory(Context context){
        //TODO 注意补充判断内存卡是否存在， 6.0 动态申请内存
        //把数据库放在内存卡
        File dbRoot = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator+DB_DIR/*+File.separator+"database"*/);
        //File dbRoot = new File(context.getFilesDir()+File.separator+DB_DIR+File.separator+"database");

        if (!dbRoot.exists()){
            dbRoot.mkdirs();
        }

        File dbFile = new File(dbRoot,DB_NAME);
        //打开或者创建数据库
        mSqliteDabase = SQLiteDatabase.openOrCreateDatabase(dbFile, null);
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
        daoSupport.init(mSqliteDabase,clazz);
        return daoSupport;
    }
}
