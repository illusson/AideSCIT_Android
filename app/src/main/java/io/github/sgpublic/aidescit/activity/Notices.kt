package io.github.sgpublic.aidescit.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import io.github.sgpublic.aidescit.Application
import io.github.sgpublic.aidescit.R
import io.github.sgpublic.aidescit.base.BaseActivity
import io.github.sgpublic.aidescit.core.manager.CalendarManager
import io.github.sgpublic.aidescit.core.module.HeaderInfoModule
import io.github.sgpublic.aidescit.core.module.ScheduleModule
import io.github.sgpublic.aidescit.databinding.ActivityNoticesBinding
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class Notices : BaseActivity<ActivityNoticesBinding>(), View.OnClickListener, HeaderInfoModule.Callback {
    companion object{
        private val pre_time_description: Array<CharSequence> = arrayOf(
            "5 分钟", "10 分钟", "15 分钟", "20 分钟"
        )

        @JvmStatic
        fun startActivity(context: Context){
            val intent = Intent().run {
                setClass(context, Notices::class.java)
            }
            context.startActivity(intent)
        }
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

    override fun onActivityCreated(hasSavedInstanceState: Boolean) {
        if (!checkSelfPermission()){
            Application.onToast(this, R.string.text_notices_permission_current)
            ViewBinding.noticesState.text = getText(R.string.text_notices_permission_current)
            ViewBinding.noticesState.setTextColor(Color.RED)
            return
        }

        val start: Calendar = Calendar.getInstance()
        start.set(2000, 4, 0)
        val end: Calendar = Calendar.getInstance()
        end.set(2000, 9, 0)
        scheduleSummer = mutableListOf(start, end)

        setOnActionMode(true, 0)
        Thread {
            if (manager.checkCalendarAccount() <= 0) {
                addCalendarAccount()
            }
            HeaderInfoModule().getSemesterInfo(this)
        }.start()
    }

    private fun checkSelfPermission(): Boolean {
        return XXPermissions.isGranted(this@Notices, Permission.WRITE_CALENDAR, Permission.READ_CALENDAR)
    }

    override fun onSemesterInfoResult(semester: Int, schoolYear: String, week: Int, startDate: Date, scheduleCanInquire: Boolean) {
        this.startDate.time = startDate
        setOnActionMode(false, 0)
    }

    override fun onFailure(code: Int, message: String?, e: Throwable?) {
        Application.onToast(this, R.string.text_load_failed, message, code)
        setOnActionMode(false, 0)
    }

    private fun addCalendarAccount(){
        if (manager.addCalendarAccount() != -1L) {
            return
        }
        runOnUiThread {
            ViewBinding.noticesState.setText(R.string.text_load_failed)
            ViewBinding.noticesInsert.isEnabled = false
            ViewBinding.noticesDelete.isEnabled = false
            Application.onToast(this, R.string.text_calendar_setup_failure)
        }
    }

    private fun setOnActionMode(is_doing: Boolean, click_index: Int) {
        isInserting = is_doing
        runOnUiThread {
            val alpha: Float = if (is_doing) 0.3F else 1.0F

            val noticesClick: ProgressBar = if (click_index == 0) {
                ViewBinding.noticesInsertDoing
            } else {
                ViewBinding.noticesDeleteDoing
            }
            noticesClick.visibility = if (is_doing) View.VISIBLE else View.INVISIBLE

            ViewBinding.noticesInsert.isClickable = !is_doing
            ViewBinding.noticesInsert.alpha = alpha
            ViewBinding.noticesDelete.isClickable = !is_doing
            ViewBinding.noticesDelete.alpha = alpha
            ViewBinding.noticesPreBase.isClickable = !is_doing
            ViewBinding.noticesPreBase.isClickable = !is_doing
            ViewBinding.noticesPreBase.alpha = alpha
            ViewBinding.noticesPreTimeBase.isClickable = !is_doing
            ViewBinding.noticesPreTimeBase.alpha = alpha
            ViewBinding.noticesNewestBase.isClickable = !is_doing
            ViewBinding.noticesNewestSwitch.isClickable = !is_doing
            ViewBinding.noticesNewestBase.alpha = alpha

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
            ViewBinding.noticesInsert.setText(R.string.text_calendar_insert_new)
            ViewBinding.noticesDelete.isEnabled = false
            ViewBinding.noticesDelete.alpha = 0.3F
        } else {
            stateText = java.lang.String.format(
                getString(R.string.text_calendar_count),
                insertCount
            )
            stateColor = Color.GREEN
            ViewBinding.noticesInsert.setText(R.string.text_calendar_insert_re)
            ViewBinding.noticesDelete.isEnabled = true
            ViewBinding.noticesDelete.alpha = 1.0F
        }
        ViewBinding.noticesState.text = stateText
        ViewBinding.noticesState.setTextColor(stateColor)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewSetup() {
        ViewBinding.noticesNewestBase.setOnClickListener(this)
        ViewBinding.noticesPreBase.setOnClickListener(this)
        ViewBinding.noticesPreTimeBase.setOnClickListener(this)
        ViewBinding.noticesInsert.setOnClickListener(this)
        ViewBinding.noticesPreSwitch.setOnCheckedChangeListener { _, isChecked ->
            ViewBinding.noticesPreTimeBase.alpha = if (isChecked) 1.0F else 0.3F
            ViewBinding.noticesPreTimeBase.isClickable = isChecked
        }
        ViewBinding.noticesDelete.setOnClickListener(this)
        ViewBinding.noticesBack.setOnClickListener(this)
        ViewBinding.noticesPreTime.text = "15"
        initViewAtBottom(ViewBinding.noticesDeleteBase)
        initViewAtTop(ViewBinding.noticesToolbar)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.notices_back -> { onBackPressed() }
            R.id.notices_newest_base -> {
                ViewBinding.noticesNewestSwitch.isChecked = !ViewBinding.noticesNewestSwitch.isChecked
            }
            R.id.notices_pre_base -> {
                ViewBinding.noticesPreSwitch.isChecked = !ViewBinding.noticesPreSwitch.isChecked
            }
            R.id.notices_pre_time_base -> {
                val preTimeSetDoing = preTimeSet
                AlertDialog.Builder(this@Notices).run {
                    setTitle(R.string.title_calendar_pre_time_choose)
                    setSingleChoiceItems(
                        pre_time_description, preTimeSet
                    ) { _, which -> preTimeSet = which }
                    setPositiveButton(R.string.text_ok){ _, _ ->
                        ViewBinding.noticesPreTime.text = preTimeArray[preTimeSet].toString()
                    }
                    setNegativeButton(R.string.text_cancel){ _, _ ->
                        preTimeSet = preTimeSetDoing
                    }
                }.show()
            }
            R.id.notices_insert -> {
                if (!checkSelfPermission()){
                    Application.onToast(this, R.string.text_notices_permission_current)
                    return
                }
                if (manager.checkCalendarAccount() <= 0) {
                    addCalendarAccount()
                }
                if (manager.checkCalendarAccount() > 0) {
                    getTable(ViewBinding.noticesNewestSwitch.isChecked)
                }
            }
            R.id.notices_delete -> {
                if (!checkSelfPermission()){
                    Application.onToast(this, R.string.text_notices_permission_current)
                    return
                }
                doDelete()
            }
        }
    }

    private fun getTable(useNewest: Boolean){
        if (useNewest){
            ScheduleModule().getSchedule(object : ScheduleModule.Callback {
                override fun onReadStart(isSundayEmpty: Boolean) {
                    onReadTable()
                }
            })
        } else {
            onReadTable()
        }
    }

    private fun onReadTable() {
        val objects: JSONObject? = io.github.sgpublic.aidescit.core.manager.CacheManager.CACHE_SCHEDULE
        doInsert(objects!!)
    }

    private fun doDelete(){
        ViewBinding.noticesState.setTextColor(Color.RED)
        setOnActionMode(true, 1)
        ViewBinding.noticesState.setText(R.string.text_calendar_doing_delete)
        Thread {
            manager.deleteCalendarEvent()
            runOnUiThread{
                setOnActionMode(false, 1)
            }
        }.start()
    }

    private fun doInsert(objects: JSONObject){
        Thread {
            runOnUiThread {
                ViewBinding.noticesState.setText(R.string.text_calendar_doing_delete)
                ViewBinding.noticesState.setTextColor(Color.RED)
                setOnActionMode(true, 0)
            }
            manager.deleteCalendarEvent()
            manager.setPreRemindTime(
                if (ViewBinding.noticesPreSwitch.isChecked) preTimeArray[preTimeSet] else 0
            )

            var insertedIndex = 0
            val termDate: Calendar = startDate
            val scheduleObj: JSONObject = objects.getJSONObject("schedule")
            val sdfDate = SimpleDateFormat("yyyy/MM/dd", Locale.CHINESE)

            var classCountIndex = 0

            for (weekIndex in 1 .. 18) {
                ScheduleModule.DAY_INDEX.forEach { day ->
                    termDate.add(Calendar.DAY_OF_YEAR, 1)

                    val scheduleSet = Calendar.getInstance()
                    scheduleSet.time = termDate.time
                    scheduleSet.set(Calendar.YEAR, 2000)
                    val isWeekend = if (termDate.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
                        || termDate.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) 1 else 0
                    val scheduleIndex: Array<Pair<String, String>> = if (
                        scheduleSet.before(scheduleSummer[1]) && scheduleSet.after(scheduleSummer[0])
                    ) {
                        CalendarManager.CLASS_SUMMER[isWeekend]
                    } else {
                        CalendarManager.CLASS_WINTER[isWeekend]
                    }
                    ScheduleModule.CLASS_INDEX.forEach classFor@{ clazz ->
                        val classLocationCount = try {
                            scheduleObj.getJSONObject(day).getJSONArray(clazz)
                        } catch (e: JSONException) {
                            return@classFor
                        }
                        for (classCount in 0 until classLocationCount.length()) {
                            classCountIndex++
                            val classData = classLocationCount.getJSONObject(classCount)
                            val classRange = classData.getJSONArray("range")
                            var rangeJudge = false
                            for (indexRange in 0 until classRange.length()){
                                if (classRange.getInt(indexRange) == weekIndex){
                                    rangeJudge = true
                                    continue
                                }
                            }
                            if (!rangeJudge){
                                continue
                            }
                            val classIndex = ScheduleModule.CLASS_INDEX.indexOf(clazz)
                            val classTime = arrayOf(
                                sdfDate.format(termDate.time) + scheduleIndex[classIndex].first,
                                sdfDate.format(termDate.time) + scheduleIndex[classIndex].second
                            )
                            insertedIndex ++
                            runOnUiThread {
                                val stateText = String.format(
                                    getString(R.string.text_calendar_doing),
                                    insertedIndex
                                )
                                ViewBinding.noticesState.text = stateText
                            }

                            manager.addCalendarEvent(
                                classTime[0],
                                classTime[1],
                                classData.getString("name"),
                                CalendarManager.CLASS_DESCRIPTION[classCount] + " " + classData.getString("room") + " " + classData.getString(
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
            }
        }.start()
    }

    override fun onBackPressed() {
        if (isInserting){
            Application.onToast(this, R.string.text_calendar_doing_warning)
        } else {
            finish()
        }
    }

    override fun onCreateViewBinding(): ActivityNoticesBinding =
        ActivityNoticesBinding.inflate(layoutInflater)
}