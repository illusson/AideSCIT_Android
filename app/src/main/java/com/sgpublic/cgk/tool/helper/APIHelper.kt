package com.sgpublic.cgk.tool.helper

import android.util.Log
import com.sgpublic.cgk.tool.manager.Security
import okhttp3.Call
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.concurrent.TimeUnit

class APIHelper(private val access: String, private val refresh: String) {
    companion object {
        private const val tag: String = "APIHelper"

        private const val API_HOST: String = "https://tool.eclass.sgpublic.xyz/api"

        const val METHOD_GET: Int = 0
        const val METHOD_POST: Int = 1

        public fun getTS() = System.currentTimeMillis() / 1000
    }

    constructor() : this("", "")
    constructor(username: String) : this(username, "")

    fun getLoginRequest(username: String, passwordEncrypted: String): Call {
        val url = "login.php"
        val argArray: Map<String, Any> = mapOf(
            "password" to passwordEncrypted,
            "ts" to getTS(),
            "username" to username
        )
        return onReturn(url, argArray)
    }

    fun getSpringboardRequest(): Call {
        val url = "springboard.php"
        val argArray: Map<String, Any> = mapOf(
            "access_token" to access,
            "ts" to getTS()
        )
        return onReturn(url, argArray)
    }

    fun getRefreshTokenRequest(): Call {
        val url = "token.php"
        val argArray: Map<String, Any> = mapOf(
            "access_token" to access,
            "refresh_token" to refresh,
            "ts" to getTS()
        )
        return onReturn(url, argArray)
    }

    fun getSentenceRequest(): Call {
        val url = "hitokoto.php"
        val argArray: Map<String, Any> = mapOf(
            "access_token" to access,
            "ts" to getTS()
        )
        return onReturn(url, argArray)
    }

    fun getDayRequest(): Call{
        val url = "day.php"
        return onReturn(url)
    }

    fun getInfoRequest(): Call{
        val url = "info.php"
        val argArray: Map<String, Any> = mapOf(
            "access_token" to access,
            "ts" to getTS()
        )
        return onReturn(url, argArray)
    }

    fun getTableRequest(year: String, semester: Int): Call{
        val url = "table.php"
        val argArray: Map<String, Any> = mapOf(
            "access_token" to access,
            "semester" to semester,
            "ts" to getTS(),
            "year" to year
        )
        return onReturn(url, argArray)
    }

    fun getExamRequest(): Call{
        val url = "exam.php"
        val argArray: Map<String, Any> = mapOf(
            "access_token" to access,
            "ts" to getTS()
        )
        return onReturn(url, argArray)
    }

    fun getAchievementRequest(schoolYear: String, semester: Int): Call{
        val url = "achievement.php"
        val argArray: Map<String, Any> = mapOf(
            "access_token" to access,
            "semester" to semester,
            "ts" to getTS(),
            "year" to schoolYear
        )
        return onReturn(url, argArray)
    }

    fun getUpdateRequest(version: String): Call {
        val url = "https://tool.eclass.sgpublic.xyz/update/index.php"
        val argArray: Map<String, Any> = mapOf("version" to version)
        return onReturn(url, argArray, METHOD_GET, false)
    }

    private fun onReturn(url: String, argArray: Map<String, Any>? = null, method: Int = METHOD_POST, withSign: Boolean = true): Call {
        val client: OkHttpClient = OkHttpClient.Builder().run{
            readTimeout(5, TimeUnit.SECONDS)
            writeTimeout(5, TimeUnit.SECONDS)
            connectTimeout(5, TimeUnit.SECONDS)
            followRedirects(false)
            followSslRedirects(false)
            build()
        }
        val urlFinal = if (url.startsWith("http")){
            url
        } else {
            val apiVersion = if (com.sgpublic.cgk.tool.BuildConfig.DEBUG) "v2" else "v1"
            "$API_HOST/$apiVersion/$url"
        }
        val request: Request = Request.Builder().run {
            if (method == METHOD_POST) {
                url(urlFinal)
                post(GetArgs(argArray).getForm(withSign))
//                Log.d(tag, GetArgs(argArray).getString(true))
            } else {
                url(urlFinal + "?" + GetArgs(argArray).getString(withSign))
            }
            build()
        }
        return client.newCall(request)
    }

    private class GetArgs(val argArray: Map<String, Any>?){
        private var string: String
        init {
            string = StringBuilder().run {
                argArray?.let{
                    for ((argName, argValue) in it){
                        val argValueDecoded = argValue.toString()
                        append("&$argName=$argValueDecoded")
                    }
                }
                toString()
            }
            if (string.length > 1){
                string = string.substring(1)
            }
        }

        fun getString(outSign: Boolean): String {
            return StringBuilder(string).run {
                if (outSign){
                    append("&sign=" + getSign())
                }
                toString()
            }
        }

        fun getForm(outSign: Boolean): FormBody {
            return FormBody.Builder().run {
                argArray?.let {
                    for ((argName, argValue) in argArray){
                        val argValueDecoded = argValue.toString()
                        add(argName, argValueDecoded)
                    }
                }
                if (outSign){
                    add("sign",  getSign())
                }
                build()
            }
        }

        private fun getSign(): String {
            val content = string + Security.SECRET_KEY
            try {
                val instance:MessageDigest = MessageDigest.getInstance("MD5")
                val digest:ByteArray = instance.digest(content.toByteArray())
                return StringBuffer().run {
                    for (b in digest) {
                        val i :Int = b.toInt() and 0xff
                        var hexString = Integer.toHexString(i)
                        if (hexString.length < 2) {
                            hexString = "0$hexString"
                        }
                        append(hexString)
                    }
                    toString()
                }
            } catch (e: NoSuchAlgorithmException) {
                return ""
            }
        }
    }
}