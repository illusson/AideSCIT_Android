package com.sgpublic.scit.tool.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.sgpublic.scit.tool.R
import com.sgpublic.scit.tool.base.BaseActivity
import com.sgpublic.scit.tool.databinding.ActivityWebviewBinding

class WebView : BaseActivity<ActivityWebviewBinding>() {
    companion object {
        fun startActivity(context: Context, tid: Int, nid: Int){
            val intents = Intent(context, WebView::class.java)
            intents.putExtra("url", "http://m.scit.cn/newsli.aspx?tid=$tid&id=$nid")
            context.startActivity(intents)
        }

        fun startActivity(context: Context, url: String){
            val intents = Intent(context, WebView::class.java)
            intents.putExtra("url", url)
            context.startActivity(intents)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        initViewAtTop(binding.webToolbar)
        val url = intent.getStringExtra("url")
        url?.let {
            binding.webView.loadUrl(it)
        }
    }

    override fun getContentView() = ActivityWebviewBinding.inflate(layoutInflater)

    override fun onSetSwipeBackEnable(): Boolean = true
}