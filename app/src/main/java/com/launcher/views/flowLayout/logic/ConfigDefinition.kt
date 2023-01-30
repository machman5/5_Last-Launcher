package com.launcher.views.flowLayout.logic

import android.view.Gravity
import android.view.View
import kotlin.math.max

class ConfigDefinition {
    private var orientation = 0
    var isDebugDraw = false
    var weightDefault = 0f
        set(weightDefault) {
            field = max(0f, weightDefault)
        }
    var gravity = 0
    var layoutDirection = 0
        set(layoutDirection) {
            field = if (layoutDirection == View.LAYOUT_DIRECTION_RTL) {
                layoutDirection
            } else {
                View.LAYOUT_DIRECTION_LTR
            }
        }
    private var maxWidth = 0
    private var maxHeight = 0
    var isCheckCanFit = false
    private var widthMode = 0
    private var heightMode = 0
    var maxLines = 0

    init {
        setOrientation(CommonLogic.HORIZONTAL)
        isDebugDraw = false
        weightDefault = 0.0f
        gravity = Gravity.NO_GRAVITY
        layoutDirection = View.LAYOUT_DIRECTION_LTR
        isCheckCanFit = true
        maxLines = 0
    }

    fun getOrientation(): Int {
        return orientation
    }

    fun setOrientation(orientation: Int) {
        if (orientation == CommonLogic.VERTICAL) {
            this.orientation = orientation
        } else {
            this.orientation = CommonLogic.HORIZONTAL
        }
    }

    fun setMaxWidth(maxWidth: Int) {
        this.maxWidth = maxWidth
    }

    fun setMaxHeight(maxHeight: Int) {
        this.maxHeight = maxHeight
    }

    val maxLength: Int
        get() = if (orientation == CommonLogic.HORIZONTAL) maxWidth else maxHeight
    val maxThickness: Int
        get() = if (orientation == CommonLogic.HORIZONTAL) maxHeight else maxWidth

    fun setWidthMode(widthMode: Int) {
        this.widthMode = widthMode
    }

    fun setHeightMode(heightMode: Int) {
        this.heightMode = heightMode
    }

    val lengthMode: Int
        get() = if (orientation == CommonLogic.HORIZONTAL) widthMode else heightMode
    val thicknessMode: Int
        get() = if (orientation == CommonLogic.HORIZONTAL) heightMode else widthMode
}
