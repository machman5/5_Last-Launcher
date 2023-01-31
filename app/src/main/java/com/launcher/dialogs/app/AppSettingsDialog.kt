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
    val view: AppTextView,
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
        if (view.isShortcut) {
            binding.menuUninstall.setText(R.string.remove)
            binding.menuHide.isEnabled = false
            binding.menuRename.isEnabled = false
            binding.menuAppInfo.isEnabled = false
        }

        binding.menuColor.setOnClickListener(this)
        binding.menuRename.setOnClickListener(this)
        binding.menuFreezeSize.setOnClickListener(this)
        binding.menuHide.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v) {
            binding.menuColor -> {
                changeColorSize(activityName, view)
            }
            binding.menuRename -> {
                renameApp(activityName, view.text.toString())
            }
            binding.menuFreezeSize -> {
                freezeAppSize(activityName)
            }
            binding.menuHide -> {
                hideApp(activityName)
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

    private fun renameApp(activityName: String, appName: String) {
        val dialogs = RenameInputDialogs(context, activityName, appName, launcherActivity)
        val window = dialogs.window
        dialogs.show()
        if (window != null) {
            window.setGravity(Gravity.BOTTOM)
            window.setBackgroundDrawableResource(android.R.color.transparent)
            window.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    private fun freezeAppSize(activityName: String) {
        val b = isAppFrozen(activityName)
        synchronized(LauncherActivity.mAppsList) {
            for (apps in LauncherActivity.mAppsList) {
                if (activityName.equals(apps.activityName, ignoreCase = true)) {
                    apps.setFreeze(!b)
                }
            }
        }
        dismiss()
    }

    private fun hideApp(activityName: String) {
        synchronized(LauncherActivity.mAppsList) {
            for (apps in LauncherActivity.mAppsList) {
                if (activityName.equals(apps.activityName, ignoreCase = true)) {
                    apps.setAppHidden(true)
                }
            }
        }
        dismiss()
    }
}
