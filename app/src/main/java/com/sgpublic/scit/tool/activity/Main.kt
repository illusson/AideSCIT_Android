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
import androidx.viewbinding.ViewBinding
import com.sgpublic.scit.tool.BuildConfig
import com.sgpublic.scit.tool.R
import com.sgpublic.scit.tool.base.ActivityCollector
import com.sgpublic.scit.tool.base.BaseActivity
import com.sgpublic.scit.tool.base.BaseFragment
import com.sgpublic.scit.tool.databinding.ActivityMainBinding
import com.sgpublic.scit.tool.fragment.Home
import com.sgpublic.scit.tool.fragment.Mine
import com.sgpublic.scit.tool.fragment.News
import com.sgpublic.scit.tool.fragment.Table
import java.util.*
import kotlin.collections.ArrayList

class Main : BaseActivity<ActivityMainBinding>() {
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
        binding.navHome.setOnClickListener {
            replaceFragment(R.id.main_fragment_home, Home(this@Main))
            selectNavigation(0)
        }
        binding.navTable.setOnClickListener {
            replaceFragment(R.id.main_fragment_table, Table(this@Main))
            selectNavigation(1)
        }
        binding.navNews.setOnClickListener {
            replaceFragment(R.id.main_fragment_news, News(this@Main))
            selectNavigation(2)
        }
        binding.navMine.setOnClickListener {
            replaceFragment(R.id.main_fragment_mine, Mine(this@Main))
            selectNavigation(3)
        }
        initViewAtBottom(binding.navView)
    }

    private fun selectNavigation(index: Int){
        if (index != 0){
            supportFragmentManager.findFragmentById(R.id.main_fragment_home)?.onPause()
        } else {
            supportFragmentManager.findFragmentById(R.id.main_fragment_home)?.onResume()
        }
        binding.navHomeImage.setColorFilter(getSelectedColor(index == 0))
        binding.navHomeTitle.setTextColor(getSelectedColor(index == 0))
        binding.navTableImage.setColorFilter(getSelectedColor(index == 1))
        binding.navTableTitle.setTextColor(getSelectedColor(index == 1))
        binding.navNewsImage.setColorFilter(getSelectedColor(index == 2))
        binding.navNewsTitle.setTextColor(getSelectedColor(index == 2))
        binding.navMineImage.setColorFilter(getSelectedColor(index == 3))
        binding.navMineTitle.setTextColor(getSelectedColor(index == 3))
    }

    private fun getSelectedColor(isSelected: Boolean): Int {
        return if (isSelected) {
            getColor(R.color.colorPrimary)
        } else {
            getColor(R.color.color_font_dark)
        }
    }

    private fun <T: ViewBinding> replaceFragment(@IdRes id: Int, fragment: BaseFragment<T>){
        for (i in 0 until binding.mainFragment.childCount){
            val mView = binding.mainFragment.getChildAt(i)
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

        val mFragment: BaseFragment<*>? = supportFragmentManager.findFragmentById(id) as BaseFragment<*>?
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

    override fun getContentView() = ActivityMainBinding.inflate(layoutInflater)

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
