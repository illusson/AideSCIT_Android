package io.github.sgpublic.aidescit.ui

import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.ogaclejapan.smarttablayout.SmartTabLayout
import io.github.sgpublic.aidescit.base.BaseFragment
import io.github.sgpublic.aidescit.core.module.BaseAPI
import io.github.sgpublic.aidescit.core.module.NewsModule
import io.github.sgpublic.aidescit.databinding.PagerNewsBinding
import io.github.sgpublic.aidescit.fragment.NewsPage

class NewsPageViewAdapter(private val context: AppCompatActivity, private val newsPager: ViewPager,
                          private val newsTab: SmartTabLayout) {
    private var callback: NewsFailureCallback? = null

    fun setNewsFailureCallback(callback: NewsFailureCallback){
        this.callback = callback
    }

    fun startLoad(){
        NewsModule().getNewsType(object : NewsModule.Callback {
            override fun onFailure(code: Int, message: String?, e: Throwable?) {
                callback?.onFailure(code, message, e)
            }

            override fun onTypeResult(types: Map<Int, String>) {
                super.onTypeResult(types)
                val list: ArrayList<BaseFragment<PagerNewsBinding>> = ArrayList()
                types.forEach { (t, u) ->
                    list.add(NewsPage(context, u, t))
                }
                context.runOnUiThread {
                        // TODO supportFragmentManager
                        newsPager.adapter = NewsPagerAdapter(context.supportFragmentManager, list)
                        newsTab.setViewPager(newsPager)
                }
            }
        })
    }

    interface NewsFailureCallback: BaseAPI.Callback
}