package io.github.sgpublic.aidescit.core.data

data class EvaluationQuestionData(
    val text: String,
    val options: ArrayList<String>,
    val selected: Int
)
