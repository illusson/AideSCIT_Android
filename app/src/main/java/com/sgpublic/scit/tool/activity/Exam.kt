package com.sgpublic.scit.tool.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sgpublic.scit.tool.R
import com.sgpublic.scit.tool.base.BaseActivity
import com.sgpublic.scit.tool.data.ExamData
import com.sgpublic.scit.tool.databinding.ActivityExamBinding
import com.sgpublic.scit.tool.databinding.ItemExamBinding
import com.sgpublic.scit.tool.helper.ExamHelper
import com.sgpublic.scit.tool.manager.CacheManager
import com.sgpublic.scit.tool.manager.ConfigManager
import org.json.JSONObject

class Exam : BaseActivity<ActivityExamBinding>(), ExamHelper.Callback {
    var examLoad: Boolean = false

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        val objects: JSONObject? = CacheManager(this@Exam).read(CacheManager.CACHE_EXAM)
        getExam(objects)
    }

    private fun getExam(objects: JSONObject? = null){
        binding.examRefresh.isRefreshing = true
        val helper = ExamHelper(this@Exam)
        if (objects != null) {
            helper.parsing(objects, this)
        } else {
            helper.getExam(ConfigManager(this@Exam)
                .getString("access_token"), this)
        }
    }

    override fun onFailure(code: Int, message: String?, e: Exception?) {
        saveExplosion(e, code)
        onToast(R.string.text_load_failed, message, code)
        runOnUiThread {
            binding.examRefresh.isRefreshing = false
        }
    }

    override fun onReadStart() {
        examLoad = false
        runOnUiThread {
            binding.examTable.visibility = View.INVISIBLE
            binding.examTableEmpty.visibility = View.VISIBLE
            binding.examTable.removeAllViews()
        }
    }

    override fun onReadData(data: ExamData) {
        if (!examLoad){
            examLoad = true
        }

        val itemExam: ItemExamBinding = ItemExamBinding.inflate(layoutInflater)
        itemExam.apply {
            examName.text = data.name
            examTime.text = data.time
            examLocation.text = data.location
            examNum.text = data.set
        }
        runOnUiThread{
            binding.examTable.addView(itemExam.root)
        }
    }

    override fun onReadFinish() {
        binding.examRefresh.isRefreshing = false
        if (examLoad){
            runOnUiThread {
                binding.examTable.visibility = View.VISIBLE
                binding.examTableEmpty.visibility = View.INVISIBLE
            }
        }
    }

    override fun onViewSetup() {
        super.onViewSetup()

        initViewAtTop(binding.examToolbar)
        binding.examRefresh.setOnRefreshListener { getExam() }
        binding.examBack.setOnClickListener { finish() }
    }

    override fun getContentView() = ActivityExamBinding.inflate(layoutInflater)

    override fun onSetSwipeBackEnable() = true
}