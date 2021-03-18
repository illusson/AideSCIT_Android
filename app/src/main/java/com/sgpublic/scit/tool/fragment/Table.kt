package com.sgpublic.scit.tool.fragment

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import androidx.appcompat.app.AppCompatActivity
import com.sgpublic.scit.tool.R
import com.sgpublic.scit.tool.base.BaseFragment
import com.sgpublic.scit.tool.base.MyLog
import com.sgpublic.scit.tool.data.TableData
import com.sgpublic.scit.tool.helper.TableHelper
import com.sgpublic.scit.tool.manager.CacheManager
import com.sgpublic.scit.tool.manager.ConfigManager
import kotlinx.android.synthetic.main.fragment_table.*
import kotlinx.android.synthetic.main.item_timetable.view.*
import org.json.JSONObject

class Table(contest: AppCompatActivity) : BaseFragment(contest), TableHelper.Callback {
    private var showTable: Boolean = false

    private var week: Int = 0
    
    private var viewWidth: Int = 0
    private var viewHeight: Int = 0

    override fun onViewSetup() {
        super.onViewSetup()

        timetable_refresh.setOnRefreshListener { getTable() }
        timetable_refresh.setColorSchemeResources(R.color.colorAlert)
        initViewAtTop(table_toolbar)

        table_pre.setOnClickListener {
            week--
            getTable(CacheManager(contest).read(CacheManager.CACHE_TABLE))

            if (week <= 1) {
                table_pre.isClickable = false
                table_pre.alpha = 0.4F
            } else if (week < 18) {
                table_next.isClickable = true
                table_next.alpha = 1.0F
            }
        }

        table_next.setOnClickListener {
            week++
            getTable(CacheManager(contest).read(CacheManager.CACHE_TABLE))

            if (week >= 18) {
                table_next.isClickable = false
                table_next.alpha = 0.4F
            } else if (week > 1) {
                table_pre.isClickable = true
                table_pre.alpha = 1.0F
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewWidth = (resources.displayMetrics.widthPixels - dip2px(40F)) / 6
        viewHeight = dip2px(110F)
        week = arguments?.getInt("week") ?: 0
        if (week == 0){
            week = ConfigManager(contest).getInt("week")
        }
        getTable(CacheManager(contest).read(CacheManager.CACHE_TABLE))
    }

    private fun getTable(objects: JSONObject? = null){
        table_pre.isEnabled = false
        table_next.isEnabled = false
        if (objects != null){
            Thread {
                TableHelper(contest).parsing(objects, week, this)
            }.start()
        } else {
            TableHelper(contest).getTable(ConfigManager(contest), week, this)
        }
        table_week.text = String.format(getString(R.string.text_table_week_index), week)
    }

    override fun onReadStart() {
        showTable = false
        runOnUiThread {
            timetable_grid.visibility = View.INVISIBLE
            timetable_grid_morning.removeAllViews()
            timetable_grid_noon.removeAllViews()
            timetable_grid_evening.removeAllViews()
        }
    }

    override fun onFailure(code: Int, message: String?, e: Exception?) {
        saveExplosion(e, code)
        onToast(R.string.text_load_failed, message, code)
        timetable_refresh.isRefreshing = false
    }

    override fun onRead(dayIndex: Int, classIndex: Int, data: TableData?) {
        if (!showTable){
            showTable = true
        }

        val parent: ViewGroup = when {
            classIndex < 2 -> timetable_grid_morning
            classIndex < 4 -> timetable_grid_noon
            else -> timetable_grid_evening
        }
        val itemTimetable: View
        if (data != null){
            itemTimetable = LayoutInflater.from(contest).inflate(
                R.layout.item_timetable,
                parent,
                false
            )
            itemTimetable.apply {
                lesson_name.text = data.name
                lesson_name.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                lesson_location.text = data.room
                lesson_teacher.text = data.teacher
            }

            itemTimetable.setOnClickListener{}
        } else {
            itemTimetable = View(contest)
        }

        val params = GridLayout.LayoutParams()
        params.rowSpec = GridLayout.spec(classIndex % 2)
        params.columnSpec = GridLayout.spec(dayIndex)
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
                timetable_grid.visibility = View.GONE
                timetable_empty.visibility = View.VISIBLE
            } else if (showTable) {
                timetable_grid.visibility = View.VISIBLE
                timetable_empty.visibility = View.GONE
            }
            table_pre.isEnabled = true
            table_next.isEnabled = true
            timetable_refresh.isRefreshing = false
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
//        val table = CacheManager(contest).read(CacheManager.CACHE_TABLE) ?: return
//        outState.putString("table", table.toString())
        outState.putInt("week", week)
        super.onSaveInstanceState(outState)
    }

    private fun getLoadState() = timetable_refresh.isRefreshing

    override fun getContentView(): Int = R.layout.fragment_table
}