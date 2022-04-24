package io.github.sgpublic.aidescit.fragment

import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import androidx.appcompat.app.AppCompatActivity
import io.github.sgpublic.aidescit.Application
import io.github.sgpublic.aidescit.R
import io.github.sgpublic.aidescit.base.BaseFragment
import io.github.sgpublic.aidescit.core.data.ScheduleData
import io.github.sgpublic.aidescit.core.manager.CacheManager
import io.github.sgpublic.aidescit.core.manager.ConfigManager
import io.github.sgpublic.aidescit.core.module.ScheduleModule
import io.github.sgpublic.aidescit.core.util.dp
import io.github.sgpublic.aidescit.databinding.FragmentScheduleBinding
import io.github.sgpublic.aidescit.databinding.ItemTimetableBinding
import org.json.JSONObject

class Schedule(context: AppCompatActivity) : BaseFragment<FragmentScheduleBinding>(context), ScheduleModule.Callback {
    private var showTable: Boolean = false

    private var week: Int = 0

    private var viewWidth: Int = 0
    private var viewHeight: Int = 0

    override fun onFragmentCreated(hasSavedInstanceState: Boolean) {
        week = arguments?.getInt("week") ?: ConfigManager.WEEK
        if (week == 0){
            week = 1
        }
        if (week <= 1) {
            ViewBinding.tablePre.isClickable = false
            ViewBinding.tablePre.alpha = 0.4F
        } else if (week >= 18) {
            ViewBinding.tableNext.isClickable = false
            ViewBinding.tableNext.alpha = 0.4F
        }
        getSchedule(CacheManager.CACHE_SCHEDULE)
    }

    override fun onViewSetup() {
        ViewBinding.timetableRefresh.setOnRefreshListener { getSchedule() }
        ViewBinding.timetableRefresh.setColorSchemeResources(R.color.colorAlert)
        initViewAtTop(ViewBinding.tableToolbar)

        ViewBinding.tablePre.setOnClickListener {
            week--
            getSchedule(CacheManager.CACHE_SCHEDULE)

            if (week <= 1) {
                ViewBinding.tablePre.isClickable = false
                ViewBinding.tablePre.alpha = 0.4F
            } else if (week < 18) {
                ViewBinding.tableNext.isClickable = true
                ViewBinding.tableNext.alpha = 1.0F
            }
        }

        ViewBinding.tableNext.setOnClickListener {
            week++
            getSchedule(CacheManager.CACHE_SCHEDULE)

            if (week >= 18) {
                ViewBinding.tableNext.isClickable = false
                ViewBinding.tableNext.alpha = 0.4F
            } else if (week > 1) {
                ViewBinding.tablePre.isClickable = true
                ViewBinding.tablePre.alpha = 1.0F
            }
        }
    }

    private fun getSchedule(objects: JSONObject? = null){
        ViewBinding.tablePre.isEnabled = false
        ViewBinding.tableNext.isEnabled = false
        if (objects != null){
            Thread {
                ScheduleModule().parsing(objects, week, this)
            }.start()
        } else {
            ScheduleModule().getSchedule(week, this)
        }
        ViewBinding.tableWeek.text = String.format(getString(R.string.text_table_week_index), week)
    }

    override fun onReadStart(isSundayEmpty: Boolean) {
        val daySize: Int
        if (isSundayEmpty) {
            runOnUiThread {
                ViewBinding.scheduleSunday.visibility = View.GONE
            }
            daySize = 6
        } else {
            daySize = 7
        }
        viewWidth = (resources.displayMetrics.widthPixels - 20.dp) / daySize
        viewHeight = 110.dp
        showTable = false
        runOnUiThread {
            ViewBinding.timetableGrid.visibility = View.INVISIBLE
            ViewBinding.timetableGridMorning.removeAllViews()
            ViewBinding.timetableGridNoon.removeAllViews()
            ViewBinding.timetableGridEvening.removeAllViews()
        }
    }

    override fun onFailure(code: Int, message: String?, e: Throwable?) {
        Application.onToast(context, R.string.text_load_failed, message, code)
        ViewBinding.timetableRefresh.isRefreshing = false
    }

    override fun onRead(day: String, clazz: String, data: ScheduleData?) {
        if (!showTable){
            showTable = true
        }

        val classIndex = ScheduleModule.CLASS_INDEX.indexOf(clazz)
        val parent: ViewGroup = when {
            classIndex < 2 -> ViewBinding.timetableGridMorning
            classIndex < 4 -> ViewBinding.timetableGridNoon
            else -> ViewBinding.timetableGridEvening
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
        params.columnSpec = GridLayout.spec(
            ScheduleModule.DAY_INDEX.indexOf(day)
        )
        params.width = viewWidth
        params.height = viewHeight
        itemTimetable.layoutParams = params

        runOnUiThread{
            parent.addView(itemTimetable)
        }
    }

    override fun onReadFinish(isEmpty: Boolean) {
        runOnUiThread {
            if (isEmpty) {
                ViewBinding.timetableGrid.visibility = View.GONE
                ViewBinding.timetableEmpty.visibility = View.VISIBLE
            } else if (showTable) {
                ViewBinding.timetableGrid.visibility = View.VISIBLE
                ViewBinding.timetableEmpty.visibility = View.GONE
            }
            ViewBinding.tablePre.isEnabled = true
            ViewBinding.tableNext.isEnabled = true
            ViewBinding.timetableRefresh.isRefreshing = false
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
//        val table = CacheManager(context).read(CacheManager.CACHE_TABLE) ?: return
//        outState.putString("table", table.toString())
        outState.putInt("week", week)
        super.onSaveInstanceState(outState)
    }

    override fun onCreateViewBinding(container: ViewGroup?): FragmentScheduleBinding =
        FragmentScheduleBinding.inflate(layoutInflater)
}