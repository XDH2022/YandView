package com.lsp.view.main

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Html
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.lsp.view.bean.Post
import com.hentai.yandeview.Retrofit.PostService
import com.hentai.yandeview.Retrofit.ServiceCreator
import com.lsp.view.R
import com.lsp.view.setting.SettingsActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {
    private var searchTag:String? = null
    private lateinit var search:EditText
    private lateinit var searchBar:LinearLayout
    private var shortAnnotationDuration:Int = 0
    private var nowPage = 1
    private lateinit var adapter: PostAdapter
    private var isLoading = false
    private var nowPosition by Delegates.notNull<Int>()
    private var username:String? = ""
    private lateinit var sourceUrl:Array<String>
    private lateinit var sourceName:Array<String>
    private lateinit var source:String
    private  var nowSourceName: String?=null
    private var isRefresh=true
    val TAG = javaClass.simpleName
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sourceName = resources.getStringArray(R.array.pic_source)
        sourceUrl = resources.getStringArray(R.array.url_source)
        val configSp =  getSharedPreferences("com.lsper.view_preferences",0)
        if (configSp.getString("sourceName",null)==null){
            configSp.edit().putString("sourceName","yande.re").apply()
            configSp.edit().putString("type","0").apply()
        }


        val fbtn = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(
            R.id.fbtn
        )
        val swipeRefreshLayout = findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(
            R.id.swipeRefreshLayout
        )
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)


        search = findViewById<EditText>(R.id.search)

//        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
//        setSupportActionBar(toolbar)


        //快捷搜索tag 来自PicActivity


        searchTag = intent.getStringExtra("searchTag")


        if (searchTag!=null){
            this.search.setText(searchTag)
            searchAction(searchTag)
        }else{
            swipeRefreshLayout.isRefreshing = true
            loadPost(this, null,nowPage.toString())
        }
        val close = findViewById<View>(R.id.close)
        val editCard = findViewById<com.google.android.material.card.MaterialCardView>(R.id.editCard)

        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24)
        }

        searchBar = findViewById<LinearLayout>(R.id.search_bar)
        shortAnnotationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)
        close.setOnClickListener {
            hiddenSearchBar()
        }

        search.setOnEditorActionListener { v, actionId, event ->
            if (actionId==EditorInfo.IME_ACTION_SEARCH){
                searchAction(search.text.toString())
                searchTag = search.text.toString()
                hiddenSearchBar()
            }
            return@setOnEditorActionListener false
        }

        fbtn.setOnClickListener {
            if (searchBar.visibility == View.GONE) {
                showSearchBar()
            }
        }

        //刷新
        swipeRefreshLayout.setOnRefreshListener {
            nowPage = 1
            isLoading = true
            loadPost(this, searchTag,nowPage.toString())
        }

        //侧边栏
        val sp = getSharedPreferences("username",0)
        username = sp.getString("username",null)

        val nav = findViewById<NavigationView>(R.id.nav)

        //加载导航栏列表
        nav.setCheckedItem(R.id.photo)
        //设置侧边栏点击逻辑
        nav.setNavigationItemSelectedListener {
            when(it.itemId){
                //收藏夹
                R.id.fav -> {
                    swipeRefreshLayout.isRefreshing=true
                    Log.w(TAG,username.toString())
                    if (username == null) {
                        alterEditDialog()
                        loadPost(this,"vote:3:$username order:vote","1")
                        drawerLayout.closeDrawers()
                    }else{
                        loadPost(this,"vote:3:$username order:vote","1")
                        drawerLayout.closeDrawers()
                    }

                    true
                }
                //画廊
                R.id.photo ->{
                    swipeRefreshLayout.isRefreshing = true
                    loadPost(this,null,"1")
                    drawerLayout.closeDrawers()
                    true
                }
                //设置
                R.id.setting -> {
                    val intent = Intent(this,SettingsActivity::class.java)
                    startActivity(intent)
                    drawerLayout.closeDrawers()
                    false

                }
                else -> false
            }
        }


    }
    //弹出键入用户名对话框
    private fun alterEditDialog(){
        val et = EditText(this)
        AlertDialog.Builder(this)
            .setTitle("请输入您的用户名")
            .setView(et)
            .setPositiveButton("确定") { _, _ ->
                Log.w(TAG,et.text.toString())
                val sharedPreferences = getSharedPreferences("username", 0).edit()
                sharedPreferences.putString("username",et.text.toString()).apply()
            }.create().show()
    }
    //隐藏搜索栏
    private fun hiddenSearchBar(){
        searchBar.animate()
            .alpha(0f)
            .setDuration(shortAnnotationDuration.toLong())
            .setListener(object :AnimatorListenerAdapter(){
                override fun onAnimationEnd(animation: Animator?) {
                    searchBar.visibility = View.GONE
                }
            })
    }
    //现实搜索栏
    private fun showSearchBar(){
        searchBar.apply {
            alpha = 0f
            visibility = View.VISIBLE
            animate()
                .alpha(1f)
                .setDuration(shortAnnotationDuration.toLong())
                .setListener(null)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
        when(item.itemId){
            android.R.id.home ->  drawerLayout.openDrawer(GravityCompat.START)
        }
        return true
    }


    private fun hideIm(){
        val imm: InputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS)
    }
    //执行搜索
    private fun searchAction(tags: String?) {
        val swipeRefreshLayout = findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(
            R.id.swipeRefreshLayout
        )
        swipeRefreshLayout.isRefreshing = true
        isLoading = true
        loadPost(this, tags,"1")


    }

    override fun onResume() {
        super.onResume()
        val configSp =  getSharedPreferences("com.lsper.view_preferences",0)
        val saveName =  configSp.getString("sourceName",null)
        if (saveName!=nowSourceName) {

            val swipeRefreshLayout =
                findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(
                    R.id.swipeRefreshLayout
                )
            swipeRefreshLayout.isRefreshing = true
            loadPost(this, searchTag, nowPage.toString())
        }
    }



    /**
     * 加载画廊
     * @param source 加载源
     * @param tags 标签
     * @param page 页数
     */
    private fun loadPost(context: Context, tags: String?,page:String){
        var postList:ArrayList<Post> = ArrayList()
        val configSp =  getSharedPreferences("com.lsper.view_preferences",0)
        for ((index,name) in sourceName.withIndex() ){
            if (name == configSp.getString("sourceName",null)){
                nowSourceName = configSp.getString("sourceName",null)
                source = sourceUrl[index]
            }
        }



        val postService: PostService = if(source!=null){
            ServiceCreator.create<PostService>(source)
        }else {
            ServiceCreator.create<PostService>("https://yande.re/")
        }
        var service:Call<ArrayList<Post>>
//        if (tyep.equals("0")){
        service   =  postService.getPostData("100", tags,page)
//        }else{
//            service =  postService.getPostData_php("dapi","post","index","100",tags,"1",nowPage.toString())
//        }

        service.enqueue(object : Callback<ArrayList<Post>> {
            override fun onFailure(call: Call<ArrayList<Post>>, t: Throwable) {
                val fbtn = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(
                    R.id.fbtn
                )
                Snackbar.make(fbtn,"请检查网络连接",Snackbar.LENGTH_LONG).show()
                val handler = Handler()
                handler.postDelayed({ loadPost(context, tags, page) },3000)
            }

            override fun onResponse(call: Call<ArrayList<Post>>, response: Response<ArrayList<Post>>) {
                val swipeRefreshLayout =
                    findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(
                        R.id.swipeRefreshLayout
                    )
                val list = response.body()

                if (list != null&&list.size>1) {
                    postList=list
                    nowPosition = postList.size-3


//                    for ((i, post) in list.withIndex()) {
//                            postList.add(post)
//                    }
                } else {
                    swipeRefreshLayout.isRefreshing = false
                    Snackbar.make(swipeRefreshLayout,"只有这么多了哦",Snackbar.LENGTH_SHORT).show()
                    Log.w(TAG, "post is null")
                    return
                }


                val layoutManager = GridLayoutManager(context, 2)
                val recyclerView =
                    findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerview)
                recyclerView.layoutManager = layoutManager

                if (isLoading){
                    adapter.notifyData(postList,isRefresh)
                    recyclerView.scrollToPosition(nowPosition)
                    Log.e("position",nowPosition.toString())
                    isRefresh = true
                    isLoading = false
                }else{
                    adapter = PostAdapter(context, postList)
                    recyclerView.adapter = adapter
                }
                adapter.setLoadMoreListener(object : PostAdapter.OnLoadMoreListener{
                    override fun loadMore(position: Int) {
                        nowPage++
                        swipeRefreshLayout.isRefreshing = true
                        isLoading = true
                        isRefresh = false
                        loadPost(this@MainActivity,tags,nowPage.toString())
                    }

                })

                swipeRefreshLayout.isRefreshing = false

            }
        })



    }
}