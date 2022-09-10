package com.lsp.view.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import java.io.File
import kotlin.concurrent.thread


object ShareUtil {
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

            context.grantUriPermission("com.lsper.view",imageUri,Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "image/*"
            shareIntent.putExtra(Intent.EXTRA_STREAM,imageUri)

            val intent = Intent.createChooser(shareIntent,"分享")

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
}