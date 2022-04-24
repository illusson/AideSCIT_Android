package io.github.sgpublic.aidescit.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.AppBarLayout
import com.zhpan.bannerview.constants.IndicatorGravity
import io.github.sgpublic.aidescit.Application
import io.github.sgpublic.aidescit.R
import io.github.sgpublic.aidescit.activity.WebView
import io.github.sgpublic.aidescit.base.BaseFragment
import io.github.sgpublic.aidescit.core.manager.CacheManager
import io.github.sgpublic.aidescit.core.manager.CalendarManager
import io.github.sgpublic.aidescit.core.manager.ConfigManager
import io.github.sgpublic.aidescit.core.module.HeaderInfoModule
import io.github.sgpublic.aidescit.core.module.NewsModule
import io.github.sgpublic.aidescit.core.module.ScheduleModule
import io.github.sgpublic.aidescit.core.util.dp
import io.github.sgpublic.aidescit.databinding.FragmentHomeBinding
import io.github.sgpublic.aidescit.databinding.ItemHomeTaskBinding
import io.github.sgpublic.aidescit.databinding.PagerNewsBinding
import io.github.sgpublic.aidescit.ui.AppBarStateChangeListener
import io.github.sgpublic.aidescit.ui.NewsBannerAdapter
import io.github.sgpublic.aidescit.ui.NewsPagerAdapter
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class Home(context: AppCompatActivity) : BaseFragment<FragmentHomeBinding>(context), NewsModule.Callback {
    private var timeChangeReceiver: TimeChangeReceiver? = null

    private var week: Int = 0

    private var taskBase1: LinearLayout? = null
    private var taskBase2: LinearLayout? = null
    private lateinit var startDate: Date

    private var callbackTable = object : ScheduleModule.Callback {
        override fun onFailure(code: Int, message: String?, e: Throwable?) {
            Application.onToast(context, R.string.text_load_failed, message, code)
        }

        override fun onReadFinish(isEmpty: Boolean) {
            HeaderInfoModule().getSemesterInfo(callbackDate)
        }
    }

    private val callbackDate = object : HeaderInfoModule.Callback{
        override fun onFailure(code: Int, message: String?, e: Throwable?) {
            Application.onToast(context, R.string.text_load_failed, message, code)
        }

        override fun onSemesterInfoResult(semester: Int, schoolYear: String, week: Int,
            startDate: Date, scheduleCanInquire: Boolean) {
            this@Home.startDate = startDate

            onTaskLoad()

            if (timeChangeReceiver == null){
                val intentFilter = IntentFilter()
                intentFilter.addAction("android.intent.action.TIME_TICK")
                timeChangeReceiver = TimeChangeReceiver()
                context.registerReceiver(timeChangeReceiver, intentFilter)
            }
        }
    }

    override fun onFragmentCreated(hasSavedInstanceState: Boolean) {
        NewsModule().getHeadline(this)
        week = ConfigManager.WEEK
        if (week == 0){
            ViewBinding.homeTitle.setText(R.string.title_news)
            ViewBinding.honeNews.visibility = View.VISIBLE
            NewsModule().getNewsType(this)
        } else {
            ViewBinding.homeTitle.setText(R.string.title_schedule)
            ViewBinding.homeTask.visibility = View.VISIBLE
            getSchedule(CacheManager.CACHE_SCHEDULE)
        }
    }

    override fun onFailure(code: Int, message: String?, e: Throwable?) {
        Application.onToast(context, R.string.text_load_failed, message, code)
    }

    override fun onTypeResult(types: Map<Int, String>) {
        super.onTypeResult(types)
        val list: ArrayList<BaseFragment<PagerNewsBinding>> = ArrayList()
        types.forEach { (t, u) ->
            list.add(NewsPage(context, u, t))
        }
        runOnUiThread {
            ViewBinding.honeNewsPager.adapter = NewsPagerAdapter(context.supportFragmentManager, list)
            ViewBinding.homeNewsTab.setViewPager(ViewBinding.honeNewsPager)
        }
    }

    private fun getSchedule(objects: JSONObject? = null){
        if (objects == null){
            ScheduleModule().getSchedule(week, callbackTable)
        } else {
            callbackTable.onReadFinish(false)
        }
    }

    override fun onViewSetup() {
        context.setSupportActionBar(ViewBinding.homeToolbar)
        context.supportActionBar?.title = ""

        taskBase1 = ViewBinding.homeTask1
        taskBase2 = ViewBinding.homeTask2
//        ViewBinding.homeRefresh.setProgressViewOffset(
//            false, dip2px(-40F), dip2px(60F)
//        )

        val date: Int = HeaderInfoModule().getDate()
        val time: Int = HeaderInfoModule().getTime()

        if (ConfigManager.WEEK == 0) {
//            ViewBinding.homeRefresh.isEnabled = false
            ViewBinding.homeHello.text = String.format(
                this.getString(R.string.text_hello_holiday),
                this.getString(time), this.getString(date)
            )
        } else {
//            ViewBinding.homeRefresh.setOnRefreshListener {
//                ScheduleHelper(context).getSchedule(week, callbackTable)
//            }
            ViewBinding.homeHello.text =
                getString(R.string.text_hello, getString(time), ConfigManager.WEEK, getString(date))
        }

        ViewBinding.homeContent.text = ConfigManager.HITOKOTO_SENTENCE

        ViewBinding.homeFrom.text = ConfigManager.HITOKOTO_FROM

        ViewBinding.homeAppbar.addOnOffsetChangedListener(object : AppBarStateChangeListener() {
            override fun onStateChanged(appBarLayout: AppBarLayout, state: State) {
                this@Home.startAnimate(state == State.COLLAPSED, 200, ViewBinding.homeTitle)
            }
        })
    }

    override fun onNewsResult(news: ArrayList<io.github.sgpublic.aidescit.core.data.NewsData>, hasNext: Boolean) {
        val banners: ArrayList<io.github.sgpublic.aidescit.core.data.BannerItem> = arrayListOf()
        for (item in news){
            val image: String = if (item.images.size == 0){ "" } else { item.images[0] }
            banners.add(
                io.github.sgpublic.aidescit.core.data.BannerItem(
                    context,
                    item.title,
                    item.type,
                    item.id,
                    image
                )
            )
        }
        ViewBinding.homeBanner.setIndicatorVisibility(View.VISIBLE)
        ViewBinding.homeBanner.setIndicatorGravity(IndicatorGravity.CENTER)
        ViewBinding.homeBanner.setOnPageClickListener { i: Int ->
            WebView.startActivity(context, banners[i].tid, banners[i].nid)
        }
        ViewBinding.homeBanner.setHolderCreator { NewsBannerAdapter() }
        runOnUiThread {
            ViewBinding.homeBanner.create(banners)
            ViewBinding.homeBanner.visibility = View.VISIBLE
//            ViewBinding.homeRefresh.isRefreshing = false
        }
    }
    
    private fun onTaskLoad(){
        if (taskBase1 == null || taskBase2 == null){
            return
        }
        if (taskBase1 == ViewBinding.homeTask1){
            taskBase1 = ViewBinding.homeTask2
            taskBase2 = ViewBinding.homeTask1
        } else {
            taskBase1 = ViewBinding.homeTask1
            taskBase2 = ViewBinding.homeTask2
        }
        runOnUiThread {
            taskBase1?.removeAllViews()
        }
        var termDate = Calendar.getInstance()
        termDate.time = startDate
        val time2 = termDate.timeInMillis
        val objects: JSONObject = CacheManager.CACHE_SCHEDULE ?: return
        val scheduleObj = objects.getJSONObject("schedule")
        val start: Calendar = Calendar.getInstance()
        start.set(2000, 4, 0)
        val end: Calendar = Calendar.getInstance()
        end.set(2000, 9, 0)
        termDate = Calendar.getInstance()
        val sdfDate = SimpleDateFormat("yyyy/MM/dd", Locale.CHINESE)
        for (i in 0 until 5){
            if (termDate.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY){
                termDate.add(Calendar.DAY_OF_YEAR, 1)
                continue
            }
            val betweenDays = ((termDate.timeInMillis-time2) / (1000*3600*24)).toInt()
            var scheduleIndex: Array<Pair<String, String>>
            val scheduleSet = Calendar.getInstance()
            scheduleSet.time = termDate.time
            scheduleSet.set(Calendar.YEAR, 2000)
            val isWeekend = if (termDate.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY || termDate.get(
                    Calendar.DAY_OF_WEEK
                ) == Calendar.SATURDAY
            ) 1 else 0
            scheduleIndex = if (scheduleSet.before(end) and scheduleSet.after(start)) {
                CalendarManager.CLASS_SUMMER[isWeekend]
            } else {
                CalendarManager.CLASS_WINTER[isWeekend]
            }
            var hasClass = false
            for (classIndex in 0 .. 4) {
                if (scheduleObj.isNull(ScheduleModule.DAY_INDEX[termDate.get(Calendar.DAY_OF_WEEK) - 2])){
                    continue
                }
                val classTable = scheduleObj.getJSONObject(
                    ScheduleModule.DAY_INDEX[termDate.get(Calendar.DAY_OF_WEEK) - 2]
                )
                if (classTable.isNull(ScheduleModule.CLASS_INDEX[classIndex])){
                    continue
                }
                val classLocationCount = classTable.getJSONArray(
                    ScheduleModule.CLASS_INDEX[classIndex]
                )
                for (class_count in 0 until classLocationCount.length()) {
                    val classData = classLocationCount.getJSONObject(class_count)
                    val classRange = classData.getJSONArray("range")
                    var rangeJudge = false
                    for (indexRange in 0 until classRange.length()){
                        rangeJudge = rangeJudge or (classRange.getInt(indexRange) == betweenDays / 7 + 1)
                    }
                    if (!rangeJudge){
                        continue
                    }
                    val dtStart = scheduleIndex[classIndex].first
                    val classStart = Calendar.getInstance()
                    val startTime = (sdfDate.format(termDate.time) + dtStart).split("/").toTypedArray()
                    classStart[Calendar.YEAR] = startTime[0].toInt()
                    classStart[Calendar.MONTH] = startTime[1].toInt() - 1
                    classStart[Calendar.DAY_OF_MONTH] = startTime[2].toInt()
                    classStart[Calendar.HOUR_OF_DAY] = startTime[3].toInt()
                    classStart[Calendar.MINUTE] = startTime[4].toInt()
                    classStart[Calendar.SECOND] = 0
                    classStart[Calendar.MILLISECOND] = 0

                    val dtEnd = scheduleIndex[classIndex].second
                    val classEnd = Calendar.getInstance()
                    val endTime = (sdfDate.format(termDate.time) + dtEnd).split("/").toTypedArray()
                    classEnd[Calendar.YEAR] = endTime[0].toInt()
                    classEnd[Calendar.MONTH] = endTime[1].toInt() - 1
                    classEnd[Calendar.DAY_OF_MONTH] = endTime[2].toInt()
                    classEnd[Calendar.HOUR_OF_DAY] = endTime[3].toInt()
                    classEnd[Calendar.MINUTE] = endTime[4].toInt()
                    classEnd[Calendar.SECOND] = 0
                    classEnd[Calendar.MILLISECOND] = 0

                    val time = Calendar.getInstance()

                    val item = ItemHomeTaskBinding.inflate(layoutInflater)
                    item.itemTaskStart.text = dtStart.replace("/", "：").subSequence(1, 6)
                    item.itemTaskEnd.text = dtEnd.replace("/", "：").subSequence(1, 6)
                    item.itemTaskTitle.text = classData.getString("name")
                    item.itemTaskLocation.text = classData.getString("room")
                    if (time.before(classEnd) && time.after(classStart)){
                        item.itemTaskBase.setCardBackgroundColor(context.getColor(R.color.color_task_doing))
                        item.itemTaskLocation.text = String.format(getString(
                            R.string.text_home_task_doing
                        ), item.itemTaskLocation.text)
                    } else if (time.after(classEnd)){
                        item.itemTaskBase.setCardBackgroundColor(context.getColor(R.color.color_task_waiting))
                        item.itemTaskBase.alpha = 0.3F
                    } else if (time.before(classStart) && classStart.timeInMillis - time.timeInMillis <= 600000L) {
                        item.itemTaskBase.setCardBackgroundColor(context.getColor(R.color.color_task_right_now))
                        item.itemTaskLocation.text = String.format(getString(R.string.text_home_task_right_now),
                            (classStart.timeInMillis - time.timeInMillis) / 60 / 1000, item.itemTaskLocation.text)
                    } else {
                        item.itemTaskBase.setCardBackgroundColor(context.getColor(R.color.color_task_waiting))
                    }

                    runOnUiThread {
                        if (!hasClass){
                            hasClass = true
                            val header = TextView(context)
                            header.text = String.format(getString(R.string.text_home_task_header),
                                classStart.get(Calendar.YEAR), classStart.get(Calendar.MONTH) + 1, classStart.get(Calendar.DAY_OF_MONTH),
                                getString(HeaderInfoModule().getDate(classStart)))
                            header.setPadding(30F.dp, 7.dp, 30.dp, 7.dp)
                            header.setTextColor(context.getColor(R.color.color_font_normal))
                            header.setTypeface(null, Typeface.BOLD)
                            header.textSize = 17F
                            taskBase1?.addView(header)
                        }
                        taskBase1?.addView(item.root)
                    }
                }
            }
            termDate.add(Calendar.DAY_OF_YEAR, 1)
        }

        runOnUiThread {
            taskBase1?.visibility = View.VISIBLE
            taskBase2?.visibility = View.GONE
        }
    }

    override fun onPause() {
        super.onPause()
        ViewBinding.homeBanner.stopLoop()
    }

    override fun onResume() {
        super.onResume()
        ViewBinding.homeBanner.startLoop()
    }

    override fun onDestroy() {
        super.onDestroy()
        timeChangeReceiver?.let {
            context.unregisterReceiver(it)
        }
    }

    override fun onCreateViewBinding(container: ViewGroup?): FragmentHomeBinding =
        FragmentHomeBinding.inflate(layoutInflater)

    inner class TimeChangeReceiver : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            onTaskLoad()
        }
    }
}