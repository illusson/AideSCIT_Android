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
import com.sgpublic.scit.tool.base.CrashHandler
import com.sgpublic.scit.tool.data.TableData
import com.sgpublic.scit.tool.databinding.FragmentTableBinding
import com.sgpublic.scit.tool.databinding.ItemTimetableBinding
import com.sgpublic.scit.tool.helper.TableHelper
import com.sgpublic.scit.tool.manager.CacheManager
import com.sgpublic.scit.tool.manager.ConfigManager
import org.json.JSONObject

class Table(private val contest: AppCompatActivity) : BaseFragment<FragmentTableBinding>(contest), TableHelper.Callback {
    private var showTable: Boolean = false

    private var week: Int = 0
    
    private var viewWidth: Int = 0
    private var viewHeight: Int = 0

    override fun onFragmentCreated(savedInstanceState: Bundle?) {
        viewWidth = (resources.displayMetrics.widthPixels - dip2px(40F)) / 6
        viewHeight = dip2px(110F)
        week = arguments?.getInt("week") ?: 0
        if (week == 0){
            week = ConfigManager.getInt("week")
        }
        getTable(CacheManager(contest).read(CacheManager.CACHE_TABLE))
    }

    override fun onViewSetup() {
        binding.timetableRefresh.setOnRefreshListener { getTable() }
        binding.timetableRefresh.setColorSchemeResources(R.color.colorAlert)
        initViewAtTop(binding.tableToolbar)

        binding.tablePre.setOnClickListener {
            week--
            getTable(CacheManager(contest).read(CacheManager.CACHE_TABLE))

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
            getTable(CacheManager(contest).read(CacheManager.CACHE_TABLE))

            if (week >= 18) {
                binding.tableNext.isClickable = false
                binding.tableNext.alpha = 0.4F
            } else if (week > 1) {
                binding.tablePre.isClickable = true
                binding.tablePre.alpha = 1.0F
            }
        }
    }

    private fun getTable(objects: JSONObject? = null){
        binding.tablePre.isEnabled = false
        binding.tableNext.isEnabled = false
        if (objects != null){
            Thread {
                TableHelper(contest).parsing(objects, week, this)
            }.start()
        } else {
            TableHelper(contest).getTable(week, this)
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

    override fun onRead(dayIndex: Int, classIndex: Int, data: TableData?) {
        if (!showTable){
            showTable = true
        }

        val parent: ViewGroup = when {
            classIndex < 2 -> binding.timetableGridMorning
            classIndex < 4 -> binding.timetableGridNoon
            else -> binding.timetableGridEvening
        }
        val itemTimetable: View
        if (data != null){
            val itemTimetableBinding = ItemTimetableBinding.inflate(layoutInflater)
            itemTimetableBinding.apply {
                lessonName.text = data.name
                lessonName.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                lessonLocation.text = data.room
                lessonTeacher.text = data.teacher
            }

            itemTimetableBinding.root.setOnClickListener{}
            itemTimetable = itemTimetableBinding.root
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
                binding.timetableGrid.visibility = View.GONE
                binding.timetableEmpty.visibility = View.VISIBLE
            } else if (showTable) {
                binding.timetableGrid.visibility = View.VISIBLE
                binding.timetableEmpty.visibility = View.GONE
            }
            binding.tablePre.isEnabled = true
            binding.tableNext.isEnabled = true
            binding.timetableRefresh.isRefreshing = false
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
//        val table = CacheManager(contest).read(CacheManager.CACHE_TABLE) ?: return
//        outState.putString("table", table.toString())
        outState.putInt("week", week)
        super.onSaveInstanceState(outState)
    }
}