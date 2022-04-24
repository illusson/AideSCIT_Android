package io.github.sgpublic.aidescit.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.text.method.LinkMovementMethod
import android.view.MenuItem
import android.view.View
import io.github.sgpublic.aidescit.Application
import io.github.sgpublic.aidescit.BuildConfig
import io.github.sgpublic.aidescit.R
import io.github.sgpublic.aidescit.base.BaseActivity
import io.github.sgpublic.aidescit.core.module.UpdateModule
import io.github.sgpublic.aidescit.core.util.ActivityCollector
import io.github.sgpublic.aidescit.databinding.ActivityAboutBinding

class About : BaseActivity<ActivityAboutBinding>(), UpdateModule.Callback {
    override fun onActivityCreated(hasSavedInstanceState: Boolean) {
        initViewAtTop(ViewBinding.aboutBack)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewSetup() {
        ViewBinding.aboutBack.setOnClickListener { finish() }

        ViewBinding.aboutUpdate.setOnClickListener {
            ViewBinding.aboutProgress.visibility = View.VISIBLE
            UpdateModule().getUpdate(this)
        }

        ViewBinding.aboutVersion.text = "V${BuildConfig.VERSION_NAME}"

        ViewBinding.aboutDeveloper.setOnClickListener {
            AlertDialog.Builder(this@About).run {
                setTitle("鸣谢")
                setMessage("(排名不分先后)\n" +
                        "移动端维护：\n   忆丶距\n" +
                        "Web端维护：\n   忆丶距、litatno\n" +
                        "API维护：\n   十一、忆丶距、litatno"
                )
                setPositiveButton(R.string.text_ok, null)
            }.show()
        }

        ViewBinding.aboutLicense.setOnClickListener {
            val intent = Intent(this@About, License::class.java)
            startActivity(intent)
        }

        ViewBinding.aboutAgreement.movementMethod = LinkMovementMethod.getInstance()
    }

    override fun onUpdateFailure(code: Int, message: String?, e: Throwable?) {
        ViewBinding.aboutProgress.visibility = View.INVISIBLE
        Application.onToast(this, R.string.title_update_error, message, code)
    }

    override fun onUpToDate() {
        ViewBinding.aboutProgress.visibility = View.INVISIBLE
        Application.onToast(this, R.string.title_update_already)
    }

    override fun onUpdate(
        force: Int,
        verName: String,
        sizeString: String,
        changelog: String,
        dlUrl: String
    ) {
        val updateHeader = intArrayOf(
            R.string.text_update_content,
            R.string.text_update_content_force
        )
        val builder = AlertDialog.Builder(this@About)
        builder.setTitle(R.string.title_update_get)
        builder.setCancelable(force == 0)
        builder.setMessage(
            java.lang.String.format(this@About.getString(updateHeader[force]), sizeString) + "\n" +
                    this@About.getString(R.string.text_update_version) + verName + "\n" +
                    this@About.getString(R.string.text_update_changelog) + "\n" + changelog
        )
        builder.setPositiveButton(R.string.text_ok) { _, _ ->
            UpdateModule().handleDownload(dlUrl)
            Application.onToast(this, R.string.title_update_is_download)
        }
        builder.setNegativeButton(R.string.text_cancel) { _, _ ->
            if (force == 1) {
                ActivityCollector.finishAll()
            }
        }
        runOnUiThread {
            ViewBinding.aboutProgress.visibility = View.INVISIBLE
            builder.show()
        }
    }

    override fun isActivityAtBottom() = false

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return true
    }

    override fun onCreateViewBinding(): ActivityAboutBinding =
        ActivityAboutBinding.inflate(layoutInflater)
}