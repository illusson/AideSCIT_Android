package com.sgpublic.cgk.tool.helper

import android.annotation.SuppressLint
import android.content.Context
import com.sgpublic.cgk.tool.R
import com.sgpublic.cgk.tool.manager.ConfigManager
import okhttp3.Call
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.*

class HeaderInfoHelper(private val context: Context) {
    companion object{
        private const val tag: String = "HeaderInfoHelper"
        private var sentence: String? = null
        private var day: String? = null
        private var e_sentence: IOException? = null
        private var e_day: IOException? = null
    }

    fun setup(access: String, callback: Callback?){
        if (sentence == null && e_sentence == null){
            val call: Call = APIHelper(access).getSentenceRequest()
            call.enqueue(object : okhttp3.Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e_sentence = e
                }

                override fun onResponse(call: Call, response: Response) {
                    sentence = response.body?.string().toString()
                }
            })
        }

        if (day == null && e_day == null){
            val call: Call = APIHelper(access).getDayRequest()
            call.enqueue(object : okhttp3.Callback{
                override fun onFailure(call: Call, e: IOException) {
                    e_day = e
                }

                override fun onResponse(call: Call, response: Response) {
                    day = response.body?.string().toString()
                }
            })
        }

        callback?.let {
            var time: Int = 0
            Thread {
                while ((sentence == null && e_sentence == null)
                    || (day == null && e_day == null)){
                    Thread.sleep(100)
                    time ++
                    if (time >= 100){
                        time = -1
                        break
                    }
                }
                if (time == -1){
                    it.onSetupTimeout()
                } else {
                    it.onSetupFinish()
                }
            }.start()
        }
    }

    fun getSentence(callback: Callback){
        if (e_sentence != null){
            if (e_sentence is UnknownHostException){
                callback.onFailure(-201, context.getString(R.string.error_network), e_sentence)
            } else {
                callback.onFailure(-202, e_sentence!!.message, e_sentence)
            }
        } else if (sentence != null) {
            try {
                val objects = JSONObject(sentence!!)
                callback.onSentenceResult(
                    objects.getString("string"),
                    objects.getString("from")
                )
            } catch (e: JSONException) {
                callback.onFailure(-204, e.message, e)
            }
        } else {
            callback.onFailure(-203, null, e_sentence)
        }
    }

    fun getWeek(callback: Callback){
        if (e_day != null){
            if (e_day is UnknownHostException){
                callback.onFailure(-211, context.getString(R.string.error_network), e_day)
            } else {
                callback.onFailure(-212, e_day!!.message, e_day)
            }
        } else if (day != null) {
            try {
                val objects = JSONObject(day!!)
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
                callback.onFailure(-214, e.message, e)
            }
        } else {
            callback.onFailure(-213, null, e_day)
        }
    }

    fun getStartDate(callback: Callback){
        if (e_day != null){
            if (e_day is UnknownHostException){
                callback.onFailure(-211, context.getString(R.string.error_network), e_day)
            } else {
                callback.onFailure(-212, e_day!!.message, e_day)
            }
        } else if (day != null) {
            try {
                val dateString = JSONObject(day!!).getString("date")
                val date = SimpleDateFormat("yyyy/MM/dd", Locale.CHINESE).parse(dateString)
                if (date != null){
                    callback.onStartDateResult(date)
                } else {
                    callback.onFailure(-215, null, null)
                }
            } catch (e: JSONException) {
                callback.onFailure(-214, e.message, e)
            }
        } else {
            callback.onFailure(-223, null, e_day)
        }
    }

    fun getSemesterInfo(callback: Callback){
        if (e_day != null){
            if (e_day is UnknownHostException){
                callback.onFailure(-231, context.getString(R.string.error_network), e_day)
            } else {
                callback.onFailure(-232, e_day!!.message, e_day)
            }
        } else if (day != null) {
            try {
                val objects = JSONObject(day!!)
                callback.onSemesterResult(
                    objects.getInt("semester"),
                    objects.getString("school_year")
                )
            } catch (e: JSONException) {
                callback.onFailure(-234, e.message, e)
            }
        } else {
            callback.onFailure(-233, null, e_day)
        }
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
        fun onSetupFinish(){}
        fun onSetupTimeout(){}
        fun onFailure(code: Int, message: String?, e: Exception? = null){}
        fun onSentenceResult(sentence: String, from: String){}
        fun onWeekResult(week: Int){}
        fun onSemesterResult(semester: Int, schoolYear: String){}
        fun onStartDateResult(startDate: Date){}
    }
}