package com.rzm.downloadlibrary.cache;

public interface ICache<T> {
    void setCache(String uniqueKey, T t);
    T getCache(String uniqueKey);
    int updateCache(String uniqueKey, T t);
}
