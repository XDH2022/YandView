package com.lsp.view.activity.main

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.os.*
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat.Type.ime
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.lsp.view.YandViewApplication
import com.lsp.view.R
import com.lsp.view.activity.BaseActivity
import com.lsp.view.activity.favtag.FavTagActivity
import com.lsp.view.activity.model.MainActivityModelImpl
import com.lsp.view.activity.setting.SettingsActivity
import com.lsp.view.bean.Post_yand
import com.lsp.view.util.CallBackStatus


class MainActivity : BaseActivity() {
    private var searchTag: String? = null
    private lateinit var search: EditText
    private lateinit var searchBar: LinearLayout
    private var shortAnnotationDuration: Int = 0
    private var nowPage = 1
    private val adapter: PostAdapter = PostAdapter(this, ArrayList())
    private var username: String? = ""
    private lateinit var sourceUrlArray: Array<String>
    private lateinit var sourceNameArray: Array<String>
    private var nowSourceName: String? = null
    val TAG = javaClass.simpleName
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private var safeMode: Boolean = true //安全模式
    private lateinit var recyclerView: RecyclerView
    private var barShow = false
    private var tags: String? = ""
    private val ISREFRESH = 1
    private val ISADDDATA = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val appbar = findViewById<AppBarLayout>(R.id.appbar)
        val nowHeight = appbar.layoutParams.height
        appbar.layoutParams.height = (application as YandViewApplication).statusBarHeight()+nowHeight

        //到达底部，加载更多数据
        adapter.setLoadMoreListener(object : PostAdapter.OnLoadMoreListener {
            //重写接口
            override fun loadMore(position: Int) {
                loadData(tags,++nowPage,ISADDDATA)


            }

        })

        //初始化数据
        sourceNameArray = resources.getStringArray(R.array.pic_source)
        sourceUrlArray = resources.getStringArray(R.array.url_source)
        val configSp = getSharedPreferences("com.lsper.view_preferences", 0)
        if (configSp.getString("sourceName", null) == null) {
            configSp.edit().putString("sourceName", "yande.re").apply()
            configSp.edit().putString("type", "0").apply()
        }
        nowSourceName = configSp.getString("sourceName","yande.re")
        safeMode = configSp.getBoolean("safe_mode",true)



        //横屏逻辑
        layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        recyclerView = findViewById(R.id.recyclerview)

        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter



        //收藏Tag
        val swipeRefreshLayout =
            findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(
                R.id.swipeRefreshLayout
            )
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)


        search = findViewById(R.id.search)
        //快捷搜索tag 来自PicActivity
        searchTag = intent.getStringExtra("searchTag")
        if (searchTag != null) {
            //tag按钮搜索
            this.search.setText(searchTag)
            searchAction(searchTag)
        } else {
            //初次启动
            loadData( tags,1,ISADDDATA)
        }

        val close = findViewById<View>(R.id.close)

        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24)
        }

        searchBar = findViewById(R.id.search_bar)
        shortAnnotationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)
        close.setOnClickListener {
            hiddenSearchBar()
            hideIm()
        }

        search.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchAction(search.text.toString())
                searchTag = search.text.toString()
                hiddenSearchBar()
            }
            return@setOnEditorActionListener false
        }


        //刷新
        swipeRefreshLayout.setOnRefreshListener {
            nowPage = 1
            loadData( searchTag, nowPage, ISREFRESH)
        }

        //侧边栏
        val sp = getSharedPreferences("username", 0)
        username = sp.getString("username", null)

        val nav = findViewById<NavigationView>(R.id.nav)

        //加载导航栏列表
        nav.setCheckedItem(R.id.photo)
        //设置侧边栏点击逻辑
        nav.setNavigationItemSelectedListener {
            when (it.itemId) {
                //画廊
                R.id.photo -> {
                    loadData( null, 1, ISREFRESH)
                    drawerLayout.closeDrawers()
                    searchTag = ""
                    true
                }
                //设置
                R.id.setting -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                    drawerLayout.closeDrawers()
                    false

                }
                R.id.taglist -> {
                    val intent = Intent(this, FavTagActivity::class.java)
                    startActivity(intent)
                    drawerLayout.closeDrawers()
                    false
                }
                else -> false
            }
        }


    }

    override fun onResume() {
        super.onResume()
        //加载源改变
        val configSp = getSharedPreferences("com.lsper.view_preferences", 0)
        if (nowSourceName != configSp.getString("sourceName",null)){
            loadData(tags,1,ISREFRESH)
            nowSourceName = configSp.getString("sourceName",null)
        }

        if (safeMode != configSp.getBoolean("safe_mode",true)){
            loadData(tags,1,ISREFRESH)
            safeMode = configSp.getBoolean("safe_mode",true)
        }

    }

    //隐藏搜索栏
    private fun hiddenSearchBar() {
        searchBar.animate()
            .alpha(0f)
            .setDuration(shortAnnotationDuration.toLong())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    searchBar.visibility = View.GONE
                }
            })
        hideIm()
        barShow = false

    }

    //现实搜索栏
    private fun showSearchBar() {
        searchBar.apply {
            alpha = 0f
            visibility = View.VISIBLE
            animate()
                .alpha(1f)
                .setDuration(shortAnnotationDuration.toLong())
                .setListener(null)
        }
        barShow = true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
                drawerLayout.openDrawer(GravityCompat.START)
            }
            R.id.search_nav -> {
                if (barShow) {
                    searchTag = search.text.toString()
                    searchAction(searchTag)
                    hiddenSearchBar()


                }

                if (searchBar.visibility == View.GONE) {
                    showSearchBar()
                }
            }
        }
        return true
    }


    private fun hideIm() {
        val controller = ViewCompat.getWindowInsetsController(window.decorView)
        controller?.hide(ime())
    }

    //执行搜索
    private fun searchAction(tags: String?) {
        var tag = tags
        var isNum = true
        try {
            tags?.toInt()
        }catch (e:NumberFormatException){
            isNum = false
        }

        if (isNum){
            tag = "id:"+tags
        }

        loadData(tag, 1, ISREFRESH)


    }


    //设置列表数据
    private fun loadData(tags: String?,page: Int,type:Int){
        //缓存搜索的tags
        this.tags = tags

        val swipeRefreshLayout =
            findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(
                R.id.swipeRefreshLayout
            )
        swipeRefreshLayout.isRefreshing = true

        //读取配置
        val configSp = getSharedPreferences("com.lsper.view_preferences", 0)
        val nowSourceName: String? = configSp.getString("sourceName",null)
        var source = ""
        for ((index,sourceName) in sourceNameArray.withIndex()){
            if (sourceName == nowSourceName){
                source = sourceUrlArray[index]
            }
        }

        //接收异步信息
        val handler = object : Handler(Looper.myLooper()!!){
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                when (msg.what){
                    CallBackStatus.DATAISNULL.ordinal ->{
                        Snackbar.make(swipeRefreshLayout,R.string.toast_load_empty,Snackbar.LENGTH_SHORT).show()
                        search.setText("")
                    }
                    CallBackStatus.OK.ordinal -> {
                        if (type == ISREFRESH )
                        //刷新数据
                            adapter.refreshData(msg.obj as ArrayList<Post_yand>)
                        else if (type == ISADDDATA)
                        //加载数据
                            adapter.addData(msg.obj as ArrayList<Post_yand>)

                    }
                    CallBackStatus.NETWORKERROR.ordinal -> {
                        Snackbar.make(swipeRefreshLayout,R.string.toast_load_network_fail,Snackbar.LENGTH_SHORT).show()
                    }
                }

                swipeRefreshLayout.isRefreshing = false

            }
        }

        MainActivityModelImpl().requestPostList(handler,source,tags,page,configSp.getBoolean("safe_mode",true))

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }






}