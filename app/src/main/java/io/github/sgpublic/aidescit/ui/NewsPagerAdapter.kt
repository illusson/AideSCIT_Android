package io.github.sgpublic.aidescit.ui

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import io.github.sgpublic.aidescit.base.BaseFragment
import io.github.sgpublic.aidescit.databinding.PagerNewsBinding

class NewsPagerAdapter(fragmentManager: FragmentManager, private val list: List<BaseFragment<PagerNewsBinding>>) :
    FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int) = list[position]

    override fun getCount() = list.size

    override fun getPageTitle(position: Int): CharSequence {
        return list[position].getTitle()
    }
}