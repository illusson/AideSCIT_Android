package io.github.sgpublic.aidescit.activity

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.view.View
import androidx.annotation.IdRes
import androidx.viewbinding.ViewBinding
import io.github.sgpublic.aidescit.Application
import io.github.sgpublic.aidescit.BuildConfig
import io.github.sgpublic.aidescit.R
import io.github.sgpublic.aidescit.base.BaseActivity
import io.github.sgpublic.aidescit.base.BaseFragment
import io.github.sgpublic.aidescit.core.manager.ConfigManager
import io.github.sgpublic.aidescit.databinding.ActivityMainBinding
import io.github.sgpublic.aidescit.fragment.Home
import io.github.sgpublic.aidescit.fragment.Mine
import io.github.sgpublic.aidescit.fragment.News
import io.github.sgpublic.aidescit.fragment.Schedule

class Main : BaseActivity<ActivityMainBinding>() {
    override fun onActivityCreated(hasSavedInstanceState: Boolean) {
        initShortsCut()
        if (BuildConfig.DEBUG){
            Application.onToast(this, "debug")
        }
        if (ConfigManager.WEEK != 0){
            return
        }
        ViewBinding.navNews.visibility = View.GONE
        if (ConfigManager.SCHEDULE_CAN_INQUIRE){
            return
        }
        ViewBinding.navTable.visibility = View.GONE
    }

    override fun onViewSetup() {
        ViewBinding.navHome.setOnClickListener {
            replaceFragment(R.id.main_fragment_home, Home(this@Main))
            selectNavigation(0)
        }
        ViewBinding.navTable.setOnClickListener {
            replaceFragment(R.id.main_fragment_table, Schedule(this@Main))
            selectNavigation(1)
        }
        ViewBinding.navNews.setOnClickListener {
            replaceFragment(R.id.main_fragment_news, News(this@Main))
            selectNavigation(2)
        }
        ViewBinding.navMine.setOnClickListener {
            replaceFragment(R.id.main_fragment_mine, Mine(this@Main))
            selectNavigation(3)
        }
        ViewBinding.navHome.callOnClick()
        initViewAtBottom(ViewBinding.navView)
    }

    private fun selectNavigation(index: Int){
        if (index != 0){
            supportFragmentManager.findFragmentById(R.id.main_fragment_home)?.onPause()
        } else {
            supportFragmentManager.findFragmentById(R.id.main_fragment_home)?.onResume()
        }
        ViewBinding.navHomeImage.setColorFilter(getSelectedColor(index == 0))
        ViewBinding.navHomeTitle.setTextColor(getSelectedColor(index == 0))
        ViewBinding.navTableImage.setColorFilter(getSelectedColor(index == 1))
        ViewBinding.navTableTitle.setTextColor(getSelectedColor(index == 1))
        ViewBinding.navNewsImage.setColorFilter(getSelectedColor(index == 2))
        ViewBinding.navNewsTitle.setTextColor(getSelectedColor(index == 2))
        ViewBinding.navMineImage.setColorFilter(getSelectedColor(index == 3))
        ViewBinding.navMineTitle.setTextColor(getSelectedColor(index == 3))
    }

    private fun getSelectedColor(isSelected: Boolean): Int {
        return if (isSelected) {
            getColor(R.color.colorPrimary)
        } else {
            getColor(R.color.color_font_dark)
        }
    }

    private fun <T: ViewBinding> replaceFragment(@IdRes id: Int, fragment: BaseFragment<T>){
        for (i in 0 until ViewBinding.mainFragment.childCount){
            val mView = ViewBinding.mainFragment.getChildAt(i)
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

    override fun isActivityAtBottom() = true

    override fun onCreateViewBinding(): ActivityMainBinding =
        ActivityMainBinding.inflate(layoutInflater)

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
