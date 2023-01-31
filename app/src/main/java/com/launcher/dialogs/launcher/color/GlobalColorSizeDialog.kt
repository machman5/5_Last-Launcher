package com.launcher.dialogs.launcher.color

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Window
import com.databinding.DlgColorSizeBinding
import com.launcher.model.Apps
import com.launcher.utils.Constants.DEFAULT_MAX_TEXT_SIZE
import com.launcher.utils.Constants.DEFAULT_MIN_TEXT_SIZE
import com.launcher.utils.Constants.DEFAULT_TEXT_SIZE_NORMAL_APPS
import com.launcher.utils.Constants.DEFAULT_TEXT_SIZE_OFTEN_APPS
import com.launcher.utils.DbUtils
import com.launcher.utils.DbUtils.appsColorDefault
import com.launcher.utils.DbUtils.getAppColor
import com.launcher.utils.DbUtils.getAppSize
import com.launcher.utils.DbUtils.globalSizeAdditionExtra
import com.launcher.utils.Utils.Companion.oftenAppsList
import com.launcher.views.colorSeekBar.ColorSeekBar.OnColorChangeListener

class GlobalColorSizeDialog(
    context: Context, private val mAppsList: List<Apps>
) : Dialog(
    context
) {

    companion object {
        private const val DELAY: Long = 100
    }

    private val handler = Handler(Looper.getMainLooper())
    private val oftenApps = oftenAppsList
    private var runnable: Runnable? = null
    private var appSize = 0
    private var mColor = 0

    private lateinit var binding: DlgColorSizeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = DlgColorSizeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window?.setBackgroundDrawableResource(android.R.color.transparent)
        binding.colorSlider1.setMaxPosition(100)
        binding.colorSlider1.isShowAlphaBar = true
        binding.colorSlider1.setBarHeight(8f)
        val colorDefault = appsColorDefault
        if (colorDefault != DbUtils.NULL_TEXT_COLOR) {
            binding.colorSlider1.color = colorDefault
        }

        // set the color and save this to database
        binding.colorSlider1.setOnColorChangeListener(object : OnColorChangeListener {
            override fun onColorChangeListener(
                colorBarPosition: Int, alphaBarPosition: Int, color: Int
            ) {
                mColor = color
                synchronized(mAppsList) {
                    for (apps in mAppsList) {
                        // only change the color of app, which had not set yet

                        apps.activityName?.let { name ->
                            if (getAppColor(name) == DbUtils.NULL_TEXT_COLOR) {
                                // change only the text view color
                                // do not save the color of individuals apps
                                apps.textView.setTextColor(color)
                            }
                        }
                    }
                }
            }

        })

        appSize = globalSizeAdditionExtra
        binding.tvSize.text = appSize.toString()
        binding.btnPlus.setOnClickListener {
            appSize++
            if (appSize >= DEFAULT_MAX_TEXT_SIZE) {
                appSize = DEFAULT_MAX_TEXT_SIZE
                //   plus.setClickable(false);
            } else {
                synchronized(mAppsList) {
                    for (apps in mAppsList) {
                        apps.activityName?.let { name ->
                            var textSize = getAppSize(name)
                            // check if text size is null then set the size to default size
                            // size is null(-1) when user installed this app
                            if (textSize == DbUtils.NULL_TEXT_SIZE) {
                                textSize = if (oftenApps.contains(name.split("/".toRegex())
                                        .dropLastWhile { it.isEmpty() }.toTypedArray()[0]
                                    )
                                ) {
                                    DEFAULT_TEXT_SIZE_OFTEN_APPS
                                } else {
                                    DEFAULT_TEXT_SIZE_NORMAL_APPS
                                }
                            }
                            apps.setSize(++textSize)
                        }
                    }
                }
            }
            binding.tvSize.text = appSize.toString()
        }
        binding.btnMinus.setOnClickListener {
            --appSize
            if (appSize < DEFAULT_MIN_TEXT_SIZE) {
                appSize = DEFAULT_MIN_TEXT_SIZE
            } else {
                synchronized(mAppsList) {
                    for (apps in mAppsList) {
                        apps.activityName?.let { name ->
                            var textSize = getAppSize(name)
                            // check if text size is null then set the size to default size
                            // size is null(-1) when user installed this app
                            if (textSize == DbUtils.NULL_TEXT_SIZE) {
                                textSize = if (oftenApps.contains(name.split("/".toRegex())
                                        .dropLastWhile { it.isEmpty() }.toTypedArray()[0]
                                    )
                                ) {
                                    DEFAULT_TEXT_SIZE_OFTEN_APPS
                                } else {
                                    DEFAULT_TEXT_SIZE_NORMAL_APPS
                                }

                                /// DbUtils.putAppSize(activity, textSize);
                            }
                            apps.setSize(--textSize)
                        }
                    }
                }
            }
            binding.tvSize.text = appSize.toString()
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
                appSize++
                if (appSize >= DEFAULT_MAX_TEXT_SIZE) {
                    appSize = DEFAULT_MAX_TEXT_SIZE
                    //   plus.setClickable(false);
                } else {
                    synchronized(mAppsList) {
                        for (apps in mAppsList) {
                            apps.activityName?.let { name ->
                                var textSize = getAppSize(name)
                                // check if text size is null then set the size to default size
                                // size is null(-1) when user installed this app
                                if (textSize == DbUtils.NULL_TEXT_SIZE) {
                                    textSize = if (oftenApps.contains(name.split("/".toRegex())
                                            .dropLastWhile { it.isEmpty() }.toTypedArray()[0]
                                        )
                                    ) {
                                        DEFAULT_TEXT_SIZE_OFTEN_APPS
                                    } else {
                                        DEFAULT_TEXT_SIZE_NORMAL_APPS
                                    }

                                    /// DbUtils.putAppSize(activity, textSize);
                                }
                                apps.setSize(++textSize)
                            }
                        }
                    }
                }
                binding.tvSize.text = appSize.toString()
                runnable?.apply {
                    handler.postDelayed(this, DELAY)
                }
            }
            runnable?.apply {
                handler.removeCallbacks(this)
                handler.postDelayed(this, DELAY)
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
                if (appSize < DEFAULT_MIN_TEXT_SIZE) {
                    appSize = DEFAULT_MIN_TEXT_SIZE
                } else {
                    synchronized(mAppsList) {
                        for (apps in mAppsList) {
                            apps.activityName?.let { name ->
                                var textSize = getAppSize(name)
                                // check if text size is null then set the size to default size
                                // size is null(-1) when user installed this app
                                if (textSize == DbUtils.NULL_TEXT_SIZE) {
                                    textSize = if (oftenApps.contains(name.split("/".toRegex())
                                            .dropLastWhile { it.isEmpty() }.toTypedArray()[0]
                                        )
                                    ) {
                                        DEFAULT_TEXT_SIZE_OFTEN_APPS
                                    } else {
                                        DEFAULT_TEXT_SIZE_NORMAL_APPS
                                    }

                                    /// DbUtils.putAppSize(activity, textSize);
                                }
                                apps.setSize(--textSize)
                            }
                        }
                    }
                }
                binding.tvSize.text = appSize.toString()
                runnable?.apply {
                    handler.postDelayed(this, DELAY)
                }
            }
            runnable?.apply {
                handler.removeCallbacks(this)
                handler.postDelayed(this, DELAY)
            }
            true
        }
    }

    override fun onStop() {
        super.onStop()
        globalSizeAdditionExtra = appSize
        appsColorDefault = mColor
    }
}
