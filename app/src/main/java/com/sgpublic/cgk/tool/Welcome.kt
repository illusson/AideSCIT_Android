package com.sgpublic.cgk.tool

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import androidx.core.content.ContextCompat
import com.sgpublic.cgk.tool.base.ActivityCollector
import com.sgpublic.cgk.tool.base.BaseActivity
import com.sgpublic.cgk.tool.data.UserInfoData
import com.sgpublic.cgk.tool.helper.HeaderInfoHelper
import com.sgpublic.cgk.tool.helper.LoginHelper
import com.sgpublic.cgk.tool.helper.UpdateHelper
import com.sgpublic.cgk.tool.helper.UserInfoHelper
import com.sgpublic.cgk.tool.manager.ConfigManager
import com.umeng.analytics.MobclickAgent


class Welcome : BaseActivity(), UpdateHelper.Callback {
    private val isFinished: MutableList<Boolean> = mutableListOf()
    private var finishedCount: Int = 0
    private var totalCount: Int = 5

    private var grand = true
    private var isLogin = false
    private var sentence: String? = null
    private var from: String? = null
    private var week: Int? = null

    override fun onActivityCreate(savedInstanceState: Bundle?) {
        checkLogin()
        getSentence()
        getWeek()
        UpdateHelper(this@Welcome).getUpdate(0, this)
    }

    override fun getContentView() = R.layout.activity_welcome

    override fun onSetSwipeBackEnable() = false

    private fun getSentence(){
        HeaderInfoHelper(
            this@Welcome,
            ConfigManager(this@Welcome).getString("username")
        ).getSentence(object : HeaderInfoHelper.Callback{
            override fun onFailure(code: Int, message: String?, e: Exception?) {
                saveExplosion(e, code)
                onFinished(false)
                e?.printStackTrace()
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
        val helper = HeaderInfoHelper(this@Welcome)
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

    private fun checkLogin() {
        val permissions = intArrayOf(
            //ContextCompat.checkSelfPermission(this@Welcome, Manifest.permission.WRITE_EXTERNAL_STORAGE),
            ContextCompat.checkSelfPermission(this@Welcome, Manifest.permission.WRITE_CALENDAR),
            ContextCompat.checkSelfPermission(this@Welcome, Manifest.permission.READ_CALENDAR)
        )
        for (permission in permissions) {
            grand = grand && permission == PackageManager.PERMISSION_GRANTED
        }
        isLogin = ConfigManager(this@Welcome).getBoolean("is_login")
        if (isLogin){
            val username = ConfigManager(this@Welcome).getString("username")
            val password = ConfigManager(this@Welcome).getString("password")

            LoginHelper(this@Welcome).login(username, password, object : LoginHelper.Callback{
                override fun onFailure(code: Int, message: String?, e: Exception?) {
                    onToast(this@Welcome, R.string.text_login_failure, message, code)
                    onFinished(false)
                }

                override fun onResult(session: String, identity: String) {
                    this@Welcome.session = session
                    ConfigManager(this@Welcome)
                        .putString("identity", identity)
                        .apply()
                    UserInfoHelper(this@Welcome, username, session).getUserInfo(identity, object : UserInfoHelper.Callback{
                        override fun onFailure(code: Int, message: String?, e: Exception?) {
                            onFinished(true)
                        }

                        override fun onResult(name: String, faculty: UserInfoData, specialty: UserInfoData, userClass: UserInfoData, grade: Int, schoolYear: String, semester: Int) {
                            ConfigManager(this@Welcome)
                                .putString("name", name)
                                .putString("faculty_name", faculty.name)
                                .putLong("faculty_id", faculty.id)
                                .putString("specialty_name", specialty.name)
                                .putLong("specialty_id", specialty.id)
                                .putString("class_name", userClass.name)
                                .putLong("class_id", userClass.id)
                                .putInt("grade", grade)
                                .putString("school_year", schoolYear)
                                .putInt("semester", semester)
                                .apply()
                            onFinished(true)
                        }
                    })
                }
            })

        } else {
            totalCount = 4
        }
        onFinished(true)
    }

    private fun onFinished(result: Boolean) {
        runOnUiThread{
            finishedCount++
            isFinished.add(result)
            if (finishedCount >= totalCount) {
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
                            Main.startActivity(this@Welcome, this@Welcome.session!!)
                        } else {
                            Login.startActivity(this@Welcome, grand)
                        }
                        return@runOnUiThread
                    }
                }
                onToast(this@Welcome, R.string.error_setup)
            }
        }
    }
}