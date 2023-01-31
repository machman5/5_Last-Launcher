package com.launcher.dialogs.launcher

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.Window
import com.R
import com.databinding.DlgThemeSelectorBinding
import com.launcher.LauncherActivity
import com.launcher.utils.DbUtils.externalSourceColor
import com.launcher.utils.DbUtils.theme

class ThemeSelectorDialog internal constructor(
    private val launcherActivity: LauncherActivity
) : Dialog(launcherActivity), View.OnClickListener {

    private lateinit var binding: DlgThemeSelectorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = DlgThemeSelectorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        for (i in 0 until binding.themeLinearLayout.childCount) {
            binding.themeLinearLayout.getChildAt(i).setOnClickListener(this)
        }
    }

    override fun onClick(view: View) {
        when (view) {
            binding.t1 -> setTheme(R.style.AppTheme)
            binding.t2 -> setTheme(R.style.Wallpaper)
            binding.t3 -> setTheme(R.style.Black)
            binding.t4 -> setTheme(R.style.White)
            binding.t5 -> setTheme(R.style.WhiteOnGrey)
            binding.t6 -> setTheme(R.style.BlackOnGrey)
            binding.t35 -> setTheme(R.style.Hacker_green)
            binding.t36 -> setTheme(R.style.Hacker_red)
        }
    }

    private fun setTheme(appTheme: Int) {
        theme = appTheme
        externalSourceColor(b = false)
        cancel()
        launcherActivity.recreate()
    }
}
