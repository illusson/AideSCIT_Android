package com.sgpublic.aidescit.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.sgpublic.aidescit.base.BaseActivity
import com.sgpublic.aidescit.databinding.ActivityWebviewBinding
import com.sgpublic.aidescit.util.MyLog

class WebView : BaseActivity<ActivityWebviewBinding>() {
    companion object {
        fun startActivity(context: Context, tid: Int, nid: Int){
            val intents = Intent(context, WebView::class.java)
            MyLog.d("http://m.scit.cn/newsli.aspx?tid=$tid&id=$nid")
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
        val url = intent.getStringExtra("url")
        url?.let {
            binding.webView.loadUrl(it)
        }
    }

    override fun onViewSetup() {
        initViewAtTop(binding.webToolbar)
    }

    override fun isActivityAtBottom(): Boolean = false
}