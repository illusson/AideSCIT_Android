package com.sgpublic.aidescit.data

data class EvaluationData (
    val subject: String,
    val teacher: String,
    val avatar: String?,
    val questions: ArrayList<EvaluationQuestionData>
)