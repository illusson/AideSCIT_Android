package com.sgpublic.scit.tool.activity

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.kongzue.dialogx.dialogs.MessageDialog
import com.sgpublic.scit.tool.R
import com.sgpublic.scit.tool.base.BaseActivity
import com.sgpublic.scit.tool.databinding.ActivityPrivacySettingBinding

class PrivacySetting : BaseActivity<ActivityPrivacySettingBinding>() {
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        checkPermissionStatus()
    }

    override fun onViewSetup() {
        initViewAtTop(binding.privacyToolbar)
        binding.privacyCalenderBase.setOnClickListener {
            gotoMiuiPermission()
        }
    }

    private fun checkSelfPermission(): Boolean {
        val permissions = intArrayOf(
            ContextCompat.checkSelfPermission(this@PrivacySetting, Manifest.permission.WRITE_CALENDAR),
            ContextCompat.checkSelfPermission(this@PrivacySetting, Manifest.permission.READ_CALENDAR)
        )
        var isAllowed = true
        for (permission in permissions) {
            isAllowed = isAllowed && permission == PackageManager.PERMISSION_GRANTED
        }
        return isAllowed
    }

    private fun checkPermissionStatus(){
        if (checkSelfPermission()){
            binding.privacyStatusCalender.text = getText(R.string.test_privacy_authorized)
        } else {
            binding.privacyStatusCalender.text = getText(R.string.test_privacy_unauthorized)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode){
            1 -> {
                checkPermissionStatus()
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
        intent.putExtra("extra_pkgname", applicationContext?.packageName)
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
        intent.putExtra("packageName", applicationContext?.packageName)
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
        intent.data = Uri.fromParts("package", applicationContext?.packageName, null)
        startActivity(intent)
    }

    override fun isActivityAtBottom() = false
}