package com.lsp.view.activity.model

import android.os.Handler
import android.os.Message
import android.util.Log
import com.hentai.yandeview.Retrofit.PostService
import com.hentai.yandeview.Retrofit.ServiceCreator
import com.lsp.view.bean.Post
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.ArrayList

class MainActivityModelImpl :MainActivityModel {
    private val TAG = this::class.java.simpleName

    override fun requestPostList(handler: Handler, source: String, tage: String?, page: Int,safeMode: Boolean){

        val list = ArrayList<Post>()

        val postService:PostService = ServiceCreator.create(source)
        val service: Call<ArrayList<Post>> = postService.getPostData("100",tage, page)

        //请求
        service.enqueue(object : Callback<ArrayList<Post>>{
            //收到相应
            override fun onResponse(
                call: Call<ArrayList<Post>>,
                response: Response<ArrayList<Post>>
            ) {
                val getValue = response.body()
                if (getValue?.size==0){
                    Log.w(TAG,"Don't get value.")
                    val msg = Message.obtain()
                    msg.what = -1
                    handler.sendMessage(msg)
                    return

                }
                if (safeMode){
                    if (getValue != null) {
                        for (item in getValue){
                            if (item.rating == "s")
                                list.add(item)
                        }
                    }
                }else {
                    if (getValue != null) {
                        list.addAll(getValue)
                    }
                }
                val msg = Message.obtain()
                msg.what = 0
                msg.obj = list
                handler.sendMessage(msg)

            }

            override fun onFailure(call: Call<ArrayList<Post>>, t: Throwable) {
                val msg = Message.obtain()
                msg.what = 1
                handler.sendMessage(msg)
            }

        })

    }
}