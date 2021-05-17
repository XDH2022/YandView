package com.lsp.view.main

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val fbtn = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(
            R.id.fbtn
        )
        val swipeRefreshLayout = findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(
            R.id.swipeRefreshLayout
        )
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
        /**
        val headerView = LayoutInflater.from(this).inflate(R.layout.nav_header,null)
        val name = headerView.findViewById<TextView>(R.id.name)
        name.setOnClickListener {

            val intent = Intent(this,LoginActivity::class.java)
            startActivity(intent)
        }*/
        //侧边栏


        val nav = findViewById<NavigationView>(R.id.nav)
        nav.setCheckedItem(R.id.photo)
        nav.setNavigationItemSelectedListener {
            true
        }


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