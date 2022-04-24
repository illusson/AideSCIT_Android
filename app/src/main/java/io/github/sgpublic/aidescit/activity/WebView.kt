package io.github.sgpublic.aidescit.activity

import android.content.Context
import android.content.Intent
import io.github.sgpublic.aidescit.base.BaseActivity
import io.github.sgpublic.aidescit.core.util.MyLog
import io.github.sgpublic.aidescit.databinding.ActivityWebviewBinding

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

    override fun onActivityCreated(hasSavedInstanceState: Boolean) {
        val url = intent.getStringExtra("url")
        url?.let {
            ViewBinding.webView.loadUrl(it)
        }
    }

    override fun onViewSetup() {
        initViewAtTop(ViewBinding.webToolbar)
    }

    override fun onCreateViewBinding(): ActivityWebviewBinding =
        ActivityWebviewBinding.inflate(layoutInflater)
}