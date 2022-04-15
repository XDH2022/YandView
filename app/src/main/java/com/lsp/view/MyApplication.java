package com.lsp.view;

import android.app.Application;

import com.google.android.material.color.DynamicColors;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        DynamicColors.applyToActivitiesIfAvailable(this);
        statusBarHeight();

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
