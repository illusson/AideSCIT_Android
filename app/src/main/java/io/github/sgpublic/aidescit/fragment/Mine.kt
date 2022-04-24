package io.github.sgpublic.aidescit.fragment

import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.lxj.xpopup.XPopup
import io.github.sgpublic.aidescit.R
import io.github.sgpublic.aidescit.activity.*
import io.github.sgpublic.aidescit.base.BaseFragment
import io.github.sgpublic.aidescit.core.manager.ConfigManager
import io.github.sgpublic.aidescit.core.module.LoginModule
import io.github.sgpublic.aidescit.core.util.MyLog
import io.github.sgpublic.aidescit.databinding.FragmentMineBinding
import java.util.*

class Mine(contest: AppCompatActivity) : BaseFragment<FragmentMineBinding>(contest) {
    override fun onFragmentCreated(hasSavedInstanceState: Boolean) {

    }

    override fun onViewSetup() {
        initViewAtTop(ViewBinding.mineToolbar)

        ViewBinding.mineUsername.text = ConfigManager.USER_NAME
        ViewBinding.mineUid.text = String.format(getString(R.string.text_uid), ConfigManager.USERNAME)

        ViewBinding.mineAbout.setOnClickListener {
            val intent = Intent(context, About::class.java)
            startActivity(intent)
        }

        ViewBinding.mineCalendar.setOnClickListener {
            if (checkSelfPermission()) {
                Notices.startActivity(context)
            } else {
                XPopup.Builder(context).asConfirm(
                    getString(R.string.title_notices_permission),
                    getString(R.string.text_notices_permission),
                    getString(R.string.text_notices_permission_cancel),
                    getString(R.string.text_notices_permission_start), {
                        getPermission()
                    }, {
                        askForPermission()
                    }, false
                ).show()
            }
        }

        ViewBinding.mineToolbar.setOnClickListener {
            MyInfo.startActivity(context)
        }

        ViewBinding.mineExam.setOnClickListener {
            Exam.startActivity(context)
        }

        ViewBinding.mineSpringboard.setOnClickListener {
            if (ViewBinding.mineSpringboardProgress.visibility != View.VISIBLE){
                setSpringBoardLoadingState(true)
                val access = ConfigManager.ACCESS_TOKEN
                LoginModule().springboard(access, object : LoginModule.SpringboardCallback {
                    override fun onFailure(code: Int, message: String?, e: Throwable?) {
                        springboard()
                    }

                    override fun onResult(location: String) {
                        springboard(location)
                    }
                })
            }
        }

        ViewBinding.mineAchievement.setOnClickListener {
            Achievement.startActivity(context)
        }

        if (ConfigManager.EVALUATE_COUNT != 0){
            ViewBinding.mineEvaluate.setOnClickListener {
                Evaluate.startActivity(context)
            }
        } else {
            ViewBinding.mineEvaluate.visibility = View.GONE
        }
    }

    private fun askForPermission(){
        XPopup.Builder(context).asConfirm(
            getString(R.string.title_notices_permission_denied),
            getString(R.string.text_notices_permission_denied),
            getString(R.string.text_notices_permission_force),
            getString(R.string.text_notices_permission_start), {
                getPermission()
            }, {
                Notices.startActivity(context)
            }, true
        ).show()
    }

    private fun getPermission(){
        val callback = object : OnPermissionCallback {
            override fun onGranted(permissions: MutableList<String>?, all: Boolean) {
                MyLog.d("OnPermissionCallback#onGranted(list[${permissions?.size ?: 0}], $all)")
                if (all){
                    Notices.startActivity(context)
                }
            }

            override fun onDenied(permissions: MutableList<String>?, never: Boolean) {
                MyLog.d("OnPermissionCallback#onDenied(list[${permissions?.size ?: 0}], $never)")
                if (!never){
                    askForPermission()
                    return
                }
                XPopup.Builder(context).asConfirm(
                    getString(R.string.title_notices_permission),
                    getString(R.string.text_notices_permission_denied_ask),
                    getString(R.string.text_notices_permission_force),
                    getString(R.string.text_notices_permission_start), {
                        XXPermissions.startPermissionActivity(context, Permission.WRITE_CALENDAR, Permission.READ_CALENDAR)
                    }, {
                        Notices.startActivity(context)
                    }, true
                ).show()
            }
        }
        XXPermissions.with(context)
            .permission(Permission.WRITE_CALENDAR, Permission.READ_CALENDAR)
            .request(callback)
    }

    private fun checkSelfPermission(): Boolean {
        return XXPermissions.isGranted(context, Permission.WRITE_CALENDAR, Permission.READ_CALENDAR)
    }

    override fun onResume() {
        super.onResume()
        ViewBinding.mineEvaluate.visibility = if (ConfigManager.EVALUATE_COUNT > 0){
            View.VISIBLE
        } else { View.GONE }
    }

    private fun springboard(location: String = "http://218.6.163.95:18080/zfca" +
            "?yhlx=student&login=0122579031373493708&url=xs_main.aspx"){
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(location)
        startActivity(intent)
        runOnUiThread {
            setSpringBoardLoadingState(false)
        }
    }

    private fun setSpringBoardLoadingState(isLoading: Boolean) {
        runOnUiThread{
            ViewBinding.mineSpringboard.isClickable = false
            ViewBinding.mineSpringboardProgress.visibility = View.VISIBLE
            if (isLoading) {
                ViewBinding.mineSpringboardProgress.animate().alpha(1f).setDuration(200).setListener(null)
            } else {
                ViewBinding.mineSpringboardProgress.animate().alpha(0f).setDuration(200).setListener(null)
                ViewBinding.mineSpringboardProgress.visibility = View.INVISIBLE
            }
            Timer().schedule(object : TimerTask() {
                override fun run() {
                    runOnUiThread {
                        ViewBinding.mineSpringboard.isClickable = true
                    }
                }
            }, 500)
        }
    }

    override fun onCreateViewBinding(container: ViewGroup?): FragmentMineBinding =
        FragmentMineBinding.inflate(layoutInflater)
}