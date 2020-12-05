# DownloadModel

使用方法：

1.Add it in your root build.gradle at the end of repositories:
```
allprojects {
    repositories {
	maven { url 'https://jitpack.io' }
    }
}	
```

2.Add the dependency:
```
dependencies {
    implementation 'com.github.renzhenming:DownloadModel:1.0.9'
}
```

3.权限
```
<!--保存下载资源到sd卡需要用到存储权限，如果你不做path的设置，可以不加-->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<!--没有网络权限，你是下载不了东西的-->
<uses-permission android:name="android.permission.INTERNET"></uses-permission>
<!--注意没有这个权限无法拉起安装页面-->
<uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />
```

4.确保你的项目中有对appcompat包的引用,目前下载器内部的引用方式和引用的版本为
```
compileOnly('com.android.support:appcompat-v7:28.0.0')
```

5.如果你的项目支持7.0以上，你需要配置一个FileProvider(当你下载的资源为apk,并且需要用到下载器中的安装方法时，这个配置时必须的,否则可以不加)
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
6.file_paths.xml文件
下载器默认资源下载存储的地址是`context.getFilesDir()+ File.separator+"download_asset"`，注意，这是一个目录。所以默认配置如下
```
<?xml version="1.0" encoding="utf-8"?>
<paths xmlns:android="http://schemas.android.com/apk/res/android">
    <files-path
        name="dm_files" //这个名字你可以随意写，只是个标示，你最好确保它不会和其他的名字重复
        path="download_asset" />
</paths>
```
当然你也可以通过实现IPath接口来自定义你想要的存储路径，比如你可以把下载的资源存放在sd卡下
```
public class MyPathManager implements IPath {
    private final Context context;

    public MyPathManager(Context context){
        this.context = context;
    }
    @Override
    public String downloadPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator+"download_asset";
    }
}
```

这样一来，你要记得更改file_path.xml中的配置
```
<paths xmlns:android="http://schemas.android.com/apk/res/android">
    <external-path
        name="dm_external_storage_root"
        path="download_asset" />
</paths>
```

或者你想存在其他目录，配置相应的file_path即可

7.下载器提供了比较灵活的接口配置，比如如果你想使用okhttp或者其他网络工具来实现下载，你可以通过IConnection接口来实现你的愿望
```
public interface IConnection {
    int getContentLength(String downloadUrl) throws Exception;

    InputStream download(String downloadUrl, int rangeStart, int rangeEnd) throws Exception;
}
```
你需要自己创建一个类来实现IConnection接口，然后这样设置给下载器即可
```
DownloadManager.getInstance(context).setConnManager(new MyConnection());
```

8.同样道理，如果你想自己处理缓存，你可以使用ICache接口
```
public interface ICache<T> {
    void setCache(String uniqueKey, T t);

    T getCache(String uniqueKey);

    List<T> getCacheByPkgName(String packageName);

    int updateCache(String uniqueKey, T t);
}
```
通过setCacheManager方法设置给下载器
```
DownloadManager.getInstance(context).setCacheManager(new MyCache())
```

9.下载器内部的线程都是通过线程池管理的，如果你有自定义线程池的需要，IThreadPool你会用得上的
```
public interface IThreadPool {
    void execute(Runnable runnable);
    void cancel(Runnable runnable);
}
```
DownloadManager.getInstance(context).setThreadPool(new MyThreadPoo())

以上提供的这些能力，你完全可以使用内部默认的配置，当然如果你不嫌麻烦或者有定制的需求，那就放心的用吧

怎么用呢？

 ###开启下载（从暂停的状态继续下载同样是这个方法）
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
 ```
 List<DownloadInfo> downloadList = DownloadManager.getInstance(context).getDownloadInfoByPkgName(context.getPackageName());
 ```
	
 ###监听下载状态
 ```
 DownloadManager.getInstance(context).registerObserver(new DownloadManager.DownloadObserver() {
      @Override
      public void onDownloadStateChanged(DownloadInfo info) {
           //从info中可以拿到当前下载的进度状态等信息，然后去更新你的界面吧，比如这样
	   
	   ......
	   
	   switch (state) {
            case DownloadInfo.STATE_NONE:
                pbProgress.setPercent(0);
                // 文字为下载
                pbProgress.setText("下载");
                break;
            case DownloadInfo.STATE_WAITING:
                pbProgress.setPercent(progress);
                pbProgress.setText("等待");
                break;
            case DownloadInfo.STATE_DOWNLOADING:
                pbProgress.setPercent(progress);
                break;
            case DownloadInfo.STATE_PAUSE:
                pbProgress.setPercent(progress);
                pbProgress.setText("继续");
                break;
            case DownloadInfo.STATE_ERROR:
                pbProgress.setPercent(progress);
                pbProgress.setText("重试");
                break;
            case DownloadInfo.STATE_SUCCESS:
                pbProgress.setPercent(1);
                pbProgress.setText("安装");
                break;

            default:
                break;
        }
      }
});
 ```
 ###移除监听
 ```
 DownloadManager.getInstance(context).unregisterObserver(downloadObserver);
 ```
