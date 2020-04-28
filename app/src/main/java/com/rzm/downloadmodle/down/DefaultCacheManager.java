package com.rzm.downloadmodle.down;

import android.text.TextUtils;

import java.util.concurrent.ConcurrentHashMap;

public class DefaultCacheManager<T> implements ICache<T> {

    // 下载对象的集合, ConcurrentHashMap是线程安全的HashMap
    private ConcurrentHashMap<String, T> mDownloadInfoMap = new ConcurrentHashMap<>();

    @Override
    public void setCache(String id, T t) {
        mDownloadInfoMap.put(id, t);
    }

    @Override
    public T getCache(String id) {
        if (!TextUtils.isEmpty(id)) {
            return mDownloadInfoMap.get(id);
        }
        return null;
    }
}
