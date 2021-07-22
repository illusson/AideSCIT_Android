package com.sgpublic.aidescit.ui

import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import com.sgpublic.aidescit.util.MyLog
import kotlin.math.abs

abstract class AppBarStateChangeListener: OnOffsetChangedListener {
    enum class State {
        EXPANDED, COLLAPSED
    }

    private var mCurrentState = State.COLLAPSED
    override fun onOffsetChanged(appBarLayout: AppBarLayout, i: Int) {
        when {
            abs(i) >= appBarLayout.totalScrollRange - 150 -> {
                State.COLLAPSED
            }
            else -> State.EXPANDED
        }.let {
            if (mCurrentState != it) {
                mCurrentState = it
                onStateChanged(appBarLayout, it)
            }
        }
    }

    abstract fun onStateChanged(appBarLayout: AppBarLayout, state: State)
}