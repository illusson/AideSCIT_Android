package com.sgpublic.aidescit.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.kongzue.dialogx.dialogs.MessageDialog
import com.sgpublic.aidescit.R
import com.sgpublic.aidescit.activity.*
import com.sgpublic.aidescit.base.BaseFragment
import com.sgpublic.aidescit.databinding.FragmentMineBinding
import com.sgpublic.aidescit.helper.LoginHelper
import com.sgpublic.aidescit.manager.ConfigManager
import com.sgpublic.aidescit.util.MyLog
import java.util.*

class Mine(contest: AppCompatActivity) : BaseFragment<FragmentMineBinding>(contest) {

    override fun onFragmentCreated(savedInstanceState: Bundle?) {

    }

    override fun onViewSetup() {
        initViewAtTop(binding.mineToolbar)

        binding.mineUsername.text = ConfigManager.getString("name", "此人没有留下姓名……")
        binding.mineUid.text = String.format(getString(R.string.text_uid), ConfigManager.getString("username", "（未知）"))

        binding.mineAbout.setOnClickListener {
            val intent = Intent(contest, About::class.java)
            startActivity(intent)
        }

        binding.mineCalendar.setOnClickListener {
            if (checkSelfPermission()) {
                Notices.startActivity(contest)
            } else {
                MessageDialog.build()
                    .setTitle(R.string.title_notices_permission)
                    .setMessage(R.string.text_notices_permission)
                    .setOkButton(R.string.text_notices_permission_start) { dialog, _ ->
                        dialog.dismiss()
                        getPermission()
                        return@setOkButton false
                    }
                    .setCancelButton(R.string.text_notices_permission_cancel){ dialog, _ ->
                        dialog.dismiss()
                        askForPermission()
                        return@setCancelButton true
                    }
                    .show()
            }
        }

        binding.mineToolbar.setOnClickListener {
            MyInfo.startActivity(contest)
        }

        binding.mineExam.setOnClickListener {
            Exam.startActivity(contest)
        }

        binding.mineSpringboard.setOnClickListener {
            if (binding.mineSpringboardProgress.visibility != View.VISIBLE){
                setSpringBoardLoadingState(true)
                val access = ConfigManager.getString("access_token", "")
                LoginHelper(contest).springboard(access, object : LoginHelper.SpringboardCallback {
                    override fun onFailure(code: Int, message: String?, e: Exception?) {
                        springboard(
                            "http://218.6.163.95:18080/zfca?yhlx=student&login=0122579031373493708&url=xs_main.aspx"
                        )
                    }

                    override fun onResult(location: String) {
                        springboard(location)
                    }
                })
            }
        }

        binding.mineAchievement.setOnClickListener {
            Achievement.startActivity(contest)
        }

        if (ConfigManager.getInt("evaluate_count", 0) != 0){
            binding.mineEvaluate.setOnClickListener {
                Evaluate.startActivity(contest)
            }
        } else {
            binding.mineEvaluate.visibility = View.GONE
        }
    }

    private fun askForPermission(){
        MessageDialog.build()
            .setTitle(R.string.title_notices_permission_denied)
            .setMessage(R.string.text_notices_permission_denied)
            .setOkButton(R.string.text_notices_permission_start) { _, _ ->
                getPermission()
                return@setOkButton false
            }
            .setCancelButton(R.string.text_notices_permission_force){ dialog, _ ->
                dialog.dismiss()
                Notices.startActivity(contest)
                return@setCancelButton true
            }
            .show()
    }

    private fun getPermission(){
        val callback = object : OnPermissionCallback {
            override fun onGranted(permissions: MutableList<String>?, all: Boolean) {
                MyLog.d("OnPermissionCallback#onGranted(list[${permissions?.size ?: 0}], $all)")
                if (all){
                    Notices.startActivity(contest)
                }
            }

            override fun onDenied(permissions: MutableList<String>?, never: Boolean) {
                MyLog.d("OnPermissionCallback#onDenied(list[${permissions?.size ?: 0}], $never)")
                if (!never){
                    askForPermission()
                    return
                }
                MessageDialog.build()
                    .setTitle(R.string.title_notices_permission)
                    .setMessage(R.string.text_notices_permission_denied_ask)
                    .setCancelButton(R.string.text_notices_permission_force) { dialog, _ ->
                        dialog.dismiss()
                        Notices.startActivity(contest)
                        true
                    }
                    .setOkButton(R.string.text_notices_permission_start) { dialog, _ ->
                        dialog.dismiss()
                        XXPermissions.startPermissionActivity(contest, Permission.WRITE_CALENDAR, Permission.READ_CALENDAR)
                        true
                    }
                    .show()
            }
        }
        XXPermissions.with(contest)
            .permission(Permission.WRITE_CALENDAR, Permission.READ_CALENDAR)
            .request(callback)
    }

    private fun checkSelfPermission(): Boolean {
        return XXPermissions.isGranted(contest, Permission.WRITE_CALENDAR, Permission.READ_CALENDAR)
    }

    override fun onResume() {
        super.onResume()
        binding.mineEvaluate.visibility = if (ConfigManager.getInt("evaluate_count", 0) > 0){
            View.VISIBLE
        } else { View.GONE }
    }

    private fun springboard(location: String){
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(location)
        startActivity(intent)
        runOnUiThread {
            setSpringBoardLoadingState(false)
        }
    }

    private fun setSpringBoardLoadingState(isLoading: Boolean) {
        runOnUiThread{
            binding.mineSpringboard.isClickable = false
            binding.mineSpringboardProgress.visibility = View.VISIBLE
            if (isLoading) {
                binding.mineSpringboardProgress.animate().alpha(1f).setDuration(200).setListener(null)
            } else {
                binding.mineSpringboardProgress.animate().alpha(0f).setDuration(200).setListener(null)
                binding.mineSpringboardProgress.visibility = View.INVISIBLE
            }
            Timer().schedule(object : TimerTask() {
                override fun run() {
                    runOnUiThread {
                        binding.mineSpringboard.isClickable = true
                    }
                }
            }, 500)
        }
    }
}