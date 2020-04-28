package com.rzm.downloadmodle.down;

import android.text.TextUtils;

public class DefaultCacheManager<T> implements ICache<T> {

    private MemoryCache<T> memory = new MemoryCache();

    @Override
    public void setCache(String id, T t) {
        memory.setCache(id,t);
    }

    @Override
    public T getCache(String id) {
        if (!TextUtils.isEmpty(id)) {
            return memory.getCache(id);
        }
        return null;
    }
}
