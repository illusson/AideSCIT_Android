package com.sgpublic.scit.tool.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.sgpublic.scit.tool.manager.ConfigManager
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.*

abstract class BaseFragment<T: ViewBinding>(val contest: AppCompatActivity) : Fragment() {
    private var _binding: T? = null
    protected val binding: T get() = _binding!!

    final override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = getContentView(inflater, container)
        return binding.root
    }

    final override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        onViewSetup()
    }

    protected fun dip2px(dpValue: Float): Int {
        val scales = contest.resources.displayMetrics.density
        return (dpValue * scales + 0.5f).toInt()
    }

    protected fun runOnUiThread(runnable: () -> Unit){
        contest.runOnUiThread(runnable)
    }

    protected fun finish(){
        contest.finish()
    }

    protected fun <T: View?> findViewById(@IdRes res: Int): T? {
        return view?.findViewById<T>(res)
    }

    open fun getTitle(): CharSequence = ""

    protected open fun initViewAtTop(view: View){
        var statusbarheight = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            statusbarheight = resources.getDimensionPixelSize(resourceId)
        }
        val params: LinearLayout.LayoutParams = view.layoutParams as LinearLayout.LayoutParams
        params.topMargin = statusbarheight
    }

    protected abstract fun getContentView(inflater: LayoutInflater, container: ViewGroup?): T

    protected open fun onViewSetup(){ }

    protected fun onToast(content: String?) {
        activity?.runOnUiThread {
            Toast.makeText(contest, content, Toast.LENGTH_SHORT).show()
        }
    }
    protected fun onToast(@StringRes content: Int) {
        onToast(resources.getText(content).toString())
    }
    protected fun onToast(@StringRes content: Int, code: Int) {
        val contentShow = (resources.getText(content).toString() + "($code)")
        onToast(contentShow)
    }
    protected fun onToast(@StringRes content: Int, message: String?, code: Int) {
        if (message != null) {
            val contentShow = resources.getText(content).toString() + "，$message($code)"
            onToast(contentShow)
        } else {
            onToast(content, code)
        }
    }

    protected open fun saveExplosion(e: Throwable?, code: Int) {
        try {
            e?.let {
                val exceptionLog: JSONObject
                var exceptionLogContent = JSONArray()
                val exception = File(
                    activity?.applicationContext?.getExternalFilesDir("log")?.path,
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
                ConfigManager(contest).putString("last_exception", configString.toString())
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