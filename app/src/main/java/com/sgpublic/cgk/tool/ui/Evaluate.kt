package com.sgpublic.cgk.tool.ui

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.GridLayout
import android.widget.SeekBar
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.sgpublic.cgk.tool.R
import com.sgpublic.cgk.tool.base.BaseActivity
import com.sgpublic.cgk.tool.data.EvaluationData
import com.sgpublic.cgk.tool.data.EvaluationQuestionData
import com.sgpublic.cgk.tool.helper.EvaluateHelper
import com.sgpublic.cgk.tool.manager.ConfigManager
import kotlinx.android.synthetic.main.activity_evaluate.*
import kotlinx.android.synthetic.main.activity_evaluate.evaluate_list
import kotlinx.android.synthetic.main.activity_notices.*
import kotlinx.android.synthetic.main.item_evaluate.*
import kotlinx.android.synthetic.main.item_evaluate.evaluate_avatar
import kotlinx.android.synthetic.main.item_evaluate.evaluate_avatar_placeholder
import kotlinx.android.synthetic.main.item_evaluate.evaluate_question_list
import kotlinx.android.synthetic.main.item_evaluate.evaluate_teacher_subject
import kotlinx.android.synthetic.main.item_evaluate.view.*
import kotlinx.android.synthetic.main.item_evaluate_question.view.*
import org.json.JSONArray
import org.json.JSONObject

class Evaluate : BaseActivity(), EvaluateHelper.GetCallback {
    private var isLoading: Boolean = false
    private lateinit var helper: EvaluateHelper

    private val selections: ArrayList<ArrayList<Int>> = ArrayList()

    private var index: Int = 0
    private var total: Int = -1

    override fun onActivityCreate(savedInstanceState: Bundle?) {
        setOnLoadState(true)
        helper.check(object : EvaluateHelper.CheckCallback {
            override fun onFailure(code: Int, message: String?, e: Exception?) {
                total = ConfigManager(this@Evaluate).getInt("evaluate_count")
                getEvaluation(index)
            }

            override fun onResult(count: Int) {
                total = count
                getEvaluation(index)
            }
        })
    }

    override fun onViewSetup() {
        super.onViewSetup()
        helper = EvaluateHelper(this@Evaluate, ConfigManager(this@Evaluate).getString(
            "access_token", ""
        ))
        setProgressState()

        evaluate_pre.setOnClickListener {
            if (!isLoading){
                getEvaluation(index - 2)
            }
        }

        evaluate_next.setOnClickListener {
            if (!isLoading){
                setOnLoadState(true)

                val objects = JSONObject()
                objects.put("p", "")
                val array = JSONArray()

                for (index in 0 until selections.size){
                    var sameCheck = 0

                    val options = selections[index]
                    val arrayOption = JSONArray()
                    for (indexOption in 0 until options.size){
                        sameCheck += options[indexOption]
                        arrayOption.put(options[indexOption])
                    }
                    array.put(arrayOption)

                    if (sameCheck % options.size == 0){
                        onToast(this@Evaluate, R.string.title_evaluate_same)
                        setOnLoadState(false)
                        break
                    }
                }
                if (this@Evaluate.isLoading){
                    objects.put("o", array)
                    helper.post(index, objects, object : EvaluateHelper.PostCallback {
                        override fun onFailure(code: Int, message: String?, e: Exception?) {
                            setProgressState()
                            onToast(this@Evaluate, R.string.title_evaluate_get_failed, message, code)
                            setOnLoadState(false)
                        }

                        override fun onResult() {
                            if (index >= total){
                                ConfigManager(this@Evaluate).putInt("evaluate_count", 0).apply()
                                onToast(this@Evaluate, R.string.title_evaluate_post_success)
                                setOnLoadState(false)
                                finish()
                            } else {
                                getEvaluation(index)
                            }
                        }
                    })
                }
            }
        }

        evaluate_refresh.setOnRefreshListener {
            evaluate_refresh.isRefreshing = false
        }
    }

    private fun getEvaluation(index: Int){
        this@Evaluate.index = index
        setOnLoadState(true)
        helper.get(index + 1, this@Evaluate)
        this@Evaluate.index++
    }

    override fun onResult(data: ArrayList<EvaluationData>) {
        runOnUiThread {
            evaluate_list.removeAllViews()
            evaluate_list.rowCount = data.size
        }
        selections.clear()
        var dataIndex = 0
        data.forEach { indexData ->
            val itemEvaluate: View = LayoutInflater.from(this@Evaluate)
                .inflate(R.layout.item_evaluate, evaluate_list, false)
            val selection: ArrayList<Int> = ArrayList()
            itemEvaluate.apply {
                indexData.avatar?.let {
                    Glide.with(this@Evaluate)
                        .load(it)
                        .addListener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                                return false
                            }

                            override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                evaluate_avatar_placeholder.animate().alpha(0f).setDuration(400)
                                    .setListener(null)
                                Handler().postDelayed({
                                    evaluate_avatar_placeholder.visibility = View.GONE
                                    evaluate_avatar.visibility = View.VISIBLE
                                    evaluate_avatar.animate().alpha(1f).setDuration(400)
                                        .setListener(null)
                                }, 400)
                                return false
                            }
                        })
                        .into(evaluate_avatar)
                }
                evaluate_teacher_name.text = indexData.teacher
                evaluate_teacher_subject.text = String.format(getString(
                    R.string.title_evaluate_subject
                ), indexData.subject)
                val questions: ArrayList<EvaluationQuestionData> = indexData.questions
                var itemIndex = 0
                questions.forEach { indexQuestion ->
                    val itemQuestion: View = LayoutInflater.from(this@Evaluate)
                        .inflate(R.layout.item_evaluate_question, evaluate_question_list, false)
                    itemQuestion.apply {
                        item_evaluate_question_text.text = indexQuestion.text
                        item_evaluate_question_max.text = indexQuestion.options.last()
                        item_evaluate_question_min.text = indexQuestion.options.first()
                        item_evaluate_question_chose.max = indexQuestion.options.size - 1
                        item_evaluate_question_chose.progress = indexQuestion.selected

                        val selected = if (indexQuestion.selected != 0) {
                            indexQuestion.selected
                        } else { 1 }
                        selection.add(selected)
                        item_evaluate_question_select.text = indexQuestion.options[selected - 1]

                        val tId = dataIndex
                        val oId = itemIndex
                        item_evaluate_question_chose.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                                item_evaluate_question_select.text = indexQuestion.options[progress]
                                selections[tId][oId] = progress
                            }

                            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                                evaluate_refresh.isEnabled = false
                            }

                            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                                evaluate_refresh.isEnabled = true
                            }
                        })
                    }
                    evaluate_question_list.addView(itemQuestion)
                    itemIndex++
                }
            }
            selections.add(selection)

            val params = GridLayout.LayoutParams()
            params.rowSpec = GridLayout.spec(dataIndex)
            params.columnSpec = GridLayout.spec(0)
            itemEvaluate.layoutParams = params

            runOnUiThread {
                evaluate_list.addView(itemEvaluate)
            }
            setOnLoadState(false)
            dataIndex++
        }
        setProgressState()
    }

    override fun onFailure(code: Int, message: String?, e: Exception?) {
        setProgressState()
        onToast(this@Evaluate, R.string.title_evaluate_get_failed, message, code)
        setOnLoadState(false)
    }

    private fun setEnable(view: View, enable: Boolean) {
        val alpha: Float = if (!enable) 0.3F else 1.0F
        runOnUiThread {
            view.alpha = alpha
            view.isEnabled = enable
        }
    }

    private fun isEnable(view: View): Boolean {
        return view.isEnabled
    }

    private fun setOnLoadState(isLoading: Boolean){
        this@Evaluate.isLoading = isLoading
        setEnable(evaluate_next, !isLoading)
        setEnable(evaluate_pre, !isLoading && index > 1)
        runOnUiThread {
            evaluate_refresh.isRefreshing = isLoading
        }
    }

    private fun setProgressState(){
        if (index == total) {
            evaluate_next.text = getString(R.string.title_evaluate_post)
        } else {
            val indexString: String
            val totalString: String
            if (total == -1){
                indexString = "-"
                totalString = "-"
            } else {
                indexString = index.toString()
                totalString = total.toString()
            }
            runOnUiThread {
                evaluate_next.text = String.format(getString(
                    R.string.title_evaluate_next
                ), indexString, totalString)
            }
        }
    }

    override fun getContentView(): Int = R.layout.activity_evaluate

    override fun onSetSwipeBackEnable(): Boolean = false

    companion object{
        @JvmStatic
        fun startActivity(context: Context){
            val intent = Intent().run {
                setClass(context, Evaluate::class.java)
            }
            context.startActivity(intent)
        }
    }
}