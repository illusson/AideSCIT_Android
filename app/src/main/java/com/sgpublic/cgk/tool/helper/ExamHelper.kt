package com.sgpublic.cgk.tool.helper

import android.content.Context
import android.util.Log
import com.sgpublic.cgk.tool.R
import com.sgpublic.cgk.tool.data.ExamData
import com.sgpublic.cgk.tool.manager.CacheManager
import okhttp3.Call
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.UnknownHostException

class ExamHelper (private val context: Context, private val username: String) {
    companion object{
        private const val tag: String = "ExamHelper"
    }

    fun getExam(session: String, callback: Callback){
        APIHelper(username, session).getExamRequest()
            .enqueue(object : okhttp3.Callback {
                override fun onFailure(call: Call, e: IOException) {
                    if (e is UnknownHostException) {
                        callback.onFailure(-601, context.getString(R.string.error_network), e)
                    } else {
                        callback.onFailure(-602, e.message, e)
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val result = response.body?.string().toString()
                    try {
                        val objects = JSONObject(result)
                        if (objects.getInt("code") == 0){
                            CacheManager(context).save(CacheManager.CACHE_EXAM, result)
                            parsing(objects, callback)
                        } else {
                            callback.onFailure(-604, objects.getString("message"))
                        }
                    } catch (e: JSONException){
                        callback.onFailure(-604, e.message, e)
                    }
                }
            })
    }

    @Throws(JSONException::class)
    fun parsing(objects: JSONObject, callback: Callback) {
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
    }

    interface Callback {
        fun onFailure(code: Int, message: String?, e: Exception? = null) {}
        fun onReadStart() {}
        fun onReadData(data: ExamData) {}
        fun onReadFinish() {}
    }
}