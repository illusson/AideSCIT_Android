package com.sgpublic.aidescit.helper

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import com.sgpublic.aidescit.BuildConfig
import com.sgpublic.aidescit.R
import okhttp3.Call
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.math.BigDecimal
import java.net.HttpURLConnection
import java.net.URL
import java.net.UnknownHostException

class UpdateHelper(val context: Context) {
    companion object{
        private const val tag = "UpdateHelper"
    }
    
    fun getUpdate(callback: Callback) {
        val call: Call = APIHelper().getUpdateRequest("release")
        call.enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (e is UnknownHostException) {
                    callback.onUpdateFailure(-701, context.getString(R.string.error_network), e)
                } else {
                    callback.onUpdateFailure(-702, null, e)
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val result = response.body?.string().toString()
                try {
                    val verCodeNow = BuildConfig.VERSION_CODE
                    val objects = JSONObject(result)
                    val updateTable = objects.getJSONObject("latest")
                    val verCode = updateTable.getLong("ver_code")
                    val urlDl = updateTable.getString("dl_url")
                    if (verCode > verCodeNow) {
                        val isForce = if (updateTable.getLong("force") > verCodeNow) 1 else 0
                        val sizeString: String = getSizeString(urlDl)
                        callback.onUpdate(
                            isForce, updateTable.getString("ver_name"), sizeString,
                            updateTable.getString("changelog"), urlDl
                        )
                    } else {
                        callback.onUpToDate()
                    }
                } catch (e: JSONException) {
                    callback.onUpdateFailure(-703, e.message, e)
                }
            }
        })
    }

    fun handleDownload(dlUrl: String) {
        val url = Uri.parse(dlUrl)
        val dir = context.applicationContext.getExternalFilesDir("update")?.path
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val req = DownloadManager.Request(url)
        req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        val apkName: String = context.getString(R.string.app_name) + "_" + System.currentTimeMillis() + ".apk"
        req.setDestinationUri(Uri.fromFile(File(dir, apkName)))
        req.setTitle("工科助手更新")
        downloadManager.enqueue(req)
    }


    private fun getSizeLong(url_string: String?): Long {
        return try {
            val url = URL(url_string)
            val urlCon = url.openConnection() as HttpURLConnection
            urlCon.setRequestProperty("accept", "*/*")
            urlCon.setRequestProperty("connection", "Keep-Alive")
            urlCon.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            urlCon.contentLength.toLong()
        } catch (e: IOException) {
            0
        }
    }

    private fun getSizeString(url_string: String?): String {
        val size = getSizeLong(url_string)
        val sizeString: String
        val fileSize = BigDecimal(size)
        val megabyte = BigDecimal(1024 * 1024)
        var returnValue: Float = fileSize.divide(megabyte, 2, BigDecimal.ROUND_UP).toFloat()
        if (returnValue > 1) {
            sizeString = "$returnValue MB"
        } else {
            val kilobyte = BigDecimal(1024)
            returnValue = fileSize.divide(kilobyte, 2, BigDecimal.ROUND_UP).toFloat()
            sizeString = "$returnValue KB"
        }
        return sizeString
    }

    interface Callback {
        fun onUpdateFailure(code: Int, message: String?, e: Throwable?){}
        fun onUpToDate(){}
        fun onUpdate(force: Int, verName: String, sizeString: String, changelog: String, dlUrl: String)
    }
}