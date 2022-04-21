package com.lsp.view.activity

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat.Type.statusBars
import com.lsp.view.R

open class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        transparentStatusBar()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            //设置导航栏透明
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }
    private fun transparentStatusBar(){
        // 设置状态栏颜色为透明
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        //状态栏反色
        val controller = ViewCompat.getWindowInsetsController(window.decorView)
        controller?.isAppearanceLightStatusBars = !isDarkMode()
    }

    /**
     * 获取当前是否为深色模式
     * 深色模式的值为:0x21
     * 浅色模式的值为:0x11
     * @return true 为是深色模式   false为不是深色模式
     */
    private fun isDarkMode(): Boolean {
        return resources.configuration.uiMode == 0x21
    }

}