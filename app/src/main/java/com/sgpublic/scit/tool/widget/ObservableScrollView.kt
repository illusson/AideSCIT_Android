package com.sgpublic.scit.tool.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.core.widget.NestedScrollView


class ObservableScrollView : NestedScrollView {
    private var contentView: View? = null
    private var scrollToBottomListener: ScrollToBottomListener? = null
    private var scrollChangeListener: ScrollChangeListener? = null

    private var scrollToEnd: Boolean = false

    constructor(context: Context) : super(context)

    constructor(
        context: Context, attrs: AttributeSet?,
        defStyle: Int
    ) : super(context, attrs, defStyle)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    fun setOnScrollToBottomListener(contentView: View?, scrollViewListener: ScrollToBottomListener? = null) {
        this.contentView = contentView
        this.scrollToBottomListener = scrollViewListener
    }

    fun setOnChangeListener(scrollViewListener: ScrollChangeListener? = null) {
        this.scrollChangeListener = scrollViewListener
    }

    override fun onScrollChanged(x: Int, y: Int, oldx: Int, oldy: Int) {
        super.onScrollChanged(x, y, oldx, oldy)
        contentView?.let {
            if (it.height > y + height) {
                scrollToEnd = false
            } else if (!scrollToEnd) {
                scrollToEnd = true
                scrollToBottomListener?.onScrollToBottom()
            }
        }

        scrollChangeListener?.onScrollChange(y, y - oldy)
    }

    interface ScrollToBottomListener {
        fun onScrollToBottom()
    }

    interface ScrollChangeListener {
        fun onScrollChange(y: Int, transform: Int)
    }
}
