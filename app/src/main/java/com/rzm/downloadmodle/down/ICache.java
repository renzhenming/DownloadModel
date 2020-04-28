package com.rzm.downloadmodle.down;

public interface ICache<T> {
    void setCache(String id,T t);
    T getCache(String id);
}
