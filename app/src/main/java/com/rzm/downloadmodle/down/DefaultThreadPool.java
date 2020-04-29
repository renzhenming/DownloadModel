package com.rzm.downloadmodle.down;

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
