package com.lsp.view.activity.pic

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lsp.view.R
import com.lsp.view.bean.Tags

class TagAdapter(val tagList: List<Tags>, val context: Context) :
    RecyclerView.Adapter<TagAdapter.ViewHolder>() {
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tagText = view.findViewById<TextView>(R.id.tag)
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
            LayoutInflater.from(parent.context).inflate(R.layout.tag_item_layout, parent, false)
        val viewHolder = ViewHolder(view)
        viewHolder.tagText.setOnLongClickListener {
            val position = viewHolder.adapterPosition
            if (position == 0) {
                //标题
                return@setOnLongClickListener false
            }
            var tagsArray: ArrayList<Tags> = ArrayList()
            val tagsArraySp = context.getSharedPreferences("tags_sp", Context.MODE_PRIVATE)
            val tagsArrayJson = tagsArraySp.getString("array", null)
            if (tagsArrayJson != null) {
                val tagListType = object : TypeToken<ArrayList<Tags>>() {}.type
                tagsArray = Gson().fromJson(tagsArrayJson, tagListType)
            }
            for (tag: Tags in tagsArray) {
                if (tag.tag == viewHolder.tagText.text.toString()) {
                    Snackbar.make(viewHolder.tagText, R.string.toast_tag_exist, Snackbar.LENGTH_SHORT).show()
                    return@setOnLongClickListener true
                }
            }

            tagsArray.add(Tags(viewHolder.tagText.text.toString()))
            tagsArraySp.edit().putString("array", Gson().toJson(tagsArray)).apply()
            Snackbar.make(viewHolder.tagText, R.string.toast_tag_add_fav, Snackbar.LENGTH_SHORT).show()

            return@setOnLongClickListener true
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tag = tagList[position]
        holder.tagText.text = tag.tag
        holder.tagText.setOnClickListener {
            mOnItemClickListener.onItemClick(it, position)
        }
        if (position == 0) {
            holder.tagText.setBackgroundResource(R.drawable.title_bg)
        }
    }

    override fun getItemCount(): Int {
        return tagList.size
    }
}