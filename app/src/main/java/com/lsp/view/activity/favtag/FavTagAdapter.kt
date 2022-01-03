package com.lsp.view.activity.favtag

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lsp.view.R
import com.lsp.view.bean.Tags

class FavTagAdapter(val tagList:ArrayList<Tags>): RecyclerView.Adapter<FavTagAdapter.ViewHolder>() {
    inner class ViewHolder(view:View):RecyclerView.ViewHolder(view) {
        val fav_tag = view.findViewById<TextView>(R.id.fav_tag)

    }

    private lateinit var mOnItemClickListener :OnItemClickListener
    interface OnItemClickListener{
        fun onItemClick(view:View, position: Int)
    }
    fun setOnItemClickListener(mOnItemClickListener: OnItemClickListener){
        this.mOnItemClickListener = mOnItemClickListener

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fav_tag_item_layout,parent,false)
        val viewHolder = ViewHolder(view)
        viewHolder.fav_tag.setOnLongClickListener {
            val position = viewHolder.adapterPosition
            removeData(position)

            true
        }
        return viewHolder
    }

    fun removeData(position: Int){
        tagList.removeAt(position)
        notifyItemRangeInserted(0,tagList.size)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tag = tagList[position].tag
        holder.fav_tag.text = tag
        holder.fav_tag.setOnClickListener {
            mOnItemClickListener.onItemClick(it,position)
        }

    }

    override fun getItemCount(): Int {
        return tagList.size
    }


}