package io.github.sgpublic.aidescit.core.module

import androidx.appcompat.app.AppCompatActivity
import okhttp3.Call
import org.json.JSONObject

class UserInfoModule {
    fun getUserInfo(access: String, callback: Callback){
        val call: Call = BaseAPI(access).getInfoRequest()
        call.enqueue(object : BaseAPI.BaseOkHttpCallback(callback) {
            override fun onResult(data: JSONObject) {
                val info = data.getJSONObject("info")
                val facultyString = info.getString("faculty")
                val specialtyString = info.getString("specialty")
                val classString = info.getString("class")
                callback.onResult(
                    info.getString("name"), facultyString, specialtyString,
                    classString, info.getInt("grade")
                )
            }

            override fun onWrongPassword() { }
        })
    }

    interface Callback: BaseAPI.Callback {
        fun onResult(name: String, faculty: String, specialty: String, userClass: String, grade: Int){}
    }
}