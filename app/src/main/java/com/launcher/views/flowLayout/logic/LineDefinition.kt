package com.launcher.views.flowLayout.logic

import kotlin.math.max

class LineDefinition(private val config: ConfigDefinition) {
    val views = ArrayList<ViewDefinition>()
    var lineLength = 0
        private set
    var lineThickness = 0
        private set
    var lineStartThickness = 0
    var lineStartLength = 0

    fun addView(child: ViewDefinition) {
        this.addView(views.size, child)
    }

    fun addView(i: Int, child: ViewDefinition) {
        views.add(i, child)
        lineLength += child.length + child.spacingLength
        lineThickness = max(lineThickness, child.thickness + child.spacingThickness)
    }

    fun canFit(child: ViewDefinition): Boolean {
        return lineLength + child.length + child.spacingLength <= config.maxLength
    }

    fun setThickness(thickness: Int) {
        lineThickness = thickness
    }

    fun setLength(length: Int) {
        lineLength = length
    }

    val x: Int
        get() = if (config.getOrientation() == CommonLogic.HORIZONTAL) lineStartLength else lineStartThickness
    val y: Int
        get() = if (config.getOrientation() == CommonLogic.HORIZONTAL) lineStartThickness else lineStartLength
}
