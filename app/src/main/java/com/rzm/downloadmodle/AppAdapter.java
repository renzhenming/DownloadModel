package com.rzm.downloadmodle;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.List;

public class AppAdapter extends RecyclerView.Adapter {
    private final List<AppInfo> list;
    private final Context context;

    public AppAdapter(List<AppInfo> list, Context context){
        this.list = list;
        this.context = context;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.app_info_view, viewGroup, false);
        AppHolder appHolder = new AppHolder(view);
        return appHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        AppHolder appHolder = (AppHolder) viewHolder;
        appHolder.bind(list.get(i));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
