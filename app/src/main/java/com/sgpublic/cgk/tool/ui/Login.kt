package com.sgpublic.cgk.tool.ui

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.animation.AlphaAnimation
import androidx.core.app.ActivityCompat
import com.facebook.rebound.SimpleSpringListener
import com.facebook.rebound.Spring
import com.facebook.rebound.SpringConfig
import com.facebook.rebound.SpringSystem
import com.sgpublic.cgk.tool.R
import com.sgpublic.cgk.tool.base.ActivityCollector
import com.sgpublic.cgk.tool.base.BaseActivity
import com.sgpublic.cgk.tool.helper.APIHelper
import com.sgpublic.cgk.tool.helper.LoginHelper
import com.sgpublic.cgk.tool.helper.UserInfoHelper
import com.sgpublic.cgk.tool.manager.ConfigManager
import kotlinx.android.synthetic.main.activity_login.*

class Login : BaseActivity(), LoginHelper.Callback {

    override fun onActivityCreate(savedInstanceState: Bundle?) {
        ConfigManager(this@Login)
            .putString("access_token", "")
            .putString("refresh_token", "")
            .putLong("token_expired", 0)
            .putString("name", "")
            .putString("faculty_name", "")
            .putString("specialty_name", "")
            .putString("class_name", "")
            .putInt("grade", 0)
            .apply()
        if (!intent.getBooleanExtra("grand", false)) {
            login_permission.visibility = View.VISIBLE
            login_content.visibility = View.GONE
        } else {
            login_permission.visibility = View.GONE
            login_content.visibility = View.VISIBLE
            setAnimation()
            setEditTextAction()
        }
    }

    override fun onViewSetup() {
        super.onViewSetup()
        login_username.setText(ConfigManager(this@Login).getString("username"))
        login_password.setText(ConfigManager(this@Login).getString("password"))

        login_action_cover.setOnClickListener { onAction() }

        login_password_clear.setOnClickListener { onPasswordClear() }

        login_password_visible.setOnClickListener { onPasswordVisible() }

        login_username_clear.setOnClickListener { onUsernameClear() }

        login_button_access.setOnClickListener {
            ActivityCompat.requestPermissions(
                this@Login, arrayOf(
                    Manifest.permission.READ_PHONE_STATE,
                    //Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_CALENDAR,
                    Manifest.permission.READ_CALENDAR
                ), 1
            )
        }
    }

    private fun onAction(){
        if (login_username.text.toString() == "" || login_password.text.toString() == "") {
            onToast(this@Login, R.string.text_login_empty)
        } else {
            setLoadingState(true)
            LoginHelper(this@Login).login(
                login_username.text.toString(),
                login_password.text.toString(),
                this
            )
        }
    }

    override fun onFailure(code: Int, message: String?, e: Exception?) {
        onToast(this@Login, R.string.text_login_failure, message, code)
        setLoadingState(false)
    }

    override fun onResult(access: String, refresh: String) {
        val helper = UserInfoHelper(this@Login, login_username.text.toString(), login_username.text.toString());
        helper.getUserInfo(access, object : UserInfoHelper.Callback{
            override fun onFailure(code: Int, message: String?, e: Exception?) {
                setLoadingState(false)
                onToast(this@Login, R.string.text_login_failure, message, code)
            }

            override fun onResult(name: String, faculty: String, specialty: String, userClass: String, grade: Int) {
                ConfigManager(this@Login)
                    .putString("access_token", access)
                    .putString("refresh_token", refresh)
                    .putLong("token_expired", APIHelper.getTS() + 2592000000L)
                    .putString("name", name)
                    .putString("faculty_name", faculty)
                    .putString("specialty_name", specialty)
                    .putString("class_name", userClass)
                    .putInt("grade", grade)
                    .apply()
                setLoadingState(false)
                Main.startActivity(this@Login)
            }
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        var isGranted = true
        for (grantResult in grantResults) {
            isGranted = isGranted && grantResult == PackageManager.PERMISSION_GRANTED
        }
        if (isGranted) {
            login_button_access.isEnabled = false
            val animation1 = AlphaAnimation(1.0f, 0.0f)
            animation1.duration = 500
            animation1.fillAfter = false
            login_permission.startAnimation(animation1)
            login_permission.visibility = View.GONE
            Handler().postDelayed({
                val animation2 = AlphaAnimation(0.0f, 1.0f)
                animation2.duration = 500
                animation2.fillAfter = false
                login_content.startAnimation(animation2)
                login_content.visibility = View.VISIBLE
            }, 500)
            Handler().postDelayed({
                setAnimation()
                setEditTextAction()
            }, 500)
        } else {
            onToast(this@Login, R.string.permission_text_denied)
        }
    }

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
        login_password.setText("")
    }

    private fun onUsernameClear() {
        login_username.setText("")
        login_password.setText("")
        ConfigManager(this@Login)
            .putString("username", "")
            .putString("password", "")
            .apply()
    }

    private fun onPasswordVisible() {
        if (login_password.inputType == 129) {
            login_password.inputType = 1
            login_password_visible.setImageResource(R.drawable.pass_visible)
        } else {
            login_password.inputType = 129
            login_password_visible.setImageResource(R.drawable.pass_invisible)
        }
    }

    private fun setEditTextAction() {
        login_username.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            if (login_username.text.toString() != "" && hasFocus) {
                login_username_clear.visibility = View.VISIBLE
            } else {
                login_username_clear.visibility = View.INVISIBLE
            }
        }
        login_username.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (login_username.text.toString() != "") {
                    login_username_clear.visibility = View.VISIBLE
                } else {
                    login_username_clear.visibility = View.INVISIBLE
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })
        login_password.onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                login_password_visible.visibility = View.VISIBLE
            } else {
                login_password_visible.visibility = View.INVISIBLE
            }
            if (login_password.text.toString() != "" && hasFocus) {
                login_password_clear.visibility = View.VISIBLE
            } else {
                login_password_clear.visibility = View.INVISIBLE
            }
        }
        login_password.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (login_password.text.toString() != "") {
                    login_password_clear.visibility = View.VISIBLE
                } else {
                    login_password_clear.visibility = View.INVISIBLE
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })
        login_password.setOnEditorActionListener { _, _, _ ->
            onAction()
            false
        }
    }

    private fun setLoadingState(isLoading: Boolean) {
        runOnUiThread{
            login_loading.isEnabled = false
            login_action_cover.isEnabled = false
            login_loading.visibility = View.VISIBLE
            login_action.visibility = View.VISIBLE
            if (isLoading) {
                login_loading.animate().alpha(1f).setDuration(200).setListener(null)
                login_action.animate().alpha(0f).setDuration(200).setListener(null)
                login_action.visibility = View.INVISIBLE
            } else {
                login_loading.animate().alpha(0f).setDuration(200).setListener(null)
                login_action.animate().alpha(1f).setDuration(200).setListener(null)
                login_loading.visibility = View.INVISIBLE
            }
            Handler().postDelayed({
                login_loading.isEnabled = true
                login_action_cover.isEnabled = true
            }, 500)
        }
    }

    private fun setAnimation() {
        Handler().postDelayed({
            val springSystem: SpringSystem = SpringSystem.create()
            val spring: Spring = springSystem.createSpring()
            spring.springConfig = SpringConfig.fromOrigamiTensionAndFriction(10.0, 5.0)
            spring.addListener(object : SimpleSpringListener() {
                override fun onSpringUpdate(spring: Spring) {
                    val value = spring.currentValue.toFloat()
                    val scale = 1f + value * 0.5f
                    login_username_base.y = dip2px(applicationContext, 800F) - scale
                }
            })
            spring.endValue = dip2px(applicationContext, 1200F).toDouble()
        }, 500)
        Handler().postDelayed({
            val springSystem: SpringSystem = SpringSystem.create()
            val spring: Spring = springSystem.createSpring()
            spring.springConfig = SpringConfig.fromOrigamiTensionAndFriction(10.0, 5.0)
            spring.addListener(object : SimpleSpringListener() {
                override fun onSpringUpdate(spring: Spring) {
                    val value = spring.currentValue.toFloat()
                    val scale = 1f + value * 0.5f
                    login_password_base.y = dip2px(applicationContext, 870F) - scale
                }
            })
            spring.endValue = dip2px(applicationContext, 1200F).toDouble()
        }, 650)
        Handler().postDelayed({
            val springSystem: SpringSystem = SpringSystem.create()
            val spring: Spring = springSystem.createSpring()
            spring.springConfig = SpringConfig.fromOrigamiTensionAndFriction(10.0, 5.0)
            spring.addListener(object : SimpleSpringListener() {
                override fun onSpringUpdate(spring: Spring) {
                    val value = spring.currentValue.toFloat()
                    val scale = 1f + value * 0.5f
                    login_action_base.y = dip2px(applicationContext, 980F) - scale
                }
            })
            spring.endValue = dip2px(applicationContext, 1200F).toDouble()
        }, 800)
    }

    override fun getContentView() = R.layout.activity_login

    override fun onSetSwipeBackEnable() = false

    companion object{
        @JvmStatic
        fun startActivity(context: Context, grand: Boolean){
            val intent = Intent().run {
                setClass(context, Login::class.java)
                putExtra("grand", grand)
            }
            context.startActivity(intent)
        }
    }
}