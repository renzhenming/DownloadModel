package com.rzm.downloadmodle;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.rzm.downloadlibrary.download.DownloadInfo;
import com.rzm.downloadlibrary.download.DownloadManager;

public class AppHolder extends RecyclerView.ViewHolder implements DownloadManager.DownloadObserver, View.OnClickListener {

    private final RingProgress pbProgress;
    private final TextView textView;
    private DownloadManager mDownloadManager;
    private int mCurrentState;
    private float mProgress;
    private AppInfo mAppInfo;
    private final Context context;

    public AppHolder(@NonNull View itemView) {
        super(itemView);
        pbProgress = itemView.findViewById(R.id.progress);
        textView = itemView.findViewById(R.id.name);
        pbProgress.setOnClickListener(this);
        context = textView.getContext();
    }

    public void bind(AppInfo appInfo) {
        textView.setText(appInfo.name);
        mAppInfo = appInfo;
        mDownloadManager = DownloadManager.getInstance(context);
        // 监听下载进度
        mDownloadManager.registerObserver(this);

        DownloadInfo downloadInfo = mDownloadManager.getDownloadInfo(appInfo.downloadUrl);
        if (downloadInfo == null) {
            // 没有下载过
            mCurrentState = DownloadManager.STATE_NONE;
            mProgress = 0;
        } else {
            // 之前下载过, 以内存中的对象的状态为准
            mCurrentState = downloadInfo.currentState;
            mProgress = downloadInfo.getProgress();
        }

        refreshUI(mProgress, mCurrentState, appInfo.downloadUrl);
    }

    private void refreshUI(float progress, int state, String downloadUrl) {
        // 在刷新界面时,确保刷新的是正确应用的界面
        if (!downloadUrl.equals(mAppInfo.downloadUrl)) {
            return;
        }
        mCurrentState = state;
        mProgress = progress;
        switch (state) {
            case DownloadManager.STATE_NONE:
                pbProgress.setPercent(0);
                // 文字为下载
                pbProgress.setText("下载");
                break;
            case DownloadManager.STATE_WAITING:
                pbProgress.setPercent(progress);
                pbProgress.setText("等待");
                break;
            case DownloadManager.STATE_DOWNLOAD:
                pbProgress.setPercent(progress);
                break;
            case DownloadManager.STATE_PAUSE:
                pbProgress.setPercent(progress);
                pbProgress.setText("继续");
                break;
            case DownloadManager.STATE_ERROR:
                pbProgress.setPercent(progress);
                pbProgress.setText("重试");
                break;
            case DownloadManager.STATE_SUCCESS:
                pbProgress.setPercent(1);
                pbProgress.setText("安装");
                break;

            default:
                break;
        }
    }

    @Override
    public void onDownloadStateChanged(DownloadInfo info) {
        // 判断要刷新的下载对象是否是当前的应用
        if (info.downloadUrl.equals(mAppInfo.downloadUrl)) {
            refreshUI(info.getProgress(), info.currentState, info.downloadUrl);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.progress:
                // 根据当前状态来决定相关操作
                if (mCurrentState == DownloadManager.STATE_NONE
                        || mCurrentState == DownloadManager.STATE_PAUSE
                        || mCurrentState == DownloadManager.STATE_ERROR) {
                    // 开始下载
                    mDownloadManager.download(new DownloadInfo.Builder()
                            .setDownloadUrl(mAppInfo.downloadUrl)
                            //downloadMd5作为本次下载的唯一标识存在
                            .setDownloadMd5(mAppInfo.downloadMd5)
                            .setName(mAppInfo.name)
                            .build()
                    );
                } else if (mCurrentState == DownloadManager.STATE_DOWNLOAD
                        || mCurrentState == DownloadManager.STATE_WAITING) {
                    // 暂停下载
                    mDownloadManager.pause(mAppInfo.downloadMd5);
                } else if (mCurrentState == DownloadManager.STATE_SUCCESS) {
                    // 开始安装
                    mDownloadManager.install(context, mAppInfo.downloadMd5);
                }
                break;

            default:
                break;
        }
    }
}