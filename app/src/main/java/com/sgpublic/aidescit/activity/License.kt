package com.sgpublic.aidescit.activity

import android.os.Bundle
import com.sgpublic.aidescit.R
import com.sgpublic.aidescit.base.BaseActivity
import com.sgpublic.aidescit.data.LicenseListData
import com.sgpublic.aidescit.databinding.ActivityLicenseBinding
import com.sgpublic.aidescit.ui.LicenseListAdapter
import java.util.*

class License : BaseActivity<ActivityLicenseBinding>() {
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        initViewAtTop(binding.licenceToolbar)
        binding.licenseBack.setOnClickListener { finish() }
        loadLicense()
    }

    override fun onViewSetup() {

    }

    override fun isActivityAtBottom(): Boolean = false

    private fun loadLicense() {
        val arrayList: ArrayList<LicenseListData> = ArrayList<LicenseListData>()
        arrayList.add(LicenseListData(
            "BannerViewPager",
            "Android，Base on ViewPager2. 这可能是全网最好用的ViewPager轮播图。简单、高效，一行代码实现循环轮播，一屏三页任意变，指示器样式任你挑。",
            "zhpanvip",
            "https://github.com/zhpanvip/BannerViewPager"
        ))
        arrayList.add(LicenseListData(
            "Blur-Fix-AndroidX",
            "Fork from https://github.com/ThomasCookDeveloperInfo/BlurKit-Fix, add AndroidX support, move to JitPack.",
            "SGPublic",
            "https://github.com/SGPublic/Blur-Fix-AndroidX"
        ))
        arrayList.add(LicenseListData(
            "DialogX",
            "\uD83D\uDCACDialogX对话框组件库，更加方便易用，可自定义程度更高，扩展性更强，轻松实现各种对话框、菜单和提示效果，更有iOS、MIUI等主题扩展可选",
            "kongzue",
            "https://github.com/kongzue/DialogX"
        ))
        arrayList.add(LicenseListData(
            "glide",
            "An image loading and caching library for Android focused on smooth scrolling.",
            "bumptech",
            "https://github.com/bumptech/glide"
        ))
        arrayList.add(LicenseListData(
            "glide-transformations",
            "An Android transformation library providing a variety of image transformations for Glide.",
            "wasabeef",
            "https://github.com/wasabeef/glide-transformations"
        ))
        arrayList.add(LicenseListData(
            "MultiWaveHeaderX",
            "Fork from https://github.com/scwang90/MultiWaveHeader, move to JitPack.",
            "SGPublic",
            "https://github.com/SGPublic/MultiWaveHeaderX"
        ))
        arrayList.add(LicenseListData(
            "okhttp",
            "Square’s meticulous HTTP client for Java and Kotlin.",
            "square",
            "https://github.com/square/okhttp"
        ))
        arrayList.add(LicenseListData(
            "rebound",
            "A Java library that models spring dynamics and adds real world physics to your app.",
            "facebookarchive",
            "https://github.com/facebookarchive/rebound"
        ))
        arrayList.add(LicenseListData(
            "Sofia",
            "Android沉浸式效果的实现，状态栏和导航栏均支持设置颜色、渐变色、图片、透明度、内容入侵和状态栏深色字体；兼容竖屏、横屏，当屏幕旋转时会自动适配。",
            "yanzhenjie",
            "https://github.com/yanzhenjie/Sofia"
        ))
        arrayList.add(LicenseListData(
            "SmartTabLayout",
            "A custom ViewPager title strip which gives continuous feedback to the user when scrolling",
            "ogaclejapan",
            "https://github.com/ogaclejapan/SmartTabLayout"
        ))
        arrayList.add(LicenseListData(
            "SwipeBackLayoutX",
            "Fork from https://github.com/ikew0ng/SwipeBackLayout, add AndroidX support, move to JitPack.",
            "SGPublic",
            "https://github.com/SGPublic/SwipeBackLayoutX"
        ))
        binding.licenseList.adapter = LicenseListAdapter(
            this@License, R.layout.item_license_list, arrayList
        )
    }
}