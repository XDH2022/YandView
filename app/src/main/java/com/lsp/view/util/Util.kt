package com.lsp.view.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.widget.Toast
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.lsp.view.R
import com.lsp.view.YandViewApplication
import java.io.File
import kotlin.concurrent.thread

object Util {
    fun share(url:String,context: Context){
        thread {
            val bitmap =  Glide.with(context).asBitmap().load(url).submit().get()
            val path = File("${context.cacheDir}/image")

            if (!path.exists())
                path.mkdirs()

            val file = File("${context.cacheDir}/image/cache.png")
            if (file.exists()){
                file.delete()
            }

            file.outputStream().apply {
                bitmap.compress(Bitmap.CompressFormat.PNG,100,this)
            }

            val imageUri = FileProvider.getUriForFile(
                context,
                "com.lsper.view.fileprovider",
                file
            )

            context.grantUriPermission("com.lsper.view",imageUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "image/*"
            shareIntent.putExtra(Intent.EXTRA_STREAM,imageUri)

            val intent = Intent.createChooser(shareIntent,R.string.title_share.toString())

            val resInfoList: List<ResolveInfo> = context.packageManager
                .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)

            for (resolveInfo in resInfoList) {
                val packageName = resolveInfo.activityInfo.packageName
                context.grantUriPermission(
                    packageName,
                    imageUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }

            context.startActivity(intent)

        }
    }

    fun download(file_url: String?, file_ext: String?,md5: String?){
        Toast.makeText(YandViewApplication.context, R.string.toast_download_start, Toast.LENGTH_SHORT).show()

        if (file_url != null) {
            if (file_ext != null) {
                YandViewApplication.downloadBinder?.downloadPic(file_url, file_ext,handler,md5)
            }
        }
    }

    private val handler = object : Handler(Looper.myLooper()!!){
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when(msg.what){
                CallBackStatus.OK.ordinal ->{
                    Toast.makeText(YandViewApplication.context, R.string.toast_download_success, Toast.LENGTH_SHORT).show()
                }

                CallBackStatus.DOWNLOADERROR.ordinal -> {
                    Toast.makeText(YandViewApplication.context, R.string.toast_download_fail, Toast.LENGTH_SHORT).show()

                }
                CallBackStatus.MD5COMPAREERROR.ordinal -> {
                    Toast.makeText(YandViewApplication.context, R.string.toast_compar_md5_fail, Toast.LENGTH_SHORT).show()

                }
                CallBackStatus.FILEEXISTS.ordinal -> {
                    Toast.makeText(YandViewApplication.context, R.string.toast_file_exist, Toast.LENGTH_SHORT).show()

                }
            }

        }


    }
}