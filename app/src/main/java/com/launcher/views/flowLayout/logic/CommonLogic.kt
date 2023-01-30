package com.launcher.views.flowLayout.logic

import android.graphics.Rect
import android.view.Gravity
import android.view.View
import kotlin.math.min
import kotlin.math.roundToInt

object CommonLogic {
    const val HORIZONTAL = 0
    const val VERTICAL = 1

    @JvmStatic
    fun calculateLinesAndChildPosition(lines: List<LineDefinition>) {
        var prevLinesThickness = 0
        val linesCount = lines.size
        for (i in 0 until linesCount) {
            val line = lines[i]
            line.lineStartThickness = prevLinesThickness
            prevLinesThickness += line.lineThickness
            var prevChildThickness = 0
            val childViews = line.views
            val childCount = childViews.size
            for (j in 0 until childCount) {
                val child = childViews[j]
                child.inlineStartLength = prevChildThickness
                prevChildThickness += child.length + child.spacingLength
            }
        }
    }

    @JvmStatic
    fun applyGravityToLines(
        lines: List<LineDefinition>,
        realControlLength: Int,
        realControlThickness: Int,
        config: ConfigDefinition
    ) {
        val linesCount = lines.size
        if (linesCount <= 0) {
            return
        }
        var remainingWeight = linesCount
        val lastLine = lines[linesCount - 1]
        var excessThickness =
            realControlThickness - (lastLine.lineThickness + lastLine.lineStartThickness)
        if (excessThickness < 0) {
            excessThickness = 0
        }
        var excessOffset = 0
        for (i in 0 until linesCount) {
            val child = lines[i]
            val weight = 1
            val gravity = getGravity(null, config)
            val extraThickness = (excessThickness * weight / remainingWeight).toFloat().roundToInt()
            excessThickness -= extraThickness
            remainingWeight -= weight
            val childLength = child.lineLength
            val childThickness = child.lineThickness
            val container = Rect()
            container.top = excessOffset
            container.left = 0
            container.right = realControlLength
            container.bottom = childThickness + extraThickness + excessOffset
            val result = Rect()
            Gravity.apply(gravity, childLength, childThickness, container, result)
            excessOffset += extraThickness
            child.lineStartLength = child.lineStartLength + result.left
            child.lineStartThickness = child.lineStartThickness + result.top
            child.setLength(result.width())
            child.setThickness(result.height())
            applyGravityToLine(child, config)
        }
    }

    fun applyGravityToLine(line: LineDefinition, config: ConfigDefinition) {
        val views = line.views
        val viewCount = views.size
        if (viewCount <= 0) {
            return
        }
        var remainingWeight = 0f
        for (i in 0 until viewCount) {
            val child = views[i]
            remainingWeight += getWeight(child, config)
        }
        val weightBased = remainingWeight > 0
        val lastChild = views[viewCount - 1]
        var excessLengthRemaining =
            line.lineLength - (lastChild.length + lastChild.spacingLength + lastChild.inlineStartLength)
        var excessOffset = 0
        for (i in 0 until viewCount) {
            val child = views[i]
            val weight = getWeight(child, config)
            val gravity = getGravity(child, config)
            var extraLength: Int
            if (!weightBased) {
                extraLength = excessLengthRemaining / (viewCount - i)
            } else {
                extraLength = (excessLengthRemaining * weight / remainingWeight).roundToInt()
                remainingWeight -= weight
            }
            excessLengthRemaining -= extraLength
            val childLength = child.length + child.spacingLength
            val childThickness = child.thickness + child.spacingThickness
            val container = Rect()
            container.top = 0
            container.left = excessOffset
            container.right = childLength + extraLength + excessOffset
            container.bottom = line.lineThickness
            val result = Rect()
            Gravity.apply(gravity, childLength, childThickness, container, result)
            excessOffset += extraLength
            child.inlineStartLength = result.left + child.inlineStartLength
            child.inlineStartThickness = result.top
            child.length = result.width() - child.spacingLength
            child.thickness = result.height() - child.spacingThickness
        }
    }

    @JvmStatic
    fun findSize(modeSize: Int, controlMaxSize: Int, contentSize: Int): Int {
        val realControlSize: Int = when (modeSize) {
            View.MeasureSpec.AT_MOST -> min(contentSize, controlMaxSize)
            View.MeasureSpec.EXACTLY -> controlMaxSize
            else -> contentSize
        }
        return realControlSize
    }

    private fun getWeight(child: ViewDefinition, config: ConfigDefinition): Float {
        return if (child.weightSpecified()) child.weight else config.weightDefault
    }

    private fun getGravity(child: ViewDefinition?, config: ConfigDefinition): Int {
        var parentGravity = config.gravity
        var childGravity: Int
        // get childGravity of child view (if exists)
        childGravity = if (child != null && child.gravitySpecified()) {
            child.gravity
        } else {
            parentGravity
        }
        childGravity = getGravityFromRelative(childGravity, config)
        parentGravity = getGravityFromRelative(parentGravity, config)

        // add parent gravity to child gravity if child gravity is not specified
        if (childGravity and Gravity.HORIZONTAL_GRAVITY_MASK == 0) {
            childGravity = childGravity or (parentGravity and Gravity.HORIZONTAL_GRAVITY_MASK)
        }
        if (childGravity and Gravity.VERTICAL_GRAVITY_MASK == 0) {
            childGravity = childGravity or (parentGravity and Gravity.VERTICAL_GRAVITY_MASK)
        }

        // if childGravity is still not specified - set default top - left gravity
        if (childGravity and Gravity.HORIZONTAL_GRAVITY_MASK == 0) {
            childGravity = childGravity or Gravity.START
        }
        if (childGravity and Gravity.VERTICAL_GRAVITY_MASK == 0) {
            childGravity = childGravity or Gravity.TOP
        }
        return childGravity
    }

    private fun getGravityFromRelative(childGravity: Int, config: ConfigDefinition): Int {
        // swap directions for vertical non relative view
        // if it is relative, then START is TOP, and we do not need to switch it here.
        // it will be switched later on onMeasure stage when calculations will be with length and thickness
        var mChildGravity = childGravity
        if (config.orientation == VERTICAL && mChildGravity and Gravity.RELATIVE_LAYOUT_DIRECTION == 0) {
            val horizontalGravity = mChildGravity
            mChildGravity = 0
            mChildGravity =
                mChildGravity or (horizontalGravity and Gravity.HORIZONTAL_GRAVITY_MASK shr Gravity.AXIS_X_SHIFT shl Gravity.AXIS_Y_SHIFT)
            mChildGravity =
                mChildGravity or (horizontalGravity and Gravity.VERTICAL_GRAVITY_MASK shr Gravity.AXIS_Y_SHIFT shl Gravity.AXIS_X_SHIFT)
        }

        // for relative layout and RTL direction swap left and right gravity
        if (config.layoutDirection == View.LAYOUT_DIRECTION_RTL && mChildGravity and Gravity.RELATIVE_LAYOUT_DIRECTION != 0) {
            val ltrGravity = mChildGravity
            mChildGravity = 0
            mChildGravity =
                mChildGravity or if (ltrGravity and Gravity.START == Gravity.START) Gravity.END else 0
            mChildGravity =
                mChildGravity or if (ltrGravity and Gravity.END == Gravity.END) Gravity.START else 0
        }
        return mChildGravity
    }

    @JvmStatic
    fun fillLines(
        views: List<ViewDefinition>,
        lines: MutableList<LineDefinition?>,
        config: ConfigDefinition
    ) {
        var currentLine = LineDefinition(config)
        lines.add(currentLine)
        val count = views.size
        for (i in 0 until count) {
            val child = views[i]
            val newLine = child.isNewLine || config.isCheckCanFit && !currentLine.canFit(child)
            if (newLine && config.maxLines > 0 && lines.size == config.maxLines) break
            if (newLine) {
                currentLine = LineDefinition(config)
                if (config.orientation == VERTICAL && config.layoutDirection == View.LAYOUT_DIRECTION_RTL) {
                    lines.add(0, currentLine)
                } else {
                    lines.add(currentLine)
                }
            }
            if (config.orientation == HORIZONTAL && config.layoutDirection == View.LAYOUT_DIRECTION_RTL) {
                currentLine.addView(0, child)
            } else {
                currentLine.addView(child)
            }
        }
    }
}
