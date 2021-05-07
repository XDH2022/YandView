package com.lsp.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lsp.view.R
import com.lsp.view.bean.Tags

class TagAdapter(val tagList:List<Tags>):RecyclerView.Adapter<TagAdapter.ViewHolder>() {
    inner class ViewHolder(view:View):RecyclerView.ViewHolder(view){
        val tagText = view.findViewById<TextView>(R.id.tag)
    }

    private lateinit var mOnItemClickListener: OnItemClickListener
    interface OnItemClickListener{
        fun onItemClick(view: View,position: Int)
    }

    fun setOnItemClickListener(mOnItemClickListener: OnItemClickListener){
        this.mOnItemClickListener = mOnItemClickListener
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.tag_item_layout,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tag = tagList[position]
        holder.tagText.text = tag.tag
        holder.tagText.setOnClickListener {
            mOnItemClickListener.onItemClick(it,position)
        }
        if (position == 0){
            holder.tagText.setBackgroundResource(R.drawable.title_bg)
        }
    }

    override fun getItemCount(): Int {
        return tagList.size
    }
}