package io.github.sgpublic.aidescit.ui.recycler

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import io.github.sgpublic.aidescit.core.data.FailedMarkData
import io.github.sgpublic.aidescit.core.data.PassedMarkData
import io.github.sgpublic.aidescit.databinding.*
import java.util.*
import kotlin.math.max

class AchieveRecyclerAdapter(private val context: AppCompatActivity):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var passed: List<PassedMarkData> = LinkedList<PassedMarkData>()
    private var failed: List<FailedMarkData> = LinkedList<FailedMarkData>()

    @SuppressLint("NotifyDataSetChanged")
    fun setData(passed: List<PassedMarkData>, failed: List<FailedMarkData>) {
        this.passed = passed
        this.failed = failed
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            position == 0 -> TYPE_PASSED_HEAD
            position == 1 -> TYPE_PASSED_EMPTY.takeIf { passed.isEmpty() } ?: TYPE_PASSED_ITEM
            position <= passed.size -> TYPE_PASSED_ITEM
            position == max(1, passed.size) + 1 -> TYPE_FAILED_HEAD
            else -> TYPE_FAILED_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        return when(viewType) {
            TYPE_PASSED_HEAD -> PassedHeadViewHolder(ItemAchievePassedHeadBinding.inflate(
                layoutInflater, parent, false
            ))
            TYPE_PASSED_ITEM -> PassedItemViewHolder(ItemAchievePassedItemBinding.inflate(
                layoutInflater, parent, false
            ))
            TYPE_PASSED_EMPTY -> PassedEmptyItemViewHolder(ItemAchievePassedEmptyBinding.inflate(
                layoutInflater, parent, false
            ))
            TYPE_FAILED_HEAD -> FailedHeadViewHolder(ItemAchieveFailedHeadBinding.inflate(
                layoutInflater, parent, false
            ))
            TYPE_FAILED_ITEM -> FailedItemViewHolder(ItemAchieveFailedItemBinding.inflate(
                layoutInflater, parent, false
            ))
            else -> throw IllegalStateException()
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is PassedItemViewHolder -> {
                holder.bind(passed[position - 1])
            }
            is FailedItemViewHolder -> {
                holder.bind(failed[position - max(1, passed.size) - 2])
            }
        }
    }

    override fun getItemCount(): Int {
        var size = max(passed.size + 1, 2)
        if (failed.isNotEmpty()) size += failed.size + 1
        return size
    }

    companion object {
        const val TYPE_PASSED_HEAD = 0
        const val TYPE_PASSED_ITEM = 1
        const val TYPE_PASSED_EMPTY = 2
        const val TYPE_FAILED_HEAD = 3
        const val TYPE_FAILED_ITEM = 4

        fun judgePass(string: String?): Boolean {
            return if (string == null || string == ""){
                false
            } else string.toFloat() >= 60
        }
    }

    class PassedHeadViewHolder(binding: ItemAchievePassedHeadBinding)
        : RecyclerView.ViewHolder(binding.root)
    class PassedItemViewHolder(private val binding: ItemAchievePassedItemBinding)
        : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: PassedMarkData) {
            binding.apply {
                achievementPassedName.text = data.name
                achievementPassedPaper?.text = data.paper
                achievementPassedMark.text = data.mark
                achievementPassedRetake.text = data.retake
                achievementPassedRebuild.text = data.rebuild
                achievementPassedCredit?.text = data.credit
                if (!judgePass(data.mark) && !judgePass(data.retake) && !judgePass(data.rebuild)){
                    achievementPassedName.setTextColor(Color.RED)
                    achievementPassedPaper?.setTextColor(Color.RED)
                    achievementPassedMark.setTextColor(Color.RED)
                    achievementPassedRetake.setTextColor(Color.RED)
                    achievementPassedRebuild.setTextColor(Color.RED)
                    achievementPassedCredit?.setTextColor(Color.RED)
                }
            }
        }
    }
    class PassedEmptyItemViewHolder(binding: ItemAchievePassedEmptyBinding)
        : RecyclerView.ViewHolder(binding.root)
    class FailedHeadViewHolder(binding: ItemAchieveFailedHeadBinding)
        : RecyclerView.ViewHolder(binding.root)
    class FailedItemViewHolder(private val binding: ItemAchieveFailedItemBinding)
        : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: FailedMarkData) {
            binding.apply {
                gradeFailedName.text = data.name
                gradeFailedMark.text = data.mark
            }
        }
    }
}