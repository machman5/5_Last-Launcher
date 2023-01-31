package com.launcher.dialogs.app

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import com.R
import com.databinding.DlgAppSettingsBinding
import com.launcher.LauncherActivity
import com.launcher.utils.DbUtils
import com.launcher.utils.DbUtils.getAppColor
import com.launcher.utils.DbUtils.getAppSize
import com.launcher.utils.DbUtils.isAppFrozen
import com.launcher.views.textview.AppTextView

/**
 * this the launcher setting Dialog
 */
class AppSettingsDialog(
    mContext: Context,
    private val launcherActivity: LauncherActivity,
    private val activityName: String,
    val appTextView: AppTextView,
) : Dialog(mContext), View.OnClickListener {
    private lateinit var binding: DlgAppSettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // no old title: Last Launcher use Activity class not AppCompatActivity so it show very old title
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = DlgAppSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // set proper item based on Db value
        if (isAppFrozen(activityName)) {
            binding.menuFreezeSize.setText(R.string.unfreeze_size)
        }
        if (appTextView.isShortcut) {
            binding.menuUninstall.setText(R.string.remove)
            binding.menuHide.isEnabled = false
            binding.menuRename.isEnabled = false
            binding.menuAppInfo.isEnabled = false
        }

        binding.menuColor.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view) {
            binding.menuColor -> {
                changeColorSize(activityName, appTextView)
            }
        }
    }

    private fun changeColorSize(activityName: String, view: TextView) {
        var color = getAppColor(activityName)
        if (color == DbUtils.NULL_TEXT_COLOR) {
            color = view.currentTextColor
        }
        var size = getAppSize(activityName)
        if (size == DbUtils.NULL_TEXT_SIZE) {
            synchronized(LauncherActivity.mAppsList) {
                for (apps in LauncherActivity.mAppsList) {
                    if (apps.activityName != null && apps.activityName == activityName) {
                        size = apps.getSize()
                        break
                    }
                }
            }
        }
        val dialogs = ColorSizeDialog(context, activityName, color, view, size)
        dialogs.show()
        val window = dialogs.window
        if (window != null) {
            window.setGravity(Gravity.BOTTOM)
            window.setBackgroundDrawableResource(android.R.color.transparent)
            window.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }
}
