package com.rzm.downloadmodle.down;

public interface IThreadPool {
    void execute(Runnable runnable);
    void cancel(Runnable runnable);
}
