package com.lsp.view.activity.pic

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
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
import com.lsp.view.activity.main.MainActivity
import com.lsp.view.service.DownloadService
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread
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
        //看不懂的硬编码 导航栏全透明
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        val window = getWindow();
        window.clearFlags(
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
            or  WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or  View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        or  View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);


        setContentView(R.layout.activity_pic)
        val serviceIntent = Intent(this,DownloadService::class.java)
        bindService(serviceIntent,connection, Context.BIND_AUTO_CREATE)

        image =findViewById<ImageView>(R.id.titleImage)
        shortAnnotationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)
        val intent = intent
        val tags = intent.getStringExtra("tags")
        if (tags != null) {
            loadTags(tags,"tags")
        }




        val author = intent.getStringExtra("author")
        if (author!=null){
            loadTags(author,"author")
        }
        val file_size = intent.getStringExtra("file_size")
        if (file_size!=null){
            loadTags(file_size,"size")
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
            loadTags(id,"id")
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

        val glideUrl = GlideUrl(url, LazyHeaders.Builder().addHeader("User-Agent","Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.93 Safari/537.36").build())
        Glide.with(this).load(glideUrl).listener(object :RequestListener<Drawable?>{
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable?>?,
                isFirstResource: Boolean
            ): Boolean {
                if (e != null) {
                    Log.e("Pic",e.stackTraceToString())
                }
                val pb = findViewById<ProgressBar>(R.id.pb)
                Snackbar.make(pb, "加载错误", Snackbar.LENGTH_LONG).setAction("查看Log") {
                    AlertDialog.Builder(this@PicActivity).apply {
                        setTitle("Log")
                        if (e != null) {
                            setMessage(e.stackTraceToString())
                        }
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
        manager.alignItems = AlignItems.CENTER
        manager.justifyContent = JustifyContent.FLEX_START

        return manager
    }

    //设置tags列表
    //这写的很烂 得改
     private  fun  loadTags(tags:String,type:String){

         when(type){
             "tags" ->{

                 tagList.add(Tags("Tag"))
                 val list = tags.split(" ")
                 val tagRecyclerView = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.tagRecyclerView)
                 tagRecyclerView.layoutManager = layoutManager()
                 for (tag in list) run {
                     tagList.add(Tags(tag))
                 }
                 val adapter = TagAdapter(tagList,this)
                 adapter.setOnItemClickListener(object : TagAdapter.OnItemClickListener{
                     override fun onItemClick(view: View, position: Int) {
                         val intent = Intent(this@PicActivity, MainActivity::class.java)
                         intent.putExtra("searchTag",tagList[position].tag)
                         startActivity(intent)
                     }

                 })
                 tagRecyclerView.adapter = adapter
             }
             "author"->{
                 authorList.add(Author("Author"))
                 val authorRecyclerView = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.authorRecyclerView)
                 authorRecyclerView.layoutManager = layoutManager()
                 authorList.add(Author(tags))
                 val adapter = AuthorAdapter(authorList)
                 authorRecyclerView.adapter = adapter
             }
             "id"->{
                 idList.add(ID("ID"))
                 idList.add(ID(tags))
                 val idRecyclerView = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.idRecyclerView)
                 idRecyclerView.layoutManager = layoutManager()
                 val adapter = IdAdapter(idList)
                 idRecyclerView.adapter = adapter
             }
             "size"->{
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