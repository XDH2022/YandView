package com.lsp.view.activity.main

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.lsp.view.R
import com.lsp.view.activity.pic.PicActivity
import com.lsp.view.bean.Post_yand


class PostAdapter(val context: Context, private var postYandList: ArrayList<Post_yand>) :
    RecyclerView.Adapter<PostAdapter.ViewHolder>() {
    val TAG = this::class.java.simpleName
    private val UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.212 Safari/537.36"


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val picImage: ImageView = view.findViewById(R.id.picImgae)

    }

    fun addData(list: ArrayList<Post_yand>) {
        val pos = postYandList.size
        postYandList.addAll(list)
        notifyItemRangeInserted(pos, list.size)
    }

    fun refreshData(list: ArrayList<Post_yand>) {

        val oldSize = postYandList.size
        postYandList.clear()
        notifyItemRangeRemoved(0,oldSize)
        postYandList.addAll(list)
        notifyItemRangeInserted(0, list.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.img_item_layout, parent, false)
        val viewHolder = ViewHolder(view)
        viewHolder.picImage.setOnClickListener {
            val position = viewHolder.adapterPosition
            Log.w("position", position.toString())
            Log.w("url", postYandList[position].sample_url)
            Log.w("rating", postYandList[position].rating)
            var file_ext = postYandList[position].file_ext

            if (file_ext == null){
                val strarr = postYandList[position].sample_url.split(".")
                file_ext = strarr[strarr.lastIndex]
            }

            PicActivity.actionStartActivity(context,postYandList[position].id,postYandList[position].sample_url,
                postYandList[position].file_url,postYandList[position].tags,file_ext,
                postYandList[position].author,postYandList[position].file_size,postYandList[position].md5)
        }


        return viewHolder
    }

    private lateinit var mLoadMoreListener: OnLoadMoreListener

    interface OnLoadMoreListener {
        fun loadMore(position: Int)
    }

    fun setLoadMoreListener(mLoadMoreListener: OnLoadMoreListener) {
        this.mLoadMoreListener = mLoadMoreListener

    }

    private fun preLoad(p:Int){
        if (p%20==0){
            //???????????????
            val last:Int = if (postYandList.size-p<20){
                postYandList.size-1
            }else{
                p+19
            }
            for(index in  p..last){
                if (p>6) {
                    val source: String = postYandList[index].sample_url
                    val glideUrl = GlideUrl(
                        source,
                        LazyHeaders.Builder().addHeader("User-Agent", UA)
                            .build()
                    )
                    Glide.with(context).download(glideUrl).preload()
                }
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        preLoad(position)

        val post = postYandList[position]

        val source: String = post.sample_url

        holder.picImage.layoutParams.height = postYandList[position].sample_height/2

        val glideUrl = GlideUrl(
            source,
            LazyHeaders.Builder().addHeader("User-Agent", UA)
                .build()
        )
        Glide.with(context).load(glideUrl).into(holder.picImage)

        if (position == postYandList.size - 1 && postYandList.size > 6) {
            //????????????
            mLoadMoreListener.loadMore(position)
        }

    }

    override fun getItemCount(): Int {
        return postYandList.size
    }


}
