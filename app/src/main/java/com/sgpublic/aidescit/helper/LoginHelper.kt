package com.sgpublic.aidescit.helper

import android.content.Context
import com.sgpublic.aidescit.R
import com.sgpublic.aidescit.activity.Login
import com.sgpublic.aidescit.manager.ConfigManager
import com.sgpublic.aidescit.util.Base64Util
import com.sgpublic.aidescit.util.MyLog
import okhttp3.Call
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.UnknownHostException
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

class LoginHelper (val context: Context) {
    fun login(username: String, password: String, callback: Callback){
        val call: Call = APIHelper().getKeyRequest()
        call.enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                MyLog.e("网络请求出错", e)
                if (e is UnknownHostException) {
                    callback.onFailure(-101, context.getString(R.string.error_network), e)
                } else {
                    callback.onFailure(-102, e.message, e)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    if (response.code == 200){
                        val result = response.body?.string().toString()
                        val objects = JSONObject(result)
                        if (objects.getInt("code") == 200) {
                            val publicKey = objects.getString("key").replace(
                                "\n", ""
                            ).run {
                                return@run substring(26, length - 24)
                            }
                            val keySpec = X509EncodedKeySpec(Base64Util.decode(publicKey))
                            val keyFactory = KeyFactory.getInstance("RSA")
                            val pubKey = keyFactory.generatePublic(keySpec)
                            val cp = Cipher.getInstance("RSA/ECB/PKCS1Padding")
                            cp.init(Cipher.ENCRYPT_MODE, pubKey)
                            val passwordHashed = objects.getString("hash") + password
                            val passwordEncrypted = Base64Util.encodeToString(
                                cp.doFinal(passwordHashed.toByteArray())
                            )
                            postData(username, passwordEncrypted, callback)
                        } else {
                            MyLog.e("服务器处理出错")
                            callback.onFailure(-104, objects.getString("message"))
                        }
                    } else {
                        MyLog.w("服务器内部出错")
                        callback.onFailure(-105, context.getString(R.string.error_server_error))
                    }
                } catch (e: JSONException){
                    MyLog.e("login数据解析失败", e)
                    callback.onFailure(-103, e.message, e)
                }
            }
        })
    }

    private fun postData(username: String, password: String, callback: Callback){
        val call: Call = APIHelper().getLoginRequest(username, password)
        call.enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                MyLog.e("网络请求出错", e)
                if (e is UnknownHostException) {
                    callback.onFailure(-111, context.getString(R.string.error_network), e)
                } else {
                    callback.onFailure(-112, e.message, e)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                parse(response, callback)
            }
        })
    }

    fun springboard(access: String, callback: SpringboardCallback){
        val call = APIHelper(access).getSpringboardRequest()
        call.enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (e is UnknownHostException) {
                    callback.onFailure(-131, context.getString(R.string.error_network), e)
                } else {
                    callback.onFailure(-132, e.message, e)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.code == 200){
                    val result = response.body?.string().toString()
                    try {
                        val objects = JSONObject(result)
                        when (objects.getInt("code")) {
                            200 -> {
                                callback.onResult(objects.getString("location"))
                            }
                            -401 -> {
                                callback.onFailure(-100, context.getString(R.string.error_login_expired))
                                Login.startActivity(context)//, true)
                            }
                            else -> {
                                callback.onFailure(-134, objects.getString("message"))
                            }
                        }
                    } catch (e: JSONException){
                        callback.onFailure(-133, e.message, e)
                    }
                } else {
                    callback.onFailure(-135, context.getString(R.string.error_server_error))
                }
            }
        })
    }

    fun refreshToken(config: ConfigManager, callback: Callback) {
        APIHelper(
            config.getString("access_token"),
            config.getString("refresh_token")
        ).getRefreshTokenRequest().enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (e is UnknownHostException) {
                    callback.onFailure(-121, context.getString(R.string.error_network), e)
                } else {
                    callback.onFailure(-122, e.message, e)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                parse(response, callback)
            }
        })
    }

    private fun parse(response: Response, callback: Callback){
        if (response.code == 200){
            val result = response.body?.string().toString()
            try {
                val objects = JSONObject(result)
                when (objects.getInt("code")) {
                    200 -> {
                        val refresh = if (!objects.isNull("refresh_token")){
                            objects.getString("refresh_token")
                        } else { "" }
                        callback.onResult(objects.getString("access_token"), refresh)
                    }
                    -401 -> {
                        MyLog.i("用户登录状态失效")
                        if (ConfigManager.getBoolean("is_login")){
                            callback.onFailure(-100, context.getString(R.string.error_login_expired))
                            Login.startActivity(context)//, true)
                        } else {
                            callback.onFailure(-100, objects.getString("message"))
                        }
                    }
                    else -> {
                        MyLog.e("服务器处理出错，${objects.getString("message")}")
                        callback.onFailure(-114, objects.getString("message"))
                    }
                }
            } catch (e: JSONException){
                MyLog.e("evaluate数据解析失败", e)
                callback.onFailure(-113, e.message, e)
            }
        } else {
            MyLog.w("服务器内部出错")
            callback.onFailure(-115, context.getString(R.string.error_server_error))
        }
    }

    interface Callback {
        fun onFailure(code: Int, message: String?, e: Exception? = null) {}
        fun onResult(access: String, refresh: String) {}
    }

    interface SpringboardCallback {
        fun onFailure(code: Int, message: String?, e: Exception? = null) {}
        fun onResult(location: String) {}
    }
}