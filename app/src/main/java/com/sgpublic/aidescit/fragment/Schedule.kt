package com.sgpublic.aidescit.fragment

import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import androidx.appcompat.app.AppCompatActivity
import com.sgpublic.aidescit.R
import com.sgpublic.aidescit.base.BaseFragment
import com.sgpublic.aidescit.data.ScheduleData
import com.sgpublic.aidescit.databinding.FragmentScheduleBinding
import com.sgpublic.aidescit.databinding.ItemTimetableBinding
import com.sgpublic.aidescit.helper.ScheduleHelper
import com.sgpublic.aidescit.manager.CacheManager
import com.sgpublic.aidescit.manager.ConfigManager
import com.sgpublic.aidescit.util.CrashHandler
import org.json.JSONObject

class Schedule(contest: AppCompatActivity) : BaseFragment<FragmentScheduleBinding>(contest), ScheduleHelper.Callback {
    private var showTable: Boolean = false

    private var week: Int = 0

    private val isSundayEmpty: Boolean = ConfigManager.getBoolean("is_sunday_empty")
    private var viewWidth: Int = 0
    private var viewHeight: Int = 0

    override fun onFragmentCreated(savedInstanceState: Bundle?) {
        val daySize: Int
        if (isSundayEmpty) {
            binding.timetableGridMorning.columnCount = 6
            binding.timetableGridNoon.columnCount = 6
            binding.timetableGridEvening.columnCount = 6
            daySize = 6
        } else {
            daySize = 7
        }
        viewWidth = (resources.displayMetrics.widthPixels - dip2px(20F)) / daySize
        viewHeight = dip2px(110F)
        week = arguments?.getInt("week")
            ?: ConfigManager.getInt("week", 0)
        if (week == 0){
            week = 1
        }
        if (week <= 1) {
            binding.tablePre.isClickable = false
            binding.tablePre.alpha = 0.4F
        } else if (week >= 18) {
            binding.tableNext.isClickable = false
            binding.tableNext.alpha = 0.4F
        }
        getSchedule(CacheManager(contest).read(CacheManager.CACHE_SCHEDULE))
    }

    override fun onViewSetup() {
        binding.timetableRefresh.setOnRefreshListener { getSchedule() }
        binding.timetableRefresh.setColorSchemeResources(R.color.colorAlert)
        initViewAtTop(binding.tableToolbar)

        binding.tablePre.setOnClickListener {
            week--
            getSchedule(CacheManager(contest).read(CacheManager.CACHE_SCHEDULE))

            if (week <= 1) {
                binding.tablePre.isClickable = false
                binding.tablePre.alpha = 0.4F
            } else if (week < 18) {
                binding.tableNext.isClickable = true
                binding.tableNext.alpha = 1.0F
            }
        }

        binding.tableNext.setOnClickListener {
            week++
            getSchedule(CacheManager(contest).read(CacheManager.CACHE_SCHEDULE))

            if (week >= 18) {
                binding.tableNext.isClickable = false
                binding.tableNext.alpha = 0.4F
            } else if (week > 1) {
                binding.tablePre.isClickable = true
                binding.tablePre.alpha = 1.0F
            }
        }
    }

    private fun getSchedule(objects: JSONObject? = null){
        binding.tablePre.isEnabled = false
        binding.tableNext.isEnabled = false
        if (objects != null){
            Thread {
                ScheduleHelper(contest).parsing(objects, week, this)
            }.start()
        } else {
            ScheduleHelper(contest).getSchedule(week, this)
        }
        binding.tableWeek.text = String.format(getString(R.string.text_table_week_index), week)
    }

    override fun onReadStart() {
        showTable = false
        runOnUiThread {
            binding.timetableGrid.visibility = View.INVISIBLE
            binding.timetableGridMorning.removeAllViews()
            binding.timetableGridNoon.removeAllViews()
            binding.timetableGridEvening.removeAllViews()
        }
    }

    override fun onFailure(code: Int, message: String?, e: Exception?) {
        CrashHandler.saveExplosion(e, code)
        onToast(R.string.text_load_failed, message, code)
        binding.timetableRefresh.isRefreshing = false
    }

    override fun onRead(dayIndex: Int, classIndex: Int, data: ScheduleData?) {
        if (isSundayEmpty && dayIndex == 0){
            return
        }

        if (!showTable){
            showTable = true
        }

        val parent: ViewGroup = when {
            classIndex < 2 -> binding.timetableGridMorning
            classIndex < 4 -> binding.timetableGridNoon
            else -> binding.timetableGridEvening
        }

        val itemTimetable = if (data != null){
            ItemTimetableBinding.inflate(layoutInflater).apply {
                lessonName.text = data.name
                lessonName.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                lessonLocation.text = data.room
                lessonTeacher.text = data.teacher
                root.setOnClickListener{}
            }.root
        } else {
            View(context)
        }

        val params = GridLayout.LayoutParams()
        params.rowSpec = GridLayout.spec(classIndex % 2)
        params.columnSpec = GridLayout.spec(dayIndex - (if (isSundayEmpty) 1 else 0))
        params.width = viewWidth
        params.height = viewHeight
        itemTimetable.layoutParams = params

        runOnUiThread{
            parent.addView(itemTimetable)
        }
    }

    override fun onReadFinish(isEmpty: Boolean, isSundayEmpty: Boolean) {
        runOnUiThread {
            if (isEmpty) {
                binding.timetableGrid.visibility = View.GONE
                binding.timetableEmpty.visibility = View.VISIBLE
            } else if (showTable) {
                binding.timetableGrid.visibility = View.VISIBLE
                binding.timetableEmpty.visibility = View.GONE
            }
            if (this.isSundayEmpty){
                binding.scheduleSunday.visibility = View.GONE
            }
            binding.tablePre.isEnabled = true
            binding.tableNext.isEnabled = true
            binding.timetableRefresh.isRefreshing = false
        }
        ConfigManager.putBoolean("is_sunday_empty", isSundayEmpty)
    }

    override fun onSaveInstanceState(outState: Bundle) {
//        val table = CacheManager(contest).read(CacheManager.CACHE_TABLE) ?: return
//        outState.putString("table", table.toString())
        outState.putInt("week", week)
        super.onSaveInstanceState(outState)
    }
}