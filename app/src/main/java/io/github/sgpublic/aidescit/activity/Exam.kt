package io.github.sgpublic.aidescit.activity

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.activity.viewModels
import io.github.sgpublic.aidescit.base.BaseViewModelActivity
import io.github.sgpublic.aidescit.databinding.ActivityExamBinding
import io.github.sgpublic.aidescit.databinding.ItemExamBinding
import io.github.sgpublic.aidescit.viewmodule.ExamViewModule

class Exam : BaseViewModelActivity<ActivityExamBinding, ExamViewModule>() {
    override val ViewModel: ExamViewModule by viewModels()

    override fun onActivityCreated(hasSavedInstanceState: Boolean) {

    }

    override fun onViewSetup() {
        initViewAtTop(ViewBinding.examToolbar)
        ViewBinding.examRefresh.setOnRefreshListener { ViewModel.getExam(this@Exam) }
        ViewBinding.examBack.setOnClickListener { finish() }
    }

    override fun onViewModelSetup() {
        ViewModel.EXAM_DATA.observe(this) {
            ViewBinding.examTable.visibility = View.INVISIBLE
            ViewBinding.examTableEmpty.visibility = View.VISIBLE
            ViewBinding.examTable.removeAllViews()

            for (data in it) {
                val itemExam: ItemExamBinding = ItemExamBinding.inflate(layoutInflater)
                itemExam.apply {
                    examName.text = data.name
                    examTime.text = data.time
                    examLocation.text = data.location
                    examNum.text = data.set
                }
                ViewBinding.examTable.addView(itemExam.root)
            }
            ViewBinding.examRefresh.isRefreshing = false
            ViewBinding.examTable.visibility = View.VISIBLE
            ViewBinding.examTableEmpty.visibility = View.INVISIBLE
        }
    }

    override fun onCreateViewBinding(): ActivityExamBinding =
        ActivityExamBinding.inflate(layoutInflater)

    companion object {
        @JvmStatic
        fun startActivity(context: Context){
            val intent = Intent().run {
                setClass(context, Exam::class.java)
            }
            context.startActivity(intent)
        }
    }
}