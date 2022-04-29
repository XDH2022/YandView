package com.lsp.view.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.os.*
import android.util.Log
import com.lsp.view.bean.Post
import com.lsp.view.util.Code
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.ArrayList
import kotlin.concurrent.thread

class DownloadService : Service() {
    private val mBinder = DownloadBinder(this)

    class DownloadBinder(val context: Context) : Binder() {
        fun sendOK(file:File, handler: Handler){
            val msg = Message.obtain()
            msg.what = Code.OK
            msg.obj = file
            handler.sendMessage(msg)
        }


        fun sendError(handler: Handler){
            val msg = Message.obtain()
            msg.what = Code.DOWNLOADERROR
            msg.obj = null
            handler.sendMessage(msg)
        }
        fun downloadPic(file_url: String, end: String,handler: Handler) {
            thread {
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
                            sendOK(file, handler)
                            return@thread

                        } else {
                            Log.e("Test", "errorNet")
                            file.delete()
                            sendError(handler)
                            return@thread
                        }


                    } catch (e: Exception) {
                        e.printStackTrace()
                        sendError(handler)
                        return@thread
                    } finally {
                        fos.close()
                    }

                } else {
                    FileD.mkdirs()
                    downloadPic(file_url, end, handler)
                }
                sendError(handler)
            }
        }
    }



    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }
}