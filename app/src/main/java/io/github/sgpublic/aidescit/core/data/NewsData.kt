package io.github.sgpublic.aidescit.core.data

data class NewsData (
    val id: Int,
    val type: Int,
    val title: String,
    val summary: String,
    val images: ArrayList<String>,
    val createTime: String
)