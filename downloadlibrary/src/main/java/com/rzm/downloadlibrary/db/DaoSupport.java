package com.rzm.downloadlibrary.db;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.rzm.downloadlibrary.utils.LogUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by rzm on 2017/8/22.
 */
public class DaoSupport<T> implements IDaoSupport<T> {

    private static final String TAG = "DaoSupport";
    private Class<T> mClazz;
    private SQLiteDatabase mSqliteDatabase;
    //缓存变量，这个只是为了显得规范，其实达不到提高效率的目的，这种写法是仿照AppCompatViewInflater写的
    private static final Object[] mPutMethodArgs = new Object[2];
    //缓存反射获取到的方法，这样如果数据量很大，那么就不需要反复的去执行反射获取方法了，达到提高效率的目的
    private static final Map<String, Method> mPutMethods = new HashMap<>();
    private QuerySupport<T> mQuerySupport;

    @Override
    public void init(SQLiteDatabase sqLiteDatabase, Class<T> clazz) {
        this.mSqliteDatabase = sqLiteDatabase;
        this.mClazz = clazz;

        //创建表 sql语句
        //"create table if not exists Person (id integer primary key autoincrement,name text, age integer,flag boolean)"
        StringBuffer buffer = new StringBuffer();
        buffer.append("create table if not exists ").append(DaoUtil.getTableName(clazz)).append(" (");

        Field[] fields = mClazz.getDeclaredFields();
        for (Field field : fields) {
            //过滤掉编译器自动生成的成员变量
            if (field.isSynthetic()){
                continue;
            }
            field.setAccessible(true);
            String name = field.getName();
            //得到int String 或者boolean 我们需要将这些转化成数据库语言类型
            String type = field.getType().getSimpleName();
            buffer.append(name).append(" ").append(DaoUtil.getColumnType(type)).append(", ");
        }
        //把stringbuffer的最后一个", ”替换成")"
        buffer.replace(buffer.length() - 2, buffer.length(), ")");
        String value = buffer.toString();
        LogUtils.d(TAG+" create table :" + value);
        mSqliteDatabase.execSQL(buffer.toString());
    }

    //插入数据库，t是任意对象
    @Override
    public long insert(T t) {
        //使用的还是原生的方式，我们只是封装一下
        ContentValues value = contentValueByObj(t);
        //速度比第三方快一倍
        return mSqliteDatabase.insert(DaoUtil.getTableName(mClazz), null, value);
    }

    @Override
    public void insert(List<T> data) {
        mSqliteDatabase.beginTransaction();
        for (T t : data) {
            insert(t);
        }
        mSqliteDatabase.setTransactionSuccessful();
        mSqliteDatabase.endTransaction();
    }

    @Override
    public QuerySupport<T> querySupport() {
        if (mQuerySupport == null){
            mQuerySupport = new QuerySupport<>(mSqliteDatabase,mClazz);
        }
        return mQuerySupport;
    }

    /**
     * 原生删除方式
     */
    @Override
    public int delete(String whereClause, String[] whereArgs) {
        return mSqliteDatabase.delete(DaoUtil.getTableName(mClazz), whereClause, whereArgs);
    }

    /**
     * 原生更新方式
     */
    @Override
    public int update(T obj, String whereClause, String... whereArgs) {
        ContentValues values = contentValueByObj(obj);
        return mSqliteDatabase.update(DaoUtil.getTableName(mClazz),
                values, whereClause, whereArgs);
    }

    /**
     * 删除所有行
     */
    @Override
    public int deleteAll() {
        return mSqliteDatabase.delete(DaoUtil.getTableName(mClazz), null, null);
    }

    private ContentValues contentValueByObj(T t) {
        ContentValues contentValues = new ContentValues();

        Field[] fields = mClazz.getDeclaredFields();
        for (Field field : fields) {
            try {
                //过滤掉系统自动生成的字段和serialVersionUID
                if (field.isSynthetic() || "serialVersionUID".equals(field.getName())){
                    continue;
                }
                field.setAccessible(true);
                //分别获取到属性和对应的值 比如 name = age  value = 12
                String name = field.getName();
                Object value = field.get(t);
                mPutMethodArgs[0] = name;
                mPutMethodArgs[1] = value;
                //使用反射获取方法执行
                String filedTypeName = field.getType().getName();
                Method putMethod = mPutMethods.get(filedTypeName);
                if (putMethod == null) {
                    // value.getClass() 获取到的是这个值的所属类型 比如如果是int age 10岁， 那么这个value获取到的就是
                    // class java.lang.Integer  即Integer.class
                    // 获取ContentValue中的put方法  例如 public void put(String key, Integer value，ContentValue是以键值对的
                    // 形式添加数据的，第一个参数肯定是String,而第二个就由我们定义的属性的性质决定了，用 value.getClass()获取
                    putMethod = ContentValues.class.getDeclaredMethod("put", String.class, value.getClass());
                    //缓存这个获取到的方法，再次执行的时候直接取用，减少反射的使用次数，（参考系统的代码方式）
                    mPutMethods.put(filedTypeName, putMethod);
                }
                putMethod.setAccessible(true);
                //反射执行这个方法，将获取到的键值存入
                putMethod.invoke(contentValues, mPutMethodArgs);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } finally {
                mPutMethodArgs[0] = null;
                mPutMethodArgs[1] = null;
            }
        }
        return contentValues;
    }
}
















