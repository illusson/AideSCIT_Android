package com.sgpublic.scit.tool.helper

import android.content.Context
import com.sgpublic.scit.tool.R
import com.sgpublic.scit.tool.util.MyLog
import com.sgpublic.scit.tool.data.ExamData
import com.sgpublic.scit.tool.manager.CacheManager
import com.sgpublic.scit.tool.activity.Login
import okhttp3.Call
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.UnknownHostException

class ExamHelper (private val context: Context) {
    fun getExam(access: String, callback: Callback){
        APIHelper(access).getExamRequest()
            .enqueue(object : okhttp3.Callback {
                override fun onFailure(call: Call, e: IOException) {
                    MyLog.e("网络请求出错", e)
                    if (e is UnknownHostException) {
                        callback.onFailure(-601, context.getString(R.string.error_network), e)
                    } else {
                        callback.onFailure(-602, e.message, e)
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.code == 200){
                        val result = response.body?.string().toString()
                        val objects = JSONObject(result)
                        when (objects.getInt("code")) {
                            200 -> {
                                CacheManager(context).save(CacheManager.CACHE_EXAM, result)
                                parsing(objects, callback)
                            }
                            -401 -> {
                                MyLog.i("用户登录状态失效")
                                callback.onFailure(-100, context.getString(R.string.error_login_expired))
                                Login.startActivity(context)//, true)
                            }
                            else -> {
                                MyLog.e("服务器处理出错")
                                callback.onFailure(-603, objects.getString("message"))
                            }
                        }
                    } else {
                        MyLog.w("服务器内部出错")
                        callback.onFailure(-605, context.getString(R.string.error_server_error))
                    }
                }
            })
    }

    fun parsing(objects: JSONObject, callback: Callback) {
        try {
            callback.onReadStart()

            val examArray: JSONArray = objects.getJSONArray("exam")
            if (examArray.length() > 0){
                for (index in 0 until examArray.length()){
                    val examData: JSONObject = examArray.getJSONObject(index)
                    callback.onReadData(ExamData(
                        examData.getString("name"),
                        examData.getString("time"),
                        examData.getString("location"),
                        examData.getString("set_num")
                    ))
                }
            }

            callback.onReadFinish()
        } catch (e: JSONException){
            MyLog.e("exam数据解析失败", e)
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