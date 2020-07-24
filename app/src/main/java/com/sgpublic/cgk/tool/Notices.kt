package com.sgpublic.cgk.tool

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.sgpublic.cgk.tool.base.BaseActivity
import com.sgpublic.cgk.tool.helper.HeaderInfoHelper
import com.sgpublic.cgk.tool.helper.TableHelper
import com.sgpublic.cgk.tool.manager.CacheManager
import com.sgpublic.cgk.tool.manager.CalendarManager
import com.sgpublic.cgk.tool.manager.ConfigManager
import kotlinx.android.synthetic.main.activity_notices.*
import kotlinx.android.synthetic.main.fragment_timetable.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class Notices : BaseActivity(), View.OnClickListener, HeaderInfoHelper.Callback {
    companion object{
        private val pre_time_description: Array<CharSequence> = arrayOf(
            "5 分钟", "10 分钟", "15 分钟", "20 分钟"
        )
    }

    private var preTimeSet: Int = 2
    private var preTimeArray: List<Int> = listOf(
        5, 10, 15, 20
    )
    private var isInserting = false
    private var insertCount: Int = 0
    private val manager: CalendarManager = CalendarManager(this@Notices)
    private val startDate: Calendar = Calendar.getInstance()
    private val scheduleWinter: MutableList<Calendar?> = mutableListOf(null, null)

    override fun onActivityCreate(savedInstanceState: Bundle?) {
        val permissions = intArrayOf(
            ContextCompat.checkSelfPermission(this@Notices, Manifest.permission.WRITE_CALENDAR),
            ContextCompat.checkSelfPermission(this@Notices, Manifest.permission.READ_CALENDAR)
        )
        var isAllowed = true
        for (permission in permissions) {
            isAllowed = isAllowed && permission == PackageManager.PERMISSION_GRANTED
        }
        if (isAllowed) {
            Thread {
                if (manager.checkCalendarAccount() <= 0) {
                    addCalendarAccount()
                }
                HeaderInfoHelper(this@Notices).getStartDate(this)
            }.start()
        } else {
            ActivityCompat.requestPermissions(
                this@Notices, arrayOf(
                    Manifest.permission.WRITE_CALENDAR,
                    Manifest.permission.READ_CALENDAR
                ), 1
            )
        }
    }

    override fun onStartDateResult(startDate: Date) {
        this.startDate.time = startDate
        setOnActionMode(false, 0)
    }

    private fun addCalendarAccount(){
        if (manager.addCalendarAccount() == -1L) {
            notices_state.setText(R.string.text_load_failed)
            notices_insert.isEnabled = false
            notices_delete.isEnabled = false
            onToast(this, R.string.text_calendar_setup_failure)
        }
    }

    private fun setOnActionMode(is_doing: Boolean, click_index: Int) {
        isInserting = is_doing
        runOnUiThread {
            val alpha: Float = if (is_doing) 0.3F else 1.0F

            val noticesClick: ProgressBar = if (click_index == 0) {
                notices_insert_doing
            } else {
                notices_delete_doing
            }
            noticesClick.visibility = if (is_doing) View.VISIBLE else View.INVISIBLE

            notices_insert.isClickable = !is_doing
            notices_insert.alpha = alpha
            notices_delete.isClickable = !is_doing
            notices_delete.alpha = alpha
            notices_pre_base.isClickable = !is_doing
            notices_pre_switch.isClickable = !is_doing
            notices_pre_base.alpha = alpha
            notices_pre_time_base.isClickable = !is_doing
            notices_pre_time_base.alpha = alpha
            notices_newest_base.isClickable = !is_doing
            notices_newest_switch.isClickable = !is_doing
            notices_newest_base.alpha = alpha

            if (!is_doing) {
                checkInsert()
            }
        }
    }

    private fun checkInsert() {
        insertCount = manager.queryAtrCount()
        val stateText: String
        val stateColor: Int
        if (insertCount == 0) {
            stateText = getText(R.string.text_calendar_count_empty).toString()
            stateColor = Color.RED
            notices_insert.setText(R.string.text_calendar_insert_new)
            notices_delete.isEnabled = false
            notices_delete.alpha = 0.3F
        } else {
            stateText = java.lang.String.format(
                getString(R.string.text_calendar_count),
                insertCount
            )
            stateColor = Color.GREEN
            notices_insert.setText(R.string.text_calendar_insert_re)
            notices_delete.isEnabled = true
            notices_delete.alpha = 1.0F
        }
        notices_state.text = stateText
        notices_state.setTextColor(stateColor)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewSetup() {
        super.onViewSetup()

        notices_newest_base.setOnClickListener(this)
        notices_pre_base.setOnClickListener(this)
        notices_pre_time_base.setOnClickListener(this)
        notices_insert.setOnClickListener(this)
        notices_pre_switch.setOnCheckedChangeListener { _, isChecked ->
            notices_pre_time_base.alpha = if (isChecked) 1.0F else 0.3F
            notices_pre_time_base.isClickable = isChecked
        }
        notices_delete.setOnClickListener(this)
        notices_back.setOnClickListener(this)
        notices_pre_time.text = "15"
    }

    override fun onClick(v: View?) {
        v?.let {
            when (it.id) {
                R.id.notices_back -> { onBackPressed() }
                R.id.notices_newest_base -> {
                    notices_newest_switch.isChecked = !notices_newest_switch.isChecked
                }
                R.id.notices_pre_base -> {
                    notices_pre_switch.isChecked = !notices_pre_switch.isChecked
                }
                R.id.notices_pre_time_base -> {
                    val preTimeSetDoing = preTimeSet
                    AlertDialog.Builder(this@Notices).run {
                        setTitle(R.string.title_calendar_pre_time_choose)
                        setSingleChoiceItems(pre_time_description, preTimeSet
                        ) { _, which -> preTimeSet = which }
                        setPositiveButton(R.string.text_ok){ _, _ ->
                            notices_pre_time.text = preTimeArray[preTimeSet].toString()
                        }
                        setNegativeButton(R.string.text_cancel){ _, _ ->
                            preTimeSet = preTimeSetDoing
                        }
                    }.show()
                }
                R.id.notices_insert -> {
                    if (manager.checkCalendarAccount() <= 0) {
                        addCalendarAccount()
                    }
                    if (manager.checkCalendarAccount() > 0) {
                        getTable(notices_newest_switch.isChecked)
                    } else { null }
                }
                R.id.notices_delete -> {
                    doDelete()
                }
                else -> null
            }
        }
    }

    private fun getTable(useNewest: Boolean){
        if (useNewest){
            TableHelper(this@Notices).getTable(
                ConfigManager(this@Notices),
                session!!, object : TableHelper.Callback{
                    override fun onReadStart() {
                        onReadTable()
                    }
                }
            )
        } else {
            onReadTable()
        }
    }

    private fun onReadTable() {
        val objects: JSONObject? = CacheManager(this@Notices).read(CacheManager.CACHE_TABLE)
        doInsert(objects!!)
    }

    private fun doDelete(){
        setSwipeBackEnable(false)
        notices_state.setTextColor(Color.RED)
        setOnActionMode(true, 1)
        notices_state.setText(R.string.text_calendar_doing_delete)
        Thread {
            manager.deleteCalendarEvent()
            runOnUiThread{
                setOnActionMode(false, 1)
                setSwipeBackEnable(true)
            }
        }.start()
    }

    private fun doInsert(objects: JSONObject){
        Thread {
            runOnUiThread {
                setSwipeBackEnable(false)
                notices_state.setText(R.string.text_calendar_doing_delete)
                notices_state.setTextColor(Color.RED)
                setOnActionMode(true, 0)
            }
            manager.deleteCalendarEvent()
            manager.setPreRemindTime(
                if (notices_pre_switch.isChecked) preTimeArray[preTimeSet] else 0
            )

            var insertedIndex = 0
            val termDate: Calendar = startDate
            val tableArray: JSONArray = objects.getJSONArray("table")
            val sdfDate = SimpleDateFormat("yyyy/MM/dd", Locale.CHINESE)

            var classCountIndex = 0

            for (week_index in 1 .. 18) {
                for (day_index in 1 .. 7) {
                    termDate.add(Calendar.DAY_OF_YEAR, 1)
                    if (day_index != 7) {
                        var scheduleIndex: Array<Array<String>>
                        val scheduleSet = Calendar.getInstance()
                        scheduleSet.time = termDate.time
                        scheduleSet[Calendar.YEAR] = 1970
                        val isWeekend =
                            if (termDate.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY || termDate.get(
                                    Calendar.DAY_OF_WEEK
                                ) == Calendar.SATURDAY
                            ) 1 else 0
                        scheduleIndex =
                            if (scheduleSet.before(scheduleWinter[1]) && scheduleSet.after(scheduleWinter[0])) {
                                CalendarManager.CLASS_SUMMER[isWeekend]
                            } else {
                                CalendarManager.CLASS_WINTER[isWeekend]
                            }
                        for (class_index in 0 .. 4) {
                            val classTable = tableArray.getJSONArray(day_index - 1)
                                .getJSONObject(class_index)
                            val classLocationCount = classTable.getInt("count")
                            if (classLocationCount != 0) {
                                for (class_count in 0 until classLocationCount) {
                                    classCountIndex++
                                    val classData = classTable.getJSONArray("data").getJSONObject(class_count)
                                    val classRange = classData.getJSONArray("range")
                                    if (week_index >= classRange.getInt(0) && week_index <= classRange.getInt(1)) {
                                        val classWeek = classData.getInt("week")
                                        if (classWeek == 2 || classWeek == week_index % 2) {
                                            val classTime = arrayOf(
                                                sdfDate.format(termDate.time) + scheduleIndex[class_index][0],
                                                sdfDate.format(termDate.time) + scheduleIndex[class_index][1]
                                            )
                                            insertedIndex ++

                                            runOnUiThread {
                                                val stateText = java.lang.String.format(
                                                    getString(R.string.text_calendar_doing),
                                                    insertedIndex
                                                )
                                                notices_state.text = stateText
                                            }

                                            manager.addCalendarEvent(
                                                classTime[0],
                                                classTime[1],
                                                classData.getString("name"),
                                                CalendarManager.CLASS_DESCRIPTION[class_count]
                                                    .toString() + " " + classData.getString("room") + " " + classData.getString(
                                                    "teacher"
                                                ),
                                                classData.getString("room") + " " + classData.getString(
                                                    "teacher"
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            runOnUiThread {
                setOnActionMode(false, 0)
                setSwipeBackEnable(true)
            }
        }.start()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var isGranted = true
        for (grantResult in grantResults) {
            isGranted = isGranted && grantResult == PackageManager.PERMISSION_GRANTED
        }
        if (!isGranted) {
            onToast(this@Notices, R.string.permission_denied)
        }
    }

    override fun onBackPressed() {
        if (isInserting){
            onToast(this@Notices, R.string.text_calendar_doing_warning)
        } else {
            finish()
        }
    }

    override fun getContentView() = R.layout.activity_notices

    override fun onSetSwipeBackEnable() = true
}