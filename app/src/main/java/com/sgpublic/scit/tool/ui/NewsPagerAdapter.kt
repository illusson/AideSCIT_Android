package com.sgpublic.scit.tool.ui

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.sgpublic.scit.tool.base.BaseFragment
import com.sgpublic.scit.tool.databinding.PagerNewsBinding

class NewsPagerAdapter(fragmentManager: FragmentManager, private val list: List<BaseFragment<PagerNewsBinding>>) :
    FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int) = list[position]

    override fun getCount() = list.size

    override fun getPageTitle(position: Int): CharSequence {
        return list[position].getTitle()
    }
}