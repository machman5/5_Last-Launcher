package com.launcher.dialogs.launcher.padding

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
import com.databinding.DlgPaddingBinding
import com.launcher.utils.Constants.MAX_PADDING_BOTTOM
import com.launcher.utils.Constants.MAX_PADDING_LEFT
import com.launcher.utils.Constants.MAX_PADDING_RIGHT
import com.launcher.utils.Constants.MAX_PADDING_TOP
import com.launcher.utils.Constants.MIN_PADDING
import com.launcher.utils.DbUtils.paddingBottom
import com.launcher.utils.DbUtils.paddingLeft
import com.launcher.utils.DbUtils.paddingRight
import com.launcher.utils.DbUtils.paddingTop
import com.launcher.views.flowLayout.FlowLayout

class PaddingDialog(
    context: Context,
    private val homeLayout: FlowLayout
) : Dialog(
    context,
    R.style.DialogSlideUpAnim
), OnLongClickListener, View.OnClickListener {

    companion object {
        private const val DELAY: Long = 10
    }

    private lateinit var binding: DlgPaddingBinding

    private val handler = Handler(Looper.getMainLooper())
    private var topInt = 0
    private var leftInt = 0
    private var rightInt = 0
    private var bottomInt = 0
    private var runnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DlgPaddingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLeftMinus.setOnLongClickListener(this)
        binding.btnLeftMinus.setOnClickListener(this)

        binding.btnRightMinus.setOnLongClickListener(this)
        binding.btnRightMinus.setOnClickListener(this)

        binding.btnTopMinus.setOnLongClickListener(this)
        binding.btnTopMinus.setOnClickListener(this)

        binding.btnBottomMinus.setOnLongClickListener(this)
        binding.btnBottomMinus.setOnClickListener(this)

        binding.btnLeftPlus.setOnLongClickListener(this)
        binding.btnLeftPlus.setOnClickListener(this)

        binding.btnRightPlus.setOnLongClickListener(this)
        binding.btnRightPlus.setOnClickListener(this)

        binding.btnTopPlus.setOnLongClickListener(this)
        binding.btnTopPlus.setOnClickListener(this)

        binding.btnBottomPlus.setOnLongClickListener(this)
        binding.btnBottomPlus.setOnClickListener(this)

        leftInt = paddingLeft
        rightInt = paddingRight
        topInt = paddingTop
        bottomInt = paddingBottom

        binding.tvLeftPadding.text = leftInt.toString()
        binding.tvRightPadding.text = rightInt.toString()
        binding.tvTopPadding.text = topInt.toString()
        binding.tvBottomPadding.text = bottomInt.toString()
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
        when (button) {
            binding.btnLeftMinus -> runner(
                button = button as TextView,
                view = binding.tvLeftPadding,
                step = -2,
                whichSide = Padding.LEFT
            )
            binding.btnLeftPlus -> runner(
                button = button as TextView,
                view = binding.tvLeftPadding,
                step = 2,
                whichSide = Padding.LEFT
            )
            binding.btnRightMinus -> runner(
                button = button as TextView,
                view = binding.tvRightPadding,
                step = -2,
                whichSide = Padding.RIGHT
            )
            binding.btnRightPlus -> runner(
                button = button as TextView,
                view = binding.tvRightPadding,
                step = 2,
                whichSide = Padding.RIGHT
            )
            binding.btnTopMinus -> runner(
                button = button as TextView,
                view = binding.tvTopPadding,
                step = -2,
                whichSide = Padding.TOP
            )
            binding.btnTopPlus -> runner(
                button = button as TextView,
                view = binding.tvTopPadding,
                step = 2,
                whichSide = Padding.TOP
            )
            binding.btnBottomMinus -> runner(
                button = button as TextView,
                view = binding.tvBottomPadding,
                step = -2,
                whichSide = Padding.BOTTOM
            )
            binding.btnBottomPlus -> runner(
                button = button as TextView,
                view = binding.tvBottomPadding,
                step = 2,
                whichSide = Padding.BOTTOM
            )
        }
        return true
    }

    override fun onClick(view: View) {
        when (view) {
            binding.btnLeftMinus -> {
                //decrease the value as minus button is pressed
                leftInt--
                // check the lower limit i.e. 0
                if (leftInt < MIN_PADDING) {
                    leftInt = MIN_PADDING
                }
                //reflect it on screen
                binding.tvLeftPadding.text = leftInt.toString()
            }
            binding.btnLeftPlus -> {
                leftInt++
                // check the upper limit
                if (leftInt > MAX_PADDING_LEFT) {
                    leftInt = MAX_PADDING_LEFT
                }
                binding.tvLeftPadding.text = leftInt.toString()
            }
            binding.btnRightMinus -> {
                rightInt--
                if (rightInt < MIN_PADDING) {
                    rightInt = MIN_PADDING
                }
                binding.tvRightPadding.text = rightInt.toString()
            }
            binding.btnRightPlus -> {
                rightInt++
                if (rightInt > MAX_PADDING_RIGHT) {
                    rightInt = MAX_PADDING_RIGHT
                }
                binding.tvRightPadding.text = rightInt.toString()
            }
            binding.btnTopMinus -> {
                topInt--
                if (topInt < MIN_PADDING) {
                    topInt = MIN_PADDING
                }
                binding.tvTopPadding.text = topInt.toString()
            }
            binding.btnTopPlus -> {
                topInt++
                if (topInt > MAX_PADDING_TOP) {
                    topInt = MAX_PADDING_TOP
                }
                binding.tvTopPadding.text = topInt.toString()
            }
            binding.btnBottomMinus -> {
                bottomInt--
                if (bottomInt < MIN_PADDING) {
                    bottomInt = MIN_PADDING
                }
                binding.tvBottomPadding.text = bottomInt.toString()
            }
            binding.btnBottomPlus -> {
                bottomInt++
                if (bottomInt > MAX_PADDING_BOTTOM) {
                    bottomInt = MAX_PADDING_BOTTOM
                }
                binding.tvBottomPadding.text = bottomInt.toString()
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
}
