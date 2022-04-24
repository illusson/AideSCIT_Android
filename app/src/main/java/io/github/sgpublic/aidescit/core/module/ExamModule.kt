package io.github.sgpublic.aidescit.core.module

import io.github.sgpublic.aidescit.core.data.ExamData
import io.github.sgpublic.aidescit.core.manager.CacheManager
import io.github.sgpublic.aidescit.core.util.MyLog
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class ExamModule(private val access: String) {
    fun getExam(callback: Callback){
        BaseAPI(access).getExamRequest().enqueue(object
            : BaseAPI.BaseOkHttpCallback(callback) {
            override fun onResult(data: JSONObject) {
                CacheManager.CACHE_EXAM = data
                parsing(data, callback)
            }
        })
    }

    fun parsing(objects: JSONObject, callback: Callback) {
        try {
            val examArray: JSONArray = objects.getJSONArray("exam")
            val data = LinkedList<ExamData>()
            if (examArray.length() > 0){
                for (index in 0 until examArray.length()){
                    val examData: JSONObject = examArray.getJSONObject(index)
                    data.add(ExamData(
                        examData.getString("name"),
                        examData.getString("time"),
                        examData.getString("location"),
                        examData.getString("set_num")
                    ))
                }
            }
            callback.onResult(data)
        } catch (e: JSONException){
            MyLog.e("exam数据解析失败", e)
            callback.onFailure(-604, e.message, e)
        }
    }

    interface Callback: BaseAPI.Callback {
        fun onResult(data: List<ExamData>) {}
    }
}