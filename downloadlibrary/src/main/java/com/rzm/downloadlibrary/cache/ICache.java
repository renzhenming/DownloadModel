package com.rzm.downloadlibrary.cache;

public interface ICache<T> {
    void setCache(String id, T t);
    T getCache(String id);
    int updateCache(String id, T t);
}
