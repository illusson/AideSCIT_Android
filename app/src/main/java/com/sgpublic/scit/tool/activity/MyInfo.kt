package com.sgpublic.scit.tool.activity

import android.content.Intent
import android.content.pm.ShortcutManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.sgpublic.scit.tool.R
import com.sgpublic.scit.tool.base.BaseActivity
import com.sgpublic.scit.tool.databinding.ActivityMyInfoBinding
import com.sgpublic.scit.tool.manager.CacheManager
import com.sgpublic.scit.tool.manager.ConfigManager

class MyInfo : BaseActivity<ActivityMyInfoBinding>() {
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        val manager = ConfigManager(this)
        binding.infoName.text = manager.getString("name")
        binding.infoUid.text = manager.getString("username")
        binding.infoFaculty.text = manager.getString("faculty_name")
        binding.infoSpecialty.text = manager.getString("specialty_name")
        binding.infoClass.text = manager.getString("class_name")
    }

    override fun onViewSetup() {
        super.onViewSetup()
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
                ConfigManager(this@MyInfo)
                    .putBoolean("is_login", false)
                    .apply()
                deleteShortCut()

                CacheManager(this@MyInfo)
                    .save(CacheManager.CACHE_ACHIEVEMENT, "")
                    .save(CacheManager.CACHE_EXAM, "")
                    .save(CacheManager.CACHE_TABLE, "")

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

    override fun getContentView() = ActivityMyInfoBinding.inflate(layoutInflater)

    override fun onSetSwipeBackEnable(): Boolean = true
}