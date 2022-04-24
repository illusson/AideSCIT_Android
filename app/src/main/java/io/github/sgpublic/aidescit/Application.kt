package io.github.sgpublic.aidescit

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentResolver
import android.content.Context
import android.content.res.Configuration
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.lxj.xpopup.XPopup
import io.github.sgpublic.aidescit.base.CrashHandler
import io.github.sgpublic.aidescit.core.util.ActivityCollector
import io.github.sgpublic.aidescit.core.util.MyLog

@Suppress("unused")
class Application : Application() {
    override fun onCreate() {
        super.onCreate()
        MyLog.v("APP启动")
        application = this
    }

    private fun startListenException() {
        Handler(mainLooper).post {
            while (true) {
                try {
                    Looper.loop()
                } catch (e: Throwable){
                    CrashHandler.saveExplosion(e, -100, "应用意外停止")
                    if (!BuildConfig.DEBUG) {
                        ActivityCollector.finishAll()
                        break
                    }
                }
            }
        }
    }

    private fun showExceptionDialog(exc: String?) {
        // TODO 应用意外退出弹窗
        if (exc == null) {
            ActivityCollector.finishAll()
            return
        }
        XPopup.Builder(APPLICATION_CONTEXT).asConfirm(
            getString(R.string.title_function_crash),
            getString(R.string.text_function_crash),
            getString(R.string.text_function_crash_exit),
            getString(R.string.text_function_crash_copy), {
                val cs = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                cs.setPrimaryClip(ClipData.newPlainText("exception", exc))
                ActivityCollector.finishAll()
            }, {
                ActivityCollector.finishAll()
            }, false
        ).show()
    }

    companion object {
        private lateinit var application: Application

        val APPLICATION_CONTEXT: Context get() = application.applicationContext
        val CONTENT_RESOLVER: ContentResolver get() = APPLICATION_CONTEXT.contentResolver

        val IS_NIGHT_MODE: Boolean get() = APPLICATION_CONTEXT.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

        fun onToast(context: AppCompatActivity, content: String?) {
            context.runOnUiThread {
                Toast.makeText(APPLICATION_CONTEXT, content, Toast.LENGTH_SHORT).show()
            }
        }
        fun onToast(context: AppCompatActivity, @StringRes content: Int) {
            onToast(context, APPLICATION_CONTEXT.resources.getText(content).toString())
        }
        fun onToast(context: AppCompatActivity, @StringRes content: Int, code: Int) {
            val contentShow = (APPLICATION_CONTEXT.resources.getText(content).toString() + "($code)")
            onToast(context, contentShow)
        }
        fun onToast(context: AppCompatActivity, @StringRes content: Int, message: String?, code: Int) {
            if (message != null) {
                val contentShow = APPLICATION_CONTEXT.resources.getText(content).toString() + "，$message($code)"
                onToast(context, contentShow)
            } else {
                onToast(context, content, code)
            }
        }

        fun getString(@StringRes textId: Int, vararg arg: Any): String {
            return APPLICATION_CONTEXT.resources.getString(textId, *arg)
        }
    }
}