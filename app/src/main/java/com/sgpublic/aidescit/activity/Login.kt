package com.sgpublic.aidescit.activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.InputMethodManager
import com.facebook.rebound.SimpleSpringListener
import com.facebook.rebound.Spring
import com.facebook.rebound.SpringConfig
import com.facebook.rebound.SpringSystem
import com.sgpublic.aidescit.R
import com.sgpublic.aidescit.widget.ActivityCollector
import com.sgpublic.aidescit.base.BaseActivity
import com.sgpublic.aidescit.databinding.ActivityLoginBinding
import com.sgpublic.aidescit.helper.APIHelper
import com.sgpublic.aidescit.helper.LoginHelper
import com.sgpublic.aidescit.helper.UserInfoHelper
import com.sgpublic.aidescit.manager.ConfigManager
import java.util.*

class Login : BaseActivity<ActivityLoginBinding>(), LoginHelper.Callback {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        ConfigManager.putBoolean("is_login", false)
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
        binding.loginUsername.setText(ConfigManager.getString("username"))
        binding.loginPassword.setText(ConfigManager.getString("password"))

        binding.loginActionCover.setOnClickListener { onAction() }

        binding.loginPasswordClear.setOnClickListener { onPasswordClear() }

        binding.loginPasswordVisible.setOnClickListener { onPasswordVisible() }

        binding.loginUsernameClear.setOnClickListener { onUsernameClear() }

        binding.loginAgreementCheck.movementMethod = LinkMovementMethod.getInstance()
        initViewAtBottom(binding.loginAgreementCheckBase)

//        login_button_access.setOnClickListener {
//            ActivityCompat.requestPermissions(
//                this@Login, arrayOf(
//                    Manifest.permission.READ_PHONE_STATE,
//                    //Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                    Manifest.permission.WRITE_CALENDAR,
//                    Manifest.permission.READ_CALENDAR
//                ), 1
//            )
//        }
    }

    private fun onAction(){
        if (!binding.loginAgreementCheck.isChecked){
            currentFocus?.let {
                (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                    .hideSoftInputFromWindow(it.windowToken, 0)
            }
            onToast(R.string.text_login_agreement)
            return
        }
        if (binding.loginUsername.text.toString() == "" || binding.loginPassword.text.toString() == "") {
            onToast(R.string.text_login_empty)
            return
        }
        setLoadingState(true)
        LoginHelper(this@Login).login(
            binding.loginUsername.text.toString(),
            binding.loginPassword.text.toString(),
            this
        )
    }

    override fun onFailure(code: Int, message: String?, e: Exception?) {
        onToast(R.string.text_login_failure, message, code)
        setLoadingState(false)
    }

    override fun onResult(access: String, refresh: String) {
        val helper = UserInfoHelper(
            this@Login, binding.loginUsername.text.toString(), binding.loginUsername.text.toString()
        )
        helper.getUserInfo(access, object : UserInfoHelper.Callback{
            override fun onFailure(code: Int, message: String?, e: Exception?) {
                setLoadingState(false)
                onToast(R.string.text_login_failure, message, code)
            }

            override fun onResult(name: String, faculty: String, specialty: String, userClass: String, grade: Int) {
                ConfigManager.putBoolean("is_login", true)
                ConfigManager.putString("username", binding.loginUsername.text.toString())
                ConfigManager.putString("access_token", access)
                ConfigManager.putString("refresh_token", refresh)
                ConfigManager.putLong("token_expired", APIHelper.getTS() + 2592000000L)
                ConfigManager.putString("name", name)
                ConfigManager.putString("faculty_name", faculty)
                ConfigManager.putString("specialty_name", specialty)
                ConfigManager.putString("class_name", userClass)
                ConfigManager.putInt("grade", grade)
                setLoadingState(false)
                Main.startActivity(this@Login)
            }
        })
    }

//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
//        var isGranted = true
//        for (grantResult in grantResults) {
//            isGranted = isGranted && grantResult == PackageManager.PERMISSION_GRANTED
//        }
//        if (isGranted) {
//            login_button_access.isEnabled = false
//            val animation1 = AlphaAnimation(1.0f, 0.0f)
//            animation1.duration = 500
//            animation1.fillAfter = false
//            login_permission.startAnimation(animation1)
//            login_permission.visibility = View.GONE
//            Handler().postDelayed({
//                val animation2 = AlphaAnimation(0.0f, 1.0f)
//                animation2.duration = 500
//                animation2.fillAfter = false
//                login_content.startAnimation(animation2)
//                login_content.visibility = View.VISIBLE
//            }, 500)
//            Handler().postDelayed({
//                setAnimation()
//                setEditTextAction()
//            }, 500)
//        } else {
//            onToast(R.string.permission_text_denied)
//        }
//    }

    override fun onBackPressed() {
        val dialog = AlertDialog.Builder(this).run {
            setTitle(R.string.title_check_cancel_login)
            setMessage(R.string.text_check_cancel_login)
            setPositiveButton(R.string.text_ok) { _, _ ->
                ActivityCollector.finishAll()
            }
            setNegativeButton(R.string.text_cancel, null)
        }
        dialog.show()
    }

    private fun onPasswordClear() {
        binding.loginPassword.setText("")
    }

    private fun onUsernameClear() {
        binding.loginUsername.setText("")
        binding.loginPassword.setText("")
        ConfigManager.putString("username", "")
        ConfigManager.putString("password", "")
    }

    private fun onPasswordVisible() {
        if (binding.loginPassword.inputType == 129) {
            binding.loginPassword.inputType = 1
            binding.loginPasswordVisible.setImageResource(R.drawable.pass_visible)
        } else {
            binding.loginPassword.inputType = 129
            binding.loginPasswordVisible.setImageResource(R.drawable.pass_invisible)
        }
    }

    private fun setEditTextAction() {
        binding.loginUsername.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            if (binding.loginUsername.text.toString() != "" && hasFocus) {
                binding.loginUsernameClear.visibility = View.VISIBLE
            } else {
                binding.loginUsernameClear.visibility = View.INVISIBLE
            }
        }
        binding.loginUsername.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (binding.loginUsername.text.toString() != "") {
                    binding.loginUsernameClear.visibility = View.VISIBLE
                } else {
                    binding.loginUsernameClear.visibility = View.INVISIBLE
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })
        binding.loginPassword.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.loginPasswordVisible.visibility = View.VISIBLE
            } else {
                binding.loginPasswordVisible.visibility = View.INVISIBLE
            }
            if (binding.loginPassword.text.toString() != "" && hasFocus) {
                binding.loginPasswordClear.visibility = View.VISIBLE
            } else {
                binding.loginPasswordClear.visibility = View.INVISIBLE
            }
        }
        binding.loginPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (binding.loginPassword.text.toString() != "") {
                    binding.loginPasswordClear.visibility = View.VISIBLE
                } else {
                    binding.loginPasswordClear.visibility = View.INVISIBLE
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })
        binding.loginPassword.setOnEditorActionListener { _, _, _ ->
            onAction()
            false
        }
    }

    private fun setLoadingState(isLoading: Boolean) {
        runOnUiThread {
            binding.loginUsername.isEnabled = !isLoading
            binding.loginPassword.isEnabled = !isLoading
            binding.loginLoading.isEnabled = !isLoading
            binding.loginActionCover.isEnabled = !isLoading
            binding.loginLoading.visibility = View.VISIBLE
            binding.loginAction.visibility = View.VISIBLE
            if (isLoading) {
                binding.loginLoading.animate().alpha(1f).setDuration(200).setListener(null)
                binding.loginAction.animate().alpha(0f).setDuration(200).setListener(null)
                binding.loginAction.visibility = View.INVISIBLE
            } else {
                binding.loginLoading.animate().alpha(0f).setDuration(200).setListener(null)
                binding.loginAction.animate().alpha(1f).setDuration(200).setListener(null)
                binding.loginLoading.visibility = View.INVISIBLE
            }
//            Timer().schedule(object : TimerTask() {
//                override fun run() {
//                    runOnUiThread {
//                        binding.loginLoading.isEnabled = true
//                        binding.loginActionCover.isEnabled = true
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
                            binding.loginUsernameBase.y = dip2px(800F) - scale
                        }
                    })
                    spring.endValue = dip2px(1200F).toDouble()
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
                            binding.loginPasswordBase.y = dip2px(870F) - scale
                        }
                    })
                    spring.endValue = dip2px(1200F).toDouble()
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
                            binding.loginActionBase.y = dip2px(980F) - scale
                        }
                    })
                    spring.endValue = dip2px(1200F).toDouble()
                }
            }
        }, 800)
        Timer().schedule(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    setAnimateState(true, 300, binding.loginAgreementCheck)
                }
            }
        }, 1400)
    }

    override fun isActivityAtBottom() = false

    companion object{
        @JvmStatic
        fun startActivity(context: Context){
            val intent = Intent().run {
                setClass(context, Login::class.java)
            }
            context.startActivity(intent)
        }
    }
}