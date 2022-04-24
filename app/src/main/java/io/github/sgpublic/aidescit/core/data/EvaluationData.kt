package io.github.sgpublic.aidescit.core.data

import io.github.sgpublic.aidescit.core.data.EvaluationQuestionData

data class EvaluationData (
    val subject: String,
    val teacher: String,
    val avatar: String?,
    val questions: ArrayList<EvaluationQuestionData>
)