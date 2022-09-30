package com.lsp.view.retrofit

import com.lsp.view.bean.Post_yand
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface PostService {
    @GET("post.json")
    fun getPostData(
        @Query("limit") limit: String,
        @Query("tags") tags: String?,
        @Query("page") page: Int
    ): Call<ArrayList<Post_yand>>
}