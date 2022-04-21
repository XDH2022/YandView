package com.lsp.view.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.os.Binder
import android.os.Environment
import android.os.IBinder
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

class DownloadService : Service() {
    private val mBinder = DownloadBinder(this)

    class DownloadBinder(val context: Context) : Binder() {
        fun downloadPic(file_url: String, end: String): Boolean {
            val time = System.currentTimeMillis()

            val FileD =
                File("${Environment.getExternalStorageDirectory()}/${Environment.DIRECTORY_PICTURES}/LspMake/")
            if (FileD.exists()) {
                val file =
                    File("${Environment.getExternalStorageDirectory()}/${Environment.DIRECTORY_PICTURES}/LspMake/$time.$end")
                val fos = FileOutputStream(file)
                try {

                    val client = OkHttpClient()
                    val request = Request.Builder()
                        .url(file_url)
                        .build()
                    val response = client.newCall(request).execute()
                    val responseData = response.body()?.bytes()
                    if (response.code() == 200) {
                        fos.write(responseData)

                        //通知媒体更新
                        MediaScannerConnection.scanFile(
                            context, arrayOf(file.path),
                            null, null
                        )

                        return true

                    } else {
                        Log.e("Test", "errorNet")
                        file.delete()
                        return false
                    }


                } catch (e: Exception) {
                    e.printStackTrace()
                    return false
                } finally {
                    fos.close()
                }

            } else {
                FileD.mkdirs()
                downloadPic(file_url, end)
            }
            return false
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }
}