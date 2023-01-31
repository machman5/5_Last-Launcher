package com.launcher.dialogs.launcher

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.LinearLayout
import com.R
import com.launcher.LauncherActivity
import com.launcher.utils.DbUtils.externalSourceColor
import com.launcher.utils.DbUtils.theme

class ThemeSelectorDialog internal constructor(
    private val launcherActivity: LauncherActivity
) : Dialog(launcherActivity), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dlg_theme_selector)
        val ll = findViewById<LinearLayout>(R.id.theme_linear_layout)
        for (i in 0 until ll.childCount) {
            ll.getChildAt(i).setOnClickListener(this)
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.t1 -> setTheme(R.style.AppTheme)
            R.id.t2 -> setTheme(R.style.Wallpaper)
            R.id.t3 -> setTheme(R.style.Black)
            R.id.t4 -> setTheme(R.style.White)
            R.id.t5 -> setTheme(R.style.WhiteOnGrey)
            R.id.t6 -> setTheme(R.style.BlackOnGrey)
            R.id.t35 -> setTheme(R.style.Hacker_green)
            R.id.t36 -> setTheme(R.style.Hacker_red)
        }
    }

    private fun setTheme(appTheme: Int) {
        theme = appTheme
        externalSourceColor(b = false)
        cancel()
        launcherActivity.recreate()
    }
}
