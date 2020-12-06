package com.sgpublic.cgk.tool.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import com.sgpublic.cgk.tool.R
import com.sgpublic.cgk.tool.base.BaseActivity
import kotlinx.android.synthetic.main.activity_evaluate.*
import kotlinx.android.synthetic.main.activity_evaluate.evaluate_list
import kotlinx.android.synthetic.main.activity_notices.*
import kotlinx.android.synthetic.main.item_evaluate.*

class Evaluate : BaseActivity() {
    override fun onActivityCreate(savedInstanceState: Bundle?) {

    }

    override fun onViewSetup() {
        super.onViewSetup()
        setProgressState()

        evaluate_refresh.isRefreshing = true
    }


    private fun setEnable(view: View, enable: Boolean) {
        if (isEnable(view) != enable){
            val alpha: Float = if (enable) 0.3F else 1.0F
            runOnUiThread {
                view.alpha = alpha
            }
        }
    }

    private fun isEnable(view: View): Boolean {
        return view.alpha == 1.0F
    }

    private fun setProgressState(index: Int = -1, total: Int = 0){
        setEnable(evaluate_pre, index <= 1)
        if (index == total) {
            evaluate_next.text = getString(R.string.title_evaluate_post)
        } else {
            val indexString: String
            val totalString: String
            if (index == -1){
                indexString = "-"
                totalString = "-"
            } else {
                indexString = index.toString()
                totalString = total.toString()
            }
            evaluate_next.text = String.format(getString(
                R.string.title_evaluate_next
            ), indexString, totalString)
        }
    }

    override fun getContentView(): Int = R.layout.activity_evaluate

    override fun onSetSwipeBackEnable(): Boolean = true

    companion object{
        @JvmStatic
        fun startActivity(context: Context){
            val intent = Intent().run {
                setClass(context, Evaluate::class.java)
            }
            context.startActivity(intent)
        }
    }
}