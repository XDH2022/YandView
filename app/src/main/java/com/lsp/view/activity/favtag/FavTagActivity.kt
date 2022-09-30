package com.lsp.view.activity.favtag

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.*
import com.google.android.material.appbar.AppBarLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lsp.view.MyApplication
import com.lsp.view.R
import com.lsp.view.activity.BaseActivity
import com.lsp.view.activity.main.MainActivity
import com.lsp.view.bean.Tags

class FavTagActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fav_tag)


        //Toolbar相关
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val appbar = findViewById<AppBarLayout>(R.id.appbar)
        val nowHeight = appbar.layoutParams.height
        appbar.layoutParams.height = (application as MyApplication).statusBarHeight()+nowHeight
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        val recyclerViewTag = findViewById<RecyclerView>(R.id.tag_recy)
        recyclerViewTag.layoutManager = layoutManager()
        val tagsArraySp = getSharedPreferences("tags_sp", Context.MODE_PRIVATE)
        val array = tagsArraySp.getString("array", null)
        var favTagList: ArrayList<Tags> = ArrayList()
        if (array != null) {
            //有收藏内容
            val tagListType = object : TypeToken<ArrayList<Tags>>() {}.type
            favTagList = Gson().fromJson(array, tagListType)

        }

        val favTagAdapter = FavTagAdapter(favTagList, this)
        favTagAdapter.setOnItemClickListener(object : FavTagAdapter.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                val intent = Intent(this@FavTagActivity, MainActivity::class.java)
                intent.putExtra("searchTag", favTagList[position].tag)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
        }

        return super.onOptionsItemSelected(item)

    }
}