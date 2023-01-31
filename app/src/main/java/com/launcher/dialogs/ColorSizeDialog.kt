package com.launcher.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Window
import android.widget.TextView
import com.R
import com.launcher.utils.DbUtils
import com.launcher.utils.DbUtils.maxAppSize
import com.launcher.utils.DbUtils.minAppSize
import com.launcher.utils.DbUtils.putAppColor
import com.launcher.utils.DbUtils.putAppSize
import com.launcher.views.colorSeekBar.ColorSeekBar
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
    context
) {
    private val handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dlg_color_size)
        window?.setBackgroundDrawableResource(android.R.color.transparent)

        val colorSlider1 = findViewById<ColorSeekBar>(R.id.colorSlider1)
        colorSlider1.setMaxPosition(100)
        colorSlider1.isShowAlphaBar = true
        colorSlider1.setBarHeight(8f)

        // todo: is this still correct?
        if (appColor != DbUtils.NULL_TEXT_COLOR) {
            colorSlider1.color = appColor
        }
        // set the color and save this to database
        colorSlider1.setOnColorChangeListener(object : OnColorChangeListener {
            override fun onColorChangeListener(
                colorBarPosition: Int, alphaBarPosition: Int, color: Int
            ) {
                textView.setTextColor(color)
                appColor = color
            }
        })

        // size related
        val btnPlus = findViewById<TextView>(R.id.btnPlus)
        val btnMinus = findViewById<TextView>(R.id.btnMinus)
        val tvSize = findViewById<TextView>(R.id.tvSize)
        tvSize.text = appSize.toString()
        btnPlus.setOnClickListener {

            // change=true;
            appSize++
            if (appSize >= DEFAULT_MAX_TEXT_SIZE) {
                appSize = DEFAULT_MAX_TEXT_SIZE
                //   plus.setClickable(false);
            }
            tvSize.text = appSize.toString()
            textView.textSize = appSize.toFloat()
        }
        btnMinus.setOnClickListener {
            //change=true;
            --appSize
            if (appSize < DEFAULT_MIN_TEXT_SIZE) {
                appSize = DEFAULT_MIN_TEXT_SIZE
            }
            tvSize.text = appSize.toString()
            textView.textSize = appSize.toFloat()
        }
        btnPlus.setOnLongClickListener {
            runnable = Runnable {
                if (!btnPlus.isPressed) {
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
                tvSize.text = appSize.toString()
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
        btnMinus.setOnLongClickListener {
            runnable = Runnable {
                if (!btnMinus.isPressed) {
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
                tvSize.text = appSize.toString()
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
