package com.sgpublic.scit.tool.activity

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.widget.GridLayout
import android.widget.SeekBar
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.sgpublic.scit.tool.R
import com.sgpublic.scit.tool.base.BaseActivity
import com.sgpublic.scit.tool.data.EvaluationData
import com.sgpublic.scit.tool.data.EvaluationQuestionData
import com.sgpublic.scit.tool.databinding.ActivityEvaluateBinding
import com.sgpublic.scit.tool.databinding.ItemEvaluateBinding
import com.sgpublic.scit.tool.databinding.ItemEvaluateQuestionBinding
import com.sgpublic.scit.tool.helper.EvaluateHelper
import com.sgpublic.scit.tool.manager.ConfigManager
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

class Evaluate : BaseActivity<ActivityEvaluateBinding>(), EvaluateHelper.GetCallback {
    private var isLoading: Boolean = false
    private lateinit var helper: EvaluateHelper

    private val selections: ArrayList<ArrayList<Int>> = ArrayList()

    private var index: Int = 0
    private var total: Int = -1

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        setOnLoadState(true)
        helper.check(object : EvaluateHelper.CheckCallback {
            override fun onFailure(code: Int, message: String?, e: Exception?) {
                total = ConfigManager.getInt("evaluate_count")
                getEvaluation(index)
            }

            override fun onResult(count: Int) {
                total = count
                getEvaluation(index)
            }
        })
    }

    override fun onViewSetup() {
        helper = EvaluateHelper(this@Evaluate, ConfigManager.getString(
            "access_token", ""
        ))
        setProgressState()

        binding.evaluatePre.setOnClickListener {
            if (!isLoading){
                getEvaluation(index - 2)
            }
        }

        binding.evaluateNext.setOnClickListener {
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
                        onToast(R.string.title_evaluate_same)
                        setOnLoadState(false)
                        break
                    }
                }
                if (this@Evaluate.isLoading){
                    objects.put("o", array)
                    helper.post(index, objects, object : EvaluateHelper.PostCallback {
                        override fun onFailure(code: Int, message: String?, e: Exception?) {
                            setProgressState()
                            onToast(R.string.title_evaluate_get_failed, message, code)
                            setOnLoadState(false)
                        }

                        override fun onResult() {
                            if (index >= total){
                                ConfigManager.putInt("evaluate_count", 0)
                                onToast(R.string.title_evaluate_post_success)
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

        binding.evaluateRefresh.setOnRefreshListener {
            binding.evaluateRefresh.isRefreshing = false
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
            binding.evaluateList.removeAllViews()
            binding.evaluateList.rowCount = data.size
        }
        selections.clear()
        var dataIndex = 0
        data.forEach { indexData ->
            val itemEvaluate: ItemEvaluateBinding = ItemEvaluateBinding.inflate(layoutInflater)
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
                                evaluateAvatarPlaceholder.animate().alpha(0f).setDuration(400)
                                    .setListener(null)
                                Timer().schedule(object : TimerTask() {
                                    override fun run() {
                                        evaluateAvatarPlaceholder.visibility = View.GONE
                                        evaluateAvatar.visibility = View.VISIBLE
                                        evaluateAvatar.animate().alpha(1f).setDuration(400)
                                            .setListener(null)
                                    }
                                }, 400)
                                return false
                            }
                        })
                        .into(evaluateAvatar)
                }
                evaluateTeacherName.text = indexData.teacher
                evaluateTeacherSubject.text = String.format(getString(
                    R.string.title_evaluate_subject
                ), indexData.subject)
                val questions: ArrayList<EvaluationQuestionData> = indexData.questions
                var itemIndex = 0
                questions.forEach { indexQuestion ->
                    val itemQuestion: ItemEvaluateQuestionBinding = ItemEvaluateQuestionBinding.inflate(layoutInflater)
                    itemQuestion.apply {
                        itemEvaluateQuestionText.text = indexQuestion.text
                        itemEvaluateQuestionMax.text = indexQuestion.options.last()
                        itemEvaluateQuestionMin.text = indexQuestion.options.first()
                        itemEvaluateQuestionChose.max = indexQuestion.options.size - 1
                        itemEvaluateQuestionChose.progress = indexQuestion.selected

                        val selected = if (indexQuestion.selected != 0) {
                            indexQuestion.selected
                        } else { 1 }
                        selection.add(selected)
                        itemEvaluateQuestionSelect.text = indexQuestion.options[selected - 1]

                        val tId = dataIndex
                        val oId = itemIndex
                        itemEvaluateQuestionChose.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                                itemEvaluateQuestionSelect.text = indexQuestion.options[progress]
                                selections[tId][oId] = progress
                            }

                            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                                binding.evaluateRefresh.isEnabled = false
                            }

                            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                                binding.evaluateRefresh.isEnabled = true
                            }
                        })
                    }
                    evaluateQuestionList.addView(itemQuestion.root)
                    itemIndex++
                }
            }
            selections.add(selection)

            val params = GridLayout.LayoutParams()
            params.rowSpec = GridLayout.spec(dataIndex)
            params.columnSpec = GridLayout.spec(0)
            itemEvaluate.root.layoutParams = params

            runOnUiThread {
                binding.evaluateList.addView(itemEvaluate.root)
            }
            setOnLoadState(false)
            dataIndex++
        }
        setProgressState()
    }

    override fun onFailure(code: Int, message: String?, e: Exception?) {
        setProgressState()
        onToast(R.string.title_evaluate_get_failed, message, code)
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
        setEnable(binding.evaluateNext, !isLoading)
        setEnable(binding.evaluatePre, !isLoading && index > 1)
        runOnUiThread {
            binding.evaluateRefresh.isRefreshing = isLoading
        }
    }

    private fun setProgressState(){
        if (index == total) {
            binding.evaluateNext.text = getString(R.string.title_evaluate_post)
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
                binding.evaluateNext.text = String.format(getString(
                    R.string.title_evaluate_next
                ), indexString, totalString)
            }
        }
    }

    override fun isActivityAtBottom() = false

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