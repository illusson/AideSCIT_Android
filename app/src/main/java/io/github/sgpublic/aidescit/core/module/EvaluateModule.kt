package io.github.sgpublic.aidescit.core.module

import io.github.sgpublic.aidescit.core.data.EvaluationData
import io.github.sgpublic.aidescit.core.data.EvaluationQuestionData
import io.github.sgpublic.aidescit.core.util.MyLog
import org.json.JSONException
import org.json.JSONObject

class EvaluateModule(private val access_token: String) {
    fun check(callback: CheckCallback){
        val call = BaseAPI(access_token).getEvaluateRequest()
        call.enqueue(object : BaseAPI.BaseOkHttpCallback(callback) {
            override fun onResult(data: JSONObject) {
                callback.onResult(data.getInt("count"))
            }
        })
    }

    fun get(index: Int, callback: GetCallback){
        val call = BaseAPI(access_token).getEvaluateRequest("get", index)
        call.enqueue(object : BaseAPI.BaseOkHttpCallback(callback) {
            override fun onResult(data: JSONObject) {
                parsing(data.getJSONObject("evaluate"), callback)
            }
        })
    }

    private fun parsing(data: JSONObject, callback: GetCallback){
        try {
            val evaluationData: ArrayList<EvaluationData> = ArrayList()

            val teachers = data.getJSONArray("evaluations")
            for (t_index in 0 until teachers.length()){
                val teacherData = teachers.getJSONObject(t_index)
                val questions: ArrayList<EvaluationQuestionData> = ArrayList()
                val objects = data.getJSONArray("questions")
                for (q_index in 0 until objects.length()){
                    val questionIndex = objects.getJSONObject(q_index)
                    val options: ArrayList<String> = ArrayList()
                    val optionsData = questionIndex.getJSONArray("options")
                    for (o_index in 0 until optionsData.length()){
                        options.add(optionsData.getString(o_index))
                    }

                    questions.add(
                        EvaluationQuestionData(
                            questionIndex.getString("text"), options,
                            teacherData.getJSONArray("options").getInt(q_index)
                        )
                    )
                }
                val avatar: String? = if (!teacherData.isNull("avatar")){
                    teacherData.getString("avatar")
                } else { null }
                evaluationData.add(
                    EvaluationData(
                        data.getString("subject"),
                        teacherData.getString("teacher"),
                        avatar, questions
                    )
                )
            }

            callback.onResult(evaluationData)
        } catch (e: JSONException){
            MyLog.e("evaluate数据解析失败", e)
            callback.onFailure(-714, e.message, e)
        }
    }

    fun post(index: Int, data: JSONObject, callback: PostCallback){
        val call = BaseAPI(access_token).getEvaluateRequest(
            "submit", index, data.toString()
        )
        call.enqueue(object : BaseAPI.BaseOkHttpCallback(callback) {
            override fun onResult(data: JSONObject) {
                callback.onResult()
            }
        })
    }

    interface CheckCallback: BaseAPI.Callback {
        fun onResult(count: Int) {}
    }

    interface GetCallback: BaseAPI.Callback {
        fun onResult(data: ArrayList<EvaluationData>) {}
    }

    interface PostCallback: BaseAPI.Callback {
        fun onResult() {}
    }
}