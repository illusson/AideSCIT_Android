package com.sgpublic.aidescit.activity

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutManager
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.sgpublic.aidescit.R
import com.sgpublic.aidescit.base.BaseActivity
import com.sgpublic.aidescit.databinding.ActivityMyInfoBinding
import com.sgpublic.aidescit.manager.CacheManager
import com.sgpublic.aidescit.manager.ConfigManager

class MyInfo : BaseActivity<ActivityMyInfoBinding>() {
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        binding.infoName.text = ConfigManager.getString("name")
        binding.infoUid.text = ConfigManager.getString("username")
        binding.infoFaculty.text = ConfigManager.getString("faculty_name")
        binding.infoSpecialty.text = ConfigManager.getString("specialty_name")
        binding.infoClass.text = ConfigManager.getString("class_name")
    }

    override fun onViewSetup() {
        initViewAtTop(binding.infoToolbar)
        binding.infoClassBase.setOnClickListener {  }
        binding.infoSpecialtyBase.setOnClickListener {  }
        binding.infoFacultyBase.setOnClickListener {  }
        binding.infoUidBase.setOnClickListener {  }
        binding.infoNameBase.setOnClickListener {  }
        binding.infoAvatarBase.setOnClickListener {  }
        binding.infoPrivacySetting.setOnClickListener {
            val intent = Intent(this@MyInfo, PrivacySetting::class.java)
            startActivity(intent)
        }
        binding.infoLogout.setOnClickListener {
            val alert = AlertDialog.Builder(this@MyInfo)
            alert.setTitle(R.string.title_check_logout)
            alert.setMessage(R.string.text_check_logout)
            alert.setPositiveButton(R.string.text_ok) { _, _ ->
                ConfigManager.putBoolean("is_login", false)
                deleteShortCut()

                CacheManager(this@MyInfo)
                    .save(CacheManager.CACHE_ACHIEVEMENT, "")
                    .save(CacheManager.CACHE_EXAM, "")
                    .save(CacheManager.CACHE_SCHEDULE, "")

                Login.startActivity(this@MyInfo)//, true)
                finish()
            }
            alert.setNegativeButton(R.string.text_cancel, null)
            alert.show()
        }
    }

    private fun deleteShortCut() {
        val mSystemService = getSystemService(ShortcutManager::class.java)
        mSystemService?.let {
            it.removeDynamicShortcuts(listOf("Achievement"))
            it.removeDynamicShortcuts(listOf("exam"))
        }
    }

    override fun isActivityAtBottom() = false

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