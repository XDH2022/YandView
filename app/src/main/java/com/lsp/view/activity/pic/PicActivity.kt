package com.lsp.view.activity.pic

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.*
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.github.chrisbanes.photoview.PhotoView
import com.google.android.flexbox.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.lsp.view.R
import com.lsp.view.activity.BaseActivity
import com.lsp.view.activity.main.MainActivity
import com.lsp.view.bean.Author
import com.lsp.view.bean.ID
import com.lsp.view.bean.Size
import com.lsp.view.bean.Tags
import com.lsp.view.util.Util
import java.io.File
import kotlin.properties.Delegates


class PicActivity : BaseActivity() {


    private val tagList = ArrayList<Tags>()
    private val authorList = ArrayList<Author>()
    private val idList = ArrayList<ID>()
    private val time = System.currentTimeMillis()
    private val sizeList = ArrayList<Size>()
    private lateinit var image: ImageView
    private lateinit var photoView: PhotoView
    private var shortAnnotationDuration by Delegates.notNull<Int>()
    private var md5 : String?= null


    companion object{
        fun actionStartActivity(context: Context,id:String,sample_url:String,file_url:String,tags:String,
                                file_ext:String,author:String,file_size:String,md5:String){
            val intent=Intent(context,PicActivity::class.java)
            intent.putExtra("id", id)
            intent.putExtra("sample_url", sample_url)
            intent.putExtra("file_url", file_url)
            intent.putExtra("tags", tags)
            intent.putExtra("file_ext", file_ext)
            intent.putExtra("author", author)
            intent.putExtra("file_size", file_size)
            intent.putExtra("md5", md5)
            context.startActivity(intent)

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_pic)

        image = findViewById<ImageView>(R.id.titleImage)
        shortAnnotationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)
        val intent = intent
        val tags = intent.getStringExtra("tags")
        if (tags != null) {
            loadTags(tags, "tags")
        }


        val author = intent.getStringExtra("author")
        if (author != null) {
            loadTags(author, "author")
        }
        val file_size = intent.getStringExtra("file_size")
        if (file_size != null) {
            loadTags(file_size, "size")
        }
        md5 = intent.getStringExtra("md5")

        val sharedPreferences = getSharedPreferences("FirstRun", 0)
        val firstRun = sharedPreferences.getBoolean("FirstRun", true)
        if (firstRun) {
            val sharedPreferences = getSharedPreferences("FirstRun", 0).edit()
            sharedPreferences.putBoolean("FirstRun", false).apply()
            val FileD =
                File("${Environment.getExternalStorageDirectory()}/${Environment.DIRECTORY_PICTURES}/LspMake")
            FileD.mkdirs()

        }


        val id = intent.getStringExtra("id")
        if (id != null) {
            loadTags(id, "id")
        }
        val sample_url = intent.getStringExtra("sample_url")
        sample_url?.let {
            loadPic(it)
        }

        val file_url = intent.getStringExtra("file_url")
        val file_ext = intent.getStringExtra("file_ext")
        val download =
            findViewById<FloatingActionButton>(
                R.id.download
            )

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
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        photoView.visibility = View.GONE
                    }
                })
        }
        val share = findViewById<FloatingActionButton>(R.id.share)
        share.setOnClickListener {
            if (sample_url != null) {
                Util.share(sample_url,this)
            }

        }

        download.setOnClickListener {
            Util.download(file_url, file_ext, md5)
        }

        val f_btn = findViewById<FloatingActionButton>(R.id.float_btn)
        val ctrl = findViewById<LinearLayout>(R.id.ctrl)


        f_btn.setOnClickListener {
            if (ctrl.visibility == View.VISIBLE)
                ctrl.visibility = View.GONE
            else
                ctrl.visibility = View.VISIBLE
        }

    }

    override fun onBackPressed() {
        if (photoView.visibility == View.GONE) {
            super.onBackPressed()
        } else {
            photoView.animate()
                .alpha(0f)
                .setDuration(shortAnnotationDuration.toLong())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        photoView.visibility = View.GONE
                    }
                })
        }
    }

    private fun loadPic(url: String) {

        val glideUrl = GlideUrl(
            url,
            LazyHeaders.Builder().addHeader(
                "User-Agent",
                "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.93 Safari/537.36"
            ).build()
        )
        Glide.with(this).load(glideUrl).listener(object : RequestListener<Drawable?> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable?>?,
                isFirstResource: Boolean
            ): Boolean {
                if (e != null) {
                    Log.e("Pic", e.stackTraceToString())
                }
                val pb = findViewById<ProgressBar>(R.id.pb)
                Snackbar.make(pb, R.string.toast_load_fail, Snackbar.LENGTH_LONG).setAction(R.string.button_check) {
                    AlertDialog.Builder(this@PicActivity).apply {
                        setTitle("Log")
                        if (e != null) {
                            setMessage(e.stackTraceToString())
                        }
                        setNegativeButton(R.string.button_ok, null)
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

    private fun layoutManager(): FlexboxLayoutManager {
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
    private fun loadTags(tags: String, type: String) {

        when (type) {
            "tags" -> {

                tagList.add(Tags("Tag"))
                val list = tags.split(" ")
                val tagRecyclerView =
                    findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.tagRecyclerView)
                tagRecyclerView.layoutManager = layoutManager()
                for (tag in list) run {
                    tagList.add(Tags(tag))
                }
                val adapter = TagAdapter(tagList, this)
                adapter.setOnItemClickListener(object : TagAdapter.OnItemClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        val intent = Intent(this@PicActivity, MainActivity::class.java)
                        intent.putExtra("searchTag", tagList[position].tag)
                        startActivity(intent)
                    }

                })
                tagRecyclerView.adapter = adapter
            }
            "author" -> {
                authorList.add(Author("Author"))
                val authorRecyclerView =
                    findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.authorRecyclerView)
                authorRecyclerView.layoutManager = layoutManager()
                authorList.add(Author(tags))
                val adapter = AuthorAdapter(authorList)
                authorRecyclerView.adapter = adapter
            }
            "id" -> {
                idList.add(ID("ID"))
                idList.add(ID(tags))
                val idRecyclerView =
                    findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.idRecyclerView)
                idRecyclerView.layoutManager = layoutManager()
                val adapter = IdAdapter(idList,this)
                idRecyclerView.adapter = adapter
            }
            "size" -> {
                sizeList.add(Size("Size"))
                sizeList.add(Size(tags))
                val sizeRecyclerView =
                    findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.sizeRecyclerView)
                sizeRecyclerView.layoutManager = layoutManager()
                val adapter = SizeAdapter(sizeList)
                sizeRecyclerView.adapter = adapter

            }
        }
    }

}