package com.sgpublic.scit.tool.ui

import android.graphics.drawable.Drawable
import android.os.Handler
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.sgpublic.scit.tool.R
import com.sgpublic.scit.tool.data.BannerItem
import com.zhpan.bannerview.holder.ViewHolder
import kotlinx.android.synthetic.main.item_news_banner.view.*

class NewsBannerAdapter : ViewHolder<BannerItem> {
    override fun getLayoutId(): Int = R.layout.item_news_banner

    override fun onBind(itemView: View?, data: BannerItem?, position: Int, size: Int) {
        data ?: return
        itemView ?: return
        if (data.image != ""){
            val requestOptions: RequestOptions = RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            Glide.with(data.context)
                .load(data.image)
                .apply(requestOptions)
                .addListener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                        return false
                    }

                    override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        itemView.banner_image_foreground.visibility = View.VISIBLE
                        itemView.banner_image_foreground.animate().alpha(1f).setDuration(400).setListener(null)
                        return false
                    }
                })
                .into(itemView.banner_image_foreground)
            Glide.with(data.context)
                .load(data.image)
                .apply(requestOptions)
                .apply(RequestOptions.bitmapTransform(BlurHelper()))
                .addListener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                        return false
                    }

                    override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        itemView.banner_image.visibility = View.VISIBLE
                        itemView.banner_image.animate().alpha(1f).setDuration(400).setListener(null)
                        return false
                    }
                })
                .into(itemView.banner_image)
            itemView.banner_content.text = data.title
        }
    }
}