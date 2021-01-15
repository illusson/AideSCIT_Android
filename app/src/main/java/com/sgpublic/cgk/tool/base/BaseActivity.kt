package com.sgpublic.cgk.tool.base

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.sgpublic.cgk.tool.BuildConfig
import com.sgpublic.cgk.tool.manager.ConfigManager
import com.umeng.analytics.MobclickAgent
import com.umeng.message.PushAgent
import me.imid.swipebacklayout.lib.SwipeBackLayout
import me.imid.swipebacklayout.lib.app.SwipeBackActivity
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.*

@SuppressLint("Registered")
abstract class BaseActivity : SwipeBackActivity() {
    protected val tag: String = javaClass.simpleName

    private val edgeSize: Int = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PushAgent.getInstance(this).onAppStart()

        ActivityCollector.addActivity(this)

        setSwipeBackEnable(onSetSwipeBackEnable())
        swipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
        swipeBackLayout.setEdgeSize(edgeSize)

        setContentView(getContentView())
        onViewSetup()
        onActivityCreate(savedInstanceState)
    }

    protected abstract fun onActivityCreate(savedInstanceState: Bundle?)

    protected abstract fun getContentView(): Int

    protected abstract fun onSetSwipeBackEnable(): Boolean

    protected open fun onViewSetup(){
        this.window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        this.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

//        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
//        val modeState: Int = this.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            this.window.setDecorFitsSystemWindows(modeState != Configuration.UI_MODE_NIGHT_YES)
//        } else {
//            if (modeState == Configuration.UI_MODE_NIGHT_YES){
//                this.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//            } else {
//                this.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
//            }
//        }
//        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    override fun onResume() {
        super.onResume()
        MobclickAgent.onResume(this)
    }

    override fun onPause() {
        super.onPause()
        MobclickAgent.onPause(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        ActivityCollector.removeActivity(this)
    }

    protected open fun setAnimateState(is_visible: Boolean, duration: Int, view: View, callback: Runnable? = null) {
        runOnUiThread {
            if (is_visible) {
                view.visibility = View.VISIBLE
                view.animate().alphaBy(0f).alpha(1f).setDuration(duration.toLong())
                    .setListener(null)
                callback?.run()
            } else {
                view.animate().alphaBy(1f).alpha(0f).setDuration(duration.toLong())
                    .setListener(null)
                Handler().postDelayed({
                    view.visibility = View.GONE
                    callback?.run()
                }, duration.toLong())
            }
        }
    }

    protected fun onToast(context: Context, content: String?) {
        runOnUiThread {
            Toast.makeText(context, content, Toast.LENGTH_SHORT).show()
        }
        if (BuildConfig.DEBUG){
            Log.d(tag, content.toString())
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

    protected fun dip2px(context: Context?, dpValue: Float): Int {
        return if (context != null){
            val scales = context.resources.displayMetrics.density
            (dpValue * scales + 0.5f).toInt()
        } else {
            0
        }
    }

    protected open fun saveExplosion(e: Throwable?, code: Int) {
        try {
            e?.let {
                val exceptionLog: JSONObject
                var exceptionLogContent = JSONArray()
                val exception = File(
                    applicationContext.getExternalFilesDir("log")?.path,
                    "exception.json"
                )
                var log_content: String
                try {
                    val fileInputStream =
                        FileInputStream(exception)
                    val bufferedReader =
                        BufferedReader(InputStreamReader(fileInputStream))
                    var line: String?
                    val stringBuilder = StringBuilder()
                    while (bufferedReader.readLine().also { line = it } != null) {
                        stringBuilder.append(line)
                    }
                    log_content = stringBuilder.toString()
                } catch (e1: IOException) {
                    log_content = ""
                }
                if (log_content != "") {
                    exceptionLog = JSONObject(log_content)
                    if (!exceptionLog.isNull("logs")) {
                        exceptionLogContent = exceptionLog.getJSONArray("logs")
                    }
                }
                val elements = e.stackTrace
                var elementIndex: StackTraceElement
                val crashMsgJson = JSONObject()
                val crashMsgArray = JSONArray()
                val crashMsgArrayIndex = JSONObject()
                val crashStackTrace = JSONArray()
                var crashMsgIndex = 0
                while (crashMsgIndex < elements.size && crashMsgIndex < 10) {
                    elementIndex = e.stackTrace[crashMsgIndex]
                    val crashStackTraceIndex = JSONObject()
                    crashStackTraceIndex.put("class", elementIndex.className)
                    crashStackTraceIndex.put("line", elementIndex.lineNumber)
                    crashStackTraceIndex.put("method", elementIndex.methodName)
                    crashStackTrace.put(crashStackTraceIndex)
                    crashMsgIndex++
                }
                val configString = StringBuilder(e.toString())
                for (config_index in 0..2) {
                    elementIndex = elements[config_index]
                    configString.append("\nat ").append(elementIndex.toString())
                }
                ConfigManager(this).putString("last_exception", configString.toString())
                crashMsgArrayIndex.put("code", code)
                crashMsgArrayIndex.put("message", e.toString())
                crashMsgArrayIndex.put("stack_trace", crashStackTrace)
                crashMsgArray.put(crashMsgArrayIndex)
                var exceptionLogIndex = 0
                while (exceptionLogIndex < exceptionLogContent.length() && exceptionLogIndex < 2) {
                    val msg_index =
                        exceptionLogContent.getJSONObject(exceptionLogIndex)
                    if (crashMsgArrayIndex.toString() != msg_index.toString()) {
                        crashMsgArray.put(msg_index)
                    }
                    exceptionLogIndex++
                }
                crashMsgJson.put("logs", crashMsgArray)
                val fileOutputStream = FileOutputStream(exception)
                fileOutputStream.write(crashMsgJson.toString().toByteArray())
                fileOutputStream.close()
            }
        } catch (ignore: JSONException) {
        } catch (ignore: IOException) {
        } catch (ignore: IllegalArgumentException) {}
    }
}