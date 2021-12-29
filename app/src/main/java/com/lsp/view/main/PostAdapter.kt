package com.lsp.view.main

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.lsp.view.bean.Post
import com.lsp.view.pic.PicActivity
import com.lsp.view.R

class PostAdapter(val context:Context, private val postList: List<Post>) :RecyclerView.Adapter<PostAdapter.ViewHolder>(){
    inner class ViewHolder(view: View) :RecyclerView.ViewHolder(view){
        val picImage: ImageView = view.findViewById<ImageView>(R.id.picImgae)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_layout,parent,false)
        val viewHolder = ViewHolder(view)
        viewHolder.picImage.setOnClickListener {
            val position = viewHolder.adapterPosition
            Log.e("position",position.toString())
            Log.e("url",postList[position].sample_url)
            val intent = Intent(context, PicActivity::class.java)
            intent.putExtra("id",postList[position].id)
            intent.putExtra("sample_url",postList[position].sample_url)
            intent.putExtra("file_url",postList[position].file_url)
            intent.putExtra("tags",postList[position].tags)
            intent.putExtra("file_ext",postList[position].file_ext)
            intent.putExtra("author",postList[position].author)
            intent.putExtra("file_size",postList[position].file_size)
            context.startActivity(intent)
        }

        return viewHolder
    }

    private lateinit var mLoadMoreListener: OnLoadMoreListener

    interface  OnLoadMoreListener{
        fun loadMore(position: Int)
    }

    fun setLoadMoreListener(mLoadMoreListener: OnLoadMoreListener){
        this.mLoadMoreListener = mLoadMoreListener

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = postList[position]
        //TODO 增加XML解析 节省流量
        val glideUrl:GlideUrl
        var source:String
        if (post.preview_url!=null) {
            source =  post.preview_url

        }else{
            source = post.file_url
        }
        glideUrl = GlideUrl(source
            ,
            LazyHeaders.Builder().addHeader(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.212 Safari/537.36"
            ).build()
        )
        Glide.with(context).load(glideUrl).into(holder.picImage)
        if (position==postList.size-1&&postList.size>6){
            //到达底部
            mLoadMoreListener.loadMore(position)
        }

    }



    override fun getItemCount(): Int {
        return postList.size
    }


}