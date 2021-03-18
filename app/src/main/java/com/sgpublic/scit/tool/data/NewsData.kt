package com.sgpublic.scit.tool.data

data class NewsData (
    val id: Int,
    val type: Int,
    val title: String,
    val summary: String,
    val images: ArrayList<String>,
    val createTime: String
)