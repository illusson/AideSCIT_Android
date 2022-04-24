package io.github.sgpublic.aidescit.activity

import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import io.github.sgpublic.aidescit.R
import io.github.sgpublic.aidescit.base.BaseActivity
import io.github.sgpublic.aidescit.databinding.ActivityPrivacySettingBinding

class PrivacySetting : BaseActivity<ActivityPrivacySettingBinding>() {
    override fun onActivityCreated(hasSavedInstanceState: Boolean) {
        checkPermissionStatus()
    }

    override fun onViewSetup() {
        initViewAtTop(ViewBinding.privacyToolbar)
        ViewBinding.privacyCalenderBase.setOnClickListener {
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
            ViewBinding.privacyStatusCalender.text = getText(R.string.test_privacy_authorized)
        } else {
            ViewBinding.privacyStatusCalender.text = getText(R.string.test_privacy_unauthorized)
        }
    }

    override fun onResume() {
        super.onResume()
        checkPermissionStatus()
    }

    override fun onCreateViewBinding(): ActivityPrivacySettingBinding =
        ActivityPrivacySettingBinding.inflate(layoutInflater)
}