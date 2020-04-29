package com.codepipes.tingadmin.custom

import android.R.attr
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.widget.NestedScrollView


class LockedNestedScrollView : NestedScrollView {

    private var mInterceptScrollViews: MutableList<View> = ArrayList<View>()

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {}

    private fun addInterceptScrollView(view: View) {
        mInterceptScrollViews.add(view)
    }

    private fun removeInterceptScrollView(view: View?) {
        mInterceptScrollViews.remove(view)
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        if (mInterceptScrollViews.size > 0) {
            val x = event.x.toInt()
            val y = event.y.toInt()
            val bounds = Rect()
            for (view in mInterceptScrollViews) {
                view.getHitRect(bounds)
                if (bounds.contains(x, y + attr.scrollY)) { return false }
            }
        } else {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> super.onTouchEvent(event)
                MotionEvent.ACTION_MOVE -> return false
                MotionEvent.ACTION_CANCEL -> super.onTouchEvent(event)
                MotionEvent.ACTION_UP -> return false
                else -> { }
            }
            return false
        }
        return super.onInterceptTouchEvent(event)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.onTouchEvent(event)
        return true
    }
}