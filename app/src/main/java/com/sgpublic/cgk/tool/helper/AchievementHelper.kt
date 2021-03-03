package com.sgpublic.cgk.tool.helper

import android.content.Context
import android.util.Log
import com.sgpublic.cgk.tool.R
import com.sgpublic.cgk.tool.base.MyLog
import com.sgpublic.cgk.tool.data.FailedMarkData
import com.sgpublic.cgk.tool.data.PassedMarkData
import com.sgpublic.cgk.tool.manager.CacheManager
import com.sgpublic.cgk.tool.manager.ConfigManager
import com.sgpublic.cgk.tool.ui.Login
import okhttp3.Call
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.UnknownHostException

class AchievementHelper (private val context: Context, private val username: String = "") {
    companion object {
        private const val tag: String = "AchievementHelper"
    }

    fun getMark(config: ConfigManager, callback: Callback){
        APIHelper(config.getString("access_token")).getAchievementRequest(
            config.getString("school_year_inquiry"),
            config.getInt("semester_inquiry")
        ).enqueue(object : okhttp3.Callback{
            override fun onFailure(call: Call, e: IOException) {
                if (e is UnknownHostException) {
                    callback.onFailure(-501, context.getString(R.string.error_network), e)
                } else {
                    callback.onFailure(-502, e.message, e)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.code == 200){
                    val result = response.body?.string().toString()
                    MyLog.d(AchievementHelper::class.java, result)
                    try {
                        val objects = JSONObject(result)
                        when (objects.getInt("code")) {
                            200 -> {
                                CacheManager(context).save(CacheManager.CACHE_ACHIEVEMENT, result)
                                parsing(objects.getJSONObject("achievement"), callback)
                            }
                            504 -> {
                                callback.onFailure(-100, context.getString(R.string.error_login_expired))
                                Login.startActivity(context, true)
                            }
                            else -> {
                                callback.onFailure(-504, objects.getString("message"))
                            }
                        }
                    } catch (e: JSONException){
                        callback.onFailure(-504, e.message, e)
                    }
                } else {
                    callback.onFailure(-505, context.getString(R.string.error_server_error))
                }
            }
        })
    }

    @Throws(JSONException::class)
    fun parsing(objects: JSONObject, callback: Callback) {
        callback.onReadStart()

        objects.getJSONObject("passed").let {
            if (it.getInt("count") > 0){
                val passedObject: JSONArray = it.getJSONArray("data")
                for (passedIndex in 0 until passedObject.length()){
                    val passedData: JSONObject = passedObject.getJSONObject(passedIndex)
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

        objects.getJSONObject("failed").let {
            if (it.getInt("count") > 0){
                val passedObject: JSONArray = it.getJSONArray("data")
                for (passedIndex in 0 until passedObject.length()){
                    val passedData: JSONObject = passedObject.getJSONObject(passedIndex)
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