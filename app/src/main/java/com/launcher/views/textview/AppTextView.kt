package com.launcher.views.textview

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.widget.TextView

/**
 * App TextView class
 * Simple TextView no cosmetic, no styling..
 * only hold some extra fields that are useful for this launcher
 */
class AppTextView : TextView {
    var isShortcut = false

    var uri: String? = null

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
    }
}
