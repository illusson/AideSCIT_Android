package com.sgpublic.scit.tool.fragment

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ShortcutManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.kongzue.dialogx.dialogs.MessageDialog
import com.sgpublic.scit.tool.R
import com.sgpublic.scit.tool.activity.*
import com.sgpublic.scit.tool.base.BaseFragment
import com.sgpublic.scit.tool.base.MyLog
import com.sgpublic.scit.tool.helper.LoginHelper
import com.sgpublic.scit.tool.manager.CacheManager
import com.sgpublic.scit.tool.manager.ConfigManager
import kotlinx.android.synthetic.main.fragment_mine.*

class Mine(contest: AppCompatActivity) : BaseFragment(contest) {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onViewSetup() {
        super.onViewSetup()
        initViewAtTop(mine_toolbar)

        mine_username.text = ConfigManager(contest).getString("name", "此人没有留下姓名……")

        mine_about.setOnClickListener {
            val intent = Intent(contest, About::class.java)
            startActivity(intent)
        }

        mine_calendar.setOnClickListener {
            if (checkSelfPermission()) {
                val intent = Intent(contest, Notices::class.java)
                startActivity(intent)
            } else {
                MessageDialog.build()
                    .setTitle(R.string.title_notices_permission)
                    .setMessage(R.string.text_notices_permission)
                    .setOkButton(R.string.text_notices_permission_start) { _, _ ->
                        ActivityCompat.requestPermissions(
                            contest, arrayOf(
                                Manifest.permission.WRITE_CALENDAR,
                                Manifest.permission.READ_CALENDAR
                            ), 1
                        )
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

        mine_exam.setOnClickListener {
            val intent = Intent(contest, Exam::class.java)
            startActivity(intent)
        }

        mine_springboard.setOnClickListener {
            if (mine_springboard_progress.visibility != View.VISIBLE){
                setSpringBoardLoadingState(true)
                val access = ConfigManager(contest).getString("access_token", "");
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

        mine_logout.setOnClickListener {
            val alert = AlertDialog.Builder(contest)
            alert.setTitle(R.string.title_check_logout)
            alert.setMessage(R.string.text_check_logout)
            alert.setPositiveButton(R.string.text_ok) { _, _ ->
                ConfigManager(contest)
                    .putBoolean("is_login", false)
                    .apply()
                deleteShortCut()

                CacheManager(contest)
                    .save(CacheManager.CACHE_ACHIEVEMENT, "")
                    .save(CacheManager.CACHE_EXAM, "")
                    .save(CacheManager.CACHE_TABLE, "")

                Login.startActivity(contest)//, true)
                finish()
            }
            alert.setNegativeButton(R.string.text_cancel, null)
            alert.show()
        }

        mine_achievement.setOnClickListener {
            val intent = Intent(contest, Achievement::class.java)
            startActivity(intent)
        }

        if (ConfigManager(contest).getInt("evaluate_count", 0) != 0){
            mine_evaluate.setOnClickListener {
                Evaluate.startActivity(contest)
            }
        } else {
            mine_evaluate.visibility = View.GONE
        }
    }

    private fun askForPermission(){
        MessageDialog.build()
            .setTitle(R.string.title_notices_permission_denied)
            .setMessage(R.string.text_notices_permission_denied)
            .setOkButton(R.string.text_notices_permission_start) { _, _ ->
                ActivityCompat.requestPermissions(
                    contest, arrayOf(
                        Manifest.permission.WRITE_CALENDAR,
                        Manifest.permission.READ_CALENDAR
                    ), 1
                )
                return@setOkButton false
            }
            .setCancelButton(R.string.text_notices_permission_force){ dialog, _ ->
                dialog.dismiss()
                val intent = Intent(contest, Notices::class.java)
                startActivity(intent)
                return@setCancelButton true
            }
            .show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode){
            1 -> {
                if (checkSelfPermission()) {
                    val intent = Intent(contest, Notices::class.java)
                    startActivity(intent)
                    return
                }
                MessageDialog.build()
                    .setTitle(R.string.title_notices_permission)
                    .setMessage(R.string.text_notices_permission_denied_ask)
                    .setOkButton(R.string.text_notices_permission_setting) { dialog, _ ->
                        dialog.dismiss()
                        gotoMiuiPermission()
                        return@setOkButton true
                    }
                    .setCancelButton(R.string.text_notices_permission_force) { dialog, _ ->
                        dialog.dismiss()
                        val intent = Intent(contest, Notices::class.java)
                        startActivity(intent)
                        return@setCancelButton true
                    }
                    .show()
            }
        }
    }

    private fun gotoMiuiPermission() {
        val intent = Intent("miui.intent.action.APP_PERM_EDITOR")
        val componentName = ComponentName (
            "com.miui.securitycenter",
            "com.miui.permcenter.permissions.PermissionsEditorActivity"
        )
        intent.component = componentName
        intent.putExtra("extra_pkgname", activity?.packageName)
        try {
            startActivityForResult(intent, 1)
        } catch (e: Exception) {
            e.printStackTrace()
            gotoMeizuPermission()
        }
    }

    private fun gotoMeizuPermission() {
        val intent = Intent("com.meizu.safe.security.SHOW_APPSEC")
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        intent.putExtra("packageName", activity?.packageName)
        try {
            startActivityForResult(intent, 1)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            gotoHuaweiPermission()
        }
    }

    private fun gotoHuaweiPermission() {
        try {
            val intent = Intent()
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            val comp = ComponentName(
                "com.huawei.systemmanager",
                "com.huawei.permissionmanager.ui.MainActivity"
            ) //华为权限管理
            intent.component = comp
            startActivityForResult(intent, 1)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            getAppDetailSettingIntent()
        }
    }

    private fun getAppDetailSettingIntent() {
        val intent = Intent()
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.action = "android.settings.APPLICATION_DETAILS_SETTINGS"
        intent.data = Uri.fromParts("package", activity?.packageName, null)
        startActivity(intent)
    }

    private fun checkSelfPermission(): Boolean {
        val permissions = intArrayOf(
            ContextCompat.checkSelfPermission(contest, Manifest.permission.WRITE_CALENDAR),
            ContextCompat.checkSelfPermission(contest, Manifest.permission.READ_CALENDAR)
        )
        var isAllowed = true
        for (permission in permissions) {
            isAllowed = isAllowed && permission == PackageManager.PERMISSION_GRANTED
        }
        return isAllowed
    }

    override fun onResume() {
        super.onResume()
        mine_evaluate.visibility = if (ConfigManager(contest).getInt("evaluate_count", 0) > 0){
            View.VISIBLE
        } else { View.GONE }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode){
            1 -> {
                if (!checkSelfPermission()){
                    MessageDialog.build()
                        .setTitle(R.string.title_notices_permission)
                        .setMessage(R.string.text_notices_permission_denied_ask)
                        .setOkButton(R.string.text_notices_permission_setting) { dialog, _ ->
                            dialog.dismiss()
                            gotoMiuiPermission()
                            return@setOkButton true
                        }
                        .setCancelButton(R.string.text_notices_permission_force){ dialog, _ ->
                            dialog.dismiss()
                            val intent = Intent(contest, Notices::class.java)
                            startActivity(intent)
                            return@setCancelButton true
                        }
                        .show()
                } else {
                    val intent = Intent(contest, Notices::class.java)
                    startActivity(intent)
                }
            }
        }
    }

    private fun deleteShortCut() {
        val mSystemService = contest.getSystemService(ShortcutManager::class.java)
        mSystemService?.let {
            it.removeDynamicShortcuts(listOf("Achievement"))
            it.removeDynamicShortcuts(listOf("exam"))
        }
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
            mine_springboard.isEnabled = false
            mine_springboard_img.visibility = View.VISIBLE
            mine_springboard_progress.visibility = View.VISIBLE
            if (isLoading) {
                mine_springboard_progress.animate().alpha(1f).setDuration(200).setListener(null)
                mine_springboard_img.animate().alpha(0f).setDuration(200).setListener(null)
                mine_springboard_img.visibility = View.INVISIBLE
            } else {
                mine_springboard_progress.animate().alpha(0f).setDuration(200).setListener(null)
                mine_springboard_img.animate().alpha(1f).setDuration(200).setListener(null)
                mine_springboard_progress.visibility = View.INVISIBLE
            }
            Handler().postDelayed({
                mine_springboard.isEnabled = true
            }, 500)
        }
    }
    
    override fun getContentView(): Int = R.layout.fragment_mine
}