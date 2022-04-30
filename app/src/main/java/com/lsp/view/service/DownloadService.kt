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
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
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
        fun downloadPic(file_url: String, end: String,handler: Handler,md5 : String?) {

            thread {
                val FileD =
                    File("${Environment.getExternalStorageDirectory()}/${Environment.DIRECTORY_PICTURES}/LspMake/")
                if (FileD.exists()) {
                    val file =
                        File("${Environment.getExternalStorageDirectory()}/${Environment.DIRECTORY_PICTURES}/LspMake/$md5.$end")
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
                            if (md5==getFileMD5(file.path)){
                                sendOK(file, handler)
                            }else{
                                file.delete()
                                sendMD5Error(handler)
                            }

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
                    downloadPic(file_url, end, handler,md5)
                }
                sendError(handler)
            }
        }

        /**
         * 获取文件MD5
         */
        fun getFileMD5(path: String?): String? {
            if (path.isNullOrEmpty()) {
                return null
            }
            var digest: MessageDigest? = null
            var fileIS: FileInputStream? = null
            val buffer = ByteArray(1024)
            var len = 0
            try {
                digest = MessageDigest.getInstance("MD5")
                val oldF = File(path)
                fileIS = FileInputStream(oldF)
                while (fileIS.read(buffer).also { len = it } != -1) {
                    digest.update(buffer, 0, len)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }finally {
                fileIS?.close()
            }
            return bytesToHexString(digest?.digest())
        }

        fun bytesToHexString(src: ByteArray?): String? {
            val result = StringBuilder("")
            if (src?.isEmpty()==true) {
                return null
            }

            src?.forEach {
                var i = it.toInt()
                //这里需要对b与0xff做位与运算，
                //若b为负数，强制转换将高位位扩展，导致错误，
                //故需要高位清零
                val hexStr = Integer.toHexString(i and 0xff)
                //若转换后的十六进制数字只有一位，
                //则在前补"0"
                if (hexStr.length == 1) {
                    result.append(0)
                }
                result.append(hexStr)
            }
            return result.toString()
        }
        fun sendMD5Error(handler: Handler){
            val msg = Message.obtain()
            msg.what = Code.MD5ERROR
            msg.obj = null
            handler.sendMessage(msg)
        }
    }



    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }
}