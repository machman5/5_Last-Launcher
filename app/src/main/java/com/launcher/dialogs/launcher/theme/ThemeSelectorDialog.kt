package com.launcher.dialogs.launcher.theme

import android.app.Dialog
import android.os.Bundle
import android.view.Window
import com.R
import com.databinding.DlgThemeSelectorBinding
import com.launcher.LauncherActivity
import com.launcher.ext.click
import com.launcher.utils.DbUtils.externalSourceColor
import com.launcher.utils.DbUtils.theme

class ThemeSelectorDialog internal constructor(
    private val launcherActivity: LauncherActivity
) : Dialog(
    launcherActivity,
    R.style.DialogSlideUpAnim,
) {

    private lateinit var binding: DlgThemeSelectorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = DlgThemeSelectorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvClose.click {
            dismiss()
        }
        binding.t1.click {
            setTheme(R.style.AppTheme)
        }
        binding.t2.click {
            setTheme(R.style.Wallpaper)
        }
        binding.t3.click {
            setTheme(R.style.Black)
        }
        binding.t4.click {
            setTheme(R.style.White)
        }
        binding.t5.click {
            setTheme(R.style.WhiteOnGrey)
        }
        binding.t6.click {
            setTheme(R.style.BlackOnGrey)
        }
        binding.t35.click {
            setTheme(R.style.Hacker_green)
        }
        binding.t36.click {
            setTheme(R.style.Hacker_red)
        }

    }

    private fun setTheme(appTheme: Int) {
        theme = appTheme
        externalSourceColor(b = false)
        cancel()
        launcherActivity.recreate()
    }
}
