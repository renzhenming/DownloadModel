package com.rzm.downloadmodle;

import android.Manifest;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.rzm.downloadlibrary.utils.Md5Utils;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static String HOST = "http://192.168.0.107:8080/";
    public String apkUrl[] = new String[]
            {"download/cn.goapk.market.apk",
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
                    "download/com.shuqi.controller.apk",
                    "download/com.snda.wifilocating.apk"};

    public String remoteUrl[] = new String[]
            {"https://imtt.dd.qq.com/16891/apk/331C4209508B757889510B1F2CC1F2D1.apk",
                    "https://imtt.dd.qq.com/16891/apk/D666376AF755BF5FDD5749D74EA8DE00.apk",
                    "https://imtt.dd.qq.com/16891/apk/CC704E92B9F62C1ABB251591B6757285.apk",
                    "https://imtt.dd.qq.com/16891/apk/3A978AFC298B5DF5A692AF89C1AAA135.apk",
                    "https://imtt.dd.qq.com/16891/apk/0DF5D9BD8BE2E46D1C212402EA18BC7F.apk",
                    "https://imtt.dd.qq.com/16891/apk/1B9A41070168C4F3E60DA9AB462858E1.apk",
                    "https://imtt.dd.qq.com/16891/apk/D3B1E8522F32826E9489A40EA6069D5F.apk",
                    "https://imtt.dd.qq.com/16891/apk/E43F654573F041E3D2F7C1399484296B.apk",
                    "https://imtt.dd.qq.com/16891/apk/09D3845A5D20BC43753427E6A63051AF.apk",
                    "https://imtt.dd.qq.com/16891/apk/EAAA23790E9A633BF6D9AF48FC03AA91.apk",
                    "https://imtt.dd.qq.com/16891/apk/0252670A109E3886370931CFA284C6E3.apk"};
    private RecyclerView recyclerView;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ArrayList<AppInfo> list = new ArrayList<>();
        for (int i = 0; i < 11; i++) {

            AppInfo info = new AppInfo();
            info.id = i + "";
            info.name = "应用" + i;
            info.downloadUrl = remoteUrl[i];
            info.downloadMd5 = Md5Utils.md5(remoteUrl[i]+"_"+i);
            list.add(info);
        }
        recyclerView.setAdapter(new AppAdapter(list, this));
    }
}
