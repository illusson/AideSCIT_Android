package io.github.sgpublic.aidescit.activity

import android.app.AlertDialog
import io.github.sgpublic.aidescit.Application
import io.github.sgpublic.aidescit.R
import io.github.sgpublic.aidescit.base.BaseActivity
import io.github.sgpublic.aidescit.core.manager.ConfigManager
import io.github.sgpublic.aidescit.core.module.EvaluateModule
import io.github.sgpublic.aidescit.core.module.HeaderInfoModule
import io.github.sgpublic.aidescit.core.module.LoginModule
import io.github.sgpublic.aidescit.core.module.UpdateModule
import io.github.sgpublic.aidescit.core.util.ActivityCollector
import io.github.sgpublic.aidescit.databinding.ActivityWelcomeBinding
import java.util.*

class Welcome : BaseActivity<ActivityWelcomeBinding>(), UpdateModule.Callback {
    private val isFinished: LinkedList<Boolean> = LinkedList()
    private var totalCount: Int = 4

    private var isLogin = false

    private lateinit var module: HeaderInfoModule

    override fun onActivityCreated(hasSavedInstanceState: Boolean) {
        if (ConfigManager.AGREEMENT_SHOWN > 0){
            appSetup()
            return
        }
        Timer().schedule(object : TimerTask() {
            override fun run() {
                appSetup()
            }
        }, 500)
    }

    private fun appSetup(){
        ConfigManager.updateAgreementShown()
        module = HeaderInfoModule(ConfigManager.ACCESS_TOKEN)
        getSemesterInfo()
        getSentence()
        checkLogin()
        UpdateModule().getUpdate(this)
    }

    override fun onViewSetup() {
        initViewAtBottom(ViewBinding.welcomeAbout)
    }

    private fun checkEvaluate(){
        val helper = EvaluateModule(ConfigManager.ACCESS_TOKEN)
        helper.check(object : EvaluateModule.CheckCallback {
            override fun onFailure(code: Int, message: String?, e: Throwable?) {
                ConfigManager.EVALUATE_COUNT = 0
                onFinished(true)
            }

            override fun onResult(count: Int) {
                ConfigManager.EVALUATE_COUNT = count
                onFinished(true)
            }
        })
    }

    private fun getSentence(){
        module.getSentence(object : HeaderInfoModule.Callback {
            override fun onFailure(code: Int, message: String?, e: Throwable?) {
                onFinished(true)
            }

            override fun onSentenceResult(sentence: String, from: String) {
                ConfigManager.HITOKOTO_SENTENCE = sentence
                ConfigManager.HITOKOTO_FROM = from
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
            String.format(this@Welcome.getString(updateHeader[force]), sizeString) + "\n" +
                    this@Welcome.getString(R.string.text_update_version) + verName + "\n" +
                    this@Welcome.getString(R.string.text_update_changelog) + "\n" + changelog
        )
        builder.setPositiveButton(R.string.text_ok) { _, _ ->
            UpdateModule().handleDownload(dlUrl)
            Application.onToast(this, R.string.title_update_is_download)
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
        module.getSemesterInfo(object : HeaderInfoModule.Callback {
            override fun onFailure(code: Int, message: String?, e: Throwable?) {
                onFinished(false)
            }

            override fun onSemesterInfoResult(
                semester: Int, schoolYear: String, week: Int,
                startDate: Date, scheduleCanInquire: Boolean
            ) {
                ConfigManager.SCHOOL_YEAR = schoolYear
                ConfigManager.SEMESTER = semester
                ConfigManager.SCHEDULE_CAN_INQUIRE = scheduleCanInquire
                ConfigManager.WEEK = week
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
        isLogin = ConfigManager.IS_LOGIN
        if (isLogin){
            val helper = LoginModule()
            helper.refreshToken(ConfigManager.ACCESS_TOKEN,
                ConfigManager.REFRESH_TOKEN, object : LoginModule.Callback {
                    override fun onFailure(code: Int, message: String?, e: Throwable?) {
                        if (code == -100){
                            Login.startActivity(this@Welcome)
                            Application.onToast(this@Welcome, message)
                        } else {
                            onFinished(false)
                        }
                    }

                    override fun onResult(access: String, refresh: String) {
                        ConfigManager.ACCESS_TOKEN = access
                        ConfigManager.REFRESH_TOKEN = refresh
                        ConfigManager.updateToken()
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
        if (isFinished.size < totalCount) {
            return
        }
        if (isLogin){
            runOnUiThread {
                Main.startActivity(this@Welcome)
            }
        } else {
            runOnUiThread {
                Login.startActivity(this@Welcome)
            }
        }
    }

    override fun onCreateViewBinding(): ActivityWelcomeBinding =
        ActivityWelcomeBinding.inflate(layoutInflater)
}