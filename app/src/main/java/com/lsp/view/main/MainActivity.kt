package com.lsp.view.main

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
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
import com.lsp.view.login.LoginActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {
    private val postList:ArrayList<Post> = ArrayList()
    private var searchTag:String? = null
    private lateinit var search:EditText
    private lateinit var searchBar:LinearLayout
    private var shortAnnotationDuration:Int = 0
    private var nowPage = 1
    private lateinit var adapter: PostAdapter
    private var isLoading = false
    private var nowPosition by Delegates.notNull<Int>()
    private var username:String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val fbtn = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(
            R.id.fbtn
        )
        val swipeRefreshLayout = findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(
            R.id.swipeRefreshLayout
        )
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)


        search = findViewById<EditText>(R.id.search)
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        searchTag = intent.getStringExtra("searchTag")
        if (searchTag!=null){
            this.search.setText(searchTag)
            searchAction(searchTag)
        }else{
            swipeRefreshLayout.isRefreshing = true
            loadPost(this, null, null,nowPage.toString())
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

        swipeRefreshLayout.setOnRefreshListener {
            Log.e("Refresh", "Is refresh")
            postList.clear()
            nowPage = 1
            loadPost(this, null, searchTag,nowPage.toString())
        }
        //登录

        //侧边栏
        val sp = getSharedPreferences("username",0)
        username = sp.getString("username",null)


        val nav = findViewById<NavigationView>(R.id.nav)
        val headerView = nav.getHeaderView(0)
        val name = headerView.findViewById<TextView>(R.id.name)
        name.text = username
        name.setOnClickListener {
            alterEditDialog()
        }

        nav.setCheckedItem(R.id.photo)
        nav.setNavigationItemSelectedListener {
            when(it.itemId){
                //收藏夹
                R.id.fav -> {
                    swipeRefreshLayout.isRefreshing=true
                    Log.e("touch","touch1")
                    Log.e("username",username.toString())
                    Log.e("touch","touch2")
                    if (username == null) {
                        alterEditDialog()
                    }
                    postList.clear()
                    loadPost(this,null,"vote:3:$username order:vote","1")
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.photo ->{
                    swipeRefreshLayout.isRefreshing = true
                    postList.clear()
                    loadPost(this,null,null,"1")
                    drawerLayout.closeDrawers()
                    true
                }
                else -> false
            }
        }


    }
    private fun alterEditDialog(){
        val et = EditText(this)
        AlertDialog.Builder(this)
            .setTitle("请输入您的用户名")
            .setView(et)
            .setPositiveButton("确定") { _, _ ->
                Log.e("edit",et.text.toString())
                val sharedPreferences = getSharedPreferences("username", 0).edit()
                sharedPreferences.putString("username",et.text.toString()).apply()
                username = et.text.toString()
                val nav = findViewById<NavigationView>(R.id.nav)
                val headerView = nav.getHeaderView(0)
                val name = headerView.findViewById<TextView>(R.id.name)
                name.text = username
            }.create().show()
    }
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
    private fun searchAction(tags: String?) {
        val swipeRefreshLayout = findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(
            R.id.swipeRefreshLayout
        )
        swipeRefreshLayout.isRefreshing = true
        postList.clear()
        loadPost(this, null, tags,"1")


    }

    private fun loadPost(context: Context, source: String?, tags: String?,page:String){
        nowPosition = postList.size-3

        val postService: PostService = if(source!=null){
            ServiceCreator.create<PostService>(source)
        }else{
            ServiceCreator.create<PostService>("https://yande.re/")
        }

        postService.getPostData("220", tags,page).enqueue(object : Callback<List<Post>> {
            override fun onFailure(call: Call<List<Post>>, t: Throwable) {
                val fbtn = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(
                    R.id.fbtn
                )
                Snackbar.make(fbtn,"请检查网络连接",Snackbar.LENGTH_LONG).show()
                val handler = Handler()
                handler.postDelayed({ loadPost(context, source, tags, page) },5000)
            }

            override fun onResponse(call: Call<List<Post>>, response: Response<List<Post>>) {
                val swipeRefreshLayout =
                    findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(
                        R.id.swipeRefreshLayout
                    )
                val list = response.body()

                if (list != null&&list.size>1) {
                    for ((i, post) in list.withIndex()) {
                            postList.add(post)
                    }
                } else {
                    swipeRefreshLayout.isRefreshing = false
                    Snackbar.make(swipeRefreshLayout,"只有这么多了哦",Snackbar.LENGTH_SHORT).show()
                    Log.e("Post", "Is null")
                    return
                }


                val layoutManager = GridLayoutManager(context, 2)
                val recyclerView =
                    findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerview)
                recyclerView.layoutManager = layoutManager

                Log.e("load",isLoading.toString())
                if (isLoading){
                    recyclerView.scrollToPosition(nowPosition)
                    adapter.notifyDataSetChanged()
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
                        loadPost(this@MainActivity,null,tags,nowPage.toString())
                    }

                })

                swipeRefreshLayout.isRefreshing = false

            }
        })



    }
}