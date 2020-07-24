package com.sgpublic.cgk.tool.base

import android.content.Context
import android.widget.Toast
import androidx.fragment.app.Fragment

open class BaseFragment : Fragment() {

    protected fun dip2px(context: Context?, dpValue: Float): Int {
        return if (context != null){
            val scales = context.resources.displayMetrics.density
            (dpValue * scales + 0.5f).toInt()
        } else {
            0
        }
    }

    open fun getTitle(): CharSequence = ""

    protected fun onToast(context: Context, content: String?) {
        activity?.runOnUiThread {
            Toast.makeText(context, content, Toast.LENGTH_SHORT).show()
        }
    }
    protected fun onToast(context: Context, content: Int) {
        onToast(context, resources.getText(content).toString())
    }
    protected fun onToast(context: Context, content: Int, code: Int) {
        val contentShow = (resources.getText(content).toString() + "($code)")
        onToast(context, contentShow)
    }
    protected fun onToast(context: Context, content: Int, message: String?, code: Int) {
        if (message != null) {
            val contentShow = resources.getText(content).toString() + "，$message($code)"
            onToast(context, contentShow)
        } else {
            onToast(context, content, code)
        }
    }
}