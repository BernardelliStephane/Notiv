package fr.steph.accordionlayout

import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible

object WidgetHelper {

    fun isNullOrBlank(`object`: Any?): Boolean {
        if (`object` == null) return true
        if (`object` is String) return `object`.isEmpty()
        if (`object` is Collection<*>) if (`object`.isEmpty()) return true
        return false
    }

    fun getFullHeight(layout: ViewGroup): Int {
        val specWidth = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        val specHeight = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        layout.measure(specWidth, specHeight)
        var totalHeight = 0
        val isInitialyVisible = layout.isVisible
        layout.isVisible = true
        val numberOfChildren = layout.childCount
        for (i in 0 until numberOfChildren) {
            val child = layout.getChildAt(i)
            totalHeight += if (child is ViewGroup) getFullHeight(child)
            else {
                val desiredWidth = View.MeasureSpec.makeMeasureSpec(layout.width, View.MeasureSpec.AT_MOST)
                child.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED)
                child.measuredHeight
            }
        }
        layout.isVisible = isInitialyVisible
        return totalHeight
    }
}