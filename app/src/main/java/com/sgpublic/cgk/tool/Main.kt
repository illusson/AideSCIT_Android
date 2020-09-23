package com.sgpublic.cgk.tool

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Typeface
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import androidx.appcompat.app.AlertDialog
import com.sgpublic.cgk.tool.base.ActivityCollector
import com.sgpublic.cgk.tool.base.BaseActivity
import com.sgpublic.cgk.tool.data.TableData
import com.sgpublic.cgk.tool.helper.HeaderInfoHelper
import com.sgpublic.cgk.tool.helper.TableHelper
import com.sgpublic.cgk.tool.manager.CacheManager
import com.sgpublic.cgk.tool.manager.ConfigManager
import com.umeng.analytics.MobclickAgent
import com.umeng.commonsdk.UMConfigure
import com.umeng.message.IUmengRegisterCallback
import com.umeng.message.PushAgent
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_mine.*
import kotlinx.android.synthetic.main.fragment_timetable.*
import kotlinx.android.synthetic.main.item_timetable.view.*
import org.android.agoo.mezu.MeizuRegister
import org.android.agoo.xiaomi.MiPushRegistar
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

class Main : BaseActivity(), TableHelper.Callback {
    private var viewNowIndex = 0

    private var viewWidth: Int = 0
    private var viewHeight: Int = 0
    private var showTable: Boolean = false

    override fun onActivityCreate(savedInstanceState: Bundle?) {
        setViewState(1)

        initShortsCut()

        if (BuildConfig.DEBUG){
            onToast(this@Main, "debug")
        }

        val objects: JSONObject? = CacheManager(this@Main).read(CacheManager.CACHE_TABLE)
        getTable(objects)
    }

    override fun onViewSetup() {
        super.onViewSetup()
        val date: Int = HeaderInfoHelper(this@Main).getDate()
        val time: Int = HeaderInfoHelper(this@Main).getTime()

        viewWidth = (resources.displayMetrics.widthPixels - dip2px(this@Main, 40F)) / 6
        viewHeight = dip2px(this@Main, 110F)

        if (ConfigManager(this@Main).getInt("week") == 0) {
            timetable_hello.text = java.lang.String.format(
                this.getString(R.string.text_hello_holiday),
                this.getString(time), this.getString(date)
            )
        } else {
            timetable_hello.text = java.lang.String.format(
                this.getString(R.string.text_hello),
                this.getString(time),
                ConfigManager(this@Main).getInt("week").toString(),
                this.getString(date)
            )
        }

        mine_username.text = ConfigManager(this@Main).getString("name", "此人没有留下姓名……")

        timetable_content.text = ConfigManager(this@Main)
            .getString("sentence", "祝你一天好心情哦~")

        timetable_from.text = ConfigManager(this@Main)
            .getString("from")

        nav_view.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_timetable -> setViewState(1)
                R.id.nav_mine -> setViewState(2)
            }
            true
        }

        mine_about.setOnClickListener {
            val intent = Intent(this@Main, About::class.java)
            intent.putExtra("session", session)
            startActivity(intent)
        }

        mine_calendar.setOnClickListener {
            val intent = Intent(this@Main, Notices::class.java)
            intent.putExtra("session", session)
            startActivity(intent)
        }

        mine_exam.setOnClickListener {
            val intent = Intent(this@Main, Exam::class.java)
            intent.putExtra("session", session)
            startActivity(intent)
        }

        mine_logout.setOnClickListener {
            val alert = AlertDialog.Builder(this@Main)
            alert.setTitle(R.string.title_check_logout)
            alert.setMessage(R.string.text_check_logout)
            alert.setPositiveButton(R.string.text_ok) { _, _ ->
                ConfigManager(this@Main)
                    .putBoolean("is_login", false)
                    .apply()
                deleteShortCut()

                CacheManager(this@Main)
                    .save(CacheManager.CACHE_ACHIEVEMENT, "")
                    .save(CacheManager.CACHE_EXAM, "")
                    .save(CacheManager.CACHE_TABLE, "")

                Login.startActivity(this@Main, true)
                finish()
            }
            alert.setNegativeButton(R.string.text_cancel, null)
            alert.show()
        }

        mine_achievement.setOnClickListener {
            val intent = Intent(this@Main, Achievement::class.java)
            intent.putExtra("session", session)
            startActivity(intent)
        }

        mine_room.setOnClickListener {
            onToast(this@Main, R.string.text_coming)
        }

        timetable_refresh.setOnRefreshListener { getTable() }
        timetable_refresh.setColorSchemeResources(R.color.colorAlert)
    }

    private fun getTable(objects: JSONObject? = null){
        timetable_refresh.isRefreshing = true
        if (objects != null){
            TableHelper(this@Main)
                .parsing(objects, ConfigManager(this@Main).getInt("week"), this)
        } else {
            session?.let {
                TableHelper(this@Main)
                    .getTable(ConfigManager(this@Main), it, this)
            }
        }
    }

    override fun onReadStart() {
        showTable = false
        runOnUiThread{
            timetable_grid.visibility = View.INVISIBLE
            timetable_grid_morning.removeAllViews()
            timetable_grid_noon.removeAllViews()
            timetable_grid_evening.removeAllViews()
        }
    }

    override fun onFailure(code: Int, message: String?, e: Exception?) {
        saveExplosion(e, code)
        onToast(this@Main, R.string.text_load_failed, message, code)
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
            itemTimetable = LayoutInflater.from(this@Main).inflate(R.layout.item_timetable, parent, false)
            itemTimetable.apply {
                lesson_name.text = data.name
                lesson_name.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                lesson_location.text = data.room
                lesson_teacher.text = data.teacher
            }

            itemTimetable.setOnClickListener{}
        } else {
            itemTimetable = View(this@Main)
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
        runOnUiThread{
            timetable_refresh.isRefreshing = false
            if (isEmpty) {
                timetable_grid.visibility = View.GONE
                timetable_empty.visibility = View.VISIBLE
            } else if (showTable) {
                timetable_grid.visibility = View.VISIBLE
                timetable_empty.visibility = View.GONE
            }
        }
    }

    private fun initShortsCut() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            var info: ShortcutInfo
            val mSystemService = getSystemService(ShortcutManager::class.java)
            val dynamicShortcuts: MutableList<ShortcutInfo> =
                ArrayList()
            var intent = Intent(this, Achievement::class.java)
            intent.action = Intent.ACTION_VIEW
            info = ShortcutInfo.Builder(this, "Achievement")
                .setShortLabel("成绩查询")
                .setIcon(
                    Icon.createWithResource(
                        this,
                        R.mipmap.ic_launcher
                    )
                )
                .setIntent(intent)
                .build()
            dynamicShortcuts.add(info)
            intent = Intent(this, Exam::class.java)
            intent.action = Intent.ACTION_VIEW
            info = ShortcutInfo.Builder(this, "exam")
                .setShortLabel("考试安排")
                .setIcon(
                    Icon.createWithResource(
                        this,
                        R.mipmap.ic_launcher
                    )
                )
                .setIntent(intent)
                .build()
            dynamicShortcuts.add(info)
            if (mSystemService != null) {
                mSystemService.dynamicShortcuts = dynamicShortcuts
            }
        }
    }

    private fun deleteShortCut() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            val mSystemService = getSystemService(ShortcutManager::class.java)
            mSystemService?.let {
                it.removeDynamicShortcuts(listOf("Achievement"))
                it.removeDynamicShortcuts(listOf("exam"))
            }
        }
    }

    private fun setViewState(viewIntoIndex: Int) {
        if (viewIntoIndex != viewNowIndex) {
            if (layout_timetable.visibility == View.VISIBLE) {
                layout_timetable.animate().alpha(0f).setDuration(200).setListener(null)
                layout_timetable.visibility = View.INVISIBLE
            }
            if (layout_mine.visibility == View.VISIBLE) {
                layout_mine.animate().alpha(0f).setDuration(200).setListener(null)
                layout_mine.visibility = View.INVISIBLE
            }
            when (viewIntoIndex) {
                1 -> {
                    layout_timetable.animate().alpha(1f).setDuration(200).setListener(null)
                    layout_timetable.visibility = View.VISIBLE
                }
                2 -> {
                    layout_mine.animate().alpha(1f).setDuration(200).setListener(null)
                    layout_mine.visibility = View.VISIBLE
                }
            }
        } else if (viewIntoIndex == 1) {
            timetable_refresh.isRefreshing = true
            getTable()
        }
        viewNowIndex = viewIntoIndex
    }

    var last: Long = -1
    override fun onBackPressed() {
        val now = System.currentTimeMillis()
        if (last == -1L) {
            onToast(this, "再点击一次退出")
            last = now
        } else {
            if (now - last < 2000) {
                ActivityCollector.finishAll()
            } else {
                last = now
                onToast(this, "请再点击一次退出")
            }
        }
    }

    private fun getLoadState() = timetable_refresh.isRefreshing

    override fun getContentView() = R.layout.activity_main

    override fun onSetSwipeBackEnable() = false

    companion object{
        @JvmStatic
        fun startActivity(context: Context, session: String){
            val intent = Intent().run {
                setClass(context, Main::class.java)
                putExtra("session", session)
            }
            context.startActivity(intent)
        }
    }


    private fun initSDK(context: Context) {
        UMConfigure.setLogEnabled(true)
        UMConfigure.init(
            context,
            "5ead0564167edd37b000004a",
            "OFFICIAL",
            UMConfigure.DEVICE_TYPE_PHONE,
            "df0fa00255f2dae35f21e3cd5166fb10"
        )
        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.LEGACY_AUTO)
        PushAgent.getInstance(this).register(object : IUmengRegisterCallback {
            override fun onSuccess(code: String) {}

            override fun onFailure(code: String, code1: String) {}
        })
        val manufacturer = Build.MANUFACTURER
        if (manufacturer != null && manufacturer.isNotEmpty()) {
            when (manufacturer.toLowerCase(Locale.getDefault())) {
                "meizu" -> MeizuRegister.register(context, "1008865", "1f796e7094b84d9ca50c0df3e5e85503")
                "huawei" -> {}
                "xiaomi" -> MiPushRegistar.register(context, "2882303761518265825", "5751826511825")
                "oppo" -> {}
                "vivo" -> {}
            }
        }
    }
}
