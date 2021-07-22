package com.sgpublic.aidescit.helper

import android.content.Context
import com.sgpublic.aidescit.R
import com.sgpublic.aidescit.util.MyLog
import com.sgpublic.aidescit.data.EvaluationData
import com.sgpublic.aidescit.data.EvaluationQuestionData
import com.sgpublic.aidescit.activity.Login
import okhttp3.Call
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.UnknownHostException

class EvaluateHelper(private val context: Context, private val access_token: String) {
    companion object {
        private const val tag: String = "EvaluateHelper"
    }

    fun check(callback: CheckCallback){
        val call = APIHelper(access_token).getEvaluateRequest()
        call.enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                MyLog.w("网络请求出错", e)
                if (e is UnknownHostException) {
                    callback.onFailure(-701, context.getString(R.string.error_network), e)
                } else {
                    callback.onFailure(-702, e.message, e)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.code == 200){
                    val result = response.body?.string().toString()
                    try {
                        val objects = JSONObject(result)
                        when (objects.getInt("code")) {
                            200 -> {
                                callback.onResult(objects.getInt("count"))
                            }
                            -401 -> {
                                MyLog.i("用户登录状态失效")
                                callback.onFailure(-100, context.getString(R.string.error_login_expired))
                                Login.startActivity(context)//, true)
                            }
                            else -> {
                                MyLog.e("服务器处理出错")
                                callback.onFailure(-704, objects.getString("message"))
                            }
                        }
                    } catch (e: JSONException){
                        MyLog.e("exam数据解析失败", e)
                        callback.onFailure(-704, e.message, e)
                    }
                } else {
                    MyLog.e("服务器内部错误")
                    callback.onFailure(-705, context.getString(R.string.error_server_error))
                }
            }
        })
    }

    fun get(index: Int, callback: GetCallback){
        val call = APIHelper(access_token).getEvaluateRequest("get", index)
        call.enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                MyLog.e("网络请求出错", e)
                if (e is UnknownHostException) {
                    callback.onFailure(-711, context.getString(R.string.error_network), e)
                } else {
                    callback.onFailure(-712, e.message, e)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.code == 200){
                    val result = response.body?.string().toString()
                        val objects = JSONObject(result)
                        when (objects.getInt("code")) {
                            200 -> {
                                parsing(objects.getJSONObject("evaluate"), callback)
                            }
                            -401 -> {
                                MyLog.i("用户登录状态失效")
                                callback.onFailure(-100, context.getString(R.string.error_login_expired))
                                Login.startActivity(context)//, true)
                            }
                            else -> {
                                MyLog.e("服务器处理出错")
                                callback.onFailure(-714, objects.getString("message"))
                            }
                        }
                } else {
                    MyLog.w("服务器内部出错")
                    callback.onFailure(-715, context.getString(R.string.error_server_error))
                }
            }
        })
    }

    private fun parsing(data: JSONObject, callback: GetCallback){
        try {
            val evaluationData: ArrayList<EvaluationData> = ArrayList()

            val teachers = data.getJSONArray("evaluations")
            for (t_index in 0 until teachers.length()){
                val teacherData = teachers.getJSONObject(t_index)
                val questions: ArrayList<EvaluationQuestionData> = ArrayList()
                val objects = data.getJSONArray("questions")
                for (q_index in 0 until objects.length()){
                    val questionIndex = objects.getJSONObject(q_index)
                    val options: ArrayList<String> = ArrayList()
                    val optionsData = questionIndex.getJSONArray("options")
                    for (o_index in 0 until optionsData.length()){
                        options.add(optionsData.getString(o_index))
                    }

                    questions.add(EvaluationQuestionData(
                        questionIndex.getString("text"), options,
                        teacherData.getJSONArray("options").getInt(q_index)
                    ))
                }
                val avatar: String? = if (!teacherData.isNull("avatar")){
                    teacherData.getString("avatar")
                } else { null }
                evaluationData.add(EvaluationData(
                    data.getString("subject"),
                    teacherData.getString("teacher"),
                    avatar, questions
                ))
            }

            callback.onResult(evaluationData)
        } catch (e: JSONException){
            MyLog.e("evaluate数据解析失败", e)
            callback.onFailure(-714, e.message, e)
        }
    }

    fun post(index: Int, data: JSONObject, callback: PostCallback){
        val call = APIHelper(access_token).getEvaluateRequest(
            "submit", index, data.toString()
        )
        call.enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                MyLog.e("网络请求出错", e)
                if (e is UnknownHostException) {
                    callback.onFailure(-721, context.getString(R.string.error_network), e)
                } else {
                    callback.onFailure(-722, e.message, e)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.code == 200){
                    val result = response.body?.string().toString()
                    try {
                        val objects = JSONObject(result)
                        when (objects.getInt("code")) {
                            200 -> {
                                callback.onResult()
                            }
                            -401 -> {
                                MyLog.i("用户登录状态失效")
                                callback.onFailure(-100, context.getString(R.string.error_login_expired))
                                Login.startActivity(context)//, true)
                            }
                            else -> {
                                MyLog.e("服务器处理出错")
                                callback.onFailure(-724, objects.getString("message"))
                            }
                        }
                    } catch (e: JSONException){
                        MyLog.e("evaluate数据解析失败", e)
                        callback.onFailure(-724, e.message, e)
                    }
                } else {
                    MyLog.w("服务器内部出错")
                    callback.onFailure(-725, context.getString(R.string.error_server_error))
                }
            }
        })
    }

    interface CheckCallback {
        fun onFailure(code: Int, message: String?, e: Exception? = null) {}
        fun onResult(count: Int) {}
    }

    interface GetCallback {
        fun onFailure(code: Int, message: String?, e: Exception? = null) {}
        fun onResult(data: ArrayList<EvaluationData>) {}
    }

    interface PostCallback {
        fun onFailure(code: Int, message: String?, e: Exception? = null) {}
        fun onResult() {}
    }
}