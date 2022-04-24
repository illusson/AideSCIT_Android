package io.github.sgpublic.aidescit.viewmodule

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import io.github.sgpublic.aidescit.base.BaseViewModel
import io.github.sgpublic.aidescit.core.data.ExamData
import io.github.sgpublic.aidescit.core.manager.CacheManager
import io.github.sgpublic.aidescit.core.manager.ConfigManager
import io.github.sgpublic.aidescit.core.module.ExamModule

class ExamViewModule : BaseViewModel(), ExamModule.Callback {
    private val module = ExamModule(ConfigManager.ACCESS_TOKEN)
    val EXAM_DATA: MutableLiveData<List<ExamData>> = MutableLiveData()

    init {
        CacheManager.CACHE_EXAM?.let {
            module.parsing(it, this)
        }
    }

    fun getExam(context: AppCompatActivity) {
        module.getExam(this)
    }

    override fun onResult(data: List<ExamData>) {
        EXAM_DATA.postValue(data)
    }
}