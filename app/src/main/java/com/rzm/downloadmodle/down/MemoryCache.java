package com.rzm.downloadmodle.down;

import android.util.LruCache;

public class MemoryCache<T> {
    public static final String TAG = "MemoryCache";

    private LruCache<String, T> lruCache;

    public MemoryCache() {
        lruCache = new LruCache((int) (Runtime.getRuntime().maxMemory()/8));
        LogUtils.d(TAG+"lruCache size = "+(int) (Runtime.getRuntime().maxMemory()/8));
    }

    public  void setCache(String id,T t){
        lruCache.put(id,t);
        LogUtils.d(TAG+"lruCache setCache id= "+id);
    }

    public T getCache(String id){
        LogUtils.d(TAG+"lruCache getCache id= "+id + " lruCache.get(id)="+lruCache.get(id));
        return lruCache.get(id);
    }
}
