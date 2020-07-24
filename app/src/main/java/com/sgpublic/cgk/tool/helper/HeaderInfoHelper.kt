package com.sgpublic.cgk.tool.helper

import android.content.Context
import com.sgpublic.cgk.tool.R
import okhttp3.Call
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.*

class HeaderInfoHelper(private val context: Context, private val username: String) {
    companion object{
        private const val tag: String = "HeaderInfoHelper"
    }

    constructor(context: Context) : this(context, "")

    fun getSentence(callback: Callback){
        val call: Call = APIHelper(username).getSentenceRequest()
        call.enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (e is UnknownHostException) {
                    callback.onFailure(-201, context.getString(R.string.error_network), e)
                } else {
                    callback.onFailure(-202, e.message, e)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val result = response.body?.string().toString()
                try {
                    val objects = JSONObject(result)
                    callback.onSentenceResult(
                        objects.getString("string"),
                        objects.getString("from")
                    )
                } catch (e: JSONException) {
                    callback.onFailure(-202, e.message, e)
                }
            }
        })
    }

    fun getWeek(callback: Callback){
        val call: Call = APIHelper().getDayRequest()
        call.enqueue(object : okhttp3.Callback{
            override fun onFailure(call: Call, e: IOException) {
                if (e is UnknownHostException) {
                    callback.onFailure(-211, context.getString(R.string.error_network), e)
                } else {
                    callback.onFailure(-212, e.message, e)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val result = response.body?.string().toString()
                try {
                    val objects = JSONObject(result)
                    when (objects.getString("direct")) {
                        "+" -> {
                            var dayCount: Int = objects.getInt("day_count")
                            dayCount = if (dayCount % 7 == 0) {
                                dayCount / 7
                            } else {
                                dayCount / 7 + 1
                            }
                            if (dayCount > 18) {
                                dayCount = 0
                            } else if (dayCount == 18 && getDate() == Calendar.SUNDAY) {
                                dayCount = 0
                            }
                            callback.onWeekResult(dayCount)
                        }
                        "-" -> callback.onWeekResult(0)
                    }
                } catch (e: JSONException) {
                    callback.onFailure(-212, e.message, e)
                }
            }
        })
    }

    fun getStartDate(callback: Callback){
        val call: Call = APIHelper().getDayRequest()
        call.enqueue(object : okhttp3.Callback{
            override fun onFailure(call: Call, e: IOException) {
                if (e is UnknownHostException) {
                    callback.onFailure(-221, context.getString(R.string.error_network), e)
                } else {
                    callback.onFailure(-222, e.message, e)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val result = response.body?.string().toString()
                try {
                    val objects = JSONObject(result)
                    val startDate: String = objects.getString("date")
                    val simpleDateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.CHINESE)
                    callback.onStartDateResult(simpleDateFormat.parse(startDate)!!)
                } catch (e: JSONException) {
                    callback.onFailure(-224, e.message, e)
                }
            }
        })
    }

    fun getDate(): Int {
        val cal = Calendar.getInstance()
        val i = cal[Calendar.DAY_OF_WEEK]
        return when (i) {
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
        fun onWeekResult(week: Int){}
        fun onStartDateResult(startDate: Date){}
    }
}