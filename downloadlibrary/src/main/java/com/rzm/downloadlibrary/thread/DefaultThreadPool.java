package com.rzm.downloadlibrary.thread;

public class DefaultThreadPool implements IThreadPool {

    @Override
    public void execute(Runnable runnable) {
        ThreadManager.getThreadPool().execute(runnable);
    }

    @Override
    public void cancel(Runnable runnable) {
        ThreadManager.getThreadPool().cancel(runnable);
    }
}
