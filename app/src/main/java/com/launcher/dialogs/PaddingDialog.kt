package com.launcher.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.View.OnLongClickListener
import android.view.Window
import android.widget.TextView
import com.R
import com.launcher.utils.Constants.MAX_PADDING_BOTTOM
import com.launcher.utils.Constants.MAX_PADDING_LEFT
import com.launcher.utils.Constants.MAX_PADDING_RIGHT
import com.launcher.utils.Constants.MAX_PADDING_TOP
import com.launcher.utils.Constants.MIN_PADDING
import com.launcher.utils.DbUtils.paddingBottom
import com.launcher.utils.DbUtils.paddingLeft
import com.launcher.utils.DbUtils.paddingRight
import com.launcher.utils.DbUtils.paddingTop
import org.apmem.tools.layouts.FlowLayout

class PaddingDialog(
    context: Context,
    private val homeLayout: FlowLayout
) : Dialog(
    context
), OnLongClickListener, View.OnClickListener {

    private val handler = Handler(Looper.getMainLooper())
    private var tvLeftPadding: TextView? = null
    private var tvRightPadding: TextView? = null
    private var tvTopPadding: TextView? = null
    private var tvBottomPadding: TextView? = null
    private var topInt = 0
    private var leftInt = 0
    private var rightInt = 0
    private var bottomInt = 0
    private var runnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dlg_padding)
        val btnLeftMinus = findViewById<TextView>(R.id.btn_left_minus)
        btnLeftMinus.setOnLongClickListener(this)
        btnLeftMinus.setOnClickListener(this)
        val btnRightMinus = findViewById<TextView>(R.id.btn_right_minus)
        btnRightMinus.setOnLongClickListener(this)
        btnRightMinus.setOnClickListener(this)
        val btnTopMinus = findViewById<TextView>(R.id.btn_top_minus)
        btnTopMinus.setOnLongClickListener(this)
        btnTopMinus.setOnClickListener(this)
        val btnBottomMinus = findViewById<TextView>(R.id.btn_bottom_minus)
        btnBottomMinus.setOnLongClickListener(this)
        btnBottomMinus.setOnClickListener(this)
        val btnLeftPlus = findViewById<TextView>(R.id.btn_left_plus)
        btnLeftPlus.setOnLongClickListener(this)
        btnLeftPlus.setOnClickListener(this)
        val btnRightPlus = findViewById<TextView>(R.id.btn_right_plus)
        btnRightPlus.setOnLongClickListener(this)
        btnRightPlus.setOnClickListener(this)
        val btnTopPlus = findViewById<TextView>(R.id.btn_top_plus)
        btnTopPlus.setOnLongClickListener(this)
        btnTopPlus.setOnClickListener(this)
        val btnBottomPlus = findViewById<TextView>(R.id.btn_bottom_plus)
        btnBottomPlus.setOnLongClickListener(this)
        btnBottomPlus.setOnClickListener(this)
        tvLeftPadding = findViewById(R.id.tv_left_padding)
        tvRightPadding = findViewById(R.id.tv_right_padding)
        tvTopPadding = findViewById(R.id.tv_top_padding)
        tvBottomPadding = findViewById(R.id.tv_bottom_padding)
        leftInt = paddingLeft
        rightInt = paddingRight
        topInt = paddingTop
        bottomInt = paddingBottom
        tvLeftPadding?.text = leftInt.toString()
        tvRightPadding?.text = rightInt.toString()
        tvTopPadding?.text = topInt.toString()
        tvBottomPadding?.text = bottomInt.toString()
    }

    override fun onStop() {
        super.onStop()

        // dialog is about to finish, so store the updated values to DB
        paddingLeft = leftInt
        paddingRight = rightInt
        paddingTop = topInt
        paddingBottom = bottomInt
    }

    override fun onLongClick(button: View): Boolean {
        when (button.id) {
            R.id.btn_left_minus -> runner(button as TextView, tvLeftPadding, -2, Padding.LEFT)
            R.id.btn_left_plus -> runner(button as TextView, tvLeftPadding, 2, Padding.LEFT)
            R.id.btn_right_minus -> runner(button as TextView, tvRightPadding, -2, Padding.RIGHT)
            R.id.btn_right_plus -> runner(button as TextView, tvRightPadding, 2, Padding.RIGHT)
            R.id.btn_top_minus -> runner(button as TextView, tvTopPadding, -2, Padding.TOP)
            R.id.btn_top_plus -> runner(button as TextView, tvTopPadding, 2, Padding.TOP)
            R.id.btn_bottom_minus -> runner(button as TextView, tvBottomPadding, -2, Padding.BOTTOM)
            R.id.btn_bottom_plus -> runner(button as TextView, tvBottomPadding, 2, Padding.BOTTOM)
        }
        return true
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btn_left_minus -> {
                //decrease the value as minus button is pressed
                leftInt--
                // check the lower limit i.e. 0
                if (leftInt < MIN_PADDING) {
                    leftInt = MIN_PADDING
                }
                //reflect it on screen
                tvLeftPadding?.text = leftInt.toString()
            }
            R.id.btn_left_plus -> {
                leftInt++
                // check the upper limit
                if (leftInt > MAX_PADDING_LEFT) {
                    leftInt = MAX_PADDING_LEFT
                }
                tvLeftPadding?.text = leftInt.toString()
            }
            R.id.btn_right_minus -> {
                rightInt--
                if (rightInt < MIN_PADDING) {
                    rightInt = MIN_PADDING
                }
                tvRightPadding?.text = rightInt.toString()
            }
            R.id.btn_right_plus -> {
                rightInt++
                if (rightInt > MAX_PADDING_RIGHT) {
                    rightInt = MAX_PADDING_RIGHT
                }
                tvRightPadding?.text = rightInt.toString()
            }
            R.id.btn_top_minus -> {
                topInt--
                if (topInt < MIN_PADDING) {
                    topInt = MIN_PADDING
                }
                tvTopPadding?.text = topInt.toString()
            }
            R.id.btn_top_plus -> {
                topInt++
                if (topInt > MAX_PADDING_TOP) {
                    topInt = MAX_PADDING_TOP
                }
                tvTopPadding?.text = topInt.toString()
            }
            R.id.btn_bottom_minus -> {
                bottomInt--
                if (bottomInt < MIN_PADDING) {
                    bottomInt = MIN_PADDING
                }
                tvBottomPadding?.text = bottomInt.toString()
            }
            R.id.btn_bottom_plus -> {
                bottomInt++
                if (bottomInt > MAX_PADDING_BOTTOM) {
                    bottomInt = MAX_PADDING_BOTTOM
                }
                tvBottomPadding?.text = bottomInt.toString()
            }
        }
        // apply all padding to home layout
        homeLayout.setPadding(leftInt, topInt, rightInt, bottomInt)
    }

    /**
     * This runner continuously update the value when button is pressed continuously
     *
     * @param button    which button is being pressed
     * @param view      which text view will be changed or updated
     * @param step      how much we increase or decrease the value -2 means we have to decrease the value by 2
     * @param whichSide to which side we apply padding usually @button @view and this param is interrelated
     */
    private fun runner(
        button: TextView,
        view: TextView?,
        step: Int,
        whichSide: Padding
    ) {
        runnable = Runnable {
            if (!button.isPressed) {
                // button is released so destroy these calls
                runnable?.apply {
                    handler.removeCallbacks(this)
                }
                return@Runnable
            }
            when (whichSide) {
                Padding.LEFT -> {
                    leftInt += step
                    // if step is positive means we are increasing the value so we have to check upper limit
                    //else we have to check lower limit i.e. 0
                    if (step > 0) {
                        if (leftInt > MAX_PADDING_LEFT) {
                            leftInt = MAX_PADDING_LEFT
                        }
                    } else {
                        if (leftInt < MIN_PADDING) {
                            leftInt = MIN_PADDING
                        }
                    }
                    // reflect this in dialog view
                    view?.text = leftInt.toString()
                }
                Padding.RIGHT -> {
                    rightInt += step
                    if (step > 0) {
                        if (rightInt > MAX_PADDING_RIGHT) {
                            rightInt = MAX_PADDING_RIGHT
                        }
                    } else {
                        if (rightInt < MIN_PADDING) {
                            rightInt = MIN_PADDING
                        }
                    }
                    view?.text = rightInt.toString()
                }
                Padding.TOP -> {
                    topInt += step
                    if (step > 0) {
                        if (topInt > MAX_PADDING_TOP) {
                            topInt = MAX_PADDING_TOP
                        }
                    } else {
                        if (topInt < MIN_PADDING) {
                            topInt = MIN_PADDING
                        }
                    }
                    view?.text = topInt.toString()
                }
                Padding.BOTTOM -> {
                    bottomInt += step
                    if (step > 0) {
                        if (bottomInt > MAX_PADDING_BOTTOM) {
                            bottomInt = MAX_PADDING_BOTTOM
                        }
                    } else {
                        if (bottomInt < MIN_PADDING) {
                            bottomInt = MIN_PADDING
                        }
                    }
                    view?.text = bottomInt.toString()
                }
            }

            // set the padding to home layout
            homeLayout.setPadding(leftInt, topInt, rightInt, bottomInt)
            // currently button is still pressed so again call this runnable
            runnable?.apply {
                handler.postDelayed(this, DELAY)
            }
        }
        runnable?.apply {
            // remove callbacks if any
            handler.removeCallbacks(this)
            // first time runner
            handler.postDelayed(this, DELAY)
        }
    }

    //enums for better understanding
    private enum class Padding {
        LEFT, RIGHT, TOP, BOTTOM
    }

    companion object {
        private const val DELAY: Long = 10
    }
}
