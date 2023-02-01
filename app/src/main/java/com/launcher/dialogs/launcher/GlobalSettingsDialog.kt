package com.launcher.dialogs.launcher

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import com.BuildConfig
import com.R
import com.databinding.DlgGlobalSettingsBinding
import com.launcher.LauncherActivity
import com.launcher.dialogs.launcher.alignment.AlignmentDialog
import com.launcher.dialogs.launcher.font.FontDialog
import com.launcher.dialogs.launcher.theme.ThemeSelectorDialog
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
import com.launcher.utils.DbUtils.theme
import com.launcher.utils.Utils.Companion.generateColorFromString
import java.text.SimpleDateFormat
import java.util.*

/**
 * this the launcher setting Dialog
 */
class GlobalSettingsDialog(
    mContext: Context,
    private val launcherActivity: LauncherActivity
) : Dialog(mContext), View.OnClickListener {
    private lateinit var binding: DlgGlobalSettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // no old title: Last Launcher use Activity class not AppCompatActivity so it show very old title
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = DlgGlobalSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.settingsThemes.setOnClickListener(this)
        binding.settingsFreezeSize.setOnClickListener(this)
        binding.settingsFonts.setOnClickListener(this)
        binding.settingsResetToDefaults.setOnClickListener(this)
        binding.settingsResetToDefaults.setTextColor(Color.parseColor("#E53935"))
        binding.settingsBackup.setOnClickListener(this)
        binding.settingsRestore.setOnClickListener(this)
        binding.settingsAlignment.setOnClickListener(this)
        binding.settingsPadding.setOnClickListener(this)
        binding.settingsColorSize.setOnClickListener(this)
        binding.settingsSortAppBy.setOnClickListener(this)
        binding.settingsSortAppReverse.setOnClickListener(this)
        binding.settingsRestartLauncher.setOnClickListener(this)
        binding.settingsColorSniffer.setOnClickListener(this)
        if (!BuildConfig.enableColorSniffer) {
            if (isRandomColor) {
                binding.settingsColorSniffer.setText(R.string.fixed_colors)
            } else {
                binding.settingsColorSniffer.setText(R.string.random_colors)
            }
        }
        binding.settingsFrozenApps.setOnClickListener(this)
        binding.settingsHiddenApps.setOnClickListener(this)

        //reflect the DB value
        if (isSizeFrozen) {
            binding.settingsFreezeSize.setText(R.string.unfreeze_app_size)
        } else {
            binding.settingsFreezeSize.setText(R.string.freeze_apps_size)
        }
    }

    override fun onClick(view: View) {
        when (view) {
            binding.settingsFonts -> {
                fontSelection()
            }
            binding.settingsThemes -> {
                showThemeDialog()
            }
            binding.settingsColorSniffer -> {
                if (BuildConfig.enableColorSniffer) {
                    showColorSnifferDialog()
                } else {
                    randomColor()
                }
            }
            binding.settingsSortAppBy -> {
                sortApps(view)
            }
            binding.settingsSortAppReverse -> {
                sortAppsReverseOrder()
            }
            binding.settingsColorSize -> {
                showColorAndSizeDialog()
            }
            binding.settingsFreezeSize -> {
                freezeAppsSize()
            }
            binding.settingsHiddenApps -> {
                hiddenApps()
            }
            binding.settingsFrozenApps -> {
                frozenApps()
            }
            binding.settingsBackup -> {
                backup()
            }
            binding.settingsRestore -> {
                restore()
            }
            binding.settingsResetToDefaults -> {
                defaultSettings()
            }
            binding.settingsAlignment -> {
                setFlowLayoutAlignment(view)
            }
            binding.settingsPadding -> {
                launcherActivity.setPadding()
                cancel()
            }
            binding.settingsRestartLauncher -> {
                launcherActivity.recreate()
            }
        }
    }

    /**
     * This method is used to control the order of apps.
     * The code block we added is to give the newly added buttons the ability to sort them by name.
     */
    private fun sortApps(view: View) {
        // set theme
        // if theme wallpaper ie transparent then we have to show other theme
        val context: Context = if (theme == R.style.Wallpaper) {
            ContextThemeWrapper(
                context,
                R.style.AppTheme
            )
        } else {
            ContextThemeWrapper(context, theme)
        }
        val popupMenu = PopupMenu(context, view)
        popupMenu.menuInflater.inflate(R.menu.popup_sort_apps, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
            cancel()
            when (menuItem.itemId) {
                R.id.menuSortByName -> launcherActivity.sortApps(Constants.SORT_BY_NAME)
                R.id.menuSortByOpeningCounts -> launcherActivity.sortApps(Constants.SORT_BY_OPENING_COUNTS)
                R.id.menuSortByColor -> launcherActivity.sortApps(Constants.SORT_BY_COLOR)
                R.id.menuSortBySize -> launcherActivity.sortApps(Constants.SORT_BY_SIZE)
                R.id.menuSortByUpdateTime -> launcherActivity.sortApps(Constants.SORT_BY_UPDATE_TIME)
                R.id.menuSortByRecentUse -> launcherActivity.sortApps(Constants.SORT_BY_RECENT_OPEN)
            }
            true
        }
        popupMenu.show()
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

    private fun setFlowLayoutAlignment(view: View) {
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

    private fun showColorSnifferDialog() {
        cancel()
        val intent = context.packageManager.getLaunchIntentForPackage("ryey.colorsniffer")

        // if color snifer app is not installed then send user to install it
        // else show color sniffer option
        if (intent == null) {
            //Change this to proper url , currently this also show BASTARD PLAY STORE
            val uri = Uri.parse("market://details?id=ryey.colorsniffer")
            val i = Intent(Intent.ACTION_VIEW, uri)
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(i)
        } else {
            ColorSnifferDialog(context, launcherActivity).show()
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
        if (!BuildConfig.DEBUG) {
            clearDB()
            launcherActivity.recreate()
        } //DO SOME ESTER EGG.. FOR DEBUG BUILD..
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
