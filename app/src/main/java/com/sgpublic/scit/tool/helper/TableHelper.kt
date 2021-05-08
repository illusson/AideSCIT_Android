package com.sgpublic.scit.tool.helper

import android.content.Context
import com.sgpublic.scit.tool.R
import com.sgpublic.scit.tool.util.MyLog
import com.sgpublic.scit.tool.data.TableData
import com.sgpublic.scit.tool.manager.CacheManager
import com.sgpublic.scit.tool.manager.ConfigManager
import com.sgpublic.scit.tool.activity.Login
import okhttp3.Call
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.UnknownHostException
import kotlin.jvm.Throws

class TableHelper (val context: Context) {
    companion object{
        private const val tag: String = "TableHelper"
    }

    fun getTable(callback: Callback){
        getTable(ConfigManager.getInt("week"), callback)
    }

    fun getTable(week: Int, callback: Callback?){
        APIHelper(ConfigManager.getString("access_token")).getTableRequest(
            ConfigManager.getString("school_year"),
            ConfigManager.getInt("semester")
        ).enqueue(object : okhttp3.Callback{
            override fun onFailure(call: Call, e: IOException) {
                MyLog.e("网络请求出错", e)
                if (e is UnknownHostException) {
                    callback?.onFailure(-401, context.getString(R.string.error_network), e)
                } else {
                    callback?.onFailure(-402, e.message, e)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val result = response.body?.string().toString()
                if (response.code == 200){
                    val objects = JSONObject(result)
                    when (objects.getInt("code")) {
                        200 -> {
                            CacheManager(context).save(CacheManager.CACHE_TABLE, result)
                            parsing(objects, week, callback)
                        }
                        -401 -> {
                            MyLog.i("用户登录状态失效")
                            callback?.onFailure(-100, context.getString(R.string.error_login_expired))
                            Login.startActivity(context)//, true)
                        }
                        else -> {
                            MyLog.e("服务器处理出错")
                            callback?.onFailure(-403, objects.getString("message"))
                        }
                    }
                } else {
                    MyLog.w("服务器内部出错")
                    callback?.onFailure(-405, context.getString(R.string.error_server_error))
                }
            }
        })
    }

    @Throws(JSONException::class)
    fun parsing(objects: JSONObject, week: Int, callback: Callback?){
        callback?.let {
            try {
                callback.onReadStart()
                var isEmpty = 0
                val tableObject = objects.getJSONArray("table")
                for (dayIndex in 0 until tableObject.length()){
                    val dayObject = tableObject.getJSONArray(dayIndex)
                    for (classIndex in 0 until dayObject.length()){
                        val indexArray = dayObject.getJSONObject(classIndex)
                            .getJSONArray("data")
                        if (indexArray.length() > 0){
                            for (index in 0 until indexArray.length()){
                                val indexObject = indexArray.getJSONObject(index)
                                val rangeObject = indexObject.getJSONArray("range")
                                var rangeJudge = false
                                for (indexRange in 0 until rangeObject.length()){
                                    rangeJudge = rangeJudge or (rangeObject.getInt(indexRange) == week)
                                }
                                if (rangeJudge){
                                    callback.onRead(dayIndex, classIndex, TableData(
                                        indexObject.getString("name"),
                                        indexObject.getString("teacher"),
                                        indexObject.getString("room")
                                    ))
                                    isEmpty ++
                                }
                            }
                            continue
                        }
                        callback.onRead(dayIndex, classIndex, null)
                    }
                }
                callback.onReadFinish(isEmpty == 0)
            } catch (e: JSONException) {
                MyLog.e("table数据解析失败", e)
                callback.onFailure(-404, e.message, e)
            }
        }
    }

    interface Callback {
        fun onFailure(code: Int, message: String?, e: Exception? = null) {}
        fun onReadStart(){}
        fun onRead(dayIndex: Int, classIndex: Int, data: TableData?){}
        fun onReadFinish(isEmpty: Boolean){}
    }
}
