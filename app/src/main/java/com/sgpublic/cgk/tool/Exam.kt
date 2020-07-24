package com.sgpublic.cgk.tool

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sgpublic.cgk.tool.base.BaseActivity
import com.sgpublic.cgk.tool.data.ExamData
import com.sgpublic.cgk.tool.helper.AchievementHelper
import com.sgpublic.cgk.tool.helper.ExamHelper
import com.sgpublic.cgk.tool.manager.CacheManager
import com.sgpublic.cgk.tool.manager.ConfigManager
import kotlinx.android.synthetic.main.activity_achievement.*
import kotlinx.android.synthetic.main.activity_exam.*
import kotlinx.android.synthetic.main.item_achievement_passed.view.*
import kotlinx.android.synthetic.main.item_exam.view.*
import org.json.JSONObject

class Exam : BaseActivity(), ExamHelper.Callback {
    var examLoad: Boolean = false

    override fun onActivityCreate(savedInstanceState: Bundle?) {
        val objects: JSONObject? = CacheManager(this@Exam).read(CacheManager.CACHE_EXAM)
        getExam(objects)
    }

    private fun getExam(objects: JSONObject? = null){
        exam_refresh.isRefreshing = true
        val helper = ExamHelper(this@Exam, ConfigManager(this@Exam).getString("username"))
        if (objects != null) {
            helper.parsing(objects, this)
        } else {
            session?.let {
                helper.getExam(it, this)
            }
        }
    }

    override fun onFailure(code: Int, message: String?, e: Exception?) {
        saveExplosion(e, code)
        onToast(this@Exam, R.string.text_load_failed, message, code)
    }

    override fun onReadStart() {
        examLoad = false
        runOnUiThread {
            exam_table.visibility = View.INVISIBLE
            exam_table_empty.visibility = View.VISIBLE
            exam_table.removeAllViews()
        }
    }

    override fun onReadData(data: ExamData) {
        if (!examLoad){
            examLoad = true
        }

        val itemExam: View = LayoutInflater.from(this@Exam)
            .inflate(R.layout.item_exam, exam_table, false)
        itemExam.apply {
            exam_name.text = data.name
            exam_time.text = data.time
            exam_location.text = data.location
            exam_num.text = data.set
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        }
        runOnUiThread{
            exam_table.addView(itemExam)
        }
    }

    override fun onReadFinish() {
        exam_refresh.isRefreshing = false
        if (examLoad){
            runOnUiThread {
                exam_table.visibility = View.VISIBLE
                exam_table_empty.visibility = View.INVISIBLE
            }
        }
    }

    override fun onViewSetup() {
        super.onViewSetup()

        exam_refresh.setOnRefreshListener { getExam() }
        exam_back.setOnClickListener { finish() }
    }

    override fun getContentView() = R.layout.activity_exam

    override fun onSetSwipeBackEnable() = true
}