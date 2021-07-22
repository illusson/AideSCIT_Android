package com.sgpublic.aidescit.helper

import android.content.Context
import com.sgpublic.aidescit.R
import com.sgpublic.aidescit.data.NewsData
import com.sgpublic.aidescit.manager.CacheManager
import com.sgpublic.aidescit.util.MyLog
import okhttp3.Call
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.UnknownHostException

class NewsHelper(private val context: Context) {
    fun getHeadline(callback: Callback){
        APIHelper().getHeadlineRequest().enqueue(object : okhttp3.Callback{
            override fun onFailure(call: Call, e: IOException) {
                MyLog.e("网络请求出错", e)
                if (e is UnknownHostException) {
                    callback.onFailure(-801, context.getString(R.string.error_network), e)
                } else {
                    callback.onFailure(-802, e.message, e)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.code == 200){
                    val result = response.body?.string().toString()
                    try {
                        val objects = JSONObject(result)
                        when (objects.getInt("code")) {
                            200 -> {
                                CacheManager(context).save(CacheManager.CACHE_HEADLINE, result)
                                parsing(objects.getJSONArray("headlines"), false, callback)
                            }
                            else -> {
                                MyLog.e("服务器处理出错，" + objects.getString("message"))
                                callback.onFailure(-803, objects.getString("message"))
                            }
                        }
                    } catch (e: JSONException){
                        MyLog.d(result)
                        MyLog.e("headline数据解析失败", e)
                        callback.onFailure(-804, e.message, e)
                    }
                } else {
                    MyLog.w("服务器内部出错")
                    callback.onFailure(-805, context.getString(R.string.error_server_error))
                }
            }
        })
    }

    fun getNewsByType(type: Int, page: Int, callback: Callback){
        APIHelper().getNewsListRequest(type, page).enqueue(object : okhttp3.Callback{
            override fun onFailure(call: Call, e: IOException) {
                MyLog.e("网络请求出错", e)
                if (e is UnknownHostException) {
                    callback.onFailure(-811, context.getString(R.string.error_network), e)
                } else {
                    callback.onFailure(-812, e.message, e)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.code == 200){
                    val result = response.body?.string().toString()
                    try {
                        val objects = JSONObject(result)
                        when (objects.getInt("code")) {
                            200 -> {
                                val news = objects.getJSONObject("news")
                                parsing(news.getJSONArray("list"), news.getBoolean("has_next"), callback)
                            }
                            else -> {
                                MyLog.e("服务器处理出错")
                                callback.onFailure(-813, objects.getString("message"))
                            }
                        }
                    } catch (e: JSONException){
                        MyLog.e("news数据解析失败", e)
                        callback.onFailure(-814, e.message, e)
                    }
                } else {
                    MyLog.w("服务器内部出错")
                    callback.onFailure(-815, context.getString(R.string.error_server_error))
                }
            }
        })
    }

    fun getNewsType(callback: Callback){
        APIHelper().getNewsTypeRequest().enqueue(object : okhttp3.Callback{
            override fun onFailure(call: Call, e: IOException) {
                MyLog.e("网络请求出错", e)
                if (e is UnknownHostException) {
                    callback.onFailure(-821, context.getString(R.string.error_network), e)
                } else {
                    callback.onFailure(-822, e.message, e)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.code == 200){
                    val result = response.body?.string().toString()
                    try {
                        val objects = JSONObject(result)
                        when (objects.getInt("code")) {
                            200 -> {
                                val charts = objects.getJSONArray("charts")
                                val types: MutableMap<Int, String> = mutableMapOf()
                                for (i in 0 until charts.length()){
                                    val chart: JSONObject = charts.getJSONObject(i)
                                    types[chart.getInt("tid")] = chart.getString("name")
                                }
                                callback.onTypeResult(types)
                            }
                            else -> {
                                MyLog.e("服务器处理出错")
                                callback.onFailure(-823, objects.getString("message"))
                            }
                        }
                    } catch (e: JSONException){
                        MyLog.e("type数据解析失败", e)
                        callback.onFailure(-824, e.message, e)
                    }
                } else {
                    MyLog.w("服务器内部出错")
                    callback.onFailure(-825, context.getString(R.string.error_server_error))
                }
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
                news.add(NewsData(
                    newsData.getInt("nid"),
                    newsData.getInt("tid"),
                    newsData.getString("title"),
                    summary, images, createTime
                ))
            }
        }
        callback.onNewsResult(news, hasNext)
    }

    interface Callback {
        fun onFailure(code: Int, message: String?, e: Exception? = null) {}
        fun onNewsResult(news: ArrayList<NewsData>, hasNext: Boolean) {}
        fun onTypeResult(types: Map<Int, String>) {}
    }
}