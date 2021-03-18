package com.sgpublic.scit.tool.activity

//import com.umeng.analytics.MobclickAgent
//import com.umeng.commonsdk.UMConfigure
//import com.umeng.message.IUmengRegisterCallback
//import com.umeng.message.PushAgent
//import org.android.agoo.mezu.MeizuRegister
//import org.android.agoo.xiaomi.MiPushRegistar
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Bundle
import android.view.View
import androidx.annotation.IdRes
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.sgpublic.scit.tool.BuildConfig
import com.sgpublic.scit.tool.R
import com.sgpublic.scit.tool.base.ActivityCollector
import com.sgpublic.scit.tool.base.BaseActivity
import com.sgpublic.scit.tool.base.BaseFragment
import com.sgpublic.scit.tool.fragment.Home
import com.sgpublic.scit.tool.fragment.Mine
import com.sgpublic.scit.tool.fragment.News
import com.sgpublic.scit.tool.fragment.Table
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_mine.*
import kotlinx.android.synthetic.main.fragment_table.*
import kotlinx.android.synthetic.main.item_timetable.view.*
import java.util.*
import kotlin.collections.ArrayList

class Main : BaseActivity() {
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        replaceFragment(R.id.main_fragment_home, Home(this@Main))
        selectNavigation(0)
        initShortsCut()
        if (BuildConfig.DEBUG){
            onToast("debug")
        }
    }

    override fun initViewAtBottom(view: View) {
        rootViewBottom = view.layoutParams.height
        ViewCompat.setOnApplyWindowInsetsListener(this.window.decorView) { v: View?, insets: WindowInsetsCompat? ->
            if (insets != null) {
                val b = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
                val params = view.layoutParams
                params.height = rootViewBottom + b
                view.layoutParams = params
                ViewCompat.onApplyWindowInsets(v!!, insets)
            }
            insets
        }
    }

    override fun onViewSetup() {
        super.onViewSetup()
        nav_home.setOnClickListener {
            replaceFragment(R.id.main_fragment_home, Home(this@Main))
            selectNavigation(0)
        }
        nav_table.setOnClickListener {
            replaceFragment(R.id.main_fragment_table, Table(this@Main))
            selectNavigation(1)
        }
        nav_news.setOnClickListener {
            replaceFragment(R.id.main_fragment_news, News(this@Main))
            selectNavigation(2)
        }
        nav_mine.setOnClickListener {
            replaceFragment(R.id.main_fragment_mine, Mine(this@Main))
            selectNavigation(3)
        }
        initViewAtBottom(nav_view)
    }

    private fun selectNavigation(index: Int){
        if (index != 0){
            supportFragmentManager.findFragmentById(R.id.main_fragment_home)?.onPause()
        } else {
            supportFragmentManager.findFragmentById(R.id.main_fragment_home)?.onResume()
        }
        nav_home_image.setColorFilter(getSelectedColor(index == 0))
        nav_home_title.setTextColor(getSelectedColor(index == 0))
        nav_table_image.setColorFilter(getSelectedColor(index == 1))
        nav_table_title.setTextColor(getSelectedColor(index == 1))
        nav_news_image.setColorFilter(getSelectedColor(index == 2))
        nav_news_title.setTextColor(getSelectedColor(index == 2))
        nav_mine_image.setColorFilter(getSelectedColor(index == 3))
        nav_mine_title.setTextColor(getSelectedColor(index == 3))
    }

    private fun getSelectedColor(isSelected: Boolean): Int {
        return if (isSelected) {
            getColor(R.color.colorPrimary)
        } else {
            getColor(R.color.color_font_dark)
        }
    }
    
    private fun replaceFragment(@IdRes id: Int, fragment: BaseFragment){
        for (i in 0 until main_fragment.childCount){
            val mView = main_fragment.getChildAt(i)
            if (mView.visibility == View.VISIBLE && mView.id == id){
                break
            }
            if (mView.id == id){
                mView.visibility = View.VISIBLE
                continue
            }
            if (mView.visibility == View.VISIBLE) {
                mView.visibility = View.GONE
                continue
            }
        }

        val mFragment: BaseFragment? = supportFragmentManager.findFragmentById(id) as BaseFragment?
        if (mFragment != null){
            return
        }
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(id, fragment)
        transaction.commit()
    }

    private fun initShortsCut() {
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

    var last: Long = -1
    override fun onBackPressed() {
        val now = System.currentTimeMillis()
        if (last == -1L) {
            onToast("再点击一次退出")
            last = now
        } else {
            if (now - last < 2000) {
                ActivityCollector.finishAll()
            } else {
                last = now
                onToast("请再点击一次退出")
            }
        }
    }

    override fun getContentView() = R.layout.activity_main

    override fun onSetSwipeBackEnable() = false

    companion object {
        @JvmStatic
        fun startActivity(context: Context){
            val intent = Intent().run {
                setClass(context, Main::class.java)
            }
            context.startActivity(intent)
        }
    }
}
