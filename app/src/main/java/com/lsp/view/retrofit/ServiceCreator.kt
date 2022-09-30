package com.lsp.view.retrofit

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ServiceCreator {


    /**
     * @param serviceClass:所属类的Service接口
     * @param source:加载根地址
     *
     */
    var source: String? = null
    fun <T> create(serviceClass: Class<T>, source: String): T {
        val retrofit = Retrofit.Builder()
            .baseUrl(source)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        ServiceCreator.source = source
        return retrofit.create(serviceClass)
    }

    /**
     * 提供更优的函数调用方式
     */
    inline fun <reified T> create(source: String): T = create(T::class.java, source)
}