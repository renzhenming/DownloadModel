package com.rzm.downloadlibrary.thread;

public interface IThreadPool {
    void execute(Runnable runnable);
    void cancel(Runnable runnable);
}
