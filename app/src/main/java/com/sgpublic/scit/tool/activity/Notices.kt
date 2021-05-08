package com.sgpublic.scit.tool.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.sgpublic.scit.tool.R
import com.sgpublic.scit.tool.base.BaseActivity
import com.sgpublic.scit.tool.base.CrashHandler
import com.sgpublic.scit.tool.databinding.ActivityNoticesBinding
import com.sgpublic.scit.tool.helper.HeaderInfoHelper
import com.sgpublic.scit.tool.helper.TableHelper
import com.sgpublic.scit.tool.manager.CacheManager
import com.sgpublic.scit.tool.manager.CalendarManager
import com.sgpublic.scit.tool.manager.ConfigManager
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class Notices : BaseActivity<ActivityNoticesBinding>(), View.OnClickListener, HeaderInfoHelper.Callback {
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
    private lateinit var scheduleSummer: MutableList<Calendar>

    private lateinit var setup: Thread

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        if (!checkSelfPermission()){
            onToast(R.string.text_notices_permission_current)
            binding.noticesState.text = getText(R.string.text_notices_permission_current)
            binding.noticesState.setTextColor(Color.RED)
            return
        }
        setup = Thread {
            if (manager.checkCalendarAccount() <= 0) {
                addCalendarAccount()
            }
            HeaderInfoHelper(this@Notices).getSemesterInfo(this)
        }

        val start: Calendar = Calendar.getInstance()
        start.set(2000, 4, 0)
        val end: Calendar = Calendar.getInstance()
        end.set(2000, 9, 0)
        scheduleSummer = mutableListOf(start, end)

        setOnActionMode(true, 0)
        setup.start()
    }

    private fun checkSelfPermission(): Boolean {
        val permissions = intArrayOf(
            ContextCompat.checkSelfPermission(this@Notices, Manifest.permission.WRITE_CALENDAR),
            ContextCompat.checkSelfPermission(this@Notices, Manifest.permission.READ_CALENDAR)
        )
        var isAllowed = true
        for (permission in permissions) {
            isAllowed = isAllowed && permission == PackageManager.PERMISSION_GRANTED
        }
        return isAllowed
    }

    override fun onSemesterInfoResult(semester: Int, schoolYear: String, week: Int, startDate: Date) {
        this.startDate.time = startDate
        setOnActionMode(false, 0)
    }

    override fun onFailure(code: Int, message: String?, e: Exception?) {
        CrashHandler.saveExplosion(e, code)
        onToast(R.string.text_load_failed, message, code)
        setOnActionMode(false, 0)
    }

    private fun addCalendarAccount(){
        if (manager.addCalendarAccount() == -1L) {
            binding.noticesState.setText(R.string.text_load_failed)
            binding.noticesInsert.isEnabled = false
            binding.noticesDelete.isEnabled = false
            onToast(R.string.text_calendar_setup_failure)
        }
    }

    private fun setOnActionMode(is_doing: Boolean, click_index: Int) {
        isInserting = is_doing
        runOnUiThread {
            val alpha: Float = if (is_doing) 0.3F else 1.0F

            val noticesClick: ProgressBar = if (click_index == 0) {
                binding.noticesInsertDoing
            } else {
                binding.noticesDeleteDoing
            }
            noticesClick.visibility = if (is_doing) View.VISIBLE else View.INVISIBLE

            binding.noticesInsert.isClickable = !is_doing
            binding.noticesInsert.alpha = alpha
            binding.noticesDelete.isClickable = !is_doing
            binding.noticesDelete.alpha = alpha
            binding.noticesPreBase.isClickable = !is_doing
            binding.noticesPreBase.isClickable = !is_doing
            binding.noticesPreBase.alpha = alpha
            binding.noticesPreTimeBase.isClickable = !is_doing
            binding.noticesPreTimeBase.alpha = alpha
            binding.noticesNewestBase.isClickable = !is_doing
            binding.noticesNewestSwitch.isClickable = !is_doing
            binding.noticesNewestBase.alpha = alpha

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
            binding.noticesInsert.setText(R.string.text_calendar_insert_new)
            binding.noticesDelete.isEnabled = false
            binding.noticesDelete.alpha = 0.3F
        } else {
            stateText = java.lang.String.format(
                getString(R.string.text_calendar_count),
                insertCount
            )
            stateColor = Color.GREEN
            binding.noticesInsert.setText(R.string.text_calendar_insert_re)
            binding.noticesDelete.isEnabled = true
            binding.noticesDelete.alpha = 1.0F
        }
        binding.noticesState.text = stateText
        binding.noticesState.setTextColor(stateColor)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewSetup() {
        binding.noticesNewestBase.setOnClickListener(this)
        binding.noticesPreBase.setOnClickListener(this)
        binding.noticesPreTimeBase.setOnClickListener(this)
        binding.noticesInsert.setOnClickListener(this)
        binding.noticesPreSwitch.setOnCheckedChangeListener { _, isChecked ->
            binding.noticesPreTimeBase.alpha = if (isChecked) 1.0F else 0.3F
            binding.noticesPreTimeBase.isClickable = isChecked
        }
        binding.noticesDelete.setOnClickListener(this)
        binding.noticesBack.setOnClickListener(this)
        binding.noticesPreTime.text = "15"
        initViewAtBottom(binding.noticesDeleteBase)
        initViewAtTop(binding.noticesToolbar)
    }

    override fun onClick(v: View?) {
        v?.let {
            when (it.id) {
                R.id.notices_back -> { onBackPressed() }
                R.id.notices_newest_base -> {
                    binding.noticesNewestSwitch.isChecked = !binding.noticesNewestSwitch.isChecked
                }
                R.id.notices_pre_base -> {
                    binding.noticesPreSwitch.isChecked = !binding.noticesPreSwitch.isChecked
                }
                R.id.notices_pre_time_base -> {
                    val preTimeSetDoing = preTimeSet
                    AlertDialog.Builder(this@Notices).run {
                        setTitle(R.string.title_calendar_pre_time_choose)
                        setSingleChoiceItems(
                            pre_time_description, preTimeSet
                        ) { _, which -> preTimeSet = which }
                        setPositiveButton(R.string.text_ok){ _, _ ->
                            binding.noticesPreTime.text = preTimeArray[preTimeSet].toString()
                        }
                        setNegativeButton(R.string.text_cancel){ _, _ ->
                            preTimeSet = preTimeSetDoing
                        }
                    }.show()
                }
                R.id.notices_insert -> {
                    if (!checkSelfPermission()){
                        onToast(R.string.text_notices_permission_current)
                        return
                    }
                    if (manager.checkCalendarAccount() <= 0) {
                        addCalendarAccount()
                    }
                    if (manager.checkCalendarAccount() > 0) {
                        getTable(binding.noticesNewestSwitch.isChecked)
                    } else { null }
                }
                R.id.notices_delete -> {
                    if (!checkSelfPermission()){
                        onToast(R.string.text_notices_permission_current)
                        return
                    }
                    doDelete()
                }
                else -> null
            }
        }
    }

    private fun getTable(useNewest: Boolean){
        if (useNewest){
            TableHelper(this@Notices).getTable(object : TableHelper.Callback{
                override fun onReadStart() {
                    onReadTable()
                }
            })
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
        binding.noticesState.setTextColor(Color.RED)
        setOnActionMode(true, 1)
        binding.noticesState.setText(R.string.text_calendar_doing_delete)
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
                binding.noticesState.setText(R.string.text_calendar_doing_delete)
                binding.noticesState.setTextColor(Color.RED)
                setOnActionMode(true, 0)
            }
            manager.deleteCalendarEvent()
            manager.setPreRemindTime(
                if (binding.noticesPreSwitch.isChecked) preTimeArray[preTimeSet] else 0
            )

            var insertedIndex = 0
            val termDate: Calendar = startDate
            val tableArray: JSONArray = objects.getJSONArray("table")
            val sdfDate = SimpleDateFormat("yyyy/MM/dd", Locale.CHINESE)

            var classCountIndex = 0

            for (week_index in 1 .. 18) {
                for (day_index in 1 .. 7) {
                    termDate.add(Calendar.DAY_OF_YEAR, 1)
                    if (day_index == 7) {
                        continue
                    }
                    var scheduleIndex: Array<Array<String>>
                    val scheduleSet = Calendar.getInstance()
                    scheduleSet.time = termDate.time
                    scheduleSet.set(Calendar.YEAR, 2000)
                    val isWeekend = if (termDate.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY || termDate.get(
                            Calendar.DAY_OF_WEEK
                        ) == Calendar.SATURDAY
                    ) 1 else 0
                    scheduleIndex = if (scheduleSet.before(scheduleSummer[1]) and scheduleSet.after(scheduleSummer[0])) {
                        CalendarManager.CLASS_SUMMER[isWeekend]
                    } else {
                        CalendarManager.CLASS_WINTER[isWeekend]
                    }
                    for (class_index in 0 .. 4) {
                        val classTable = tableArray.getJSONArray(day_index - 1)
                            .getJSONObject(class_index)
                        val classLocationCount = classTable.getJSONArray("data")
                        if (classLocationCount.length() == 0) {
                            continue
                        }
                        for (class_count in 0 until classLocationCount.length()) {
                            classCountIndex++
                            val classData = classLocationCount.getJSONObject(class_count)
                            val classRange = classData.getJSONArray("range")
                            var rangeJudge = false
                            for (indexRange in 0 until classRange.length()){
                                rangeJudge = rangeJudge or (classRange.getInt(indexRange) == week_index)
                            }
                            if (!rangeJudge){
                                continue
                            }
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
                                binding.noticesState.text = stateText
                            }

                            manager.addCalendarEvent(
                                classTime[0],
                                classTime[1],
                                classData.getString("name"),
                                CalendarManager.CLASS_DESCRIPTION[class_count] + " " + classData.getString("room") + " " + classData.getString(
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
            onToast(R.string.permission_denied)
        } else {
            setup.start()
        }
    }

    override fun onBackPressed() {
        if (isInserting){
            onToast(R.string.text_calendar_doing_warning)
        } else {
            finish()
        }
    }

    override fun isActivityAtBottom() = false
}