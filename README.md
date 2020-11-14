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
	implementation 'com.github.renzhenming:DownloadModel:xxxx'
}
```

确保你的项目中有对appcompat包的引用,目前下载器内部的引用方式和引用的版本为
```
compileOnly('com.android.support:appcompat-v7:28.0.0')
```

如果你的项目支持7.0以上，你需要配置一个FileProvider(当你下载的资源为apk,并且需要用到下载器中的安装方法时，这个配置时必须的,否则可以不加)
```
<!--Android 7.0适配-应用之间共享文件 -->
<provider
    android:name="android.support.v4.content.FileProvider"
    android:authorities="${applicationId}.rzm.download.provider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>        
```
file_paths.xml文件
下载器默认资源下载存储的地址是`context.getFilesDir()+ File.separator+"download_asset"`，注意，这是一个目录。所以默认配置如下
```
<?xml version="1.0" encoding="utf-8"?>
<paths xmlns:android="http://schemas.android.com/apk/res/android">
    <files-path
        name="download_asset"
        path="." />
</paths>
```
当然你也可以通过实现IPath接口来自定义你想要的存储路径，比如你可以把下载的资源存放在sd卡下
```
Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator+"download_asset"
```
这样一来，你要记得更改file_path.xml中的配置，切记

 ###开启下载
 ```
 DownloadManager.getInstance(context).download(uniqueKey);
 ```
 ###暂停下载
  ```
 DownloadManager.getInstance(context).pause(uniqueKey);
 ```
 ###获取当前下载状态
 ```
 DownloadInfo downloadInfo = DownloadManager.getInstance(context).getDownloadInfo(uniqueKey);
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
 
 ###如果下载的是apk,还可以根据包名查询
 List<DownloadInfo> downloadList = DownloadManager.getInstance(context).getDownloadInfoByPkgName(context.getPackageName());
	
 ###监听下载状态
 ```
 DownloadManager.getInstance(context).registerObserver(new DownloadManager.DownloadObserver() {
      @Override
      public void onDownloadStateChanged(DownloadInfo info) {
           //从info中可以拿到当前下载的进度状态等信息
      }
});
 ```
 ###移除监听
 ```
 DownloadManager.getInstance(context).unregisterObserver(downloadObserver);
 ```
