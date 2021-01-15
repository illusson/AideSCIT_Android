package com.sgpublic.cgk.tool.ui

import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.sgpublic.cgk.tool.R
import com.sgpublic.cgk.tool.base.BaseActivity
import com.sgpublic.cgk.tool.data.FailedMarkData
import com.sgpublic.cgk.tool.data.PassedMarkData
import com.sgpublic.cgk.tool.helper.AchievementHelper
import com.sgpublic.cgk.tool.manager.CacheManager
import com.sgpublic.cgk.tool.manager.ConfigManager
import kotlinx.android.synthetic.main.activity_achievement.*
import kotlinx.android.synthetic.main.item_achievement_passed.view.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class Achievement : BaseActivity(), AchievementHelper.Callback {
    var hasLoaded: Int = 0

    var achievementFailedLoad: Boolean = false
    var achievementPassedLoad: Boolean = false

    override fun onActivityCreate(savedInstanceState: Bundle?) {
        val objects: JSONObject? = CacheManager(this@Achievement).read(CacheManager.CACHE_ACHIEVEMENT)
        getAchievement(objects)
        if (savedInstanceState != null){
            hasLoaded = savedInstanceState.getInt("hasLoaded")
            if (savedInstanceState.getInt("hasLoaded") > 1){
                val helper = AchievementHelper(this@Achievement)
                if (objects != null) {
                    try {
                        helper.parsing(objects.getJSONObject("achievement"), this)
                    } catch (e: JSONException){
                        onReadFinish()
                    }
                } else {
                    helper.getMark(ConfigManager(this@Achievement), this)
                }
            }
        } else if (objects != null){
            getAchievement()
        }
    }

    private fun getAchievement(objects: JSONObject? = null){
        val year = achievement_year.selectedItem.toString()
        val semester = achievement_term.selectedItem.toString().toInt()
        val config = ConfigManager(this@Achievement);
        config.putString("school_year_inquiry", year)
            .putInt("semester_inquiry", semester)
            .apply()
        achievement_refresh.isRefreshing = true
        val helper = AchievementHelper(this@Achievement, config.getString("username"))
        if (objects != null) {
            try {
                helper.parsing(objects.getJSONObject("achievement"), this)
            } catch (e: JSONException){
                onReadFinish()
            }
        } else {
            helper.getMark(ConfigManager(this@Achievement), this)
        }
    }

    override fun onFailure(code: Int, message: String?, e: Exception?) {
        saveExplosion(e, code)
        onToast(this@Achievement, R.string.text_load_failed, message, code)
        runOnUiThread {
            achievement_refresh.isRefreshing = false
        }
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
            if(achievement_passed_paper != null) {
                achievement_passed_paper.text = data.paper
            }
            achievement_passed_mark.text = data.mark
            achievement_passed_retake.text = data.retake
            achievement_passed_rebuild.text = data.rebuild
            achievement_passed_credit.text = data.credit
            if (!judgePass(data.mark) && !judgePass(data.retake) && !judgePass(data.rebuild)){
                achievement_passed_name.setTextColor(Color.RED)
                if(achievement_passed_paper != null) {
                    achievement_passed_paper.setTextColor(Color.RED)
                }
                achievement_passed_mark.setTextColor(Color.RED)
                achievement_passed_retake.setTextColor(Color.RED)
                achievement_passed_rebuild.setTextColor(Color.RED)
                achievement_passed_credit.setTextColor(Color.RED)
            }
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        }
        runOnUiThread{
            achievement_table_passed.addView(itemAchievementPassed)
        }
    }

    private fun judgePass(string: String?): Boolean {
        return if (string == null || string == ""){
            false
        } else string.toInt() >= 60
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
        val config = ConfigManager(this@Achievement)
        val gradeStudent: Int = config.getInt("grade", 2019)
        val yearStudent: String = config.getString("school_year_inquiry", config.getString("school_year", "2019-2020"))
        var yearSelected = 0
        for (year_index in 0 until 4) {
            val itemYearText =
                (gradeStudent + year_index).toString() + "-" + (gradeStudent + year_index + 1)
            listYear.add(itemYearText)
            if (yearStudent == itemYearText) {
                yearSelected = year_index
            }
        }
        val arrayAdapterYear: ArrayAdapter<String?> = ArrayAdapter(this@Achievement,
            R.layout.item_option, listYear)
        achievement_year.adapter = arrayAdapterYear
        achievement_year.setSelection(yearSelected, true)

        val semesterStudent: Int = config.getInt("semester_inquiry", config.getInt("semester", 1)) - 1
        val listTerm: List<String?> = listOf("1", "2")
        val arrayAdapterTerm: ArrayAdapter<String?> = ArrayAdapter(this@Achievement,
            R.layout.item_option, listTerm)
        achievement_term.adapter = arrayAdapterTerm
        achievement_term.setSelection(semesterStudent, true)

        achievement_action.setOnClickListener { getAchievement() }
        achievement_refresh.setOnRefreshListener { getAchievement() }
        achievement_back.setOnClickListener { finish() }
        achievement_landscape.setOnClickListener {
            requestedOrientation = if(requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            } else {
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("hasLoaded", hasLoaded)
        super.onSaveInstanceState(outState)
    }

    override fun getContentView() = R.layout.activity_achievement

    override fun onSetSwipeBackEnable() = true
}