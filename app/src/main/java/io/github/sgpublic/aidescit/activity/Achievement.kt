package io.github.sgpublic.aidescit.activity

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import io.github.sgpublic.aidescit.Application
import io.github.sgpublic.aidescit.R
import io.github.sgpublic.aidescit.base.BaseViewModelActivity
import io.github.sgpublic.aidescit.databinding.ActivityAchievementBinding
import io.github.sgpublic.aidescit.ui.recycler.AchieveRecyclerAdapter
import io.github.sgpublic.aidescit.viewmodule.AchievementViewModule

class Achievement : BaseViewModelActivity<ActivityAchievementBinding, AchievementViewModule>() {
    override val ViewModel: AchievementViewModule by viewModels()

    override fun onActivityCreated(hasSavedInstanceState: Boolean) {
        ViewModel.init()
        ViewModel.getAchieve(this@Achievement, true)
    }

    override fun onViewModelSetup() {
        ViewModel.SEMESTER_SELECTED.observe(this) {
            ViewBinding.achievementSemester.setSelection(it)
        }
        ViewModel.YEAR_SELECTED.observe(this) {
            ViewBinding.achievementYear.setSelection(it)
        }
        ViewModel.ACHIEVE.observe(this) {
            adapter.setData(it.PASSED_MARK, it.FAILED_MARK)
        }
        ViewModel.EXCEPTION.observe(this) {
            ViewModel.LOADING.postValue(false)
            Application.onToast(this@Achievement, R.string.error_load, it.message, it.code)
        }
        ViewModel.LOADING.observe(this) {
            ViewBinding.achievementRefresh.isRefreshing = it
        }
    }

    private lateinit var adapter: AchieveRecyclerAdapter
    override fun beforeCreate() {
        adapter = AchieveRecyclerAdapter(this)
        super.beforeCreate()
    }

    override fun onViewSetup() {
        ViewBinding.achievementList.adapter = adapter

        val arrayAdapterYear: ArrayAdapter<String> = ArrayAdapter(this@Achievement,
            R.layout.item_option, ViewModel.SCHOOL_YEAR)
        ViewBinding.achievementYear.adapter = arrayAdapterYear
        ViewBinding.achievementYear.setSelection(ViewModel.getYearSelectedId(), true)
        ViewBinding.achievementYear.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                ViewBinding.achievementAction.isClickable = true
                ViewBinding.achievementSemester.isEnabled = position != 0
                ViewModel.YEAR_SELECTED.postValue(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                ViewBinding.achievementAction.isClickable = false
            }
        }
        ViewBinding.achievementSemester.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                ViewBinding.achievementAction.isClickable = true
                ViewModel.SEMESTER_SELECTED.postValue(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                ViewBinding.achievementAction.isClickable = false
            }
        }

        val arrayAdapterTerm: ArrayAdapter<String> = ArrayAdapter(this@Achievement,
            R.layout.item_option, ViewModel.SEMESTER)
        ViewBinding.achievementSemester.adapter = arrayAdapterTerm
        ViewBinding.achievementSemester.setSelection(ViewModel.getSemesterSelectedId(), true)

        ViewBinding.achievementAction.setOnClickListener {
            ViewModel.getAchieve(this@Achievement)
        }
        ViewBinding.achievementRefresh.setOnRefreshListener {
            ViewModel.getAchieve(this@Achievement)
        }
        ViewBinding.achievementBack.setOnClickListener { finish() }
        ViewBinding.achievementLandscape.setOnClickListener {
            requestedOrientation = if(requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            } else {
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
        }
    }

    override fun isActivityAtBottom() = false

    override fun onCreateViewBinding(): ActivityAchievementBinding =
        ActivityAchievementBinding.inflate(layoutInflater)

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