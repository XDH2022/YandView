package com.lsp.view.bean

class Post_yand(
    val preview_url: String,
    val file_url: String,
    val sample_url: String,
    val tags: String,
    val file_ext: String,
    val file_size: String,
    val sample_height : Int,
    val sample_width: Int,
    id: String,
    rating: String,
    author: String,
    val md5 : String
):Post(id, rating, author)