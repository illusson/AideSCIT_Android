package com.sgpublic.cgk.tool.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.sgpublic.cgk.tool.R
import com.sgpublic.cgk.tool.base.BaseActivity
import kotlinx.android.synthetic.main.activity_evaluate.*

class Evaluate : BaseActivity() {
    override fun onActivityCreate(savedInstanceState: Bundle?) {
        evaluate_next.text = String.format(getString(R.string.title_evaluate_next), "-", "-")
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