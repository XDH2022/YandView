package com.lsp.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lsp.view.R
import com.lsp.view.bean.Author

class AuthorAdapter (val tagList:List<Author>):RecyclerView.Adapter<AuthorAdapter.ViewHolder>(){
    inner class ViewHolder(view:View):RecyclerView.ViewHolder(view){
        val tagText = view.findViewById<TextView>(R.id.tag)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.tag_item_layout,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tag = tagList[position]
        holder.tagText.text = tag.author
        if (position == 0){
            holder.tagText.setBackgroundResource(R.drawable.title_bg)
        }
    }

    override fun getItemCount(): Int {
        return tagList.size
    }
}