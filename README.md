# DownloadModel
下载器探索

使用方法：

Add it in your root build.gradle at the end of repositories:
```
allprojects {
	repositories {
		maven { url 'https://jitpack.io' }
	}
}	
```
Add the dependency:
```
dependencies {
	implementation 'com.github.renzhenming:DownloadModel:v1.0.2'
}
```
 ###开启下载
 ```
 DownloadManager.getInstance(context).download(downloadUrl);
 ```
 ###暂停下载
  ```
 DownloadManager.getInstance(context).pause(downloadUrl);
 ```
 ###获取当前下载状态
 ```
 DownloadInfo downloadInfo = mDownloadManager.getDownloadInfo(downloadUrl);
 if (downloadInfo == null) {
    // 没有下载过
     mCurrentState = DownloadManager.STATE_NONE;
     mProgress = 0;
 } else {
     // 之前下载过, 以缓存中的对象的状态为准
     mCurrentState = downloadInfo.currentState;
     mProgress = downloadInfo.getProgress();
 }
 ```
 ###监听下载状态
 ```
 DownloadManager.getInstance(context).registerObserver(new DownloadManager.DownloadObserver() {
      @Override
      public void onDownloadStateChanged(DownloadInfo info) {
                
      }

      @Override
      public void onDownloadProgressChanged(DownloadInfo info) {

      }
});
 ```
