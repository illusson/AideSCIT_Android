package com.sgpublic.scit.tool.fragment

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sgpublic.scit.tool.R
import com.sgpublic.scit.tool.base.BaseFragment
import com.sgpublic.scit.tool.helper.NewsHelper
import com.sgpublic.scit.tool.ui.NewsPagerAdapter
import kotlinx.android.synthetic.main.fragment_news.*

class News(contest: AppCompatActivity) : BaseFragment(contest), NewsHelper.Callback {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        NewsHelper(contest).getNewsType(this)
    }

    override fun onFailure(code: Int, message: String?, e: Exception?) {
        super.onFailure(code, message, e)
        saveExplosion(e, code)
        onToast(R.string.text_load_failed, message, code)
    }

    override fun onTypeResult(types: Map<Int, String>) {
        super.onTypeResult(types)
        val list: ArrayList<BaseFragment> = ArrayList()
        types.forEach { (t, u) ->
            list.add(NewsPager(contest, u, t))
        }
        runOnUiThread {
            news_pager.adapter = NewsPagerAdapter(contest.supportFragmentManager, list)
            news_tab.setViewPager(news_pager)
        }
    }

    override fun onViewSetup() {
        super.onViewSetup()
        initViewAtTop(news_toolbar)
    }

    override fun getContentView(): Int = R.layout.fragment_news
}