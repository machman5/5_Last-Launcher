package com.launcher.views.flowLayout.logic

import android.view.Gravity
import android.view.View

class ViewDefinition(
    private val config: ConfigDefinition,
    val view: View
) {
    var inlineStartLength = 0
    var weight = 0f
    var gravity = 0
    var isNewLine = false
    var inlineStartThickness = 0
    var width = 0
    var height = 0
    private var leftMargin = 0
    private var topMargin = 0
    private var rightMargin = 0
    private var bottomMargin = 0

    var length: Int
        get() = if (config.getOrientation() == CommonLogic.HORIZONTAL) width else height
        set(length) {
            if (config.getOrientation() == CommonLogic.HORIZONTAL) {
                width = length
            } else {
                height = length
            }
        }
    val spacingLength: Int
        get() = if (config.getOrientation() == CommonLogic.HORIZONTAL) leftMargin + rightMargin else topMargin + bottomMargin
    var thickness: Int
        get() = if (config.getOrientation() == CommonLogic.HORIZONTAL) height else width
        set(thickness) {
            if (config.getOrientation() == CommonLogic.HORIZONTAL) {
                height = thickness
            } else {
                width = thickness
            }
        }
    val spacingThickness: Int
        get() = if (config.getOrientation() == CommonLogic.HORIZONTAL) topMargin + bottomMargin else leftMargin + rightMargin

    fun weightSpecified(): Boolean {
        return weight >= 0
    }

    fun gravitySpecified(): Boolean {
        return gravity != Gravity.NO_GRAVITY
    }

    fun setMargins(
        leftMargin: Int,
        topMargin: Int,
        rightMargin: Int,
        bottomMargin: Int
    ) {
        this.leftMargin = leftMargin
        this.topMargin = topMargin
        this.rightMargin = rightMargin
        this.bottomMargin = bottomMargin
    }

    val inlineX: Int
        get() = if (config.getOrientation() == CommonLogic.HORIZONTAL) inlineStartLength else inlineStartThickness
    val inlineY: Int
        get() = if (config.getOrientation() == CommonLogic.HORIZONTAL) inlineStartThickness else inlineStartLength
}
