package com.sgpublic.scit.tool.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.sgpublic.scit.tool.R
import com.sgpublic.scit.tool.base.BaseFragment
import com.sgpublic.scit.tool.base.CrashHandler
import com.sgpublic.scit.tool.databinding.FragmentNewsBinding
import com.sgpublic.scit.tool.databinding.PagerNewsBinding
import com.sgpublic.scit.tool.helper.NewsHelper
import com.sgpublic.scit.tool.ui.NewsPagerAdapter

class News(private val contest: AppCompatActivity) : BaseFragment<FragmentNewsBinding>(contest), NewsHelper.Callback {

    override fun onFragmentCreated(savedInstanceState: Bundle?) {
        NewsHelper(contest).getNewsType(this)
    }

    override fun onFailure(code: Int, message: String?, e: Exception?) {
        super.onFailure(code, message, e)
        CrashHandler.saveExplosion(e, code)
        onToast(R.string.text_load_failed, message, code)
    }

    override fun onTypeResult(types: Map<Int, String>) {
        super.onTypeResult(types)
        val list: ArrayList<BaseFragment<PagerNewsBinding>> = ArrayList()
        types.forEach { (t, u) ->
            list.add(NewsPager(contest, u, t))
        }
        runOnUiThread {
            binding.newsPager.adapter = NewsPagerAdapter(contest.supportFragmentManager, list)
            binding.newsTab.setViewPager(binding.newsPager)
        }
    }

    override fun onViewSetup() {
        initViewAtTop(binding.newsToolbar)
    }
}