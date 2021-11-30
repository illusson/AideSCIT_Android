package com.sgpublic.aidescit.activity

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.sgpublic.aidescit.R
import com.sgpublic.aidescit.base.BaseActivity
import com.sgpublic.aidescit.util.CrashHandler
import com.sgpublic.aidescit.data.FailedMarkData
import com.sgpublic.aidescit.data.PassedMarkData
import com.sgpublic.aidescit.databinding.ActivityAchievementBinding
import com.sgpublic.aidescit.databinding.ItemAchievementFailedBinding
import com.sgpublic.aidescit.databinding.ItemAchievementPassedBinding
import com.sgpublic.aidescit.helper.AchievementHelper
import com.sgpublic.aidescit.manager.CacheManager
import com.sgpublic.aidescit.manager.ConfigManager
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
                    helper.getMark(this)
                }
            }
        } else if (objects != null){
            getAchievement()
        }
    }

    private fun getAchievement(objects: JSONObject? = null){
        val year = binding.achievementYear.selectedItem.toString()
        val semester = binding.achievementTerm.selectedItemPosition
        ConfigManager.putString("school_year_inquiry", year)
        ConfigManager.putInt("semester_inquiry", semester)
        binding.achievementRefresh.isRefreshing = true
        val helper = AchievementHelper(this@Achievement)
        if (objects != null) {
            try {
                helper.parsing(objects.getJSONObject("achieve"), this)
            } catch (e: JSONException){
                onReadFinish()
            }
        } else {
            helper.getMark(this)
        }
    }

    override fun onFailure(code: Int, message: String?, e: Exception?) {
        CrashHandler.saveExplosion(e, code)
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
            binding.achievementTableFailed.visibility = View.GONE
            binding.achievementTableFailedEmpty.visibility = View.VISIBLE

            binding.achievementTablePassed.removeAllViews()
            binding.achievementTablePassed.visibility = View.GONE
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
                binding.achievementTablePassedEmpty.visibility = View.GONE
            }
            if (achievementFailedLoad){
                binding.achievementTableFailed.visibility = View.VISIBLE
                binding.achievementTableFailedEmpty.visibility = View.GONE
            }
        }

    }

    override fun onViewSetup() {
        initViewAtTop(binding.achievementToolbar)

        val listYear: MutableList<String?> = ArrayList()
        listYear.add(getString(R.string.text_achievement_all))
        val gradeStudent: Int = ConfigManager.getInt("grade", 2019)
        val yearStudent: String = ConfigManager.getString("school_year_inquiry", ConfigManager.getString("school_year", "2019-2020"))
        var yearSelected = 0
        for (year_index in 0 until 4) {
            val itemYearText = (gradeStudent + year_index).toString() +
                    "-" + (gradeStudent + year_index + 1)
            listYear.add(itemYearText)
            if (yearStudent == itemYearText) {
                yearSelected = year_index + 1
            }
        }
        val arrayAdapterYear: ArrayAdapter<String?> = ArrayAdapter(this@Achievement,
            R.layout.item_option, listYear)
        binding.achievementYear.adapter = arrayAdapterYear
        binding.achievementYear.setSelection(yearSelected, true)
        binding.achievementYear.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                binding.achievementAction.isClickable = true
                binding.achievementTerm.isEnabled = position != 0
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                binding.achievementAction.isClickable = false
            }
        }
        binding.achievementTerm.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                binding.achievementAction.isClickable = true
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                binding.achievementAction.isClickable = false
            }
        }

        val semesterStudent: Int = ConfigManager.getInt("semester_inquiry", ConfigManager.getInt("semester", 1))
        val listTerm: List<String?> = listOf(getString(R.string.text_achievement_year), "1", "2")
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

    override fun isActivityAtBottom() = false

    companion object {
        @JvmStatic
        fun startActivity(context: Context){
            val intent = Intent().run {
                setClass(context, Achievement::class.java)
            }
            context.startActivity(intent)
        }
    }
}