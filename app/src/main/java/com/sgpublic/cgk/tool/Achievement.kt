package com.sgpublic.cgk.tool

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.sgpublic.cgk.tool.base.BaseActivity
import com.sgpublic.cgk.tool.data.FailedMarkData
import com.sgpublic.cgk.tool.data.PassedMarkData
import com.sgpublic.cgk.tool.helper.AchievementHelper
import com.sgpublic.cgk.tool.manager.CacheManager
import com.sgpublic.cgk.tool.manager.ConfigManager
import kotlinx.android.synthetic.main.activity_achievement.*
import kotlinx.android.synthetic.main.item_achievement_passed.view.*
import org.json.JSONObject
import java.util.*

class Achievement : BaseActivity(), AchievementHelper.Callback {
    var achievementFailedLoad: Boolean = false
    var achievementPassedLoad: Boolean = false

    override fun onActivityCreate(savedInstanceState: Bundle?) {
        val objects: JSONObject? = CacheManager(this@Achievement).read(CacheManager.CACHE_ACHIEVEMENT)
        getAchievement(objects)
        if (objects != null){
            getAchievement()
        }
    }

    private fun getAchievement(objects: JSONObject? = null){
        achievement_refresh.isRefreshing = true
        val helper = AchievementHelper(this@Achievement, ConfigManager(this@Achievement).getString("username"))
        if (objects != null) {
            helper.parsing(objects, this)
        } else {
            session?.let {
                helper.getMark(
                    achievement_year.selectedItem.toString(),
                    achievement_term.selectedItem.toString().toInt(),
                    it, this
                )
            }
        }
    }

    override fun onFailure(code: Int, message: String?, e: Exception?) {
        saveExplosion(e, code)
        onToast(this@Achievement, R.string.text_load_failed, message, code)
    }

    override fun onReadStart() {
        achievementFailedLoad = false
        achievementPassedLoad = false
        runOnUiThread{
            achievement_table_failed.removeAllViews()
            achievement_table_failed.visibility = View.INVISIBLE
            achievement_table_failed_empty.visibility = View.VISIBLE

            achievement_table_passed.removeAllViews()
            achievement_table_passed.visibility = View.INVISIBLE
            achievement_table_passed_empty.visibility = View.VISIBLE
        }
    }

    override fun onReadPassed(data: PassedMarkData) {
        if (!achievementPassedLoad){
            achievementPassedLoad = true
        }

        val itemAchievementPassed: View = LayoutInflater.from(this@Achievement)
            .inflate(R.layout.item_achievement_passed, achievement_table_passed, false)
        itemAchievementPassed.apply {
            achievement_passed_name.text = data.name
            achievement_passed_mark.text = data.mark
            achievement_passed_retake.text = data.retake
            achievement_passed_rebuild.text = data.rebuild
            achievement_passed_credit.text = data.credit
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        }
        runOnUiThread{
            achievement_table_passed.addView(itemAchievementPassed)
        }
    }

    override fun onReadFailed(data: FailedMarkData) {
        if (!achievementFailedLoad){
            achievementFailedLoad = true
        }

        val itemAchievementFailed: View = LayoutInflater.from(this@Achievement)
            .inflate(R.layout.item_achievement_passed, achievement_table_failed, false)
        itemAchievementFailed.apply {
            achievement_passed_name.text = data.name
            achievement_passed_mark.text = data.mark
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        }
        runOnUiThread{
            achievement_table_failed.addView(itemAchievementFailed)
        }
    }

    override fun onReadFinish() {
        runOnUiThread {
            achievement_refresh.isRefreshing = false
            if (achievementPassedLoad){
                achievement_table_passed.visibility = View.VISIBLE
                achievement_table_passed_empty.visibility = View.INVISIBLE
            }
            if (achievementFailedLoad){
                achievement_table_failed.visibility = View.VISIBLE
                achievement_table_failed_empty.visibility = View.INVISIBLE
            }
        }
    }

    override fun onViewSetup() {
        super.onViewSetup()

        val listYear: MutableList<String?> = ArrayList()
        val achievementStudent: Int = ConfigManager(this@Achievement).getInt("grade", 2019)
        val yearStudent: String = ConfigManager(this@Achievement).getString("school_year", "2019-2020")
        var yearSelected = 0
        for (year_index in 0 until 4) {
            val itemYearText =
                (achievementStudent + year_index).toString() + "-" + (achievementStudent + year_index + 1)
            listYear.add(itemYearText)
            if (yearStudent == itemYearText) {
                yearSelected = year_index
            }
        }
        val arrayAdapterYear: ArrayAdapter<String?> = ArrayAdapter(this@Achievement, R.layout.item_option, listYear)
        achievement_year.adapter = arrayAdapterYear
        achievement_year.setSelection(yearSelected, true)

        val termStudent: Int = ConfigManager(this@Achievement).getInt("semester", 1) - 1
        val listTerm: List<String?> = listOf("1", "2")
        val arrayAdapterTerm: ArrayAdapter<String?> = ArrayAdapter(this@Achievement, R.layout.item_option, listTerm)
        achievement_term.adapter = arrayAdapterTerm
        achievement_term.setSelection(termStudent, true)

        achievement_action.setOnClickListener { getAchievement() }
        achievement_refresh.setOnRefreshListener { getAchievement() }
        achievement_back.setOnClickListener { finish() }
    }

    override fun getContentView() = R.layout.activity_achievement

    override fun onSetSwipeBackEnable() = true
}