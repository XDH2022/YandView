package com.lsp.view.util

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.widget.Toast
import com.lsp.view.MyApplication

object DownloadUtil {

    fun download(file_url: String?, file_ext: String?,md5: String?){
        Toast.makeText(MyApplication.getContext(), "开始保存", Toast.LENGTH_SHORT).show()

        if (file_url != null) {
            if (file_ext != null) {
                MyApplication.getDownloadBinder().downloadPic(file_url, file_ext,handler,md5)
            }
        }
    }

    private val handler = object : Handler(Looper.myLooper()!!){
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when(msg.what){
                CallBackStatus.OK.ordinal ->{
                    Toast.makeText(MyApplication.getContext(), "保存成功", Toast.LENGTH_SHORT).show()
                }

                CallBackStatus.DOWNLOADERROR.ordinal -> {
                    Toast.makeText(MyApplication.getContext(), "下载异常", Toast.LENGTH_SHORT).show()

                }
                CallBackStatus.MD5COMPAREERROR.ordinal -> {
                    Toast.makeText(MyApplication.getContext(), "文件下载异常，MD5对比失败", Toast.LENGTH_SHORT).show()

                }
            }

        }


    }
}