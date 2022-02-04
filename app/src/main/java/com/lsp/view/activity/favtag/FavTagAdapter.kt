package com.lsp.view.activity.favtag

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lsp.view.R
import com.lsp.view.bean.Tags

class FavTagAdapter(val tagList: ArrayList<Tags>, val context: Context) :
    RecyclerView.Adapter<FavTagAdapter.ViewHolder>() {
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val fav_tag = view.findViewById<TextView>(R.id.fav_tag)

    }

    private lateinit var mOnItemClickListener: OnItemClickListener

    interface OnItemClickListener {
        fun onItemClick(view: View, position: Int)
    }

    fun setOnItemClickListener(mOnItemClickListener: OnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.fav_tag_item_layout, parent, false)
        val viewHolder = ViewHolder(view)
        viewHolder.fav_tag.setOnLongClickListener {
            val position = viewHolder.adapterPosition
            removeData(position)


            true
        }
        return viewHolder
    }

    fun removeData(position: Int) {
        tagList.removeAt(position)
        notifyItemRangeInserted(0, tagList.size)
        val tagsArraySp = context.getSharedPreferences("tags_sp", Context.MODE_PRIVATE)
        val tagsArrayJson = tagsArraySp.getString("array", null)
        val tagListType = object : TypeToken<ArrayList<Tags>>() {}.type
        val tagArray: ArrayList<Tags> = Gson().fromJson(tagsArrayJson, tagListType)
        tagArray.removeAt(position)
        tagsArraySp.edit().putString("array", Gson().toJson(tagArray)).apply()

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tag = tagList[position].tag
        holder.fav_tag.text = tag
        holder.fav_tag.setOnClickListener {
            mOnItemClickListener.onItemClick(it, position)
        }

    }

    override fun getItemCount(): Int {
        return tagList.size
    }


}