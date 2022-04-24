package io.github.sgpublic.aidescit.core.module

import io.github.sgpublic.aidescit.core.data.NewsData
import io.github.sgpublic.aidescit.core.manager.CacheManager
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class NewsModule {
    fun getHeadline(callback: Callback){
        BaseAPI().getHeadlineRequest().enqueue(object
            : BaseAPI.BaseOkHttpCallback(callback) {
            override fun onResult(data: JSONObject) {
                CacheManager.CACHE_HEADLINE = data
                parsing(data.getJSONArray("headlines"), false, callback)
            }
        })
    }

    fun getNewsByType(type: Int, page: Int, callback: Callback){
        BaseAPI().getNewsListRequest(type, page).enqueue(object
            : BaseAPI.BaseOkHttpCallback(callback) {
            override fun onResult(data: JSONObject) {
                val news = data.getJSONObject("news")
                parsing(news.getJSONArray("list"), news.getBoolean("has_next"), callback)
            }
        })
    }

    fun getNewsType(callback: Callback){
        BaseAPI().getNewsTypeRequest().enqueue(object : BaseAPI.BaseOkHttpCallback(callback) {
            override fun onResult(data: JSONObject) {
                val charts = data.getJSONArray("charts")
                val types: MutableMap<Int, String> = mutableMapOf()
                for (i in 0 until charts.length()){
                    val chart: JSONObject = charts.getJSONObject(i)
                    types[chart.getInt("tid")] = chart.getString("name")
                }
                callback.onTypeResult(types)
            }
        })
    }

    @Throws(JSONException::class)
    fun parsing(array: JSONArray, hasNext: Boolean, callback: Callback) {
        val news: ArrayList<NewsData> = arrayListOf()
        if (array.length() > 0){
            for (passedIndex in 0 until array.length()){
                val newsData: JSONObject = array.getJSONObject(passedIndex)
                val imagesData: JSONArray = try {
                    newsData.getJSONArray("images")
                } catch (e: JSONException) {
                    JSONArray().put(newsData.getString("image"))
                }
                val images: ArrayList<String> = arrayListOf()
                if (imagesData.length() > 0){
                    for (imageIndex in 0 until imagesData.length()){
                        images.add(imagesData.getString(imageIndex))
                    }
                }
                val summary = try {
                    newsData.getString("summary")
                } catch (e: JSONException) {
                    ""
                }
                val createTime = try {
                    newsData.getString("create_time")
                } catch (e: JSONException) {
                    ""
                }
                news.add(
                    NewsData(
                        newsData.getInt("nid"),
                        newsData.getInt("tid"),
                        newsData.getString("title"),
                        summary, images, createTime
                    )
                )
            }
        }
        callback.onNewsResult(news, hasNext)
    }

    interface Callback: BaseAPI.Callback {
        fun onNewsResult(news: ArrayList<NewsData>, hasNext: Boolean) {}
        fun onTypeResult(types: Map<Int, String>) {}
    }
}