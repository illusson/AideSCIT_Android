package io.github.sgpublic.aidescit.fragment

import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.TextUtils
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import io.github.sgpublic.aidescit.Application
import io.github.sgpublic.aidescit.R
import io.github.sgpublic.aidescit.activity.WebView
import io.github.sgpublic.aidescit.base.BaseViewModelFragment
import io.github.sgpublic.aidescit.core.data.NewsData
import io.github.sgpublic.aidescit.core.module.NewsModule
import io.github.sgpublic.aidescit.core.util.dp
import io.github.sgpublic.aidescit.databinding.ItemNewsBinding
import io.github.sgpublic.aidescit.databinding.PagerNewsBinding
import io.github.sgpublic.aidescit.viewmodule.PagerNewsViewModule
import io.github.sgpublic.aidescit.widget.ObservableScrollView
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class NewsPage(contest: AppCompatActivity, private val name: String, private val tid: Int)
    : BaseViewModelFragment<PagerNewsBinding, PagerNewsViewModule>(contest) {
    private var listPageSize = 0
    private var hasNext = false
    private var loading = false

    override val ViewModel: PagerNewsViewModule by activityViewModels()

    private val sortData: ArrayList<CharSequence> = arrayListOf()

    override fun getTitle() = name

    override fun onFragmentCreated(hasSavedInstanceState: Boolean) {
        listPageSize = 0

        ViewBinding.sortRefresh.setOnRefreshListener {
            listPageSize = 0
            ViewBinding.sortGridIndex.removeAllViews()
            getGridData()
        }

        ViewBinding.sortRefresh.isRefreshing = true

        if (hasSavedInstanceState) {
            hasNext = STATE.getBoolean("sortHasNext")
            loadInstance(STATE.getCharSequenceArrayList("sortData"))
        } else {
            getGridData()
        }
    }

    override fun onViewSetup() {
        ViewBinding.sortScroll.setOnScrollToBottomListener(ViewBinding.sortScrollContent, object : ObservableScrollView.ScrollToBottomListener{
            override fun onScrollToBottom() {
                if (!loading){
                    getGridData()
                }
            }
        })
    }

    private fun getGridData() {
        ViewBinding.sortRefresh.isRefreshing = true
        NewsModule().getNewsByType(tid, listPageSize, object : NewsModule.Callback {
            override fun onFailure(code: Int, message: String?, e: Throwable?) {
                ViewBinding.sortRefresh.isRefreshing = false
                Application.onToast(context, R.string.error_news_load, message, code)
            }

            override fun onNewsResult(news: ArrayList<NewsData>, hasNext: Boolean) {
                super.onNewsResult(news, hasNext)
                this@NewsPage.hasNext = hasNext
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
            ViewBinding.sortGridEnd.setText(R.string.error_loading_more)
            ViewBinding.sortScroll.setOnScrollToBottomListener(
                ViewBinding.sortScrollContent, object : ObservableScrollView.ScrollToBottomListener {
                    override fun onScrollToBottom() {
                        getGridData()
                    }
                })
        } else {
            ViewBinding.sortGridEnd.setText(R.string.error_no_more)
            ViewBinding.sortScroll.setOnScrollToBottomListener(null)
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
                    val imageView = ImageView(context)
                    imageView.alpha = 0.0F
                    imageView.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0F)
                    imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                    val glide = Glide.with(context)
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
                val summary = TextView(context)
                summary.text = dataIndex.summary
                summary.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                summary.setLineSpacing(6.dp.toFloat(), 1F)
                summary.paint.flags = Paint.UNDERLINE_TEXT_FLAG
                summary.paint.isAntiAlias = true
                summary.maxLines = 3
                summary.ellipsize = TextUtils.TruncateAt.END
                summary.setBackgroundColor(context.getColor(R.color.colorCompat))
                summary.setTextColor(context.getColor(R.color.colorPrimary))
                summary.setPadding(20.dp, 20.dp, 20.dp, 20.dp)
                itemNews.itemNewsSummaries.addView(summary)
            }
            itemNews.root.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            itemNews.root.setOnClickListener {
                WebView.startActivity(context, dataIndex.type, dataIndex.id)
            }
            runOnUiThread {
                ViewBinding.sortGridIndex.addView(itemNews.root)
            }
        }

        saveInstance(dataArray)
        ViewBinding.sortRefresh.isRefreshing = false
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
        STATE.putCharSequenceArrayList("sortData", sortData)
        STATE.putBoolean("sortHasNext", hasNext)

        super.onSaveInstanceState(outState)
    }

    override fun onCreateViewBinding(container: ViewGroup?): PagerNewsBinding =
        PagerNewsBinding.inflate(layoutInflater)
}
