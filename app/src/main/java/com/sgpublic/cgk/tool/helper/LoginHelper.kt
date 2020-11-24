package com.sgpublic.cgk.tool.helper

import android.content.Context
import android.util.Log
import com.sgpublic.cgk.tool.R
import com.sgpublic.cgk.tool.base.Base64Helper
import com.sgpublic.cgk.tool.manager.ConfigManager
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
    companion object {
        private const val tag: String = "LoginHelper"
        private const val publicKey: String = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCmBCWNtxeofYkH1e9GXKgszj4E\n" +
                "cJojNvlesPDM201q+fiVf2X4SWPNjdduRS19dq9Koq4Dz0ul3xV6E3ydCHl88qSa\n" +
                "94fDGZa24UueYVYE0ytYuJcOu164GlIfu48Rir0NXA2BfoQxMcSpMmLJt20rSg+E\n" +
                "oP24zaj3ti78b1zJEwIDAQAB"
    }

    fun login(username: String, password: String, callback: Callback){
        try {
            val keySpec = X509EncodedKeySpec(Base64Helper.Decode(publicKey))
            val keyFactory = KeyFactory.getInstance("RSA")
            val pubKey = keyFactory.generatePublic(keySpec)
            val cp = Cipher.getInstance("RSA/ECB/PKCS1Padding")
            cp.init(Cipher.ENCRYPT_MODE, pubKey)
            val passwordEncrypted = Base64Helper.Encode(
                cp.doFinal(password.toByteArray())
            ).toString()

            val call: Call = APIHelper().getLoginRequest(username, passwordEncrypted)
            call.enqueue(object : okhttp3.Callback{
                override fun onFailure(call: Call, e: IOException) {
                    if (e is UnknownHostException) {
                        callback.onFailure(-101, context.getString(R.string.error_network), e)
                    } else {
                        callback.onFailure(-102, e.message, e)
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    parse(response, callback)
                }
            })
        } catch (e: java.lang.Exception) {
            callback.onFailure(-105, e.message, e)
        }
    }

    fun springboard(access: String, callback: SpringboardCallback){
        val helper = APIHelper(ConfigManager(context)
            .getString("access_token")).getSpringboardRequest()
        helper.enqueue(object : okhttp3.Callback{
            override fun onFailure(call: Call, e: IOException) {
                if (e is UnknownHostException) {
                    callback.onFailure(-101, context.getString(R.string.error_network), e)
                } else {
                    callback.onFailure(-102, e.message, e)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.code == 200){
                    val result = response.body?.string().toString()
                    try {
                        val objects = JSONObject(result)
                        if (objects.getInt("code") == 200) {
                            callback.onResult(objects.getString("location"))
                        } else {
                            callback.onFailure(-104, objects.getString("message"))
                        }
                    } catch (e: JSONException){
                        callback.onFailure(-103, e.message, e)
                    }
                } else {
                    callback.onFailure(-105, context.getString(R.string.error_server_error))
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

    private fun parse(response: Response, callback: Callback){
        if (response.code == 200){
            val result = response.body?.string().toString()
            try {
                val objects = JSONObject(result)
                if (objects.getInt("code") == 200) {
                    callback.onResult(
                        objects.getString("access_token"),
                        objects.getString("refresh_token")
                    )
                } else {
                    callback.onFailure(-104, objects.getString("message"))
                }
            } catch (e: JSONException){
                callback.onFailure(-103, e.message, e)
            }
        } else {
            callback.onFailure(-105, context.getString(R.string.error_server_error))
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