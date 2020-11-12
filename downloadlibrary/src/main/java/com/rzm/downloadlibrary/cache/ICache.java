package com.rzm.downloadlibrary.cache;

import java.util.List;

public interface ICache<T> {
    void setCache(String uniqueKey, T t);

    T getCache(String uniqueKey);

    List<T> getCacheByPkgName(String packageName);

    int updateCache(String uniqueKey, T t);
}
