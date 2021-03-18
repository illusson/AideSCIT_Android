package com.sgpublic.scit.tool.activity

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.kongzue.dialogx.DialogX
import com.kongzue.dialogx.dialogs.MessageDialog
import com.kongzue.dialogx.interfaces.OnBindView
import com.kongzue.dialogx.interfaces.OnDialogButtonClickListener
import com.sgpublic.scit.tool.R
import com.sgpublic.scit.tool.base.ActivityCollector
import com.sgpublic.scit.tool.base.BaseActivity
import com.sgpublic.scit.tool.base.MyLog
import com.sgpublic.scit.tool.helper.*
import com.sgpublic.scit.tool.manager.ConfigManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_welcome.*
import java.util.*

class Welcome : BaseActivity(), UpdateHelper.Callback {
    private val isFinished: MutableList<Boolean> = mutableListOf()
    private var totalCount: Int = 3

//    private var grand = true
    private var isLogin = false

    private lateinit var helper: HeaderInfoHelper

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        if (ConfigManager(this@Welcome).getBoolean("agreement_shown")){
            appSetup()
        } else {
            Handler().postDelayed({
                MessageDialog.build()
                    .setCustomView(object : OnBindView<MessageDialog>(R.layout.dialog_agreement) {
                        override fun onBind(dialog: MessageDialog?, v: View?) {
                            v?.findViewById<TextView>(R.id.dialog_agreement_content_1)?.movementMethod = LinkMovementMethod.getInstance()
                        }
                    })
                    .setCancelable(false)
                    .setOkButton(R.string.text_agreement_agree, OnDialogButtonClickListener { dialog, _ ->
                        dialog.dismiss()
                        appSetup()
                        return@OnDialogButtonClickListener true
                    })
                    .setCancelButton(R.string.text_agreement_disagree, OnDialogButtonClickListener{ _, _ ->
                        finish()
                        return@OnDialogButtonClickListener true
                    })
                    .show()
            }, 500L)
        }
    }

    private fun appSetup(){
        ConfigManager(this@Welcome)
            .putBoolean("agreement_shown", true)
            .apply()
        helper = HeaderInfoHelper(this@Welcome, ConfigManager(this@Welcome).getString("access_token"))
        getSemesterInfo()
        getSentence()
        checkLogin()
    }

    override fun onViewSetup() {
        super.onViewSetup()
        initViewAtBottom(welcome_about)
    }

    override fun getContentView() = R.layout.activity_welcome

    override fun onSetSwipeBackEnable() = false

    private fun checkEvaluate(){
        val helper = EvaluateHelper(this@Welcome, ConfigManager(this@Welcome).getString(
            "access_token", ""
        ))
        helper.check(object : EvaluateHelper.CheckCallback {
            override fun onFailure(code: Int, message: String?, e: Exception?) {
                ConfigManager(this@Welcome)
                    .putInt("evaluate_count", 0)
                    .apply()
                onFinished(true)
            }

            override fun onResult(count: Int) {
                ConfigManager(this@Welcome)
                    .putInt("evaluate_count", count)
                    .apply()
                onFinished(true)
            }
        })
    }

    private fun getSentence(){
        helper.getSentence(object : HeaderInfoHelper.Callback {
            override fun onFailure(code: Int, message: String?, e: Exception?) {
                onFinished(true)
            }

            override fun onSentenceResult(sentence: String, from: String) {
                ConfigManager(this@Welcome)
                    .putString("sentence", sentence)
                    .putString("from", from)
                    .apply()
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
            onToast(R.string.title_update_is_download)
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


    private fun getSemesterInfo(){
        helper.getSemesterInfo(object : HeaderInfoHelper.Callback {
            override fun onFailure(code: Int, message: String?, e: Exception?) {
                saveExplosion(e, code)
                onFinished(false)
            }

            override fun onSemesterInfoResult(semester: Int, schoolYear: String, week: Int, startDate: Date) {
                ConfigManager(this@Welcome)
                    .putString("school_year", schoolYear)
                    .putInt("semester", semester)
                    .putInt("week", week)
                    .apply()
                onFinished(true)
            }
        })
    }

    private fun checkLogin(): Boolean {
//        val permissions = intArrayOf(
//            ContextCompat.checkSelfPermission(this@Welcome, Manifest.permission.WRITE_CALENDAR),
//            ContextCompat.checkSelfPermission(this@Welcome, Manifest.permission.READ_CALENDAR)
//        )
//        for (permission in permissions) {
//            grand = grand && permission == PackageManager.PERMISSION_GRANTED
//        }
        isLogin = ConfigManager(this@Welcome).getBoolean("is_login")
        val manager = ConfigManager(this@Welcome)
        if (isLogin && manager.getLong("token_expired", 0) < APIHelper.getTS()){
            val helper = LoginHelper(this@Welcome)
            helper.refreshToken(ConfigManager(this@Welcome), object : LoginHelper.Callback {
                override fun onFailure(code: Int, message: String?, e: Exception?) {
                    MyLog.d(message, e)
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
            Thread {
                Thread.sleep(1000)
                onFinished(true)
            }.start()
        }
        return isLogin
    }

    private fun onFinished(result: Boolean) {
        isFinished.add(result)
        if (isFinished.size >= totalCount) {
            var allFinished = true
            for (index in isFinished) {
                allFinished = allFinished && index
            }
            if (allFinished) {
                if (isLogin){
                    runOnUiThread {
                        Main.startActivity(this@Welcome)
                    }
                } else {
                    runOnUiThread {
                        Login.startActivity(this@Welcome)//, grand)
                    }
                }
                return
            }
            runOnUiThread {
                onToast(R.string.error_setup_failed)
            }
        }
    }
}