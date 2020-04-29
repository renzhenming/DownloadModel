package com.rzm.downloadmodle;

import android.Manifest;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static String HOST = "http://192.168.0.102:8080/";
    public  String apkUrl[] =new String[]
            {"download/cn.etouch.ecalendar.apk",
            "download/cn.goapk.market.apk",
            "download/cn.kuwo.player.apk",
            "download/com.achievo.vipshop.apk",
            "download/com.autonavi.minimap.apk",
            "download/com.baidu.tieba.apk",
            "download/com.changba.apk",
            "download/com.eg.android.AlipayGphone.apk",
            "download/com.estrongs.android.pop.apk",
            "download/com.feelingtouch.dragon2.apk",
            "download/com.medapp.man.apk",
            "download/com.mogujie.apk",
            "download/com.sankuai.meituan.apk",
            "download/com.sds.android.ttpod.apk",
            "download/com.shuqi.controller.apk" ,
            "download/com.snda.wifilocating.apk"};
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
            info.downloadUrl = HOST+apkUrl[i];
            list.add(info);
        }
        recyclerView.setAdapter(new AppAdapter(list,this));
    }
}
