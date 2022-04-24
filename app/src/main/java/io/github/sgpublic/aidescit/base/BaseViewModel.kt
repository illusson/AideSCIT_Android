package io.github.sgpublic.aidescit.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

abstract class BaseViewModel: ViewModel() {
    val EXCEPTION: MutableLiveData<ExceptionData> = MutableLiveData()
    fun getExceptionData() = EXCEPTION.value
    data class ExceptionData(var code: Int, var message: String?)

    open val LOADING: MutableLiveData<Boolean> = MutableLiveData()
}

fun MutableLiveData<BaseViewModel.ExceptionData>.postValue(
    code: Int, message: String?
) {
    postValue(BaseViewModel.ExceptionData(code, message))
}