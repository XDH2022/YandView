package com.lsp.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.lsp.view.bean.Post
import com.hentai.yandeview.Retrofit.PostService
import com.hentai.yandeview.Retrofit.ServiceCreator
import com.lsp.view.adapter.PostAdapter
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
    private lateinit var adapter:PostAdapter
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
                Snackbar.make(fbtn,"请检查网络连接",Snackbar.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call<List<Post>>, response: Response<List<Post>>) {
                val swipeRefreshLayout =
                    findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(
                        R.id.swipeRefreshLayout
                    )
                val list = response.body()

                if (list != null) {
                    for ((i, post) in list.withIndex()) {
                        postList.add(post)
                    }
                } else {
                    Log.e("Post", "Is null")
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
                adapter.setLoadMoreListener(object :PostAdapter.OnLoadMoreListener{
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