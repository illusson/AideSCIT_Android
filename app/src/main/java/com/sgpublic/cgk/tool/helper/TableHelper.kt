package com.sgpublic.cgk.tool.helper;

import android.content.Context
import android.util.Log
import com.sgpublic.cgk.tool.R
import com.sgpublic.cgk.tool.base.MyLog
import com.sgpublic.cgk.tool.data.TableData
import com.sgpublic.cgk.tool.manager.CacheManager
import com.sgpublic.cgk.tool.manager.ConfigManager
import com.sgpublic.cgk.tool.ui.Login
import okhttp3.Call
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.UnknownHostException

class TableHelper (val context: Context) {
    companion object{
        private const val tag: String = "TableHelper"
    }

    fun getTable(config: ConfigManager, callback: Callback){
        APIHelper(config.getString("access_token")).getTableRequest(
            config.getString("school_year"),
            config.getInt("semester")
        ).enqueue(object : okhttp3.Callback{
            override fun onFailure(call: Call, e: IOException) {
                if (e is UnknownHostException) {
                    callback.onFailure(-401, context.getString(R.string.error_network), e)
                } else {
                    callback.onFailure(-402, e.message, e)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val result = response.body?.string().toString()
                MyLog.d(TableHelper::class.java, result)
                if (response.code == 200){
                    try {
                        val objects = JSONObject(result)
                        when (objects.getInt("code")) {
                            200 -> {
                                CacheManager(context).save(CacheManager.CACHE_TABLE, result)
                                parsing(objects, config.getInt("week"), callback)
                            }
                            504 -> {
                                callback.onFailure(-100, context.getString(R.string.error_login_expired))
                                Login.startActivity(context, true)
                            }
                            else -> {
                                callback.onFailure(-403, objects.getString("message"))
                            }
                        }
                    } catch (e: JSONException) {
                        callback.onFailure(-404, e.message, e)
                    }
                } else {
                    callback.onFailure(-405, context.getString(R.string.error_server_error))
                }
            }
        })
    }

    @Throws(JSONException::class)
    fun parsing(objects: JSONObject, week: Int, callback: Callback){
        callback.onReadStart()
        var isEmpty = 0
        val tableObject = objects.getJSONArray("table")
        for (dayIndex in 0 until tableObject.length()){
            val dayObject = tableObject.getJSONArray(dayIndex)
            for (classIndex in 0 until dayObject.length()){
                val classObject = dayObject.getJSONObject(classIndex)
                val classCount = classObject.getInt("count")
                if (classCount > 0){
                    val indexArray = classObject.getJSONArray("data")
                    for (index in 0 until classCount){
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
    }

    interface Callback {
        fun onFailure(code: Int, message: String?, e: Exception? = null) {}
        fun onReadStart(){}
        fun onRead(dayIndex: Int, classIndex: Int, data: TableData?){}
        fun onReadFinish(isEmpty: Boolean){}
    }
}
