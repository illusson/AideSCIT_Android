package com.sgpublic.cgk.tool.helper

import android.content.Context
import android.util.Log
import com.sgpublic.cgk.tool.R
import com.sgpublic.cgk.tool.base.MyLog
import com.sgpublic.cgk.tool.data.ExamData
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

class ExamHelper (private val context: Context) {
    companion object{
        private const val tag: String = "ExamHelper"
    }

    fun getExam(access: String, callback: Callback){
        APIHelper(access).getExamRequest()
            .enqueue(object : okhttp3.Callback {
                override fun onFailure(call: Call, e: IOException) {
                    if (e is UnknownHostException) {
                        callback.onFailure(-601, context.getString(R.string.error_network), e)
                    } else {
                        callback.onFailure(-602, e.message, e)
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.code == 200){
                        val result = response.body?.string().toString()
                        MyLog.d(ExamHelper::class.java, result)
                        val objects = JSONObject(result)
                        when (objects.getInt("code")) {
                            200 -> {
                                CacheManager(context).save(CacheManager.CACHE_EXAM, result)
                                parsing(objects, callback)
                            }
                            504 -> {
                                callback.onFailure(-100, context.getString(R.string.error_login_expired))
                                Login.startActivity(context, true)
                            }
                            else -> {
                                callback.onFailure(-604, objects.getString("message"))
                            }
                        }
                    } else {
                        callback.onFailure(-605, context.getString(R.string.error_server_error))
                    }
                }
            })
    }

    fun parsing(objects: JSONObject, callback: Callback) {
        try {
            callback.onReadStart()

            val examObject: JSONObject = objects.getJSONObject("exam")
            if (examObject.getInt("count") > 0){
                val examArray: JSONArray = examObject.getJSONArray("data")
                for (index in 0 until examArray.length()){
                    val examData: JSONObject = examArray.getJSONObject(index)
                    callback.onReadData(ExamData(
                        examData.getString("name"),
                        examData.getString("time"),
                        examData.getString("location"),
                        examData.getString("sit_num")
                    ))
                }
            }

            callback.onReadFinish()
        } catch (e: JSONException){
            callback.onFailure(-604, e.message, e)
        }
    }

    interface Callback {
        fun onFailure(code: Int, message: String?, e: Exception? = null) {}
        fun onReadStart() {}
        fun onReadData(data: ExamData) {}
        fun onReadFinish() {}
    }
}