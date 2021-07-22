package com.sgpublic.aidescit.helper

import android.content.Context
import com.sgpublic.aidescit.R
import com.sgpublic.aidescit.util.MyLog
import com.sgpublic.aidescit.activity.Login
import okhttp3.Call
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.UnknownHostException

class UserInfoHelper(val context: Context, private val username: String, private val session: String) {
    companion object {
        private const val tag: String = "UserInfoHelper"
    }

    fun getUserInfo(access: String, callback: Callback){
        val call: Call = APIHelper(access).getInfoRequest()
        call.enqueue(object : okhttp3.Callback{
            override fun onFailure(call: Call, e: IOException) {
                MyLog.e("网络请求出错", e)
                if (e is UnknownHostException) {
                    callback.onFailure(-301, context.getString(R.string.error_network), e)
                } else {
                    callback.onFailure(-302, e.message, e)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.code == 200){
                    val result: String = response.body?.string().toString()
                    try {
                        var objects = JSONObject(result)
                        when (objects.getInt("code")) {
                            200 -> {
                                objects = objects.getJSONObject("info")
                                val facultyString = objects.getString("faculty")
                                val specialtyString = objects.getString("specialty")
                                val classString = objects.getString("class")
                                callback.onResult(
                                    objects.getString("name"),
                                    facultyString,
                                    specialtyString,
                                    classString,
                                    objects.getInt("grade")
                                )
                            }
                            -401 -> {
                                MyLog.i("用户登录状态失效")
                                callback.onFailure(-100, context.getString(R.string.error_login_expired))
                                Login.startActivity(context)//, true)
                            }
                            else -> {
                                MyLog.e("服务器处理出错")
                                callback.onFailure(-304, objects.getString("message"))
                            }
                        }
                    } catch (e: JSONException) {
                        MyLog.e("info数据解析失败", e)
                        callback.onFailure(-303, e.message, e)
                    }
                } else {
                    MyLog.w("服务器内部出错")
                    callback.onFailure(-305, context.getString(R.string.error_server_error))
                }
            }
        })
    }

    interface Callback {
        fun onFailure(code: Int, message: String?, e: Exception? = null) {}
        fun onResult(name: String, faculty: String, specialty: String, userClass: String, grade: Int){}
    }
}