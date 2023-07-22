package com.launcher.utils

import android.app.Activity
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import kotlin.math.abs

class Gestures(
    context: Activity?,
    onSwipeListener: OnSwipeListener
) : SimpleOnGestureListener() {

    companion object {
        private const val minScrollDistanceX = 50
        private const val minScrollDistanceY = 50
    }

    private val detector: GestureDetector
    private val listener: OnSwipeListener

    init {
        detector = GestureDetector(context, this)
        listener = onSwipeListener
    }

    fun onTouchEvent(event: MotionEvent?) {
        event?.apply {
            detector.onTouchEvent(this)
        }
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        var consumed = false
        val absDistanceX = abs(distanceX)
        val absDistanceY = abs(distanceY)
        if (absDistanceX > absDistanceY) {
            // horizontal scroll
            if (absDistanceX > minScrollDistanceX) {
                if (distanceX > 0) {
                    listener.onSwipe(Direction.SWIPE_LEFT)
                } else {
                    listener.onSwipe(Direction.SWIPE_RIGHT)
                }
                consumed = true
            }
        } else {
            // vertical scroll
            if (absDistanceY > minScrollDistanceY) {
                if (distanceY > 0) {
                    listener.onSwipe(Direction.SWIPE_UP)
                } else {
                    listener.onSwipe(Direction.SWIPE_DOWN)
                }
                consumed = true
            }
        }
        return consumed
    }

    override fun onDoubleTap(arg: MotionEvent): Boolean {
        listener.onDoubleTap()
        return true
    }

    enum class Direction {
        SWIPE_UP, SWIPE_DOWN, SWIPE_LEFT, SWIPE_RIGHT
    }

    interface OnSwipeListener {
        fun onSwipe(direction: Direction?)
        fun onDoubleTap()
    }
}
