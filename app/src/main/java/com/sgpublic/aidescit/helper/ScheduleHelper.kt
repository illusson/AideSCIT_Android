package com.sgpublic.aidescit.helper

import android.content.Context
import com.sgpublic.aidescit.R
import com.sgpublic.aidescit.activity.Login
import com.sgpublic.aidescit.data.ScheduleData
import com.sgpublic.aidescit.manager.CacheManager
import com.sgpublic.aidescit.manager.ConfigManager
import com.sgpublic.aidescit.util.MyLog
import okhttp3.Call
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.UnknownHostException

class ScheduleHelper (val context: Context) {
    companion object {
        val DAY_INDEX = arrayOf(
            "monday", "tuesday", "wednesday", "thursday",
            "friday", "saturday", "sunday"
        )

        val CLASS_INDEX = arrayOf(
            "am1", "am2", "pm1", "pm2", "ev"
        )
    }

    fun getSchedule(callback: Callback){
        getSchedule(ConfigManager.getInt("week"), callback)
    }

    fun getSchedule(week: Int, callback: Callback?){
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
                            CacheManager(context).save(CacheManager.CACHE_SCHEDULE, result)
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
                var isEmpty = 0
                val tableObject = objects.getJSONObject("schedule")
                callback.onReadStart(tableObject.isNull(DAY_INDEX[6]))
                DAY_INDEX.forEach forDay@{ day ->
                    if (tableObject.isNull(day)){
                        for (clazz in CLASS_INDEX){
                            callback.onRead(day, clazz, null)
                        }
                        return@forDay
                    }
                    val dayObject = tableObject.getJSONObject(day)
                    CLASS_INDEX.forEach forClass@{ clazz ->
                        if (dayObject.isNull(clazz)){
                            callback.onRead(day, clazz, null)
                            return@forClass
                        }
                        val indexArray = dayObject.getJSONArray(clazz)
                        for (index in 0 until indexArray.length()) {
                            val indexObject = indexArray.getJSONObject(index)
                            val rangeObject = indexObject.getJSONArray("range")
                            var rangeJudge = false
                            for (indexRange in 0 until rangeObject.length()) {
                                if (rangeObject.getInt(indexRange) == week){
                                    rangeJudge = true
                                    break
                                }
                            }
                            if (rangeJudge){
                                callback.onRead(day, clazz, ScheduleData(
                                    indexObject.getString("name"),
                                    indexObject.getString("teacher"),
                                    indexObject.getString("room")
                                ))
                                isEmpty++
                            } else {
                                callback.onRead(day, clazz, null)
                            }
                        }
                    }
                }
                callback.onReadFinish(isEmpty == 0)
            } catch (e: JSONException) {
                MyLog.e("schedule数据解析失败", e)
                callback.onFailure(-404, e.message, e)
            }
        }
    }

    interface Callback {
        fun onFailure(code: Int, message: String?, e: Exception? = null) {}
        fun onReadStart(isSundayEmpty: Boolean = false){}
        fun onRead(day: String, clazz: String, data: ScheduleData?){}
        fun onReadFinish(isEmpty: Boolean){}
    }
}
