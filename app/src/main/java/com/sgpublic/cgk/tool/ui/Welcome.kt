package com.sgpublic.cgk.tool.ui

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.sgpublic.cgk.tool.R
import com.sgpublic.cgk.tool.base.ActivityCollector
import com.sgpublic.cgk.tool.base.BaseActivity
import com.sgpublic.cgk.tool.helper.*
import com.sgpublic.cgk.tool.manager.ConfigManager


class Welcome : BaseActivity(), UpdateHelper.Callback {
    private val isFinished: MutableList<Boolean> = mutableListOf()
    private var totalCount: Int = 5

    private var grand = true
    private var isLogin = false
    private var sentence: String? = null
    private var from: String? = null
    private var week: Int? = null

    private lateinit var helper: HeaderInfoHelper

    override fun onActivityCreate(savedInstanceState: Bundle?) {
        helper = HeaderInfoHelper(this@Welcome)
        helper.setup(ConfigManager(this@Welcome).getString("access_token"),
            object : HeaderInfoHelper.Callback {
                override fun onSetupFinish() {
                    UpdateHelper(this@Welcome).getUpdate(0, this@Welcome)
                    checkLogin()
                    getSentence()
                    getWeek()
                    getSemesterInfo()
                }

                override fun onSetupTimeout() {
                    runOnUiThread {
                        onToast(this@Welcome, R.string.error_setup_timeout)
                    }
                }
            }
        )
    }

    override fun getContentView() = R.layout.activity_welcome

    override fun onSetSwipeBackEnable() = false

    private fun getSentence(){
        helper.getSentence(object : HeaderInfoHelper.Callback{
            override fun onFailure(code: Int, message: String?, e: Exception?) {
                saveExplosion(e, code)
                onFinished(false)
            }

            override fun onSentenceResult(sentence: String, from: String) {
                this@Welcome.sentence = sentence
                this@Welcome.from = from
                onFinished(true)
            }
        })
    }

    override fun onUpdateFailure(code: Int, message: String?, e: Throwable?) {
        onFinished(true)
    }

    override fun onUpToDate() {
        onFinished(true)
    }

    override fun onUpdate(force: Int, verName: String, sizeString: String, changelog: String, dlUrl: String) {
        val updateHeader = intArrayOf(
            R.string.text_update_content,
            R.string.text_update_content_force
        )
        val builder = AlertDialog.Builder(this@Welcome)
        builder.setTitle(R.string.title_update_get)
        builder.setCancelable(force == 0)
        builder.setMessage(
            java.lang.String.format(this@Welcome.getString(updateHeader[force]), sizeString) + "\n" +
                    this@Welcome.getString(R.string.text_update_version) + verName + "\n" +
                    this@Welcome.getString(R.string.text_update_changelog) + "\n" + changelog
        )
        builder.setPositiveButton(R.string.text_ok) { _, _ ->
            UpdateHelper(applicationContext).handleDownload(dlUrl)
            onToast(this@Welcome, R.string.title_update_is_download)
            onFinished(true)
        }
        builder.setNegativeButton(R.string.text_cancel) { _, _ ->
            if (force == 1) {
                ActivityCollector.finishAll()
            } else {
                onFinished(true)
            }
        }
        runOnUiThread {
            builder.show()
        }
    }

    private fun getWeek(){
        helper.getWeek(object : HeaderInfoHelper.Callback{
            override fun onFailure(code: Int, message: String?, e: Exception?) {
                saveExplosion(e, code)
                onFinished(false)
            }

            override fun onWeekResult(week: Int) {
                this@Welcome.week = week
                onFinished(true)
            }
        })
    }

    private fun getSemesterInfo(){
        helper.getSemesterInfo(object : HeaderInfoHelper.Callback{
            override fun onFailure(code: Int, message: String?, e: Exception?) {
                saveExplosion(e, code)
                onFinished(false)
            }

            override fun onSemesterResult(semester: Int, schoolYear: String) {
                ConfigManager(this@Welcome)
                    .putString("school_year", schoolYear)
                    .putInt("semester", semester)
                    .apply()
                onFinished(true)
            }
        })
    }

    private fun checkLogin() {
        val permissions = intArrayOf(
            ContextCompat.checkSelfPermission(this@Welcome, Manifest.permission.WRITE_CALENDAR),
            ContextCompat.checkSelfPermission(this@Welcome, Manifest.permission.READ_CALENDAR)
        )
        for (permission in permissions) {
            grand = grand && permission == PackageManager.PERMISSION_GRANTED
        }
        isLogin = ConfigManager(this@Welcome).getString("access_token", "") != ""
        val manager = ConfigManager(this@Welcome);
        if (manager.getLong("token_expired", 0) < APIHelper.getTS()){
            val helper = LoginHelper(this@Welcome)
            helper.refreshToken(ConfigManager(this@Welcome), object : LoginHelper.Callback {
                override fun onFailure(code: Int, message: String?, e: Exception?) {
                    onFinished(false)
                }

                override fun onResult(access: String, refresh: String) {
                    ConfigManager(this@Welcome)
                        .putString("access_token", access)
                        .putString("refresh_token", refresh)
                        .putLong("token_expired", System.currentTimeMillis() + 2591990L)
                        .apply()
                    onFinished(true)
                }
            })
        } else {
            onFinished(true)
        }
    }

    private fun onFinished(result: Boolean) {
        isFinished.add(result)
        if (isFinished.size >= totalCount) {
            var allFinished = true
            for (index in isFinished) {
                allFinished = allFinished && index
            }
            if (allFinished) {
                ConfigManager(this@Welcome)
                    .putInt("week", week!!)
                    .putString("sentence", sentence.toString())
                    .putString("from", from.toString())
                    .apply()
                if (isLogin){
                    runOnUiThread {
                        Main.startActivity(this@Welcome)
                    }
                } else {
                    runOnUiThread {
                        Login.startActivity(this@Welcome, grand)
                    }
                }
                return
            }
            runOnUiThread {
                onToast(this@Welcome, R.string.error_setup_failed)
            }
        }
    }
}