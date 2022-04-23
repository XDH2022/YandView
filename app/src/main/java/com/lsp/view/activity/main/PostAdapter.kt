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
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val picImage: ImageView = view.findViewById<ImageView>(R.id.picImgae)

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
                Log.e("file_ext",file_ext)
            }

            PicActivity.actionStartActivity(context,postYandList[position].id,postYandList[position].sample_url,
                postYandList[position].file_url,postYandList[position].tags,file_ext,
                postYandList[position].author,postYandList[position].file_size)
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

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = postYandList[position]
        //TODO 增加XML解析 节省流量
        val glideUrl: GlideUrl
        var source: String = post.sample_url

        glideUrl = GlideUrl(
            source,
            LazyHeaders.Builder().addHeader(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.212 Safari/537.36"
            ).build()
        )
        holder.picImage.layoutParams.height = postYandList[position].sample_height
        Glide.with(context).load(glideUrl).into(holder.picImage)
        if (position == postYandList.size - 1 && postYandList.size > 6) {
            //到达底部
            mLoadMoreListener.loadMore(position)
        }

    }

    override fun getItemCount(): Int {
        return postYandList.size
    }


}