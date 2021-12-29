package com.lsp.view.pic

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.github.chrisbanes.photoview.PhotoView
import com.google.android.flexbox.*
import com.google.android.material.snackbar.Snackbar
import com.lsp.view.R
import com.lsp.view.bean.Author
import com.lsp.view.bean.ID
import com.lsp.view.bean.Size
import com.lsp.view.bean.Tags
import com.lsp.view.main.MainActivity
import com.lsp.view.service.DownloadService
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.*
import java.lang.Exception
import kotlin.collections.ArrayList
import kotlin.concurrent.thread
import kotlin.math.log
import kotlin.properties.Delegates

class PicActivity : AppCompatActivity() {


    private val tagList = ArrayList<Tags>()
    private val authorList = ArrayList<Author>()
    private val idList = ArrayList<ID>()
    private val time = System.currentTimeMillis()
    private val sizeList = ArrayList<Size>()
    private lateinit var image:ImageView
    private lateinit var photoView:PhotoView
    private var shortAnnotationDuration by Delegates.notNull<Int>()
    lateinit var downloadBinder:DownloadService.DownloadBinder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pic)
        val serviceIntent = Intent(this,DownloadService::class.java)
        bindService(serviceIntent,connection, Context.BIND_AUTO_CREATE)

        image =findViewById<ImageView>(R.id.titleImage)
        shortAnnotationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)


        val intent = intent
        val tags = intent.getStringExtra("tags")
        if (tags != null) {
            loadTags<Tags>(tags)
        }




        val author = intent.getStringExtra("author")
        if (author!=null){
            loadTags<Author>(author)
        }
        val file_size = intent.getStringExtra("file_size")
        if (file_size!=null){
            loadTags<Size>(file_size)
        }

        val sharedPreferences = getSharedPreferences("FirstRun",0)
        val firstRun = sharedPreferences.getBoolean("FirstRun",true)
        if (firstRun){
            val sharedPreferences = getSharedPreferences("FirstRun",0).edit()
            sharedPreferences.putBoolean("FirstRun",false).apply()
            val FileD = File("${Environment.getExternalStorageDirectory()}/${Environment.DIRECTORY_PICTURES}/LspMake")
            FileD.mkdirs()

        }


        val id = intent.getStringExtra("id")
        if (id!=null){
            loadTags<ID>(id)
        }
        val sample_url = intent.getStringExtra("sample_url")
        sample_url?.let {
            loadPic(it)
        }

        val file_url = intent.getStringExtra("file_url")
        val file_ext = intent.getStringExtra("file_ext")
        val fbtn = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(
            R.id.fbtn
        )

        val path = "${Environment.getExternalStorageDirectory()}/${Environment.DIRECTORY_PICTURES}/LspMake/" +
                "${time}.$file_ext"


        photoView = findViewById<PhotoView>(R.id.photoView)

        image.setOnClickListener {
            photoView.apply {
                alpha = 0f
                visibility = View.VISIBLE
                animate()
                    .alpha(1f)
                    .setDuration(shortAnnotationDuration.toLong())
                    .setListener(null)
            }
            Glide.with(this).load(sample_url).into(photoView)


        }
        photoView.setOnClickListener {
            photoView.animate()
                .alpha(0f)
                .setDuration(shortAnnotationDuration.toLong())
                .setListener(object : AnimatorListenerAdapter(){
                    override fun onAnimationEnd(animation: Animator?) {
                        photoView.visibility = View.GONE
                    }
                })
        }

        fbtn.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)!=
                PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),1)
            }else{
                if (file_url != null) {
                    if (file_ext != null) {
                        downloadAction(file_url,file_ext)
                    }
                }
            }
        }
    }

    private fun downloadAction(file_url:String,file_ext:String){
        Toast.makeText(this,"开始保存",Toast.LENGTH_SHORT).show()
        thread {
            Looper.prepare()

            if (downloadBinder.downloadPic(file_url,file_ext)){
                Toast.makeText(this,"保存成功",Toast.LENGTH_SHORT).show()
                Log.w("Save","Saved")
            }else{
                Toast.makeText(this,"下载异常",Toast.LENGTH_SHORT).show()

            }
            Looper.loop()

        }
    }

    override fun onBackPressed() {
        if (photoView.visibility==View.GONE) {
            super.onBackPressed()
        }else{
            photoView.animate()
                .alpha(0f)
                .setDuration(shortAnnotationDuration.toLong())
                .setListener(object : AnimatorListenerAdapter(){
                    override fun onAnimationEnd(animation: Animator?) {
                        photoView.visibility = View.GONE
                    }
                })
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        val file_ext = intent.getStringExtra("file_ext")
        val file_url = intent.getStringExtra("file_url")
        val fbtn = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(
            R.id.fbtn
        )

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    if (file_url != null) {
                        if (file_ext != null) {
                            downloadAction(file_url,file_ext)

                        }
                    }
                }else{
                    Snackbar.make(fbtn,"该操作必须拥有文件写入权限",Snackbar.LENGTH_SHORT).setAction("授权"){
                        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),1)
                    }.show()
                }
            }
        }
    }

    private fun loadPic(url:String){
        val glideUrl = GlideUrl(url, LazyHeaders.Builder().addHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.212 Safari/537.36").build())

        Glide.with(this).load(glideUrl).listener(object :RequestListener<Drawable?>{
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable?>?,
                isFirstResource: Boolean
            ): Boolean {
                val pb = findViewById<ProgressBar>(R.id.pb)
                Log.e("erroe",e.toString())
                Snackbar.make(pb, "加载错误", Snackbar.LENGTH_LONG).setAction("查看Log") {
                    AlertDialog.Builder(this@PicActivity).apply {
                        setTitle("Log")
                        setMessage(e.toString())
                        setNegativeButton("确定", null)
                        create()
                        show()
                    }
                }.show()

                pb.visibility = View.INVISIBLE
                return false

            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable?>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                val pb = findViewById<ProgressBar>(R.id.pb)
                pb.visibility = View.INVISIBLE
                return false


            }

        }).into(image)

    }

     private fun layoutManager():FlexboxLayoutManager{
        val manager = object : FlexboxLayoutManager(this, FlexDirection.ROW, FlexWrap.WRAP) {
            override fun canScrollVertically(): Boolean {
                return false
            }
        }
        manager.flexWrap = FlexWrap.WRAP
        manager.flexDirection = FlexDirection.ROW
        manager.alignItems = AlignItems.CENTER
        manager.justifyContent = JustifyContent.FLEX_START
        return manager
    }

    //设置tags列表
    //这写的很烂 得改
     private inline fun <reified T> loadTags(tags:String){

         when(T::class.java){
             Tags::class.java ->{
                 tagList.add(Tags("Tag"))
                 val list = tags.split(" ")
                 val tagRecyclerView = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.tagRecyclerView)
                 tagRecyclerView.layoutManager = layoutManager()
                 for (tag in list) run {
                     tagList.add(Tags(tag))
                 }
                 val adapter = TagAdapter(tagList)
                 adapter.setOnItemClickListener(object : TagAdapter.OnItemClickListener{
                     override fun onItemClick(view: View, position: Int) {
                         val intent = Intent(this@PicActivity, MainActivity::class.java)
                         intent.putExtra("searchTag",tagList[position].tag)
                         startActivity(intent)
                     }

                 })
                 tagRecyclerView.adapter = adapter
             }
             Author::class.java->{
                 authorList.add(Author("Author"))
                 val authorRecyclerView = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.authorRecyclerView)
                 authorRecyclerView.layoutManager = layoutManager()
                 authorList.add(Author(tags))
                 val adapter = AuthorAdapter(authorList)
                 authorRecyclerView.adapter = adapter
             }
             ID::class.java->{
                 idList.add(ID("ID"))
                 idList.add(ID(tags))
                 val idRecyclerView = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.idRecyclerView)
                 idRecyclerView.layoutManager = layoutManager()
                 val adapter = IdAdapter(idList)
                 idRecyclerView.adapter = adapter
             }
             Size::class.java->{
                 sizeList.add(Size("Size"))
                 sizeList.add(Size(tags))
                 val sizeRecyclerView = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.sizeRecyclerView)
                 sizeRecyclerView.layoutManager = layoutManager()
                 val adapter = SizeAdapter(sizeList)
                 sizeRecyclerView.adapter = adapter

             }
         }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(connection)
    }

    private val connection = object : ServiceConnection{
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            downloadBinder = p1 as DownloadService.DownloadBinder;
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            TODO("Not yet implemented")
        }
    }

}