package com.sgpublic.scit.tool.activity

import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.sgpublic.scit.tool.R
import com.sgpublic.scit.tool.base.BaseActivity
import com.sgpublic.scit.tool.data.FailedMarkData
import com.sgpublic.scit.tool.data.PassedMarkData
import com.sgpublic.scit.tool.databinding.ActivityAchievementBinding
import com.sgpublic.scit.tool.databinding.ItemAchievementFailedBinding
import com.sgpublic.scit.tool.databinding.ItemAchievementPassedBinding
import com.sgpublic.scit.tool.helper.AchievementHelper
import com.sgpublic.scit.tool.manager.CacheManager
import com.sgpublic.scit.tool.manager.ConfigManager
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class Achievement : BaseActivity<ActivityAchievementBinding>(), AchievementHelper.Callback {
    var hasLoaded: Int = 0

    var achievementFailedLoad: Boolean = false
    var achievementPassedLoad: Boolean = false

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        val objects: JSONObject? = CacheManager(this@Achievement).read(CacheManager.CACHE_ACHIEVEMENT)
        getAchievement(objects)
        if (savedInstanceState != null){
            hasLoaded = savedInstanceState.getInt("hasLoaded")
            if (savedInstanceState.getInt("hasLoaded") > 1){
                val helper = AchievementHelper(this@Achievement)
                if (objects != null) {
                    try {
                        helper.parsing(objects.getJSONObject("achieve"), this)
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
        val year = binding.achievementYear.selectedItem.toString()
        val semester = binding.achievementTerm.selectedItem.toString().toInt()
        val config = ConfigManager(this@Achievement)
        config.putString("school_year_inquiry", year)
            .putInt("semester_inquiry", semester)
            .apply()
        binding.achievementRefresh.isRefreshing = true
        val helper = AchievementHelper(this@Achievement)
        if (objects != null) {
            try {
                helper.parsing(objects.getJSONObject("achieve"), this)
            } catch (e: JSONException){
                onReadFinish()
            }
        } else {
            helper.getMark(ConfigManager(this@Achievement), this)
        }
    }

    override fun onFailure(code: Int, message: String?, e: Exception?) {
        saveExplosion(e, code)
        onToast(R.string.text_load_failed, message, code)
        runOnUiThread {
            binding.achievementRefresh.isRefreshing = false
        }
    }

    override fun onReadStart() {
        achievementFailedLoad = false
        achievementPassedLoad = false
        runOnUiThread{
            binding.achievementTableFailed.removeAllViews()
            binding.achievementTableFailed.visibility = View.INVISIBLE
            binding.achievementTableFailedEmpty.visibility = View.VISIBLE

            binding.achievementTablePassed.removeAllViews()
            binding.achievementTablePassed.visibility = View.INVISIBLE
            binding.achievementTablePassedEmpty.visibility = View.VISIBLE
        }
    }

    override fun onReadPassed(data: PassedMarkData) {
        if (!achievementPassedLoad){
            achievementPassedLoad = true
        }

        ItemAchievementPassedBinding.inflate(layoutInflater)
        val itemPassed: ItemAchievementPassedBinding = ItemAchievementPassedBinding.inflate(layoutInflater)
        itemPassed.apply {
            achievementPassedName.text = data.name
            achievementPassedPaper.text = data.paper
            achievementPassedMark.text = data.mark
            achievementPassedRetake.text = data.retake
            achievementPassedRebuild.text = data.rebuild
            achievementPassedCredit.text = data.credit
            if (!judgePass(data.mark) && !judgePass(data.retake) && !judgePass(data.rebuild)){
                achievementPassedName.setTextColor(Color.RED)
                achievementPassedPaper.setTextColor(Color.RED)
                achievementPassedMark.setTextColor(Color.RED)
                achievementPassedRetake.setTextColor(Color.RED)
                achievementPassedRebuild.setTextColor(Color.RED)
                achievementPassedCredit.setTextColor(Color.RED)
            }
        }
        runOnUiThread{
            binding.achievementTablePassed.addView(itemPassed.root)
        }
    }

    private fun judgePass(string: String?): Boolean {
        return if (string == null || string == ""){
            false
        } else string.toFloat() >= 60
    }

    override fun onReadFailed(data: FailedMarkData) {
        if (!achievementFailedLoad){
            achievementFailedLoad = true
        }

        val itemFailed: ItemAchievementFailedBinding = ItemAchievementFailedBinding.inflate(layoutInflater)
        itemFailed.apply {
            gradeFailedName.text = data.name
            gradeFailedMark.text = data.mark
        }
        runOnUiThread{
            binding.achievementTableFailed.addView(itemFailed.root)
        }
    }

    override fun onReadFinish() {
        runOnUiThread {
            binding.achievementRefresh.isRefreshing = false
            if (achievementPassedLoad){
                binding.achievementTablePassed.visibility = View.VISIBLE
                binding.achievementTablePassedEmpty.visibility = View.INVISIBLE
            }
            if (achievementFailedLoad){
                binding.achievementTableFailed.visibility = View.VISIBLE
                binding.achievementTableFailedEmpty.visibility = View.INVISIBLE
            }
        }

    }

    override fun onViewSetup() {
        super.onViewSetup()
        initViewAtTop(binding.achievementToolbar)

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
        binding.achievementYear.adapter = arrayAdapterYear
        binding.achievementYear.setSelection(yearSelected, true)

        val semesterStudent: Int = config.getInt("semester_inquiry", config.getInt("semester", 1)) - 1
        val listTerm: List<String?> = listOf("1", "2")
        val arrayAdapterTerm: ArrayAdapter<String?> = ArrayAdapter(this@Achievement,
            R.layout.item_option, listTerm)
        binding.achievementTerm.adapter = arrayAdapterTerm
        binding.achievementTerm.setSelection(semesterStudent, true)

        binding.achievementAction.setOnClickListener { getAchievement() }
        binding.achievementRefresh.setOnRefreshListener { getAchievement() }
        binding.achievementBack.setOnClickListener { finish() }
        binding.achievementLandscape.setOnClickListener {
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

    override fun getContentView() = ActivityAchievementBinding.inflate(layoutInflater)

    override fun onSetSwipeBackEnable() = true
}