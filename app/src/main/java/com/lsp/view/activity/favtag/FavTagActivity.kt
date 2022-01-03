package com.lsp.view.activity.favtag

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lsp.view.R
import com.lsp.view.activity.main.MainActivity
import com.lsp.view.bean.Tags

class FavTagActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fav_tag)

        val recyclerViewTag = findViewById<RecyclerView>(R.id.tag_recy)
        recyclerViewTag.layoutManager = layoutManager()
        val tagsArraySp = getSharedPreferences("tags_sp", Context.MODE_PRIVATE)
        val array = tagsArraySp.getString("array",null)
        var favTagList :ArrayList<Tags> = ArrayList()
        if (array!=null){
            //有收藏内容
            val tagListType = object : TypeToken<ArrayList<Tags>>(){}.type
            favTagList = Gson().fromJson(array,tagListType)

        }else{
            favTagList.add(Tags("没有任何Tag"))
        }

        val favTagAdapter = FavTagAdapter(favTagList)
        favTagAdapter.setOnItemClickListener(object :FavTagAdapter.OnItemClickListener{
            override fun onItemClick(view: View, position: Int) {
                val intent = Intent(this@FavTagActivity, MainActivity::class.java)
                intent.putExtra("searchTag",favTagList[position].tag)
                startActivity(intent)
            }

        })
        recyclerViewTag.adapter = favTagAdapter

    }

    private fun layoutManager(): FlexboxLayoutManager {
        val manager = object : FlexboxLayoutManager(this, FlexDirection.ROW, FlexWrap.WRAP) {
            override fun canScrollVertically(): Boolean {
                return false
            }
        }
        manager.flexWrap = FlexWrap.WRAP
        manager.flexDirection = FlexDirection.ROW
        manager.alignItems = AlignItems.CENTER
        manager.justifyContent = JustifyContent.FLEX_START
        return manager
    }
}