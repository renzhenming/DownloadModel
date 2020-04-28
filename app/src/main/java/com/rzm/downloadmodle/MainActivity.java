package com.rzm.downloadmodle;

import android.Manifest;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.rzm.downloadmodle.down.DownloadInfo;
import com.rzm.downloadmodle.down.DownloadManager;
import com.rzm.downloadmodle.down.LogUtils;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public  String apkUrl[] =new String[]
            {"http://192.168.0.103:8080/download/cn.etouch.ecalendar.apk",
            "http://192.168.0.103:8080/download/cn.goapk.market.apk",
            "http://192.168.0.103:8080/download/cn.kuwo.player.apk",
            "http://192.168.0.103:8080/download/com.achievo.vipshop.apk",
            "http://192.168.0.103:8080/download/com.autonavi.minimap.apk",
            "http://192.168.0.103:8080/download/com.baidu.tieba.apk",
            "http://192.168.0.103:8080/download/com.changba.apk",
            "http://192.168.0.103:8080/download/com.eg.android.AlipayGphone.apk",
            "http://192.168.0.103:8080/download/com.estrongs.android.pop.apk",
            "http://192.168.0.103:8080/download/com.feelingtouch.dragon2.apk",
            "http://192.168.0.103:8080/download/com.medapp.man.apk",
            "http://192.168.0.103:8080/download/com.mogujie.apk",
            "http://192.168.0.103:8080/download/com.sankuai.meituan.apk",
            "http://192.168.0.103:8080/download/com.sds.android.ttpod.apk",
            "http://192.168.0.103:8080/download/com.shuqi.controller.apk" ,
            "http://192.168.0.103:8080/download/com.snda.wifilocating.apk"};
    private RecyclerView recyclerView;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},0);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ArrayList<AppInfo> list = new ArrayList<>();
        for (int i = 0; i < 16; i++) {
            AppInfo info = new AppInfo();
            info.id = i+"";
            info.name ="应用"+i;
            info.downloadUrl = apkUrl[i];
            list.add(info);
        }
        recyclerView.setAdapter(new AppAdapter(list,this));
    }

    public void onClick(View v) {
        DownloadManager.getInstance().registerObserver(new DownloadManager.DownloadObserver() {
            @Override
            public void onDownloadStateChanged(DownloadInfo info) {
                LogUtils.d("info.currentState "+info.currentState);
            }

            @Override
            public void onDownloadProgressChanged(DownloadInfo info) {
                LogUtils.d("info.getProgress "+info.getProgress());
            }
        });
        final AppInfo info = new AppInfo();
        info.id = "12345";
        info.size = 58775165;
        info.downloadUrl = apkUrl[0];
        info.name = "应用名";
        DownloadManager.getInstance().download(info);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                LogUtils.d("十秒后暂停");
                DownloadManager.getInstance().pause("12345");
            }
        },10*1000);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                LogUtils.d("20秒后再次开启");
                DownloadManager.getInstance().download(info);
            }
        },20*1000);
    }
}
