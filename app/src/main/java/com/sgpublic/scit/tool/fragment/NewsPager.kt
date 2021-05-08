package com.sgpublic.scit.tool.fragment

import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.sgpublic.scit.tool.R
import com.sgpublic.scit.tool.activity.WebView
import com.sgpublic.scit.tool.base.BaseFragment
import com.sgpublic.scit.tool.util.MyLog
import com.sgpublic.scit.tool.data.NewsData
import com.sgpublic.scit.tool.databinding.ItemNewsBinding
import com.sgpublic.scit.tool.databinding.PagerNewsBinding
import com.sgpublic.scit.tool.helper.NewsHelper
import com.sgpublic.scit.tool.widget.ObservableScrollView
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.lang.NullPointerException
import kotlin.jvm.Throws

class NewsPager(private val contest: AppCompatActivity, private val name: String, private val tid: Int) : BaseFragment<PagerNewsBinding>(contest) {
    private var listPageSize = 0
    private var hasNext = false
    private var loading = false

    private val sortData: ArrayList<CharSequence> = arrayListOf()

    override fun getTitle() = name

    override fun onFragmentCreated(savedInstanceState: Bundle?) {
        listPageSize = 0

        binding.sortRefresh.setOnRefreshListener {
            listPageSize = 0
            binding.sortGridIndex.removeAllViews()
            getGridData()
        }

        binding.sortRefresh.isRefreshing = true

        if (savedInstanceState != null) {
            hasNext = savedInstanceState.getBoolean("sortHasNext")
            loadInstance(savedInstanceState.getCharSequenceArrayList("sortData"))
        } else {
            getGridData()
        }
    }

    override fun onViewSetup() {
        binding.sortScroll.setOnScrollToBottomListener(binding.sortScrollContent, object : ObservableScrollView.ScrollToBottomListener{
            override fun onScrollToBottom() {
                if (!loading){
                    getGridData()
                }
            }
        })
    }

    private fun getGridData() {
        binding.sortRefresh.isRefreshing = true
        NewsHelper(contest).getNewsByType(tid, listPageSize, object : NewsHelper.Callback {
            override fun onFailure(code: Int, message: String?, e: Exception?) {
                binding.sortRefresh.isRefreshing = false
                onToast(R.string.error_news_load, message, code)
            }

            override fun onNewsResult(news: ArrayList<NewsData>, hasNext: Boolean) {
                super.onNewsResult(news, hasNext)
                this@NewsPager.hasNext = hasNext
                runOnUiThread {
                    try {
                        loadGirdData(news)
                    } catch (ignored: NullPointerException) { }
                }
            }
        })
    }

    @Throws(NullPointerException::class)
    private fun loadGirdData(dataArray: ArrayList<NewsData>) {
        if (hasNext) {
            binding.sortGridEnd.setText(R.string.error_loading_more)
            binding.sortScroll.setOnScrollToBottomListener(
                binding.sortScrollContent, object : ObservableScrollView.ScrollToBottomListener {
                    override fun onScrollToBottom() {
                        getGridData()
                    }
                })
        } else {
            binding.sortGridEnd.setText(R.string.error_no_more)
            binding.sortScroll.setOnScrollToBottomListener(null)
        }

        for (i in 0 until dataArray.size) {
            val itemNews = ItemNewsBinding.inflate(layoutInflater)

            val dataIndex: NewsData = dataArray[i]
            itemNews.itemNewsTitle.text = dataIndex.title
            itemNews.itemNewsCreateTime.text = dataIndex.createTime

            if (dataIndex.images.size > 0){
                val requestOptions: RequestOptions = RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                for (imageIndex in dataIndex.images){
                    val imageView = ImageView(contest)
                    imageView.alpha = 0.0F
                    imageView.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0F)
                    imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                    val glide = Glide.with(contest)
                        .load(imageIndex)
                        .apply(requestOptions)
                        .addListener(object : RequestListener<Drawable> {
                            override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                imageView.animate().alpha(1f).setDuration(400)
                                    .start()
                                return false
                            }

                            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                                return false
                            }
                        })
                    runOnUiThread {
                        glide.into(imageView)
                    }
                    itemNews.itemNewsSummaries.addView(imageView)
                }
            } else {
                itemNews.itemNewsSummaries.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                val summary = TextView(contest)
                summary.text = dataIndex.summary
                summary.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                summary.setLineSpacing(dip2px(6.0F).toFloat(), 1.0F)
                summary.paint.flags = Paint.UNDERLINE_TEXT_FLAG
                summary.paint.isAntiAlias = true
                summary.maxLines = 3
                summary.ellipsize = TextUtils.TruncateAt.END
                summary.setBackgroundColor(contest.getColor(R.color.colorCompat))
                summary.setTextColor(contest.getColor(R.color.colorPrimary))
                summary.setPadding(dip2px(20.0F), dip2px(20.0F), dip2px(20.0F), dip2px(20.0F))
                itemNews.itemNewsSummaries.addView(summary)
            }
            itemNews.root.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            itemNews.root.setOnClickListener {
                WebView.startActivity(contest, dataIndex.type, dataIndex.id)
            }
            runOnUiThread {
                binding.sortGridIndex.addView(itemNews.root)
            }
        }

        saveInstance(dataArray)
        binding.sortRefresh.isRefreshing = false
        listPageSize++
    }

    private fun saveInstance(dataArray: ArrayList<NewsData>) {
        if (sortData.size < 1) {
            val arrays = JSONArray()
            for (dataIndex in dataArray) {
                val objectIndex: JSONObject = JSONObject().apply {
                    put("title", dataIndex.title)
                    put("tid", dataIndex.type)
                    put("nid", dataIndex.id)
                    put("create_time", dataIndex.createTime)
                    put("summary", dataIndex.summary)
                }
                val images = JSONArray()
                for (imageItem in dataIndex.images){
                    images.put(imageItem)
                }
                objectIndex.put("images", images)
                arrays.put(objectIndex)
            }
            sortData.add(arrays.toString())
        }
    }

    private fun loadInstance(dataArray: ArrayList<CharSequence>?) {
        Thread {
            dataArray?.let {
                try {
                    val dataIndex: ArrayList<NewsData> = arrayListOf()
                    val jsonArray = JSONArray(it[0].toString())
                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        val images = ArrayList<String>()
                        val imagesArray = jsonObject.getJSONArray("images")
                        for (index in 0 until imagesArray.length()){
                            images.add(imagesArray.getString(index))
                        }
                        dataIndex.add(
                            NewsData(
                                jsonObject.getInt("tid"),
                                jsonObject.getInt("nid"),
                                jsonObject.getString("title"),
                                jsonObject.getString("summary"),
                                images,
                                jsonObject.getString("create_time"),
                            )
                        )
                    }
                    loadGirdData(dataIndex)
                } catch (ignored: JSONException) {
                } catch (ignored: NullPointerException) {
                } catch (ignored: IndexOutOfBoundsException) { }
            }
        }.start()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putCharSequenceArrayList("sortData", sortData)
        outState.putBoolean("sortHasNext", hasNext)
        super.onSaveInstanceState(outState)
    }
}
