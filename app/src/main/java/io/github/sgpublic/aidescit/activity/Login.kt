package io.github.sgpublic.aidescit.activity

import android.content.Context
import android.content.Intent
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.InputMethodManager
import com.facebook.rebound.SimpleSpringListener
import com.facebook.rebound.Spring
import com.facebook.rebound.SpringConfig
import com.facebook.rebound.SpringSystem
import com.lxj.xpopup.XPopup
import io.github.sgpublic.aidescit.Application
import io.github.sgpublic.aidescit.R
import io.github.sgpublic.aidescit.base.BaseActivity
import io.github.sgpublic.aidescit.core.manager.ConfigManager
import io.github.sgpublic.aidescit.core.module.LoginModule
import io.github.sgpublic.aidescit.core.module.UserInfoModule
import io.github.sgpublic.aidescit.core.util.ActivityCollector
import io.github.sgpublic.aidescit.core.util.dp
import io.github.sgpublic.aidescit.databinding.ActivityLoginBinding
import java.util.*

class Login : BaseActivity<ActivityLoginBinding>(), LoginModule.Callback {

    override fun onActivityCreated(hasSavedInstanceState: Boolean) {
        ConfigManager.IS_LOGIN = false
//        if (!intent.getBooleanExtra("grand", false)) {
//            login_permission.visibility = View.VISIBLE
//            login_content.visibility = View.GONE
//        } else {
//            login_permission.visibility = View.GONE
//            login_content.visibility = View.VISIBLE
//        }
        setAnimation()
        setEditTextAction()
    }

    override fun onViewSetup() {
        ViewBinding.loginUsername.setText(ConfigManager.USERNAME)

        ViewBinding.loginActionCover.setOnClickListener { onAction() }

        ViewBinding.loginPasswordClear.setOnClickListener { onPasswordClear() }

        ViewBinding.loginPasswordVisible.setOnClickListener { onPasswordVisible() }

        ViewBinding.loginUsernameClear.setOnClickListener { onUsernameClear() }

        ViewBinding.loginAgreementCheck.movementMethod = LinkMovementMethod.getInstance()
        ViewBinding.loginAgreementCheck.text = Html.fromHtml(
            getString(R.string.text_agreement_summary), Html.FROM_HTML_MODE_LEGACY
        )
        initViewAtBottom(ViewBinding.loginAgreementCheckBase)
    }

    private fun onAction(){
        if (ViewBinding.loginUsername.text.toString() == "" ||
            ViewBinding.loginPassword.text.toString() == "") {
            Application.onToast(this, R.string.text_login_empty)
            return
        }
        if (!ViewBinding.loginAgreementCheck.isChecked){
            currentFocus?.let {
                (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                    .hideSoftInputFromWindow(it.windowToken, 0)
            }
            val popup = XPopup.Builder(this@Login).asConfirm(
                getString(R.string.title_agreement),
                Html.fromHtml(getString(R.string.text_agreement_content_1) + "\n "
                        + getString(R.string.text_agreement_content_2), Html.FROM_HTML_MODE_LEGACY),
                getString(R.string.text_agreement_disagree),
                getString(R.string.text_agreement_agree), {
                    ViewBinding.loginAgreementCheck.isChecked = true
                    onAction()
                }, {
                    Application.onToast(this, R.string.text_login_agreement)
                }, false
            )
            runOnUiThread {
                popup.show()
            }
            return
        }
        setLoadingState(true)
        LoginModule().login(
            ViewBinding.loginUsername.text.toString(),
            ViewBinding.loginPassword.text.toString(),
            this
        )
    }

    override fun onFailure(code: Int, message: String?, e: Throwable?) {
        Application.onToast(this, R.string.text_login_failure, message, code)
        setLoadingState(false)
    }

    override fun onResult(access: String, refresh: String) {
        val helper = UserInfoModule()
        helper.getUserInfo(access, object : UserInfoModule.Callback {
            override fun onFailure(code: Int, message: String?, e: Throwable?) {
                setLoadingState(false)
                Application.onToast(this@Login, R.string.text_login_failure, message, code)
            }

            override fun onResult(name: String, faculty: String, specialty: String, userClass: String, grade: Int) {
                ConfigManager.IS_LOGIN = true
                ConfigManager.USERNAME = ViewBinding.loginUsername.text.toString()
                ConfigManager.ACCESS_TOKEN = access
                ConfigManager.REFRESH_TOKEN = refresh
                ConfigManager.updateToken()
                ConfigManager.USER_NAME = name
                ConfigManager.USER_FACULTY = faculty
                ConfigManager.USER_SPECIALTY = specialty
                ConfigManager.USER_CLASS = userClass
                ConfigManager.USER_GRADE = grade
                setLoadingState(false)
                Main.startActivity(this@Login)
            }
        })
    }

    override fun onBackPressed() {
        XPopup.Builder(this).asConfirm(
            getString(R.string.title_check_cancel_login),
            getString(R.string.text_check_cancel_login)
        ) {
            ActivityCollector.finishAll()
        }.show()
    }

    private fun onPasswordClear() {
        ViewBinding.loginPassword.setText("")
    }

    private fun onUsernameClear() {
        ViewBinding.loginUsername.setText("")
        ViewBinding.loginPassword.setText("")
        ConfigManager.USERNAME = ""
    }

    private fun onPasswordVisible() {
        if (ViewBinding.loginPassword.inputType == 129) {
            ViewBinding.loginPassword.inputType = 1
            ViewBinding.loginPasswordVisible.setImageResource(R.drawable.pass_visible)
        } else {
            ViewBinding.loginPassword.inputType = 129
            ViewBinding.loginPasswordVisible.setImageResource(R.drawable.pass_invisible)
        }
    }

    private fun setEditTextAction() {
        ViewBinding.loginUsername.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            if (ViewBinding.loginUsername.text.toString() != "" && hasFocus) {
                ViewBinding.loginUsernameClear.visibility = View.VISIBLE
            } else {
                ViewBinding.loginUsernameClear.visibility = View.INVISIBLE
            }
        }
        ViewBinding.loginUsername.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (ViewBinding.loginUsername.text.toString() != "") {
                    ViewBinding.loginUsernameClear.visibility = View.VISIBLE
                } else {
                    ViewBinding.loginUsernameClear.visibility = View.INVISIBLE
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })
        ViewBinding.loginPassword.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                ViewBinding.loginPasswordVisible.visibility = View.VISIBLE
            } else {
                ViewBinding.loginPasswordVisible.visibility = View.INVISIBLE
            }
            if (ViewBinding.loginPassword.text.toString() != "" && hasFocus) {
                ViewBinding.loginPasswordClear.visibility = View.VISIBLE
            } else {
                ViewBinding.loginPasswordClear.visibility = View.INVISIBLE
            }
        }
        ViewBinding.loginPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (ViewBinding.loginPassword.text.toString() != "") {
                    ViewBinding.loginPasswordClear.visibility = View.VISIBLE
                } else {
                    ViewBinding.loginPasswordClear.visibility = View.INVISIBLE
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })
        ViewBinding.loginPassword.setOnEditorActionListener { _, _, _ ->
            onAction()
            false
        }
    }

    private fun setLoadingState(isLoading: Boolean) {
        runOnUiThread {
            ViewBinding.loginUsername.isEnabled = !isLoading
            ViewBinding.loginPassword.isEnabled = !isLoading
            ViewBinding.loginLoading.isEnabled = !isLoading
            ViewBinding.loginActionCover.isEnabled = !isLoading
            ViewBinding.loginLoading.visibility = View.VISIBLE
            ViewBinding.loginAction.visibility = View.VISIBLE
            if (isLoading) {
                ViewBinding.loginLoading.animate().alpha(1f).setDuration(200).setListener(null)
                ViewBinding.loginAction.animate().alpha(0f).setDuration(200).setListener(null)
                ViewBinding.loginAction.visibility = View.INVISIBLE
            } else {
                ViewBinding.loginLoading.animate().alpha(0f).setDuration(200).setListener(null)
                ViewBinding.loginAction.animate().alpha(1f).setDuration(200).setListener(null)
                ViewBinding.loginLoading.visibility = View.INVISIBLE
            }
//            Timer().schedule(object : TimerTask() {
//                override fun run() {
//                    runOnUiThread {
//                        ViewBinding.loginLoading.isEnabled = true
//                        ViewBinding.loginActionCover.isEnabled = true
//                    }
//                }
//            }, 500)
        }
    }

    private fun setAnimation() {
        Timer().schedule(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    val springSystem: SpringSystem = SpringSystem.create()
                    val spring: Spring = springSystem.createSpring()
                    spring.springConfig = SpringConfig.fromOrigamiTensionAndFriction(10.0, 5.0)
                    spring.addListener(object : SimpleSpringListener() {
                        override fun onSpringUpdate(spring: Spring) {
                            val value = spring.currentValue.toFloat()
                            val scale = 1f + value * 0.5f
                            ViewBinding.loginUsernameBase.y = 800.dp - scale
                        }
                    })
                    spring.endValue = 1200.dp.toDouble()
                }
            }
        }, 500)
        Timer().schedule(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    val springSystem: SpringSystem = SpringSystem.create()
                    val spring: Spring = springSystem.createSpring()
                    spring.springConfig = SpringConfig.fromOrigamiTensionAndFriction(10.0, 5.0)
                    spring.addListener(object : SimpleSpringListener() {
                        override fun onSpringUpdate(spring: Spring) {
                            val value = spring.currentValue.toFloat()
                            val scale = 1f + value * 0.5f
                            ViewBinding.loginPasswordBase.y = 870.dp - scale
                        }
                    })
                    spring.endValue = 1200.dp.toDouble()
                }
            }
        }, 650)
        Timer().schedule(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    val springSystem: SpringSystem = SpringSystem.create()
                    val spring: Spring = springSystem.createSpring()
                    spring.springConfig = SpringConfig.fromOrigamiTensionAndFriction(10.0, 5.0)
                    spring.addListener(object : SimpleSpringListener() {
                        override fun onSpringUpdate(spring: Spring) {
                            val value = spring.currentValue.toFloat()
                            val scale = 1f + value * 0.5f
                            ViewBinding.loginActionBase.y = 980.dp - scale
                        }
                    })
                    spring.endValue = 1200.dp.toDouble()
                }
            }
        }, 800)
        Timer().schedule(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    setAnimateState(true, 300, ViewBinding.loginAgreementCheck)
                }
            }
        }, 1400)
    }

    override fun onCreateViewBinding(): ActivityLoginBinding =
        ActivityLoginBinding.inflate(layoutInflater)

    companion object{
        @JvmStatic
        fun startActivity(context: Context){
            val intent = Intent().run {
                setClass(context, Login::class.java)
                setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
}