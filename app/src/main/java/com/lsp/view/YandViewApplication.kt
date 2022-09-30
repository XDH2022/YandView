package com.lsp.view

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import android.content.ServiceConnection
import android.content.ComponentName
import android.content.Context
import android.os.IBinder
import com.lsp.view.service.DownloadService.DownloadBinder
import com.google.android.material.color.DynamicColors
import android.content.Intent
import com.lsp.view.service.DownloadService
import androidx.lifecycle.LifecycleOwner

class YandViewApplication : Application(), DefaultLifecycleObserver {
    private val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            downloadBinder = iBinder as DownloadBinder
        }

        override fun onServiceDisconnected(componentName: ComponentName) {}
    }

    override fun onCreate() {
        super<Application>.onCreate()
        context = applicationContext
        DynamicColors.applyToActivitiesIfAvailable(this)
        val serviceIntent = Intent(this, DownloadService::class.java)
        bindService(serviceIntent, connection, BIND_AUTO_CREATE)
        statusBarHeight()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        unbindService(connection)
    }

    //计算状态栏高度
    fun statusBarHeight(): Int {
        var height = 0
        val resourceId = resources.getIdentifier(
            "status_bar_height",
            "dimen",
            "android"
        )
        if (resourceId > 0) {
            height = resources.getDimensionPixelSize(resourceId)
        }
        return height
    }

    companion object {
        var context: Context? = null
            private set
        var downloadBinder: DownloadBinder? = null
            private set
    }
}