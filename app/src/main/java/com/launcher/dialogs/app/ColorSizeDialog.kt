package com.launcher.dialogs.app

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Window
import android.widget.TextView
import com.R
import com.databinding.DlgColorSizeBinding
import com.launcher.ext.click
import com.launcher.utils.DbUtils
import com.launcher.utils.DbUtils.maxAppSize
import com.launcher.utils.DbUtils.minAppSize
import com.launcher.utils.DbUtils.putAppColor
import com.launcher.utils.DbUtils.putAppSize
import com.launcher.views.colorSeekBar.ColorSeekBar.OnColorChangeListener

// choose color Dialog
class ColorSizeDialog     // boolean change=false;
    (
    context: Context,
    private val appPackage: String,
    private var appColor: Int,
    private val textView: TextView,
    private var appSize: Int
) : Dialog(
    context,
    R.style.DialogSlideUpAnim
) {
    private lateinit var binding: DlgColorSizeBinding
    private val handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = DlgColorSizeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window?.setBackgroundDrawableResource(android.R.color.transparent)

        binding.tvClose.click {
            dismiss()
        }
        binding.colorSlider1.setMaxPosition(100)
        binding.colorSlider1.isShowAlphaBar = true
        binding.colorSlider1.setBarHeight(8f)

        // todo: is this still correct?
        if (appColor != DbUtils.NULL_TEXT_COLOR) {
            binding.colorSlider1.color = appColor
        }
        // set the color and save this to database
        binding.colorSlider1.setOnColorChangeListener(object : OnColorChangeListener {
            override fun onColorChangeListener(
                colorBarPosition: Int, alphaBarPosition: Int, color: Int
            ) {
                textView.setTextColor(color)
                appColor = color
            }
        })

        // size related
        binding.tvSize.text = appSize.toString()
        binding.btnPlus.click {

            // change=true;
            appSize++
            if (appSize >= DEFAULT_MAX_TEXT_SIZE) {
                appSize = DEFAULT_MAX_TEXT_SIZE
                //   plus.setClickable(false);
            }
            binding.tvSize.text = appSize.toString()
            textView.textSize = appSize.toFloat()
        }
        binding.btnMinus.click {
            //change=true;
            --appSize
            if (appSize < DEFAULT_MIN_TEXT_SIZE) {
                appSize = DEFAULT_MIN_TEXT_SIZE
            }
            binding.tvSize.text = appSize.toString()
            textView.textSize = appSize.toFloat()
        }
        binding.btnPlus.setOnLongClickListener {
            runnable = Runnable {
                if (!binding.btnPlus.isPressed) {
                    runnable?.apply {
                        handler.removeCallbacks(this)
                    }
                    return@Runnable
                }
                // increase value
                // change=true;
                appSize++
                if (appSize >= DEFAULT_MAX_TEXT_SIZE) {
                    appSize = DEFAULT_MAX_TEXT_SIZE
                    //   plus.setClickable(false);
                }
                binding.tvSize.text = appSize.toString()
                textView.textSize = appSize.toFloat()
                runnable?.apply {
                    handler.postDelayed(this, DELAY.toLong())
                }
            }
            runnable?.apply {
                handler.removeCallbacks(this)
                handler.postDelayed(this, DELAY.toLong())
            }
            true
        }
        binding.btnMinus.setOnLongClickListener {
            runnable = Runnable {
                if (!binding.btnMinus.isPressed) {
                    runnable?.apply {
                        handler.removeCallbacks(this)
                    }
                    return@Runnable
                }
                // decrease value
                --appSize
                // change=true;
                if (appSize < DEFAULT_MIN_TEXT_SIZE) {
                    appSize = DEFAULT_MIN_TEXT_SIZE
                }
                binding.tvSize.text = appSize.toString()
                textView.textSize = appSize.toFloat()
                runnable?.apply {
                    handler.postDelayed(this, DELAY.toLong())
                }
            }
            runnable?.apply {
                handler.removeCallbacks(this)
                handler.postDelayed(this, DELAY.toLong())
            }
            true
        }
    }

    override fun onStop() {
        super.onStop()
        putAppColor(activityName = appPackage, color = appColor)
        putAppSize(activityName = appPackage, size = appSize)
    }

    companion object {
        private const val DELAY = 25

        private val DEFAULT_MIN_TEXT_SIZE = minAppSize
        private val DEFAULT_MAX_TEXT_SIZE = maxAppSize
    }
}
