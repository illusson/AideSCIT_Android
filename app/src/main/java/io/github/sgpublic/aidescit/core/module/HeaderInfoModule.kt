package io.github.sgpublic.aidescit.core.module

import io.github.sgpublic.aidescit.R
import io.github.sgpublic.aidescit.core.util.MyLog
import okhttp3.Call
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class HeaderInfoModule(private val access: String = "") {
    fun getSentence(callback: Callback){
        val call: Call = BaseAPI(access).getSentenceRequest()
        call.enqueue(object : BaseAPI.BaseOkHttpCallback(callback) {
            override fun onResult(data: JSONObject) {
                callback.onSentenceResult(
                    data.getString("hitokoto"),
                    data.getString("from")
                )
            }
        })
    }

    fun getSemesterInfo(callback: Callback){
        val call: Call = BaseAPI(access).getDayRequest()
        call.enqueue(object : BaseAPI.BaseOkHttpCallback(callback) {
            override fun onResult(data: JSONObject) {
                val dateString = data.getString("date")
                val date = SimpleDateFormat("yyyy/MM/dd", Locale.CHINESE).parse(dateString)
                if (date == null){
                    MyLog.e("evaluate数据解析失败", Throwable("无法解析开学日期"))
                    callback.postFailure(-234, "开学时间解析失败")
                    return
                }
                var week = 0
                var count = data.getInt("day_count")
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
                    data.getInt("semester"),
                    data.getString("school_year"),
                    week, date, data.getBoolean("schedule_can_inquire")
                )
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

    interface Callback: BaseAPI.Callback {
        fun onSentenceResult(sentence: String, from: String){}
        fun onSemesterInfoResult(semester: Int, schoolYear: String, week: Int, startDate: Date, scheduleCanInquire: Boolean){}
    }
}