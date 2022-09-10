package com.lsp.view;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.material.color.DynamicColors;
import com.lsp.view.service.DownloadService;

public class MyApplication extends Application implements DefaultLifecycleObserver{
    private static Context context;
    private static DownloadService.DownloadBinder downloadBinder;
    private final ServiceConnection connection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            downloadBinder = (DownloadService.DownloadBinder) iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        DynamicColors.applyToActivitiesIfAvailable(this);

        Intent serviceIntent = new Intent(this, DownloadService.class);
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);

        statusBarHeight();
    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onDestroy(owner);
        unbindService(connection);
    }

    public static Context getContext() {
        return context;
    }

    public static DownloadService.DownloadBinder getDownloadBinder() {
        return downloadBinder;
    }

    //计算状态栏高度
    public int statusBarHeight() {
        int height = 0;
        int resourceId = getResources().getIdentifier(
                "status_bar_height",
                "dimen",
                "android"
        );
        if (resourceId > 0) {
            height = getResources().getDimensionPixelSize(resourceId);
        }
        return height;
    }
}
