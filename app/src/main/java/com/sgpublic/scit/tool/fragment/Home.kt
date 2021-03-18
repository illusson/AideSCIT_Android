package com.sgpublic.scit.tool.fragment

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.sgpublic.scit.tool.R
import com.sgpublic.scit.tool.activity.WebView
import com.sgpublic.scit.tool.base.BaseFragment
import com.sgpublic.scit.tool.data.BannerItem
import com.sgpublic.scit.tool.data.NewsData
import com.sgpublic.scit.tool.helper.HeaderInfoHelper
import com.sgpublic.scit.tool.helper.NewsHelper
import com.sgpublic.scit.tool.helper.TableHelper
import com.sgpublic.scit.tool.manager.CacheManager
import com.sgpublic.scit.tool.manager.CalendarManager
import com.sgpublic.scit.tool.manager.ConfigManager
import com.sgpublic.scit.tool.ui.NewsBannerAdapter
import com.zhpan.bannerview.BannerViewPager
import com.zhpan.bannerview.constants.IndicatorGravity
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.item_home_task.view.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class Home(contest: AppCompatActivity) : BaseFragment(contest), NewsHelper.Callback {
    private var homeBanner: BannerViewPager<BannerItem, NewsBannerAdapter>? = null
    private var timeChangeReceiver: TimeChangeReceiver? = null

    private var week: Int = 0

    private var taskBase1 = home_task_1
    private var taskBase2 = home_task_2
    private lateinit var startDate: Date

    private var callbackTable = object : TableHelper.Callback {
        override fun onFailure(code: Int, message: String?, e: Exception?) {
            super.onFailure(code, message, e)
            saveExplosion(e, code)
            onToast(R.string.text_load_failed, message, code)
        }

        override fun onReadFinish(isEmpty: Boolean) {
            super.onReadFinish(isEmpty)
            HeaderInfoHelper(contest).getSemesterInfo(callbackDate)
        }
    }

    private val callbackDate = object : HeaderInfoHelper.Callback{
        override fun onFailure(code: Int, message: String?, e: Exception?) {
            super.onFailure(code, message, e)
            saveExplosion(e, code)
            onToast(R.string.text_load_failed, message, code)
        }

        override fun onSemesterInfoResult(semester: Int, schoolYear: String, week: Int,
            startDate: Date) {
            this@Home.startDate = startDate

            onTaskLoad()

            if (timeChangeReceiver == null){
                val intentFilter = IntentFilter()
                intentFilter.addAction("android.intent.action.TIME_TICK")
                timeChangeReceiver = TimeChangeReceiver()
                contest.registerReceiver(timeChangeReceiver, intentFilter)
            }
        }
    }

    override fun getContentView(): Int = R.layout.fragment_home

    @SuppressLint("ClickableViewAccessibility")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        NewsHelper(contest).getHeadline(this)
        week = ConfigManager(contest).getInt("week")
        getTable(CacheManager(contest).read(CacheManager.CACHE_TABLE))
    }

    private fun getTable(objects: JSONObject? = null){
        if (objects == null){
            TableHelper(contest).getTable(ConfigManager(contest), week, callbackTable)
        } else {
            callbackTable.onReadFinish(false)
        }
    }

    override fun onViewSetup() {
        super.onViewSetup()
        initViewAtTop(home_hello)

        val date: Int = HeaderInfoHelper(contest).getDate()
        val time: Int = HeaderInfoHelper(contest).getTime()

        if (ConfigManager(contest).getInt("week") == 0) {
            home_hello.text = java.lang.String.format(
                this.getString(R.string.text_hello_holiday),
                this.getString(time), this.getString(date)
            )
        } else {
            home_hello.text = java.lang.String.format(
                this.getString(R.string.text_hello),
                this.getString(time),
                ConfigManager(contest).getInt("week").toString(),
                this.getString(date)
            )
        }

        home_content.text = ConfigManager(contest)
            .getString("sentence", "祝你一天好心情哦~")

        home_from.text = ConfigManager(contest)
            .getString("from")

        homeBanner = findViewById<BannerViewPager<BannerItem, NewsBannerAdapter>>(R.id.home_banner)

        home_refresh.setOnRefreshListener {
            NewsHelper(contest).getHeadline(this)
        }
    }

    override fun onNewsResult(news: ArrayList<NewsData>, hasNext: Boolean) {
        val banners: ArrayList<BannerItem> = arrayListOf()
        for (item in news){
            val image: String = if (item.images.size == 0){ "" } else { item.images[0] }
            banners.add(BannerItem(contest, item.title, item.type, item.id, image))
        }
        if (homeBanner == null){
            return
        }
        homeBanner!!.setIndicatorVisibility(View.VISIBLE)
        homeBanner!!.setIndicatorGravity(IndicatorGravity.CENTER)
        homeBanner!!.setOnPageClickListener { i: Int ->
            WebView.startActivity(contest, banners[i].tid, banners[i].nid)
        }
        homeBanner!!.setHolderCreator { NewsBannerAdapter() }
        runOnUiThread {
            homeBanner!!.create(banners)
            homeBanner!!.visibility = View.VISIBLE
            home_refresh.isRefreshing = false
        }
    }
    
    private fun onTaskLoad(){
        if (taskBase1 == home_task_1){
            taskBase1 = home_task_2
            taskBase2 = home_task_1
        } else {
            taskBase1 = home_task_1
            taskBase2 = home_task_2
        }
        runOnUiThread {
            taskBase1.removeAllViews()
        }
        var termDate = Calendar.getInstance()
        termDate.time = startDate
        val time2 = termDate.timeInMillis
        val objects: JSONObject = CacheManager(contest)
            .read(CacheManager.CACHE_TABLE) ?: return
        val tableArray = objects.getJSONArray("table")
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
            var scheduleIndex: Array<Array<String>>
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
            for (class_index in 0 .. 4) {
                val classTable = tableArray.getJSONArray(termDate.get(Calendar.DAY_OF_WEEK) - 2)
                    .getJSONObject(class_index)
                val classLocationCount = classTable.getJSONArray("data")
                if (classLocationCount.length() == 0) {
                    continue
                }
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
                    val dtStart = scheduleIndex[class_index][0]
                    val dtEnd = scheduleIndex[class_index][1]

                    var time = Calendar.getInstance()
                    val startTime = (sdfDate.format(termDate.time) + dtStart).split("/").toTypedArray()
                    time[Calendar.YEAR] = startTime[0].toInt()
                    time[Calendar.MONTH] = startTime[1].toInt() - 1
                    time[Calendar.DAY_OF_MONTH] = startTime[2].toInt()
                    time[Calendar.HOUR_OF_DAY] = startTime[3].toInt()
                    time[Calendar.MINUTE] = startTime[4].toInt()
                    time[Calendar.SECOND] = 0
                    time[Calendar.MILLISECOND] = 0
                    val classStart = time

                    val endTime = (sdfDate.format(termDate.time) + dtEnd).split("/").toTypedArray()
                    time[Calendar.YEAR] = endTime[0].toInt()
                    time[Calendar.MONTH] = endTime[1].toInt() - 1
                    time[Calendar.DAY_OF_MONTH] = endTime[2].toInt()
                    time[Calendar.HOUR_OF_DAY] = endTime[3].toInt()
                    time[Calendar.MINUTE] = endTime[4].toInt()
                    time[Calendar.SECOND] = 0
                    time[Calendar.MILLISECOND] = 0
                    val classEnd = time

                    val item = LayoutInflater.from(contest).inflate(R.layout.item_home_task, taskBase1, false)
                    time = Calendar.getInstance()

                    item.item_task_start.text = scheduleIndex[class_index][0].replace("/", "：").subSequence(1, 6)
                    item.item_task_end.text = scheduleIndex[class_index][1].replace("/", "：").subSequence(1, 6)
                    item.item_task_title.text = classData.getString("name")
                    item.item_task_location.text = classData.getString("room")
                    if (time.before(classEnd) && time.after(classStart)){
                        item.item_task_base.setCardBackgroundColor(contest.getColor(R.color.color_task_doing))
                    } else if (time.after(classEnd)){
                        item.item_task_base.setCardBackgroundColor(contest.getColor(R.color.color_task_waiting))
                        item.item_task_base.alpha = 0.3F
                    } else if (time.before(classStart) && classStart.timeInMillis - time.timeInMillis <= 600000L) {
                        item.item_task_base.setCardBackgroundColor(contest.getColor(R.color.color_task_right_now))
                        item.item_task_location.text = String.format(getString(R.string.text_home_task_right_now),
                            (classStart.timeInMillis - time.timeInMillis) / 60 / 1000, item.item_task_location.text)
                    } else {
                        item.item_task_base.setCardBackgroundColor(contest.getColor(R.color.color_task_waiting))
                    }

                    runOnUiThread {
                        if (!hasClass){
                            hasClass = true
                            val header = TextView(contest)
                            header.text = String.format(getString(R.string.text_home_task_header),
                                classStart.get(Calendar.YEAR), classStart.get(Calendar.MONTH) + 1, classStart.get(Calendar.DAY_OF_MONTH),
                                getString(HeaderInfoHelper(contest).getDate(classStart)))
                            header.setPadding(dip2px(30F), dip2px(7F), dip2px(30F), dip2px(7F))
                            header.setTextColor(contest.getColor(R.color.color_font_normal))
                            header.setTypeface(null, Typeface.BOLD)
                            header.textSize = 17F
                            taskBase1.addView(header)
                        }
                        taskBase1.addView(item)
                    }
                }
            }
            termDate.add(Calendar.DAY_OF_YEAR, 1)
        }

        runOnUiThread {
            taskBase1.visibility = View.VISIBLE
            taskBase2.visibility = View.GONE
        }
    }

    override fun onPause() {
        super.onPause()
        homeBanner?.stopLoop()
    }

    override fun onResume() {
        super.onResume()
        homeBanner?.startLoop()
    }

    override fun onDestroy() {
        super.onDestroy()
        contest.unregisterReceiver(timeChangeReceiver)
    }

    inner class TimeChangeReceiver : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            onTaskLoad()
        }
    }
}