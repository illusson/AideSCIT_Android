package com.sgpublic.scit.tool.helper

import com.sgpublic.scit.tool.base.MyLog
import com.sgpublic.scit.tool.manager.Security
import okhttp3.Call
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.concurrent.TimeUnit

class APIHelper(private val access: String, private val refresh: String) {
    companion object {
        private const val API_HOST: String = "https://tool.eclass.sgpublic.xyz"
        private const val PLATFORM: String = "android"

        const val METHOD_GET: Int = 0
        const val METHOD_POST: Int = 1

        fun getTS() = System.currentTimeMillis() / 1000
    }

    constructor() : this("", "")
    constructor(username: String) : this(username, "")

    fun getKeyRequest(): Call {
        val url = "getKey"
        return onReturn(url)
    }

    fun getLoginRequest(username: String, passwordEncrypted: String): Call {
        val url = "login"
        val argArray: Map<String, Any> = mapOf(
            "app_key" to Security.getAppKey(),
            "password" to passwordEncrypted,
            "platform" to PLATFORM,
            "ts" to getTS(),
            "username" to username
        )
        return onReturn(url, argArray)
    }

    fun getSpringboardRequest(): Call {
        val url = "springboard"
        val argArray: Map<String, Any> = mapOf(
            "access_token" to access,
            "app_key" to Security.getAppKey(),
            "platform" to PLATFORM,
            "ts" to getTS()
        )
        return onReturn(url, argArray)
    }

    fun getRefreshTokenRequest(): Call {
        val url = "token"
        val argArray: Map<String, Any> = mapOf(
            "access_token" to access,
            "app_key" to Security.getAppKey(),
            "platform" to PLATFORM,
            "refresh_token" to refresh,
            "ts" to getTS()
        )
        return onReturn(url, argArray)
    }

    fun getSentenceRequest(): Call {
        val url = "hitokoto"
        val argArray: Map<String, Any> = mapOf(
            "app_key" to Security.getAppKey(),
            "platform" to PLATFORM,
            "ts" to getTS()
        )
        return onReturn(url, argArray, METHOD_POST, true)
    }

    fun getDayRequest(): Call {
        val url = "day"
        val argArray: Map<String, Any> = mapOf(
            "app_key" to Security.getAppKey(),
            "platform" to PLATFORM,
            "ts" to getTS()
        )
        return onReturn(url, argArray)
    }

    fun getNewsTypeRequest(): Call {
        val url = "news"
        val argArray: Map<String, Any> = mapOf(
            "action" to "type",
            "app_key" to Security.getAppKey(),
            "platform" to PLATFORM,
            "ts" to getTS()
        )
        return onReturn(url, argArray)
    }

    fun getHeadlineRequest(): Call {
        val url = "news"
        val argArray: Map<String, Any> = mapOf(
            "action" to "headline",
            "app_key" to Security.getAppKey(),
            "platform" to PLATFORM,
            "ts" to getTS()
        )
        return onReturn(url, argArray)
    }

    fun getNewsListRequest(type: Int, page: Int): Call {
        val url = "news"
        val argArray: Map<String, Any> = mapOf(
            "action" to "list",
            "app_key" to Security.getAppKey(),
            "page" to page,
            "platform" to PLATFORM,
            "tid" to type,
            "ts" to getTS()
        )
        return onReturn(url, argArray)
    }

    fun getInfoRequest(): Call {
        val url = "info"
        val argArray: Map<String, Any> = mapOf(
            "access_token" to access,
            "app_key" to Security.getAppKey(),
            "platform" to PLATFORM,
            "ts" to getTS()
        )
        return onReturn(url, argArray)
    }

    fun getTableRequest(year: String, semester: Int): Call {
        val url = "table"
        val argArray: Map<String, Any> = mapOf(
            "access_token" to access,
            "app_key" to Security.getAppKey(),
            "platform" to PLATFORM,
            "semester" to semester,
            "ts" to getTS(),
            "year" to year
        )
        return onReturn(url, argArray)
    }

    fun getExamRequest(): Call {
        val url = "exam"
        val argArray: Map<String, Any> = mapOf(
            "access_token" to access,
            "app_key" to Security.getAppKey(),
            "platform" to PLATFORM,
            "ts" to getTS()
        )
        return onReturn(url, argArray)
    }

    fun getAchievementRequest(schoolYear: String, semester: Int): Call {
        val url = "achieve"
        val argArray: Map<String, Any> = mapOf(
            "access_token" to access,
            "app_key" to Security.getAppKey(),
            "platform" to PLATFORM,
            "semester" to semester,
            "ts" to getTS(),
            "year" to schoolYear
        )
        return onReturn(url, argArray)
    }

    fun getEvaluateRequest(action: String = "check", index: Int = -1, data: String? = null): Call {
        val url = "evaluate"
        val argArray: MutableMap<String, Any> = mutableMapOf(
            "access_token" to access,
            "app_key" to Security.getAppKey(),
            "action" to action,
            "platform" to PLATFORM
        )
        data?.run {
            argArray["data"] = this
        }
        if (index >= 0){
            argArray["index"] = index
        }
        argArray["ts"] = getTS()

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
            connectTimeout(10, TimeUnit.SECONDS)
            callTimeout(5, TimeUnit.MINUTES)
            followRedirects(false)
            followSslRedirects(false)
            build()
        }
        var urlFinal = if (url.startsWith("http")){ url } else {
            //debug & api/v1
            val apiVersion = "api"
            "$API_HOST/$apiVersion/$url"
        }
        val request: Request = Request.Builder().run {
            val body = GetArgs(argArray)
            if (method == METHOD_POST) {
                MyLog.v("HTTP请求：POST $urlFinal, [Body]" + body.getString(withSign))
                url(urlFinal)
                post(body.getForm(withSign))
            } else {
                urlFinal += "?" + body.getString(withSign)
                MyLog.v("HTTP请求：GET $urlFinal")
                url(urlFinal)
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
            val content = string + Security.getAppSecret()
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