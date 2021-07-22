package com.sgpublic.aidescit.fragment

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sgpublic.aidescit.R
import com.sgpublic.aidescit.base.BaseFragment
import com.sgpublic.aidescit.databinding.FragmentNewsBinding
import com.sgpublic.aidescit.databinding.PagerNewsBinding
import com.sgpublic.aidescit.helper.NewsHelper
import com.sgpublic.aidescit.ui.NewsPagerAdapter
import com.sgpublic.aidescit.util.CrashHandler

class News(contest: AppCompatActivity) : BaseFragment<FragmentNewsBinding>(contest), NewsHelper.Callback {
    override fun onFragmentCreated(savedInstanceState: Bundle?) {
        NewsHelper(contest).getNewsType(this)
    }

    override fun onViewSetup() {
        initViewAtTop(binding.newsToolbar)
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
            list.add(NewsPage(contest, u, t))
        }
        (contest as AppCompatActivity).run {
            runOnUiThread {
                binding.newsPager.adapter = NewsPagerAdapter(supportFragmentManager, list)
                binding.newsTab.setViewPager(binding.newsPager)
            }
        }
    }
}