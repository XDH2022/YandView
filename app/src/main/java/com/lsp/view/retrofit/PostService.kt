package com.hentai.yandeview.Retrofit

import com.lsp.view.bean.Post
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface PostService {
    @GET("post.json")
    fun getPostData(@Query("limit") limit: String,@Query("tags") tags:String?,@Query("page") page:String): Call<ArrayList<Post>>

    @GET("index.php")
    fun getPostData_php(@Query("page") page:String,@Query("s") s:String,@Query("q")q:String,@Query("limit") limit: String,@Query("tags") tags:String?,@Query("json")json:String,@Query("pid")pid:String): Call<ArrayList<Post>>
}