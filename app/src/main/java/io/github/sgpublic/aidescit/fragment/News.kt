package io.github.sgpublic.aidescit.fragment

import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import io.github.sgpublic.aidescit.Application
import io.github.sgpublic.aidescit.R
import io.github.sgpublic.aidescit.base.BaseFragment
import io.github.sgpublic.aidescit.core.module.NewsModule
import io.github.sgpublic.aidescit.databinding.FragmentNewsBinding
import io.github.sgpublic.aidescit.databinding.PagerNewsBinding
import io.github.sgpublic.aidescit.ui.NewsPagerAdapter

class News(contest: AppCompatActivity) : BaseFragment<FragmentNewsBinding>(contest), NewsModule.Callback {
    override fun onFragmentCreated(hasSavedInstanceState: Boolean) {
        NewsModule().getNewsType(this)
    }

    override fun onViewSetup() {
        initViewAtTop(ViewBinding.newsToolbar)
    }

    override fun onFailure(code: Int, message: String?, e: Throwable?) {
        Application.onToast(context, R.string.text_load_failed, message, code)
    }

    override fun onTypeResult(types: Map<Int, String>) {
        super.onTypeResult(types)
        val list: ArrayList<BaseFragment<PagerNewsBinding>> = ArrayList()
        types.forEach { (t, u) ->
            list.add(NewsPage(context, u, t))
        }
        context.runOnUiThread {
            ViewBinding.newsPager.adapter = NewsPagerAdapter(context.supportFragmentManager, list)
            ViewBinding.newsTab.setViewPager(ViewBinding.newsPager)
        }
    }

    override fun onCreateViewBinding(container: ViewGroup?): FragmentNewsBinding =
        FragmentNewsBinding.inflate(layoutInflater)
}