package io.github.sgpublic.aidescit.core.module

import io.github.sgpublic.aidescit.core.util.Base64Util
import okhttp3.Call
import org.json.JSONObject
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

class LoginModule {
    fun login(username: String, password: String, callback: Callback){
        val call: Call = BaseAPI().getKeyRequest()
        call.enqueue(object : BaseAPI.BaseOkHttpCallback(callback) {
            override fun onResult(data: JSONObject) {
                val publicKey = data.getString("key").replace(
                    "\n", ""
                ).run {
                    return@run substring(26, length - 24)
                }
                val keySpec = X509EncodedKeySpec(Base64Util.decode(publicKey))
                val keyFactory = KeyFactory.getInstance("RSA")
                val pubKey = keyFactory.generatePublic(keySpec)
                val cp = Cipher.getInstance("RSA/ECB/PKCS1Padding")
                cp.init(Cipher.ENCRYPT_MODE, pubKey)
                val passwordHashed = data.getString("hash") + password
                val passwordEncrypted = Base64Util.encodeToString(
                    cp.doFinal(passwordHashed.toByteArray())
                )
                postData(username, passwordEncrypted, callback)
            }
        })
    }

    private fun postData(username: String, password: String, callback: Callback){
        val call: Call = BaseAPI().getLoginRequest(username, password)
        call.enqueue(object : BaseAPI.BaseOkHttpCallback(callback) {
            override fun onResult(data: JSONObject) {
                parse(data, callback)
            }

            override fun onWrongPassword() { }
        })
    }

    fun springboard(access: String, callback: SpringboardCallback){
        val call = BaseAPI(access).getSpringboardRequest()
        call.enqueue(object : BaseAPI.BaseOkHttpCallback(callback) {
            override fun onResult(data: JSONObject) {
                callback.onResult(data.getString("location"))
            }
        })
    }

    fun refreshToken(access: String, refresh: String, callback: Callback) {
        BaseAPI(access, refresh).getRefreshTokenRequest().enqueue(object
            : BaseAPI.BaseOkHttpCallback(callback) {
            override fun onResult(data: JSONObject) {
                parse(data, callback)
            }
        })
    }

    fun parse(data: JSONObject, callback: Callback) {
        val refresh = if (!data.isNull("refresh_token")){
            data.getString("refresh_token")
        } else { "" }
        callback.onResult(data.getString("access_token"), refresh)
    }

    interface Callback: BaseAPI.Callback {
        fun onResult(access: String, refresh: String) {}
    }

    interface SpringboardCallback: BaseAPI.Callback {
        fun onResult(location: String) {}
    }
}