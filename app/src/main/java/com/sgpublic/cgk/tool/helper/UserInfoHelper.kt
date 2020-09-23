package com.sgpublic.cgk.tool.helper

import android.content.Context
import android.util.Log
import com.sgpublic.cgk.tool.R
import com.sgpublic.cgk.tool.data.UserInfoData
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.UnknownHostException

class UserInfoHelper(val context: Context, private val username: String, private val session: String) {
    companion object {
        private const val tag: String = "UserInfoHelper"
    }

    fun getUserInfo(identity: String = "student", callback: Callback){
        val call: Call = APIHelper(username, session).getInfoRequest(identity)
        call.enqueue(object : okhttp3.Callback{
            override fun onFailure(call: Call, e: IOException) {
                if (e is UnknownHostException) {
                    callback.onFailure(-301, context.getString(R.string.error_network), e)
                } else {
                    callback.onFailure(-302, e.message, e)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val result: String = response.body?.string().toString()
                try {
                    val objects = JSONObject(result)
                    if (objects.getInt("code") == 0){
                        val facultyObject = objects.getJSONObject("faculty")
                        val faculty = UserInfoData(
                            facultyObject.getString("name"),
                            facultyObject.getLong("id")
                        )
                        val specialtyObject = objects.getJSONObject("specialty")
                        val specialty = UserInfoData(
                            specialtyObject.getString("name"),
                            specialtyObject.getLong("id")
                        )
                        val classObject = objects.getJSONObject("class")
                        val userClass = UserInfoData(
                            classObject.getString("name"),
                            classObject.getLong("id")
                        )

                        callback.onResult(
                            objects.getString("name"),
                            faculty,
                            specialty,
                            userClass,
                            objects.getInt("grade")
                        )
                    } else {
                        callback.onFailure(-304, objects.getString("message"))
                    }
                } catch (e: JSONException) {
                    callback.onFailure(-303, e.message, e)
                }
            }
        })
    }

    interface Callback {
        fun onFailure(code: Int, message: String?, e: Exception? = null) {}
        fun onResult(
            name: String,
            faculty: UserInfoData,
            specialty: UserInfoData,
            userClass: UserInfoData,
            grade: Int
        ){}
    }
}