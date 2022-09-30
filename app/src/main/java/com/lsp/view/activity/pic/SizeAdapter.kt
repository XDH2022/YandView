package com.lsp.view.activity.pic

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lsp.view.R
import com.lsp.view.bean.Size
import java.math.RoundingMode
import java.text.DecimalFormat

class SizeAdapter(val tagList: List<Size>) : RecyclerView.Adapter<SizeAdapter.ViewHolder>() {
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tagText = view.findViewById<TextView>(R.id.tag)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.tag_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tag = tagList[position]
        if (position == 0) {
            holder.tagText.text = tag.file_size
            holder.tagText.setBackgroundResource(R.drawable.title_bg)
        } else {
            val sizeKb = tag.file_size.toFloat() / 1024
            val format = DecimalFormat("0.##")
            format.roundingMode = RoundingMode.FLOOR
            val size = format.format(sizeKb)
            holder.tagText.text = "$size KB"
        }
    }

    override fun getItemCount(): Int {
        return tagList.size
    }
}