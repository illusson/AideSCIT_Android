package com.sgpublic.aidescit.helper

import com.sgpublic.aidescit.BuildConfig
import com.sgpublic.aidescit.manager.Security
import com.sgpublic.aidescit.util.MyLog
import okhttp3.Call
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.concurrent.TimeUnit

class APIHelper(private val access: String, private val refresh: String) {
    companion object {
        private val API_HOST: String get() = if (BuildConfig.DEBUG){
//            "https://api.sgpublic.xyz/aidescit"
            "http://192.168.2.155:8001/aidescit"
        } else {
            "https://api.sgpublic.xyz/aidescit"
        }
        private const val PLATFORM: String = "android"

        const val METHOD_GET: Int = 0
        const val METHOD_POST: Int = 1

        fun getTS() = System.currentTimeMillis() / 1000
    }

    constructor() : this("", "")
    constructor(username: String) : this(username, "")

    fun getKeyRequest(): Call {
        val url = "public_key"
        return onReturn(url)
    }

    fun getLoginRequest(username: String, passwordEncrypted: String): Call {
        val url = "login"
        val argArray: Map<String, Any> = mapOf(
            "password" to passwordEncrypted,
            "username" to username
        )
        return onReturn(url, argArray)
    }

    fun getSpringboardRequest(): Call {
        val url = "login/springboard"
        val argArray: Map<String, Any> = mapOf(
            "access_token" to access
        )
        return onReturn(url, argArray)
    }

    fun getRefreshTokenRequest(): Call {
        val url = "login/token"
        val argArray: Map<String, Any> = mapOf(
            "access_token" to access,
            "refresh_token" to refresh
        )
        return onReturn(url, argArray)
    }

    fun getSentenceRequest(): Call {
        val url = "hitokoto"
        return onReturn(url)
    }

    fun getDayRequest(): Call {
        val url = "day"
        return onReturn(url)
    }

    fun getNewsTypeRequest(): Call {
        val url = "news/type"
        return onReturn(url)
    }

    fun getHeadlineRequest(): Call {
        val url = "news/headlines"
        return onReturn(url)
    }

    fun getNewsListRequest(type: Int, page: Int): Call {
        val url = "news"
        val argArray: Map<String, Any> = mapOf(
            "page" to page,
            "tid" to type
        )
        return onReturn(url, argArray)
    }

    fun getInfoRequest(): Call {
        val url = "info"
        val argArray: Map<String, Any> = mapOf(
            "access_token" to access
        )
        return onReturn(url, argArray)
    }

    fun getTableRequest(year: String, semester: Int): Call {
        val url = "schedule"
        val argArray: Map<String, Any> = mapOf(
            "access_token" to access,
            "semester" to semester,
            "year" to year
        )
        return onReturn(url, argArray)
    }

    fun getExamRequest(): Call {
        val url = "exam"
        val argArray: Map<String, Any> = mapOf(
            "access_token" to access
        )
        return onReturn(url, argArray)
    }

    fun getAchievementRequest(schoolYear: String, semester: Int): Call {
        val url = "achieve"
        val argArray: Map<String, Any> = mapOf(
            "access_token" to access,
            "semester" to semester,
            "year" to schoolYear
        )
        return onReturn(url, argArray)
    }

    fun getEvaluateRequest(action: String = "check", index: Int = -1, data: String? = null): Call {
        val url = "evaluate"
        val argArray: MutableMap<String, Any> = mutableMapOf(
            "access_token" to access,
            "action" to action
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
        val url = "https://aidescit.sgpublic.xyz/app/update/index.php"
        val argArray: Map<String, Any> = mapOf("version" to version)
        return onReturn(url, argArray, METHOD_GET, false)
    }

    private fun onReturn(url: String, argArray: Map<String, Any>? = null, method: Int = METHOD_POST, withSign: Boolean = true): Call {
        val client: OkHttpClient = OkHttpClient.Builder().run{
            readTimeout(10, TimeUnit.SECONDS)
            writeTimeout(10, TimeUnit.SECONDS)
            connectTimeout(10, TimeUnit.SECONDS)
            callTimeout(10, TimeUnit.MINUTES)
            followRedirects(false)
            followSslRedirects(false)
            build()
        }
        var urlFinal = if (url.startsWith("http")){ url } else {
            "$API_HOST/$url"
        }
        val request: Request = Request.Builder().run {
            val body = ArgsGetter(mutableMapOf<String, Any>(
                "app_key" to Security.getAppKey(),
                "platform" to PLATFORM,
                "ts" to getTS()
            ).also {
                argArray?.let { args ->
                    it.putAll(args)
                }
            })
            if (method == METHOD_POST) {
                MyLog.v("HTTP请求\nPOST $urlFinal\n[Body]" + body.getString(withSign))
                url(urlFinal)
                post(body.getForm(withSign))
            } else {
                urlFinal += "?" + body.getString(withSign)
                MyLog.v("HTTP请求\nGET $urlFinal")
                url(urlFinal)
            }
            build()
        }
        return client.newCall(request)
    }

    private class ArgsGetter(val argArray: Map<String, Any>?){
        private val instance: MessageDigest get() = MessageDigest.getInstance("MD5")
        private var string: String

        init {
            string = StringBuilder().run {
                argArray?.toSortedMap()?.let{
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
                argArray?.toSortedMap()?.let {
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
                val digest = instance.digest(content.toByteArray())
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