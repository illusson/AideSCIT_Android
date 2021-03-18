package com.sgpublic.scit.tool.activity

import android.os.Bundle
import android.widget.ListView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import com.sgpublic.scit.tool.R
import com.sgpublic.scit.tool.base.BaseActivity
import com.sgpublic.scit.tool.data.LicenseListData
import com.sgpublic.scit.tool.ui.LicenseListAdapter
import kotlinx.android.synthetic.main.activity_about.*
import kotlinx.android.synthetic.main.activity_license.*
import java.util.*

class License : BaseActivity() {
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        initViewAtTop(licence_toolbar)
        license_back.setOnClickListener { finish() }
        loadLicense()
    }

    override fun getContentView(): Int = R.layout.activity_license

    override fun onSetSwipeBackEnable(): Boolean = true

    private fun loadLicense() {
        val arrayList: ArrayList<LicenseListData> = ArrayList<LicenseListData>()
        arrayList.add(
            LicenseListData(
                "BannerViewPager",
                "Android，Base on ViewPager2. 这可能是全网最好用的ViewPager轮播图。简单、高效，一行代码实现循环轮播，一屏三页任意变，指示器样式任你挑。",
                "zhpanvip",
                "https://github.com/zhpanvip/BannerViewPager"
            )
        )
        arrayList.add(
            LicenseListData(
                "BlurKit-Fix",
                "A fix for blur kit that lets the consumer specify a view in the view heirarchy to blur.",
                "ThomasCookDeveloperInfo",
                "https://github.com/ThomasCookDeveloperInfo/BlurKit-Fix"
            )
        )
        arrayList.add(
            LicenseListData(
                "rebound",
                "A Java library that models spring dynamics and adds real world physics to your app.",
                "facebookarchive",
                "https://github.com/facebookarchive/rebound"
            )
        )
        arrayList.add(
            LicenseListData(
                "SmartTabLayout",
                "A custom ViewPager title strip which gives continuous feedback to the user when scrolling",
                "ogaclejapan",
                "https://github.com/ogaclejapan/SmartTabLayout"
            )
        )
        arrayList.add(
            LicenseListData(
                "SwipeBackLayout",
                "An Android library that help you to build app with swipe back gesture.",
                "ikew0ng",
                "https://github.com/ikew0ng/SwipeBackLayout"
            )
        )
        arrayList.add(
            LicenseListData(
                "glide",
                "An image loading and caching library for Android focused on smooth scrolling.",
                "bumptech",
                "https://github.com/bumptech/glide"
            )
        )
        arrayList.add(
            LicenseListData(
                "glide-transformations",
                "An Android transformation library providing a variety of image transformations for Glide.",
                "wasabeef",
                "https://github.com/wasabeef/glide-transformations"
            )
        )
        arrayList.add(
            LicenseListData(
                "okhttp",
                "Square’s meticulous HTTP client for Java and Kotlin.",
                "square",
                "https://github.com/square/okhttp"
            )
        )
        license_list.adapter = LicenseListAdapter(
            this@License, R.layout.item_license_list, arrayList
        )
    }
}