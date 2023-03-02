package com.launcher.dialogs.app

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import com.R
import com.databinding.DlgAppSettingsBinding
import com.launcher.LauncherActivity
import com.launcher.ext.click
import com.launcher.ext.rateApp
import com.launcher.model.Shortcut
import com.launcher.utils.Constants
import com.launcher.utils.DbUtils
import com.launcher.utils.DbUtils.getAppColor
import com.launcher.utils.DbUtils.getAppSize
import com.launcher.utils.DbUtils.isAppFrozen
import com.launcher.utils.DbUtils.removeAppName
import com.launcher.utils.DbUtils.removeColor
import com.launcher.utils.DbUtils.removeSize
import com.launcher.utils.DbUtils.sortsTypes
import com.launcher.views.textview.AppTextView

/**
 * this the launcher setting Dialog
 */
class AppSettingsDialog(
    mContext: Context,
    private val launcherActivity: LauncherActivity,
    private val activityName: String,
    val view: AppTextView,
) : Dialog(mContext, R.style.DialogSlideUpAnim) {
    private lateinit var binding: DlgAppSettingsBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // no old title: Last Launcher use Activity class not AppCompatActivity so it show very old title
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = DlgAppSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvSetting.text = "${context.getString(R.string.setting)}: ${view.text}"

        // set proper item based on Db value
        if (isAppFrozen(activityName)) {
            binding.menuFreezeSize.setText(R.string.unfreeze_size)
        }
        if (view.isShortcut) {
            binding.menuUninstall.setText(R.string.remove)
            binding.menuHide.visibility = View.GONE
            binding.vHide.visibility = View.GONE
            binding.menuRename.visibility = View.GONE
            binding.vRename.visibility = View.GONE
            binding.menuAppInfo.visibility = View.GONE
            binding.vAppInfo.visibility = View.GONE
        }

        binding.tvClose.click {
            dismiss()
        }
        binding.menuColor.click {
            changeColorSize(activityName, view)
        }
        binding.menuRename.click {
            renameApp(activityName, view.text.toString())
            dismiss()
        }
        binding.menuFreezeSize.click {
            freezeAppSize(activityName)
        }
        binding.menuHide.click {
            hideApp(activityName)
        }
        binding.menuRateThisApp.click {
            val apps = launcherActivity.getApp(activityName)
            apps.packageName?.let {
                launcherActivity.rateApp(it)
            }
        }
        binding.menuUninstall.click {
            if (view.isShortcut) {
                removeShortcut(view)
            } else {
                uninstallApp(activityName)
            }
            dismiss()
        }
        binding.menuAppInfo.click {
            showAppInfo(activityName)
        }
        binding.menuResetToDefault.click {
            resetApp(activityName)
        }
        binding.menuResetColor.click {
            resetAppColor(activityName)
        }
        binding.menuLockOrUnlock.apply {
            val apps = launcherActivity.getApp(activityName)
            apps.packageName?.let {
                val isAppLock = DbUtils.isAppLock(it)
                text = if (isAppLock) {
                    context.getString(R.string.unlock)
                } else {
                    context.getString(R.string.lock)
                }
            }
            click {
                apps.packageName?.let {
                    val isAppLock = DbUtils.isAppLock(it)
                    if (isAppLock) {
                        DbUtils.setAppLock(packageName = it, value = false)
                    } else {
                        DbUtils.setAppLock(packageName = it, value = true)
                    }
                    dismiss()
                }
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

    private fun removeShortcut(view: AppTextView) {
        if (view.uri != null) {
            launcherActivity.shortcutUtils.removeShortcut(
                Shortcut(
                    name = view.text.toString(), uris = view.uri!!
                )
            )
        }
        launcherActivity.loadApps()
    }

    private fun uninstallApp(activityName: String) {
        val intent = Intent(Intent.ACTION_UNINSTALL_PACKAGE)
        intent.data =
            Uri.parse("package:" + activityName.split("/".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()[0])
        intent.putExtra(Intent.EXTRA_RETURN_RESULT, true)
        launcherActivity.startActivityForResult(intent, 97)
    }

    private fun showAppInfo(activityName: String) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse("package:" + activityName.split("/".toRegex()).dropLastWhile {
            it.isEmpty()
        }.toTypedArray()[0])
        launcherActivity.startActivity(intent)
    }

    private fun resetApp(activityName: String) {
        removeAppName(activityName)
        removeColor(activityName)
        removeSize(activityName)
        launcherActivity.addAppAfterReset(activityName, true)
        dismiss()
    }

    private fun resetAppColor(activityName: String) {
        removeColor(activityName)
        val sortNeeded = sortsTypes == Constants.SORT_BY_COLOR
        launcherActivity.addAppAfterReset(activityName, sortNeeded)
        dismiss()
    }
}
