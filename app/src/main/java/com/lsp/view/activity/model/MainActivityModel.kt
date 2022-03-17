package com.lsp.view.activity.model

import android.os.Handler
import com.lsp.view.bean.Post
import java.util.*

interface MainActivityModel {
    fun requestPostList(handler: Handler, source: String, tage: String?, page: Int, safeMode: Boolean)

}