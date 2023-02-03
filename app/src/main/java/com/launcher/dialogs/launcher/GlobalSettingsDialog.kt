package com.launcher.dialogs.launcher

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import com.BuildConfig
import com.R
import com.databinding.DlgGlobalSettingsBinding
import com.launcher.FakeLauncherActivity
import com.launcher.LauncherActivity
import com.launcher.dialogs.YesNoDialog
import com.launcher.dialogs.launcher.alignment.AlignmentDialog
import com.launcher.dialogs.launcher.font.FontDialog
import com.launcher.dialogs.launcher.sort.SortDialog
import com.launcher.dialogs.launcher.theme.ThemeSelectorDialog
import com.launcher.ext.*
import com.launcher.utils.Constants
import com.launcher.utils.DbUtils
import com.launcher.utils.DbUtils.appSortReverseOrder
import com.launcher.utils.DbUtils.clearDB
import com.launcher.utils.DbUtils.getAppColor
import com.launcher.utils.DbUtils.isRandomColor
import com.launcher.utils.DbUtils.isSizeFrozen
import com.launcher.utils.DbUtils.randomColor
import com.launcher.utils.DbUtils.settingsFreezeSize
import com.launcher.utils.DbUtils.sortsTypes
import com.launcher.utils.Utils.Companion.generateColorFromString
import java.text.SimpleDateFormat
import java.util.*

/**
 * this the launcher setting Dialog
 */
class GlobalSettingsDialog(
    mContext: Context,
    private val launcherActivity: LauncherActivity
) : Dialog(
    mContext,
    R.style.DialogSlideUpAnim,
) {
    private lateinit var binding: DlgGlobalSettingsBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // no old title: Last Launcher use Activity class not AppCompatActivity so it show very old title
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = DlgGlobalSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvVersion.text = "Version ${BuildConfig.VERSION_NAME}"

        binding.tvClose.click {
            dismiss()
        }
        binding.tvPolicy.click {
            launcherActivity.openBrowserPolicy()
        }
        binding.tvDefaultLauncher.click {
            launcherActivity.chooseLauncher(FakeLauncherActivity::class.java)
        }
        binding.settingsThemes.click {
            showThemeDialog()
        }
        binding.settingsFreezeSize.click {
            freezeAppsSize()
        }
        binding.settingsFonts.click {
            fontSelection()
        }
        binding.settingsResetToDefaults.click {
            defaultSettings()
        }
        binding.settingsBackup.click {
            backup()
        }
        binding.settingsRestore.click {
            restore()
        }
        binding.settingsAlignment.click {
            setFlowLayoutAlignment()
        }
        binding.settingsPadding.click {
            launcherActivity.setPadding()
            cancel()
        }
        binding.settingsColorSize.click {
            showColorAndSizeDialog()
        }
        binding.settingsSortAppBy.click {
            sortApps()
        }
        binding.settingsSortAppReverse.click {
            sortAppsReverseOrder()
        }
        binding.settingsRestartLauncher.click {
            launcherActivity.recreate()
        }
        binding.settingsColorSniffer.click {
            randomColor()
        }
        if (isRandomColor) {
            binding.settingsColorSniffer.setText(R.string.fixed_colors)
        } else {
            binding.settingsColorSniffer.setText(R.string.random_colors)
        }
        binding.settingsFrozenApps.click {
            frozenApps()
        }
        binding.settingsHiddenApps.click {
            hiddenApps()
        }
        binding.tvRateApp.click {
            launcherActivity.rateApp(launcherActivity.packageName)
        }
        binding.tvMoreApp.click {
            launcherActivity.moreApp()
        }
        binding.tvShareApp.click {
            launcherActivity.shareApp()
        }

        //reflect the DB value
        if (isSizeFrozen) {
            binding.settingsFreezeSize.setText(R.string.unfreeze_app_size)
        } else {
            binding.settingsFreezeSize.setText(R.string.freeze_apps_size)
        }
    }

    /**
     * This method is used to control the order of apps.
     * The code block we added is to give the newly added buttons the ability to sort them by name.
     */
    private fun sortApps() {
        dismiss()
        val dialogs = SortDialog(
            mContext = context,
            launcherActivity = launcherActivity
        )
        dialogs.show()
        val window = dialogs.window
        if (window != null) {
            window.setGravity(Gravity.BOTTOM)
            window.setBackgroundDrawableResource(android.R.color.transparent)
            window.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    private fun sortAppsReverseOrder() {
        appSortReverseOrder = !appSortReverseOrder
        launcherActivity.sortApps(sortsTypes)
        cancel()
    }

    private fun showColorAndSizeDialog() {
        launcherActivity.setColorsAndSize()
        cancel()
    }

    private fun setFlowLayoutAlignment() {
        dismiss()
        val dialogs = AlignmentDialog(
            mContext = context,
            launcherActivity = launcherActivity
        )
        dialogs.show()
        val window = dialogs.window
        if (window != null) {
            window.setGravity(Gravity.BOTTOM)
            window.setBackgroundDrawableResource(android.R.color.transparent)
            window.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    private fun randomColor() {
        val rColor = !isRandomColor
        randomColor(rColor)
        cancel()
        if (rColor) {
            var color: Int
            for (app in LauncherActivity.mAppsList) {
                app.activityName?.let { name ->
                    color = getAppColor(name)
                    if (color == DbUtils.NULL_TEXT_COLOR) {
                        color = generateColorFromString(name)
                        app.textView.setTextColor(color)
                    }
                }
            }
        } else {
            launcherActivity.recreate()
        }
    }

    private fun freezeAppsSize() {
        val b = isSizeFrozen
        settingsFreezeSize(!b)
        if (!b) {
            binding.settingsFreezeSize.setText(R.string.unfreeze_app_size)
        } else {
            binding.settingsFreezeSize.setText(R.string.freeze_apps_size)
        }
        dismiss()
    }

    private fun frozenApps() {
        launcherActivity.showFrozenApps()
        cancel()
    }

    //show hidden apps
    private fun hiddenApps() {
        launcherActivity.showHiddenApps()
        cancel()
    }

    private fun showThemeDialog() {
        cancel()
        val dialogs = ThemeSelectorDialog(launcherActivity)
        dialogs.show()
        val window = dialogs.window
        if (window != null) {
            window.setGravity(Gravity.BOTTOM)
            window.setBackgroundDrawableResource(android.R.color.transparent)
            window.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    private fun defaultSettings() {
        dismiss()
        val dialogs = YesNoDialog(
            mContext = context,
            title = context.getString(R.string.warning),
            msg = context.getString(R.string.reset_to_default_settings),
            yes = context.getString(R.string.yes),
            no = context.getString(R.string.no),
            onClickYes = {
                clearDB()
                launcherActivity.recreate()
            },
            onClickNo = {
                //do nothing
            }
        )
        dialogs.show()
        val window = dialogs.window
        if (window != null) {
            window.setGravity(Gravity.BOTTOM)
            window.setBackgroundDrawableResource(android.R.color.transparent)
            window.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    private fun backup() {
        cancel()
        val intentBackupFiles: Intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Intent(Intent.ACTION_CREATE_DOCUMENT)
        } else {
            Intent(Intent.ACTION_GET_CONTENT)
        }
        intentBackupFiles.addCategory(Intent.CATEGORY_OPENABLE)
        intentBackupFiles.type = "*/*"
        val df = SimpleDateFormat("yyyy_MM_dd_HHSS", Locale.getDefault())
        df.format(Date())
        val date = df.format(Date())
        intentBackupFiles.putExtra(Intent.EXTRA_TITLE, "Backup_LastLauncher_$date")
        val intent = Intent.createChooser(
            intentBackupFiles,
            launcherActivity.getString(R.string.choose_old_backup_files)
        )
        launcherActivity.startActivityForResult(intent, Constants.BACKUP_REQUEST)
    }

    private fun restore() {
        cancel()
        val intentRestoreFiles = Intent(Intent.ACTION_GET_CONTENT)
        intentRestoreFiles.addCategory(Intent.CATEGORY_OPENABLE)
        intentRestoreFiles.type = "*/*"
        val intent = Intent.createChooser(
            intentRestoreFiles,
            launcherActivity.getString(R.string.choose_old_backup_files)
        )
        launcherActivity.startActivityForResult(intent, Constants.RESTORE_REQUEST)
    }

    private fun fontSelection() {
        dismiss()
        val dialogs = FontDialog(
            mContext = context,
            launcherActivity = launcherActivity
        )
        dialogs.show()
        val window = dialogs.window
        if (window != null) {
            window.setGravity(Gravity.BOTTOM)
            window.setBackgroundDrawableResource(android.R.color.transparent)
            window.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }
}
