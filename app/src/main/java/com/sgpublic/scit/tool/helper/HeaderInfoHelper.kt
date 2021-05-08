package com.sgpublic.scit.tool.helper

import android.content.Context
import com.sgpublic.scit.tool.R
import com.sgpublic.scit.tool.util.MyLog
import okhttp3.Call
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.*

class HeaderInfoHelper(private val context: Context, private val access: String) {
    constructor(context: Context): this(context, "")

    fun getSentence(callback: Callback){
        val call: Call = APIHelper(access).getSentenceRequest()
        call.enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                MyLog.w("网络请求出错", e)
                if (e is UnknownHostException){
                    callback.onFailure(-201, context.getString(R.string.error_network), e)
                } else {
                    callback.onFailure(-202, e.message, e)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.code == 200){
                    val sentence = response.body?.string().toString()
                    try {
                        val objects = JSONObject(sentence)
                        if (objects.getInt("code") != 200){
                            MyLog.e("服务器处理出错，" + objects.getString("message"))
                            callback.onFailure(-204, objects.getString("message"))
                            return
                        }
                        callback.onSentenceResult(
                            objects.getString("hitokoto"),
                            objects.getString("from")
                        )
                    } catch (e: JSONException) {
                        MyLog.e("hilikoto数据解析失败", e)
                        callback.onFailure(-203, e.message, e)
                    }
                } else {
                    MyLog.w("服务器内部出错")
                    callback.onFailure(-205, context.getString(R.string.error_server_error))
                }
            }
        })
    }

    fun getSemesterInfo(callback: Callback){
        val call: Call = APIHelper(access).getDayRequest()
        call.enqueue(object : okhttp3.Callback{
            override fun onFailure(call: Call, e: IOException) {
                if (e is UnknownHostException){
                    MyLog.e("网络请求出错", e)
                    callback.onFailure(-231, context.getString(R.string.error_network), e)
                } else {
                    callback.onFailure(-232, e.message, e)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val day = response.body?.string().toString()
                try {
                    val dayObject = JSONObject(day)
                    if (dayObject.getInt("code") != 200){
                        MyLog.e("服务器处理出错，" + dayObject.getString("message"))
                        callback.onFailure(-504, dayObject.getString("message"))
                        return
                    }
                    val dateString = dayObject.getString("date")
                    val date = SimpleDateFormat("yyyy/MM/dd", Locale.CHINESE).parse(dateString)
                    if (date == null){
                        MyLog.e("evaluate数据解析失败", Throwable("无法解析开学日期"))
                        callback.onFailure(-234, "开学时间解析失败")
                        return
                    }
                    var week = 0
                    var count = dayObject.getInt("day_count")
                    if (count >= 0){
                        count = if (count % 7 == 0) {
                            count / 7
                        } else {
                            count / 7 + 1
                        }
                        if (count > 18) {
                            count = 0
                        } else if (count == 18 && getDate() == Calendar.SUNDAY) {
                            count = 0
                        }
                        week = count
                    }
                    callback.onSemesterInfoResult(
                        dayObject.getInt("semester"),
                        dayObject.getString("school_year"),
                        week, date
                    )
                } catch (e: JSONException) {
                    MyLog.e("day数据解析失败", e)
                    callback.onFailure(-234, e.message, e)
                }
            }
        })
    }

    fun getDate(date: Calendar? = null): Int {
        var cal = Calendar.getInstance()
        if (date != null){
            cal = date
        }
        return when (cal[Calendar.DAY_OF_WEEK]) {
            Calendar.SUNDAY -> R.string.text_sunday
            Calendar.MONDAY -> R.string.text_monday
            Calendar.TUESDAY -> R.string.text_tuesday
            Calendar.WEDNESDAY -> R.string.text_wednesday
            Calendar.THURSDAY -> R.string.text_thursday
            Calendar.FRIDAY -> R.string.text_friday
            Calendar.SATURDAY -> R.string.text_saturday
            else -> 0
        }
    }

    fun getTime(): Int {
        val cal = Calendar.getInstance()
        val hour = cal[Calendar.HOUR_OF_DAY]
        return when {
            hour <= 8 -> R.string.text_hello_morning
            hour <= 11 -> R.string.text_hello_am
            hour <= 13 -> R.string.text_hello_lunch
            hour <= 18 -> R.string.text_hello_afternoon
            else -> R.string.text_hello_dinner
        }
    }

    interface Callback {
        fun onFailure(code: Int, message: String?, e: Exception? = null){}
        fun onSentenceResult(sentence: String, from: String){}
        fun onSemesterInfoResult(semester: Int, schoolYear: String, week: Int, startDate: Date){}
    }
}