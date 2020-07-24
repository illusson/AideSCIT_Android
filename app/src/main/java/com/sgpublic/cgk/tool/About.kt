package com.sgpublic.cgk.tool

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.MenuItem
import android.view.View
import com.sgpublic.cgk.tool.base.ActivityCollector
import com.sgpublic.cgk.tool.base.BaseActivity
import com.sgpublic.cgk.tool.helper.UpdateHelper
import kotlinx.android.synthetic.main.activity_about.*

class About : BaseActivity(), UpdateHelper.Callback {
    @SuppressLint("SetTextI18n")
    override fun onActivityCreate(savedInstanceState: Bundle?) {
        about_back.setOnClickListener { finish() }

        about_update.setOnClickListener {
            about_progress.visibility = View.VISIBLE
            UpdateHelper(this@About).getUpdate(0, this)
        }

        about_version.text = "V${BuildConfig.VERSION_NAME}"

        about_feedback.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://wj.qq.com/s2/5031470/7164/")
            startActivity(intent)
        }

        about_developer.setOnClickListener {
            AlertDialog.Builder(this@About).run {
                setTitle("鸣谢");
                setMessage("工科助手使用了以下开源项目：\n" +
                        "com.facebook.rebound:rebound\n" +
                        "com.squareup.okhttp3:okhttp\n" +
                        "me.imid.swipebacklayout.lib:library\n" +
                        "感谢前辈们的辛勤付出。\n\n" +
                        "(排名不分先后)\n" +
                        "移动端维护：\n   夙戓\n" +
                        "Web端维护：\n   litatno\n" +
                        "API维护：\n   十一、夙戓、litatno");
                setPositiveButton(R.string.text_ok,null);
            }.show()
        }
    }

    override fun onUpdateFailure(code: Int, message: String?, e: Throwable?) {
        about_progress.visibility = View.INVISIBLE
        onToast(this@About, R.string.title_update_error, message, code)
    }

    override fun onUpToDate() {
        about_progress.visibility = View.INVISIBLE
        onToast(this@About, R.string.title_update_already)
    }

    override fun onUpdate(force: Int, verName: String, sizeString: String, changelog: String, dlUrl: String) {
        val updateHeader = intArrayOf(
            R.string.text_update_content,
            R.string.text_update_content_force
        )
        val builder =
            AlertDialog.Builder(this@About)
        builder.setTitle(R.string.title_update_get)
        builder.setCancelable(force == 0)
        builder.setMessage(
            java.lang.String.format(this@About.getString(updateHeader[force]), sizeString) + "\n" +
                    this@About.getString(R.string.text_update_version) + verName + "\n" +
                    this@About.getString(R.string.text_update_changelog) + "\n" + changelog
        )
        builder.setPositiveButton(R.string.text_ok) { _, _ ->
            UpdateHelper(applicationContext).handleDownload(dlUrl)
            onToast(this@About, R.string.title_update_is_download)
        }
        builder.setNegativeButton(R.string.text_cancel) { _, _ ->
            if (force == 1) {
                ActivityCollector.finishAll()
            }
        }
        runOnUiThread {
            about_progress.visibility = View.INVISIBLE
            builder.show()
        }
    }

    override fun getContentView() = R.layout.activity_about

    override fun onSetSwipeBackEnable() = true

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return true
    }
}