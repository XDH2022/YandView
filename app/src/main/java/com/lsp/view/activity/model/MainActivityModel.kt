package com.lsp.view.activity.model

import android.os.Handler

interface MainActivityModel {
    fun requestPostList(handler: Handler, source: String, tage: String?, page: Int, safeMode: Boolean)
}