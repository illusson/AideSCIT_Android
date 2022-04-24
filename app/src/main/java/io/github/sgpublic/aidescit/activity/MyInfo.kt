package io.github.sgpublic.aidescit.activity

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutManager
import com.lxj.xpopup.XPopup
import io.github.sgpublic.aidescit.R
import io.github.sgpublic.aidescit.base.BaseActivity
import io.github.sgpublic.aidescit.core.manager.CacheManager
import io.github.sgpublic.aidescit.core.manager.ConfigManager
import io.github.sgpublic.aidescit.databinding.ActivityMyInfoBinding

class MyInfo : BaseActivity<ActivityMyInfoBinding>() {
    override fun onActivityCreated(hasSavedInstanceState: Boolean) {
        ViewBinding.infoName.text = ConfigManager.USER_NAME
        ViewBinding.infoUid.text = ConfigManager.USERNAME
        ViewBinding.infoFaculty.text = ConfigManager.USER_FACULTY
        ViewBinding.infoSpecialty.text = ConfigManager.USER_SPECIALTY
        ViewBinding.infoClass.text = ConfigManager.USER_CLASS
    }

    override fun onViewSetup() {
        initViewAtTop(ViewBinding.infoToolbar)
        ViewBinding.infoClassBase.setOnClickListener {  }
        ViewBinding.infoSpecialtyBase.setOnClickListener {  }
        ViewBinding.infoFacultyBase.setOnClickListener {  }
        ViewBinding.infoUidBase.setOnClickListener {  }
        ViewBinding.infoNameBase.setOnClickListener {  }
        ViewBinding.infoAvatarBase.setOnClickListener {  }
        ViewBinding.infoPrivacySetting.setOnClickListener {
            val intent = Intent(this@MyInfo, PrivacySetting::class.java)
            startActivity(intent)
        }
        ViewBinding.infoLogout.setOnClickListener {
            XPopup.Builder(this).asConfirm(
                getString(R.string.title_check_logout),
                getString(R.string.text_check_logout),
            ) {
                ConfigManager.IS_LOGIN = false
                deleteShortCut()

                CacheManager.CACHE_ACHIEVEMENT = null
                CacheManager.CACHE_EXAM = null
                CacheManager.CACHE_SCHEDULE = null

                Login.startActivity(this@MyInfo)
            }
        }
    }

    private fun deleteShortCut() {
        val mSystemService = getSystemService(ShortcutManager::class.java)
        mSystemService?.let {
            it.removeDynamicShortcuts(listOf("Achievement"))
            it.removeDynamicShortcuts(listOf("exam"))
        }
    }

    override fun onCreateViewBinding(): ActivityMyInfoBinding =
        ActivityMyInfoBinding.inflate(layoutInflater)

    companion object {
        @JvmStatic
        fun startActivity(context: Context){
            val intent = Intent().run {
                setClass(context, MyInfo::class.java)
            }
            context.startActivity(intent)
        }
    }
}