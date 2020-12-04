package com.rzm.downloadlibrary.download;

import android.content.Context;
import android.text.TextUtils;

import com.rzm.downloadlibrary.net.DefaultConnectionManager;
import com.rzm.downloadlibrary.net.IConnection;
import com.rzm.downloadlibrary.path.DefaultPathManager;
import com.rzm.downloadlibrary.path.IPath;
import com.rzm.downloadlibrary.utils.HttpUtils;
import com.rzm.downloadlibrary.utils.LogUtils;
import com.rzm.downloadlibrary.utils.Md5Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class DownloadTask implements Runnable {
    //资源下载地址
    private String downloadUrl;
    //路径管理类
    private IPath pathManager;
    //网络工具
    private IConnection connManager;
    //开启几个线程下载
    public int threadCount = 1;
    //从下载链接中截取出来的文件名
    private String fileName;
    //下载监听
    private DownloadListener downloadListener;
    //保存每个线程信息
    private ArrayList<DownloadThread> downloadThreads;
    //待下载资源的大小
    private int fileTotalLength;
    //计数器
    private CountDownLatch countDownLatch;
    //资源下载后保存的路径
    private String downloadPath;
    //下载是否暂停
    private volatile boolean pause = false;
    private String randomFileStr;

    public DownloadTask(Context context) {
        pathManager = new DefaultPathManager(context);
        connManager = new DefaultConnectionManager();
    }

    /**
     * 资源下载地址
     *
     * @param downloadUrl
     * @return
     */
    public DownloadTask setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
        return this;
    }

    /**
     * 资源名:如果设置了名字，就使用，否则从url中提取
     *
     * @param downloadName
     * @return
     */
    public DownloadTask setDownloadName(String downloadName) {
        this.fileName = downloadName;
        return this;
    }

    /**
     * 资源下载后保存的路径
     *
     * @param path
     * @return
     */
    public DownloadTask setSavePath(IPath path) {
        this.pathManager = path;
        return this;
    }

    /**
     * 网络链接工具
     *
     * @param connection
     * @return
     */
    public DownloadTask setConnManager(IConnection connection) {
        this.connManager = connection;
        return this;
    }

    /**
     * 开启几个线程下载
     *
     * @param count
     * @return
     */
    public DownloadTask setThreadCount(int count) {
        if (count <= 0) {
            count = 1;
        }
        this.threadCount = count;
        return this;
    }

    public DownloadTask setDownloadListener(DownloadListener downloadListener) {
        this.downloadListener = downloadListener;
        return this;
    }

    public void pause(boolean pause) {
        this.pause = pause;
    }

    public DownloadTask build() {
        if (TextUtils.isEmpty(downloadUrl)) {
            throw new NullPointerException("downloadUrl cannot be null");
        }
        File file = new File(pathManager.downloadPath());
        if (!file.exists() || !file.isDirectory()) {
            file.mkdirs();
        }
        downloadThreads = new ArrayList<>(threadCount);
        countDownLatch = new CountDownLatch(threadCount);
        downloadPath = pathManager.downloadPath();
        randomFileStr = Md5Utils.md5(downloadUrl);
        pause = false;
        return this;
    }

    private boolean createSameSizeFile(String downloadPath, int fileTotalLength) throws IOException {
        //创建一个和服务器资源大小一样的临时文件，来占位。
        File file = new File(downloadPath + File.separator + getFileName() + ".tmp");
        //创建一个随机访问文件对象    file：文件  mode：文件的模式 rwd：读写写入硬盘
        RandomAccessFile randFile = new RandomAccessFile(file, "rwd");
        //为随机文件设置一个文件大小，占位
        randFile.setLength(fileTotalLength);
        randFile.close();
        return true;
    }

    private void startDownloadThreads(String downloadUrl, String downloadPath, int fileTotalLength, int threadCount, CountDownLatch countDownLatch, List<DownloadThread> downloadThreads) {
        //根据资源的大小和线程数量计算每个线程下载文件的大小，还要计算每个线程下载的开始位置和结束位置。
        int blockSize = fileTotalLength / threadCount;//每个线程下载的大小
        //循环计算每个线程的开始位置和结束位置
        LogUtils.d("start " + threadCount + "thread to download each download length is ：" + blockSize);
        for (int threadId = 0; threadId < threadCount; threadId++) {
            //线程的开始下载位置
            int startIndex = threadId * blockSize;
            //下载的结束位置
            int endIndex = (threadId + 1) * blockSize - 1;
            //计算最后一个线程的结束位置
            if (threadId == threadCount - 1) {
                endIndex = fileTotalLength - 1;
            }
            LogUtils.d("thread" + threadId + "begin at :" + startIndex + " and end at ：" + endIndex);
            //开启这些线程去下载
            DownloadThread downloadThread = new DownloadThread(downloadUrl, downloadPath, threadId, startIndex, endIndex, countDownLatch);
            downloadThreads.add(downloadThread);
            new Thread(downloadThread).start();
        }
    }

//    private void startFinishThread(String downloadUrl) {
//        FinishThread finishThread = new FinishThread(downloadUrl);
//        new Thread(finishThread).start();
//    }

    private void starProgressTask(List<DownloadThread> downloadThreads, int fileTotalLength) {
        ProgressThread progressThread = new ProgressThread();
        new Thread(progressThread).start();
    }

    @Override
    public void run() {
        try {
            onDownloadStart();
            int contentLength = connManager.getContentLength(downloadUrl);
            LogUtils.d("asset length = " + contentLength);
            if (contentLength > 0) {
                fileTotalLength = contentLength;
                createSameSizeFile(downloadPath, fileTotalLength);
                //开启多个线程下载
                startDownloadThreads(downloadUrl, downloadPath, fileTotalLength, threadCount,
                        countDownLatch, downloadThreads);
                //开启等待完成线程
                //startFinishThread(downloadUrl);
                starProgressTask(downloadThreads, fileTotalLength);
            } else {
                onDownloadFailed(-1, "contentLength < 0");
            }
        } catch (Exception e1) {
            e1.printStackTrace();
            onDownloadFailed(-1, e1.toString());
        }
    }

    private void onDownloadStart() {
        if (downloadListener != null) {
            downloadListener.onStart(downloadUrl);
        }
    }

    private void onDownloadProgress(int downloadSize, int fileTotalLength) {
        if (downloadListener != null) {
            downloadListener.onProgress(downloadSize, fileTotalLength);
        }
    }

    private void onDownloadPause() {
        if (downloadListener != null) {
            downloadListener.onPause(downloadUrl);
        }
    }

    private void onDownloadSuccess(String downloadUrl, String path) {
        if (downloadListener != null) {
            downloadListener.onSuccess(downloadUrl, path);
        }
    }

    private void onDownloadFailed(int code, String message) {
        if (downloadListener != null) {
            downloadListener.onFailed(downloadUrl, code, message);
        }
    }

    class ProgressThread implements Runnable {
        @Override
        public void run() {
            while (true) {
                if (pause) {
                    onDownloadPause();
                    LogUtils.d("download " + downloadUrl + " pause, break loop");
                    break;
                }
                if (countDownLatch.getCount() == 0) {
                    afterDownloadComplete();
                    break;
                }
                int downloadSize = 0;
                for (DownloadThread downloadThread : downloadThreads) {
                    downloadSize += downloadThread.getDownloadSize();
                }
                if (downloadSize == fileTotalLength) {
                    afterDownloadComplete();
                    break;
                } else {
                    onDownloadProgress(downloadSize, fileTotalLength);
                }
            }
        }
    }

    private void afterDownloadComplete() {
        try {
            //下载完成，重命名文件
            File downloadFile = new File(pathManager.downloadPath() + File.separator + getFileName() + ".tmp");
            File destFile = new File(pathManager.downloadPath() + File.separator + getFileName());
            downloadFile.renameTo(destFile);
            LogUtils.d("all download finished, downloadFile.length()=" + downloadFile.length() + " destFile.length=" + destFile.length() + " fileTotalLength=" + fileTotalLength);
            //下载完毕清空临时文件
            for (int threadId = 0; threadId < threadCount; threadId++) {
                new File(pathManager.downloadPath() + File.separator + randomFileStr + threadId + ".txt").delete();
            }
            LogUtils.d("temp file deleted");
            if (destFile.length() == fileTotalLength) {
                LogUtils.d("download success url = " + downloadUrl);
                onDownloadSuccess(downloadUrl, pathManager.downloadPath() + File.separator + getFileName());
            } else {
                LogUtils.d("download " + downloadUrl + " fail destFile.length() != fileTotalLength ");
                destFile.delete();
                onDownloadFailed(-1, "download file finish, but file is illegal");
            }
        } catch (Exception e) {
            e.printStackTrace();
            onDownloadFailed(-1, e.toString());
        }
    }

//    class FinishThread implements Runnable {
//
//        private String randomTempFileStr;
//
//        public FinishThread(String downloadUrl) {
//            if (!TextUtils.isEmpty(downloadUrl))
//                randomTempFileStr = Md5Utils.md5(downloadUrl);
//        }
//
//        @Override
//        public void run() {
//            try {
//                countDownLatch.await();
//                //暂停后不再执行
//                if (pause) {
//                    return;
//                }
//                //下载完成，重命名文件
//                File downloadFile = new File(pathManager.downloadPath() + File.separator + getFileName() + ".tmp");
//                File destFile = new File(pathManager.downloadPath() + File.separator + getFileName());
//                downloadFile.renameTo(destFile);
//                LogUtils.d("all download finished, downloadFile.length()=" + downloadFile.length() + " destFile.length=" + destFile.length() + " fileTotalLength=" + fileTotalLength);
//                //下载完毕清空临时文件
//                for (int threadId = 0; threadId < threadCount; threadId++) {
//                    new File(pathManager.downloadPath() + File.separator + randomTempFileStr + threadId + ".txt").delete();
//                }
//                LogUtils.d("temp file deleted");
//                if (destFile.length() == fileTotalLength) {
//                    LogUtils.d("download success url = " + downloadUrl);
//                    onDownloadSuccess(downloadUrl, pathManager.downloadPath() + File.separator + getFileName());
//                } else {
//                    LogUtils.d("download " + downloadUrl + " fail destFile.length() != fileTotalLength ");
//                    destFile.delete();
//                    onDownloadFailed(-1, "download file finish, but file is illegal");
//                }
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//                onDownloadFailed(-1, "thread was interrupt");
//            }
//        }
//    }

    //下载文件的线程
    class DownloadThread implements Runnable {

        private final int threadId;
        private final CountDownLatch countDownLatch;
        private final String downloadUrl;
        private final String downloadPath;
        private int startIndex;
        private int endIndex;
        private int currentTreadDownloadPosition;

        //构造方法接受每个线程的下载信息
        public DownloadThread(String downloadUrl, String downloadPath, int threadId, int startIndex,
                              int endIndex, CountDownLatch count) {
            this.downloadUrl = downloadUrl;
            this.downloadPath = downloadPath;
            this.threadId = threadId;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.countDownLatch = count;
        }

        public int getDownloadSize() {
            return currentTreadDownloadPosition - startIndex;
        }

        @Override
        public void run() {
            try {
                download(downloadUrl, downloadPath, threadId,
                        startIndex, endIndex);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                countDownLatch.countDown();
            }
        }

        /**
         * 下载，是阻塞方法，直到下载成功
         *
         * @param downloadUrl 下载地址
         * @param startIndex  下载开始位置（如果是单线程下载，开始位置为0）
         * @param endIndex    下载结束位置 （如果是多线程下载，结束位置不是fileLength,是指定的下载点）
         * @throws IOException
         */
        private void download(String downloadUrl, String downloadPath, int threadId, int startIndex, int endIndex) throws Exception {
            //上次下载的位置
            int lastDownloadIndex = startIndex;
            //读取上次下载的结束位置，作为本次下载的开始位置
            File downloadIndexFile = new File(downloadPath + File.separator + randomFileStr + threadId + ".txt");
            if (downloadIndexFile.exists()) {
                BufferedReader bf = new BufferedReader(new InputStreamReader(new FileInputStream(downloadIndexFile)));
                //读取上次下载的位置
                String lastDownloadIndex_str = bf.readLine();
                bf.close();
                try {
                    lastDownloadIndex = Integer.parseInt(lastDownloadIndex_str);
                } catch (Exception e) {
                    lastDownloadIndex = 0;
                }
                LogUtils.d("读取到上次保存的位置，从这个位置继续下载 lastDownloadIndex=" + lastDownloadIndex);
            }
            InputStream downloadStream = connManager.download(downloadUrl, lastDownloadIndex, endIndex);
            if (downloadStream != null) {
                File file = new File(downloadPath + File.separator + getFileName() + ".tmp");
                //创建一个随机访问文件,有指定从某个位置开始写入
                RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rwd");
                File positionFile = new File(downloadPath + File.separator + randomFileStr + threadId + ".txt");
                RandomAccessFile randomAccessFile2 = new RandomAccessFile(positionFile, "rwd");
                //指定当前线程从哪个位置开始写入流
                randomAccessFile.seek(lastDownloadIndex);
                byte[] buffer = new byte[1024];
                int len = 0;
                int total = 0;//当前线程本次下载的字节数
                while ((len = downloadStream.read(buffer)) != -1 && !pause) {
                    //开始写入流信息到文件
                    randomAccessFile.write(buffer, 0, len);
                    total = total + len;
                    //记录当前线程本次的下载位置，保存到文件中，目的是为下次下载时直接从保存的位置开始下载
                    //当前线层下载的位置，其实就是下次下载的开始位置。
                    currentTreadDownloadPosition = lastDownloadIndex + total;
                    //保存本次线程当前下载到的位置
                    randomAccessFile2.seek(0);
                    randomAccessFile2.write((String.valueOf(currentTreadDownloadPosition)).getBytes());
                }
                //释放资源
                downloadStream.close();
                randomAccessFile.close();
                randomAccessFile2.close();
            }
        }
    }

    public String getFileName() {
        if (TextUtils.isEmpty(fileName)) {
            fileName = HttpUtils.getFileName(downloadUrl);
        }
        return fileName;
    }

    public interface DownloadListener {
        void onSuccess(String downloadUrl, String path);

        void onFailed(String downloadUrl, int failCode, String errorMessage);

        void onStart(String downloadUrl);

        void onProgress(int current, int total);

        void onPause(String downloadUrl);
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public String getDownloadPath() {
        return downloadPath;
    }

    public int getFileTotalLength() {
        return fileTotalLength;
    }

    public int getThreadCount() {
        return threadCount;
    }
}

