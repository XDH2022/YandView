package com.hentai.yandeview.Retrofit

import com.lsp.view.bean.Post
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface PostService {
    @GET("post.json")
    fun getPostData(@Query("limit") limit: String,@Query("tags") tags:String?,@Query("page") page:String): Call<List<Post>>
}