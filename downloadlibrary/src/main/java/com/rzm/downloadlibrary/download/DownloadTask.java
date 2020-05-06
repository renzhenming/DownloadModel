package com.rzm.downloadlibrary.download;

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
    //资源下载后保存的路径
    private IPath pathManager = new DefaultPathManager();
    //网络工具
    private IConnection connManager = new DefaultConnectionManager();
    //开启几个线程下载
    public int threadCount = 1;//线程数量
    //资源长度的请求的超时时间，单位毫秒
    private int contentLengthTimeout = 20 * 1000;
    //发起下载请求的超时时间
    private int downloadRequestTimeout = 20 * 1000;
    //从下载链接中截取出来的文件名
    private String fileName;
    //下载监听
    private DownloadListener downloadListener;
    //保存每个线程信息
    private ArrayList<DownloadThread> downloadThreads;
    //待下载资源的大小
    private int fileTotalLength;
    private CountDownLatch countDownLatch;
    //资源下载后保存的路径
    private String downloadPath;
    //下载是否暂停
    private volatile boolean pause = false;

    public DownloadTask() {

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
     * 设置获取资源长度的请求的超时时间，单位毫秒
     *
     * @param timemills
     * @return
     */
    public DownloadTask contentLengthTimeout(int timemills) {
        this.contentLengthTimeout = timemills;
        return this;
    }

    /**
     * 设置下载请求的超时时间，单位毫秒
     *
     * @param timemills
     * @return
     */
    public DownloadTask downloadRequestTimeout(int timemills) {
        this.downloadRequestTimeout = timemills;
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
    public void pause(boolean pause){
        this.pause = pause;
    }

    public DownloadTask build() {
        if (!new File(pathManager.downloadPath()).isDirectory())
            new File(pathManager.downloadPath()).mkdirs();
        downloadThreads = new ArrayList<>(threadCount);
        countDownLatch = new CountDownLatch(threadCount);
        downloadPath = pathManager.downloadPath();
        pause = false;
        return this;
    }

    private int getContentLength(String downloadUrl, int timeOut) throws IOException {
        return connManager.getContentLength(downloadUrl,timeOut);
    }

    private boolean createSameSizeFile(String downloadPath,int fileTotalLength) throws IOException {
        //创建一个和服务器资源大小一样的临时文件，来占位。
        File file = new File(downloadPath + File.separator + getFileName() + ".tmp");
        //创建一个随机访问文件对象    file：文件  mode：文件的模式 rwd：读写写入硬盘
        RandomAccessFile randFile = new RandomAccessFile(file, "rwd");
        //为随机文件设置一个文件大小，占位
        randFile.setLength(fileTotalLength);
        randFile.close();
        return true;
    }

    private void startDownloadThreads(String downloadUrl, String downloadPath, int fileTotalLength, int threadCount, CountDownLatch countDownLatch, int downloadRequestTimeout, List<DownloadThread> downloadThreads) {
        //根据资源的大小和线程数量计算每个线程下载文件的大小，还要计算每个线程下载的开始位置和结束位置。
        int blockSize = fileTotalLength / threadCount;//每个线程下载的大小
        //循环计算每个线程的开始位置和结束位置
        LogUtils.d("开启" + threadCount + "个线程开始下载，每个线程需要下载的资源长度为：" + blockSize);
        for (int threadId = 0; threadId < threadCount; threadId++) {
            int startIndex = threadId * blockSize;//线程的开始下载位置
            int endIndex = (threadId + 1) * blockSize - 1;//下载的结束位置
            //计算最后一个线程的结束位置
            if (threadId == threadCount - 1) {
                endIndex = fileTotalLength - 1;//最后一个线程下载的结束位置
            }
            LogUtils.d("线程" + threadId + "下载的开始位置:" + startIndex + " 结束下载的位置：" + endIndex);
            //开启这些线程去下载
            DownloadThread downloadThread = new DownloadThread(downloadUrl, downloadPath,threadId,startIndex, endIndex, countDownLatch, downloadRequestTimeout);
            downloadThreads.add(downloadThread);
            new Thread(downloadThread).start();
        }
    }

    private void startFinishThread(String downloadUrl) {
        Thread thread1 = new Thread(new FinishThread(downloadUrl));
        thread1.start();
    }

    private void starProgressTask(List<DownloadThread> downloadThreads,int fileTotalLength,DownloadListener downloadListener) throws InterruptedException {
        while (true) {
            int downloadSize = 0;
            for (DownloadThread downloadThread : downloadThreads) {
                downloadSize += downloadThread.getDownloadSize();
            }
            float percent = (downloadSize * 1.0f) / (fileTotalLength * 1.0f) * 100;
            if (downloadListener != null) {
                if (pause){
                    downloadListener.onPause();
                    LogUtils.d("下载暂停，跳出进度循环");
                    break;
                }else {
                    if (percent == 0){
                        continue;
                    }
                    LogUtils.d("总下载进度" + percent + "% downloadSize=" + downloadSize + " fileTotalLength=" + fileTotalLength);
                    downloadListener.onProgress(downloadSize, fileTotalLength);
                }
            }
            if (percent >= 99) {
                break;
            }
        }
    }

    @Override
    public void run() {
        try {
            if (downloadListener != null){
                downloadListener.onStart();
            }
            int contentLength = getContentLength(downloadUrl, contentLengthTimeout);
            LogUtils.d("获取到资源长度：" + contentLength);
            if (contentLength > 0) {
                fileTotalLength = contentLength;
                createSameSizeFile(downloadPath,fileTotalLength);
                //开启多个线程下载
                startDownloadThreads(downloadUrl,downloadPath,fileTotalLength,threadCount,
                        countDownLatch,downloadRequestTimeout,downloadThreads);
                //开启等待完成线程
                startFinishThread(downloadUrl);
                starProgressTask(downloadThreads,fileTotalLength,downloadListener);
            } else {
                if (downloadListener != null) {
                    downloadListener.onFailed(-1, "contentLength < 0");
                }
            }
        } catch (Exception e1) {
            e1.printStackTrace();
            if (downloadListener != null) {
                downloadListener.onFailed(-1, e1.toString());
            }
        }
    }

    class FinishThread implements Runnable {

        private String randomTempFileStr;

        public FinishThread(String downloadUrl) {
            if (!TextUtils.isEmpty(downloadUrl))
                randomTempFileStr = Md5Utils.md5(downloadUrl);
        }

        @Override
        public void run() {
            try {
                countDownLatch.await();
                //暂停后不再执行
                if (pause){
                    return;
                }
                //下载完成，重命名文件
                File downloadFile = new File(pathManager.downloadPath() + File.separator + getFileName() + ".tmp");
                File destFile = new File(pathManager.downloadPath() + File.separator + getFileName());
                downloadFile.renameTo(destFile);
                LogUtils.d("全部下载完成 downloadFile.length()=" + downloadFile.length() + " destFile.length=" + destFile.length() + " fileTotalLength=" + fileTotalLength);
                //下载完毕清空临时文件
                for (int threadId = 0; threadId < threadCount; threadId++) {
                    new File(pathManager.downloadPath() + File.separator + randomTempFileStr+ threadId + ".txt").delete();
                }
                LogUtils.d("删除临时文件完成");
                if (downloadListener != null && destFile.length() == fileTotalLength) {
                    downloadListener.onSuccess(pathManager.downloadPath() + File.separator + getFileName());
                } else {
                    destFile.delete();
                    if (downloadListener != null) {
                        String codeStr = "";
                        String errorStr = "";
                        for (DownloadThread downloadThread : downloadThreads) {
                            codeStr += downloadThread.getErrorMsg()[0] + ",";
                            errorStr += downloadThread.getErrorMsg()[1] + ",";
                        }
                        downloadListener.onFailed(-1, codeStr + errorStr);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                if (downloadListener != null) {
                    downloadListener.onFailed(-1, "thread was interrupt");
                }
            }
        }
    }

    //下载文件的线程
    class DownloadThread implements Runnable {

        private final int threadId;
        private String[] errorMsg = new String[2];
        private int downloadRequestTimeout = 20 * 1000;
        private final CountDownLatch countDownLatch;
        private final String downloadUrl;
        private final String downloadPath;
        private int startIndex;
        private int endIndex;
        private int currentTreadDownloadPosition;

        //构造方法接受每个线程的下载信息
        public DownloadThread(String downloadUrl, String downloadPath,int threadId, int startIndex,
                              int endIndex, CountDownLatch count, int downloadRequestTimeout) {
            this.downloadUrl = downloadUrl;
            this.downloadPath = downloadPath;
            this.threadId = threadId;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.countDownLatch = count;
            this.downloadRequestTimeout = downloadRequestTimeout;
        }

        public int getDownloadSize() {
            return currentTreadDownloadPosition - startIndex;
        }

        public String[] getErrorMsg() {
            return errorMsg;
        }

        @Override
        public void run() {
            try {
                String[] downloadMsg = download(downloadUrl, downloadPath,threadId,
                        startIndex, endIndex, downloadRequestTimeout);
                errorMsg[0] = downloadMsg[0];
                errorMsg[1] = downloadMsg[1];
            } catch (Exception e) {
                e.printStackTrace();
                errorMsg[0] = String.valueOf(-1);
                errorMsg[1] = e.toString();
            } finally {
                countDownLatch.countDown();
            }
        }

        /**
         * 下载，是阻塞方法，直到下载成功
         *
         * @param downloadUrl            下载地址
         * @param startIndex             下载开始位置（如果是单线程下载，开始位置为0）
         * @param endIndex               下载结束位置 （如果是多线程下载，结束位置不是fileLength,是指定的下载点）
         * @param downloadRequestTimeout 请求超时时间
         * @return errorMsg 是一个数组，记录了本次请求的失败原因，errorMsg[0]是code,errorMsg[1]是error信息
         * @throws IOException
         */
        private String[] download(String downloadUrl, String downloadPath, int threadId,int startIndex, int endIndex, int downloadRequestTimeout) throws IOException {
            String randomFileStr = Md5Utils.md5(downloadUrl);
            String[] errorMsg = new String[2];
            //上次下载的位置
            int lastDownloadIndex = startIndex;
            //读取上次下载的结束位置，作为本次下载的开始位置
            File downloadIndexFile = new File(downloadPath + File.separator + randomFileStr+ threadId + ".txt");
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
                LogUtils.d("读取到上次保存的位置，从这个位置继续下载 lastDownloadIndex="+lastDownloadIndex);
            }
            InputStream downloadStream = connManager.download(downloadUrl, lastDownloadIndex, endIndex, downloadRequestTimeout);
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
                    //保存本次线程下载的位置
                    randomAccessFile2.seek(0);
                    randomAccessFile2.write((String.valueOf(currentTreadDownloadPosition)).getBytes());//保存本次下载的位置
                }
                //释放资源
                downloadStream.close();
                randomAccessFile.close();
                randomAccessFile2.close();
            } else {
                String[] responseInfo = connManager.getResponseInfo();
                errorMsg[0] = responseInfo[0];
                errorMsg[1] = responseInfo[1];
            }
            return errorMsg;
        }
    }

    public String getFileName(){
        if (TextUtils.isEmpty(fileName)) {
            fileName = HttpUtils.getFileName(downloadUrl);
        }
        return fileName;
    }

    public interface DownloadListener {
        void onSuccess(String path);
        void onFailed(int failCode, String errorMessage);
        void onStart();
        void onProgress(int current, int total);
        void onPause();
    }
}
