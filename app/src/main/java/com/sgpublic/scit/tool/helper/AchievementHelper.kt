package com.sgpublic.scit.tool.helper

import android.content.Context
import com.sgpublic.scit.tool.R
import com.sgpublic.scit.tool.base.MyLog
import com.sgpublic.scit.tool.data.FailedMarkData
import com.sgpublic.scit.tool.data.PassedMarkData
import com.sgpublic.scit.tool.manager.CacheManager
import com.sgpublic.scit.tool.manager.ConfigManager
import com.sgpublic.scit.tool.activity.Login
import okhttp3.Call
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.UnknownHostException

class AchievementHelper(private val context: Context) {
    fun getMark(config: ConfigManager, callback: Callback){
        var schoolYearInquiry = config.getString("school_year_inquiry")
        if (schoolYearInquiry == context.getString(R.string.text_achievement_all)){
            schoolYearInquiry = "all"
        }
        APIHelper(config.getString("access_token")).getAchievementRequest(
            schoolYearInquiry, config.getInt("semester_inquiry")
        ).enqueue(object : okhttp3.Callback{
            override fun onFailure(call: Call, e: IOException) {
                MyLog.e("网络请求出错", e)
                if (e is UnknownHostException) {
                    callback.onFailure(-501, context.getString(R.string.error_network), e)
                } else {
                    callback.onFailure(-502, e.message, e)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.code == 200){
                    val result = response.body?.string().toString()
                    try {
                        val objects = JSONObject(result)
                        when (objects.getInt("code")) {
                            200 -> {
                                CacheManager(context).save(CacheManager.CACHE_ACHIEVEMENT, result)
                                parsing(objects.getJSONObject("achieve"), callback)
                            }
                            -401 -> {
                                MyLog.i("用户登录状态失效")
                                callback.onFailure(-100, context.getString(R.string.error_login_expired))
                                Login.startActivity(context)//, true)
                            }
                            else -> {
                                MyLog.e("服务器处理出错")
                                callback.onFailure(-504, objects.getString("message"))
                            }
                        }
                    } catch (e: JSONException){
                        MyLog.e("achieve数据解析失败", e)
                        callback.onFailure(-504, e.message, e)
                    }
                } else {
                    MyLog.w("服务器内部出错")
                    callback.onFailure(-505, context.getString(R.string.error_server_error))
                }
            }
        })
    }

    @Throws(JSONException::class)
    fun parsing(objects: JSONObject, callback: Callback) {
        callback.onReadStart()

        objects.getJSONArray("current").let {
            if (it.length() > 0){
                for (passedIndex in 0 until it.length()){
                    val passedData: JSONObject = it.getJSONObject(passedIndex)
                    callback.onReadPassed(PassedMarkData(
                        passedData.getString("name"),
                        passedData.getString("paper_score"),
                        passedData.getString("mark"),
                        passedData.getString("retake"),
                        passedData.getString("rebuild"),
                        passedData.getString("credit")
                    ))
                }
            }
        }

        objects.getJSONArray("failed").let {
            if (it.length() > 0){
                for (passedIndex in 0 until it.length()){
                    val passedData: JSONObject = it.getJSONObject(passedIndex)
                    callback.onReadFailed(FailedMarkData(
                        passedData.getString("name"),
                        passedData.getString("mark")
                    ))
                }
            }
        }

        callback.onReadFinish()
    }

    interface Callback {
        fun onFailure(code: Int, message: String?, e: Exception? = null) {}
        fun onReadStart() {}
        fun onReadPassed(data: PassedMarkData) {}
        fun onReadFailed(data: FailedMarkData) {}
        fun onReadFinish() {}
    }
}