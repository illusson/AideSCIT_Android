package com.sgpublic.aidescit.activity

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Bundle
import android.view.View
import androidx.annotation.IdRes
import androidx.viewbinding.ViewBinding
import com.sgpublic.aidescit.BuildConfig
import com.sgpublic.aidescit.R
import com.sgpublic.aidescit.base.BaseActivity
import com.sgpublic.aidescit.base.BaseFragment
import com.sgpublic.aidescit.databinding.ActivityMainBinding
import com.sgpublic.aidescit.fragment.Home
import com.sgpublic.aidescit.fragment.Mine
import com.sgpublic.aidescit.fragment.News
import com.sgpublic.aidescit.fragment.Schedule
import com.sgpublic.aidescit.manager.ConfigManager

class Main : BaseActivity<ActivityMainBinding>() {
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        initShortsCut()
        if (BuildConfig.DEBUG){
            onToast("debug")
        }
        if (ConfigManager.getInt("week", 0) != 0){
            return
        }
        binding.navNews.visibility = View.GONE
        if (ConfigManager.getBoolean("scheduleCanInquire", true)){
            return
        }
        binding.navTable.visibility = View.GONE
    }

    override fun onViewSetup() {
        binding.navHome.setOnClickListener {
            replaceFragment(R.id.main_fragment_home, Home(this@Main))
            selectNavigation(0)
        }
        binding.navTable.setOnClickListener {
            replaceFragment(R.id.main_fragment_table, Schedule(this@Main))
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
        binding.navHome.callOnClick()
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

    override fun isActivityAtBottom() = true

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
