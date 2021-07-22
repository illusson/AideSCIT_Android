package com.sgpublic.aidescit.ui

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.ogaclejapan.smarttablayout.SmartTabLayout
import com.sgpublic.aidescit.base.BaseFragment
import com.sgpublic.aidescit.databinding.PagerNewsBinding
import com.sgpublic.aidescit.fragment.NewsPage
import com.sgpublic.aidescit.helper.NewsHelper
import com.sgpublic.aidescit.util.CrashHandler

class NewsPageViewAdapter(private val contest: Context, private val newsPager: ViewPager,
                          private val newsTab: SmartTabLayout) {
    private var callback: NewsFailureCallback? = null

    fun setNewsFailureCallback(callback: NewsFailureCallback){
        this.callback = callback
    }

    fun startLoad(){
        NewsHelper(contest).getNewsType(object : NewsHelper.Callback {
            override fun onFailure(code: Int, message: String?, e: Exception?) {
                super.onFailure(code, message, e)
                CrashHandler.saveExplosion(e, code)
                callback?.onFailure(code, message, e)
            }

            override fun onTypeResult(types: Map<Int, String>) {
                super.onTypeResult(types)
                val list: ArrayList<BaseFragment<PagerNewsBinding>> = ArrayList()
                types.forEach { (t, u) ->
                    list.add(NewsPage(contest, u, t))
                }
                (contest as AppCompatActivity).run {
                    runOnUiThread {
                        newsPager.adapter = NewsPagerAdapter(supportFragmentManager, list)
                        newsTab.setViewPager(newsPager)
                    }
                }
            }
        })
    }

    interface NewsFailureCallback {
        fun onFailure(code: Int, message: String?, e: Exception?)
    }
}