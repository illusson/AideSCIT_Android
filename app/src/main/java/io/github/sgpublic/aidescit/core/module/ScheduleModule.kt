package io.github.sgpublic.aidescit.core.module

import io.github.sgpublic.aidescit.core.data.ScheduleData
import io.github.sgpublic.aidescit.core.manager.CacheManager
import io.github.sgpublic.aidescit.core.manager.ConfigManager
import io.github.sgpublic.aidescit.core.util.MyLog
import org.json.JSONException
import org.json.JSONObject

class ScheduleModule {
    companion object {
        val DAY_INDEX = arrayOf(
            "monday", "tuesday", "wednesday", "thursday",
            "friday", "saturday", "sunday"
        )

        val CLASS_INDEX = arrayOf(
            "am1", "am2", "pm1", "pm2", "ev"
        )
    }

    fun getSchedule(callback: Callback){
        getSchedule(ConfigManager.WEEK, callback)
    }

    fun getSchedule(week: Int, callback: Callback?){
        BaseAPI(ConfigManager.ACCESS_TOKEN).getTableRequest(
            ConfigManager.SCHOOL_YEAR, ConfigManager.SEMESTER
        ).enqueue(object : BaseAPI.BaseOkHttpCallback(callback) {
            override fun onResult(data: JSONObject) {
                CacheManager.CACHE_SCHEDULE = data
                parsing(data, week, callback)
            }
        })
    }

    @Throws(JSONException::class)
    fun parsing(objects: JSONObject, week: Int, callback: Callback?){
        callback?.let {
            try {
                var isEmpty = 0
                val tableObject = objects.getJSONObject("schedule")
                callback.onReadStart(tableObject.isNull(DAY_INDEX[6]))
                DAY_INDEX.forEach forDay@{ day ->
                    if (tableObject.isNull(day)){
                        for (clazz in CLASS_INDEX){
                            callback.onRead(day, clazz, null)
                        }
                        return@forDay
                    }
                    val dayObject = tableObject.getJSONObject(day)
                    CLASS_INDEX.forEach forClass@{ clazz ->
                        if (dayObject.isNull(clazz)){
                            callback.onRead(day, clazz, null)
                            return@forClass
                        }
                        val indexArray = dayObject.getJSONArray(clazz)
                        for (index in 0 until indexArray.length()) {
                            val indexObject = indexArray.getJSONObject(index)
                            val rangeObject = indexObject.getJSONArray("range")
                            var rangeJudge = false
                            for (indexRange in 0 until rangeObject.length()) {
                                if (rangeObject.getInt(indexRange) == week){
                                    rangeJudge = true
                                    break
                                }
                            }
                            if (rangeJudge){
                                callback.onRead(day, clazz,
                                    ScheduleData(
                                        indexObject.getString("name"),
                                        indexObject.getString("teacher"),
                                        indexObject.getString("room")
                                    )
                                )
                                isEmpty++
                            } else {
                                callback.onRead(day, clazz, null)
                            }
                        }
                    }
                }
                callback.onReadFinish(isEmpty == 0)
            } catch (e: JSONException) {
                MyLog.e("schedule数据解析失败", e)
                callback.onFailure(-404, e.message, e)
            }
        }
    }

    interface Callback: BaseAPI.Callback {
        fun onReadStart(isSundayEmpty: Boolean = false){}
        fun onRead(day: String, clazz: String, data: ScheduleData?){}
        fun onReadFinish(isEmpty: Boolean){}
    }
}
