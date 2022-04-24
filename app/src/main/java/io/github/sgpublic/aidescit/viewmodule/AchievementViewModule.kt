package io.github.sgpublic.aidescit.viewmodule

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import io.github.sgpublic.aidescit.Application
import io.github.sgpublic.aidescit.R
import io.github.sgpublic.aidescit.base.BaseViewModel
import io.github.sgpublic.aidescit.base.postValue
import io.github.sgpublic.aidescit.core.data.FailedMarkData
import io.github.sgpublic.aidescit.core.data.PassedMarkData
import io.github.sgpublic.aidescit.core.manager.CacheManager
import io.github.sgpublic.aidescit.core.manager.ConfigManager
import io.github.sgpublic.aidescit.core.module.AchievementModule
import java.util.*

class AchievementViewModule: BaseViewModel(), AchievementModule.Callback {
    private val module = AchievementModule(ConfigManager.ACCESS_TOKEN)
    fun init() {
        CacheManager.CACHE_ACHIEVEMENT?.let {
            module.parsing(it, this)
        }
    }

    val SCHOOL_YEAR = LinkedList<String>().also {
        it.add(Application.getString(R.string.text_achievement_all))
    }
    val YEAR_SELECTED: MutableLiveData<Int> = MutableLiveData<Int>(0.let {
        val yearStudent: String = ConfigManager.SCHOOL_YEAR_INQUIRY
        var result = 0
        for (index in 0 until 4) {
            val yearStart = ConfigManager.USER_GRADE + index
            val itemYearText = "$yearStart-${yearStart + 1}"
            SCHOOL_YEAR.add(itemYearText)
            if (yearStudent == itemYearText) {
                result = index + 1
            }
        }
        return@let result
    })
    fun getYearSelectedId(): Int = YEAR_SELECTED.value ?: 0
    fun getYearSelectedName(id: Int = YEAR_SELECTED.value ?: 0): String {
        return if (id == 0) "all" else SCHOOL_YEAR[id]
    }

    val SEMESTER = listOf(Application.getString(R.string.text_achievement_year), "1", "2")
    val SEMESTER_SELECTED: MutableLiveData<Int> =
        MutableLiveData(ConfigManager.SEMESTER_INQUIRY)
    fun getSemesterSelectedId(): Int = SEMESTER_SELECTED.value ?: 0

    val ACHIEVE: MutableLiveData<Achieve> = MutableLiveData()
    fun getAchieve(context: AppCompatActivity, entry: Boolean = false) {
        if (entry && ACHIEVE.value != null) return
        ConfigManager.SCHOOL_YEAR_INQUIRY = getYearSelectedName()
        ConfigManager.SEMESTER_INQUIRY = getSemesterSelectedId()
        LOADING.postValue(true)
        module.getMark(
            getYearSelectedName(), getSemesterSelectedId(),
            this
        )
    }

    override fun onResult(passed: List<PassedMarkData>, failed: List<FailedMarkData>) {
        ACHIEVE.postValue(Achieve(passed, failed))
        LOADING.postValue(false)
    }

    override fun onFailure(code: Int, message: String?, e: Throwable?) {
        EXCEPTION.postValue(code, message)
    }

    data class Achieve(
        var PASSED_MARK: List<PassedMarkData> = listOf(),
        var FAILED_MARK: List<FailedMarkData> = listOf()
    )
}