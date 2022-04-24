package io.github.sgpublic.aidescit.core.module

import io.github.sgpublic.aidescit.core.data.FailedMarkData
import io.github.sgpublic.aidescit.core.data.PassedMarkData
import io.github.sgpublic.aidescit.core.manager.CacheManager
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class AchievementModule(private val access: String) {
    fun getMark(year: String, semester: Int, callback: Callback){
        BaseAPI(access).getAchievementRequest(year, semester).enqueue(object
            : BaseAPI.BaseOkHttpCallback() {
            override fun onResult(data: JSONObject) {
                Thread.sleep(500)
                CacheManager.CACHE_ACHIEVEMENT = data
                parsing(data.getJSONObject("achieve"), callback)
            }
        })
    }

    @Throws(JSONException::class)
    fun parsing(objects: JSONObject, callback: Callback) {
        val passed: LinkedList<PassedMarkData> = LinkedList()
        val failed: LinkedList<FailedMarkData> = LinkedList()
        if (!objects.isNull("current")){
            objects.getJSONArray("current").let { current ->
                if (current.length() <= 0){
                    return@let
                }
                for (passedIndex in 0 until current.length()){
                    val pass = current.getJSONObject(passedIndex)
                    val item = PassedMarkData()
                    item.name = pass.getString("name")
                    if (!pass.isNull("paper_score")){
                        item.paper = pass.getDouble("paper_score").toString()
                    }
                    item.mark = pass.getString("mark")
                    if (!pass.isNull("retake")){
                        item.retake = pass.getDouble("retake").toString()
                    }
                    if (!pass.isNull("rebuild")){
                        item.rebuild = pass.getDouble("rebuild").toString()
                    }
                    item.credit = pass.getString("credit")
                    passed.add(item)
                }
            }
        }

        if (!objects.isNull("failed")){
            objects.getJSONArray("failed").let {
                if (it.length() <= 0){
                    return@let
                }
                for (passedIndex in 0 until it.length()){
                    val passedData: JSONObject = it.getJSONObject(passedIndex)
                    failed.add(FailedMarkData(
                        passedData.getString("name"),
                        passedData.getString("mark")
                    ))
                }
            }
        }

        callback.onResult(passed, failed)
    }

    interface Callback: BaseAPI.Callback {
        fun onResult(passed: List<PassedMarkData>, failed: List<FailedMarkData>)
    }
}