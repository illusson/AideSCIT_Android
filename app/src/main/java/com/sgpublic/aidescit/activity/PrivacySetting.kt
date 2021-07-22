package com.sgpublic.aidescit.activity

import android.os.Bundle
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.sgpublic.aidescit.R
import com.sgpublic.aidescit.base.BaseActivity
import com.sgpublic.aidescit.databinding.ActivityPrivacySettingBinding

class PrivacySetting : BaseActivity<ActivityPrivacySettingBinding>() {
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        checkPermissionStatus()
    }

    override fun onViewSetup() {
        initViewAtTop(binding.privacyToolbar)
        binding.privacyCalenderBase.setOnClickListener {
            XXPermissions.startPermissionActivity(this@PrivacySetting, listOf(
                Permission.WRITE_CALENDAR
            ))
        }
    }

    private fun checkSelfPermission(): Boolean {
        return XXPermissions.isGranted(this@PrivacySetting,
            Permission.WRITE_CALENDAR, Permission.READ_CALENDAR)
    }

    private fun checkPermissionStatus(){
        if (checkSelfPermission()){
            binding.privacyStatusCalender.text = getText(R.string.test_privacy_authorized)
        } else {
            binding.privacyStatusCalender.text = getText(R.string.test_privacy_unauthorized)
        }
    }

    override fun onResume() {
        super.onResume()
        checkPermissionStatus()
    }

    override fun isActivityAtBottom() = false
}