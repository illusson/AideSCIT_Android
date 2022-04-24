package io.github.sgpublic.aidescit.core.util

import android.util.TypedValue
import io.github.sgpublic.aidescit.Application

val Int.dp: Int get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(),
    Application.APPLICATION_CONTEXT.resources.displayMetrics).toInt()
val Float.dp: Int get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this,
    Application.APPLICATION_CONTEXT.resources.displayMetrics).toInt()

val Int.sp: Int get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this.toFloat(),
    Application.APPLICATION_CONTEXT.resources.displayMetrics).toInt()
val Float.sp: Int get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this,
    Application.APPLICATION_CONTEXT.resources.displayMetrics).toInt()