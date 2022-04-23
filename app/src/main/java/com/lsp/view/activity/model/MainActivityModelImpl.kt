package com.lsp.view.activity.model

import android.os.Handler
import android.os.Message
import com.hentai.yandeview.Retrofit.PostService
import com.hentai.yandeview.Retrofit.ServiceCreator
import com.lsp.view.bean.Post
import com.lsp.view.bean.Post_yand
import com.lsp.view.util.Code
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.ArrayList

class MainActivityModelImpl :MainActivityModel {
    private val TAG = this::class.java.simpleName
    override fun requestPostList(handler: Handler, source: String, tage: String?, page: Int,safeMode: Boolean){
        val postService:PostService = ServiceCreator.create(source)
        val service: Call<ArrayList<Post_yand>> = postService.getPostData("100",tage, page)
        request(service,safeMode, handler)
    }

    fun <T:Post>sendData(value: ArrayList<T>,handler: Handler){
        val msg = Message.obtain()
        msg.what = Code.OK
        msg.obj = value
        handler.sendMessage(msg)
    }

    fun sendNullMsg(handler: Handler){
        val msg = Message.obtain()
        msg.what = Code.DATAISNULL
        handler.sendMessage(msg)
    }

    fun sendConnErrorMsg(handler: Handler){
        val msg = Message.obtain()
        msg.what = Code.NETWORKERROR
        handler.sendMessage(msg)
    }

    /**
     * @param service ArrayList接收边界为Post的泛型，若直接使用Post作为泛型参数，需要强制类型转换。
     */
    fun <T : Post> request(service:Call<ArrayList<T>>,safeMode: Boolean,handler: Handler){

        service.enqueue(object : Callback<ArrayList<T>>{
            override fun onResponse(call: Call<ArrayList<T>>, response: Response<ArrayList<T>>) {
                val getValue:ArrayList<T>? = response.body()

                if (getValue!=null) {
                    if (getValue.size==0){
                        sendNullMsg(handler)
                        return
                    }
                    if (safeMode) {
                        val iter = getValue!!.iterator()
                        while (iter.hasNext()) {
                            val post = iter.next()
                            if (post.rating != "s")
                                iter.remove()
                        }
                    }

                    sendData(getValue!!,handler)
                }

            }

            override fun onFailure(call: Call<ArrayList<T>>, t: Throwable) {
                sendConnErrorMsg(handler)
            }

        })
    }
}