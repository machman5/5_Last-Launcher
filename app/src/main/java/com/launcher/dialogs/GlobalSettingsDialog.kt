package com.launcher.dialogs

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import android.widget.TextView
import com.BuildConfig
import com.R
import com.launcher.LauncherActivity
import com.launcher.utils.Constants
import com.launcher.utils.DbUtils
import com.launcher.utils.DbUtils.appSortReverseOrder
import com.launcher.utils.DbUtils.clearDB
import com.launcher.utils.DbUtils.freezeSize
import com.launcher.utils.DbUtils.getAppColor
import com.launcher.utils.DbUtils.isFontExists
import com.launcher.utils.DbUtils.isRandomColor
import com.launcher.utils.DbUtils.isSizeFrozen
import com.launcher.utils.DbUtils.randomColor
import com.launcher.utils.DbUtils.removeFont
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
    private var freezeSize: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // no old title: Last Launcher use Activity class not AppCompatActivity so it show very old title
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dlg_global_settings)
        findViewById<View>(R.id.settings_themes).setOnClickListener(this)
        freezeSize = findViewById(R.id.settings_freeze_size)
        freezeSize?.setOnClickListener(this)
        findViewById<View>(R.id.settings_fonts).setOnClickListener(this)
        val reset = findViewById<TextView>(R.id.settings_reset_to_defaults)
        reset.setOnClickListener(this)
        reset.setTextColor(Color.parseColor("#E53935"))
        findViewById<View>(R.id.settings_backup).setOnClickListener(this)
        findViewById<View>(R.id.settings_restore).setOnClickListener(this)
        findViewById<View>(R.id.settings_alignment).setOnClickListener(this)
        findViewById<View>(R.id.settings_padding).setOnClickListener(this)
        findViewById<View>(R.id.settings_color_size).setOnClickListener(this)
        findViewById<View>(R.id.settings_sort_app_by).setOnClickListener(this)
        findViewById<View>(R.id.settings_sort_app_reverse).setOnClickListener(this)
        findViewById<View>(R.id.settings_restart_launcher).setOnClickListener(this)

        //TODO: remove this var
        val colorSniffer = findViewById<TextView>(R.id.settings_color_sniffer)
        colorSniffer.setOnClickListener(this)
        if (!BuildConfig.enableColorSniffer) {
            if (isRandomColor) {
                colorSniffer.setText(R.string.fixed_colors)
            } else {
                colorSniffer.setText(R.string.random_colors)
            }
        }
        findViewById<View>(R.id.settings_frozen_apps).setOnClickListener(this)
        findViewById<View>(R.id.settings_hidden_apps).setOnClickListener(this)

        //reflect the DB value
        if (isSizeFrozen) {
            freezeSize?.setText(R.string.unfreeze_app_size)
        } else {
            freezeSize?.setText(R.string.freeze_apps_size)
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.settings_fonts -> {
                fontSelection(view)
            }
            R.id.settings_themes -> {
                showThemeDialog()
            }
            R.id.settings_color_sniffer -> {
                if (BuildConfig.enableColorSniffer) {
                    showColorSnifferDialog()
                } else {
                    randomColor()
                }
            }
            R.id.settings_sort_app_by -> {
                sortApps(view)
            }
            R.id.settings_sort_app_reverse -> {
                sortAppsReverseOrder()
            }
            R.id.settings_color_size -> {
                showColorAndSizeDialog()
            }
            R.id.settings_freeze_size -> {
                freezeAppsSize()
            }
            R.id.settings_hidden_apps -> {
                hiddenApps()
            }
            R.id.settings_frozen_apps -> {
                frozenApps()
            }
            R.id.settings_backup -> {
                backup()
            }
            R.id.settings_restore -> {
                restore()
            }
            R.id.settings_reset_to_defaults -> {
                defaultSettings()
            }
            R.id.settings_alignment -> {
                setFlowLayoutAlignment(view)
            }
            R.id.settings_padding -> {
                launcherActivity.setPadding()
                cancel()
            }
            R.id.settings_restart_launcher -> {
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
        // set theme
        // if theme is  wallpaper i.e. transparent then we have to show other theme:
        val context: Context = if (theme == R.style.Wallpaper) {
            ContextThemeWrapper(
                context,
                R.style.AppTheme
            )
        } else {
            ContextThemeWrapper(context, theme)
        }
        val popupMenu = PopupMenu(context, view)
        popupMenu.menuInflater.inflate(R.menu.popup_alignment, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.menuCenter -> launcherActivity.setFlowLayoutAlignment(Gravity.CENTER or Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL)
                R.id.menuEnd -> launcherActivity.setFlowLayoutAlignment(Gravity.END or Gravity.CENTER_VERTICAL)
                R.id.menuStart -> launcherActivity.setFlowLayoutAlignment(Gravity.START or Gravity.CENTER_VERTICAL)
            }
            true
        }
        popupMenu.show()
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
        freezeSize(!b)
        if (!b) {
            freezeSize?.setText(R.string.unfreeze_app_size)
        } else {
            freezeSize?.setText(R.string.freeze_apps_size)
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
        ThemeSelectorDialog(launcherActivity).show()
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

    private fun setFonts() {
        cancel()
        val intentSetFonts = Intent(Intent.ACTION_GET_CONTENT)
        intentSetFonts.addCategory(Intent.CATEGORY_OPENABLE)
        //intentSetFonts.setType("application/x-font-ttf");
        // intentSetFonts.setType("file/plain");
        intentSetFonts.type = "*/*"
        val intent = Intent.createChooser(intentSetFonts, "Choose Fonts")
        launcherActivity.startActivityForResult(intent, Constants.FONTS_REQUEST)
    }

    private fun fontSelection(view: View) {
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
        popupMenu.menuInflater.inflate(R.menu.popup_font_selection, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.menuChooseFonts -> setFonts()
                R.id.menuDefaultFont -> {
                    if (isFontExists) {
                        removeFont()
                        launcherActivity.setFont()
                        launcherActivity.loadApps()
                        cancel()
                    }
                }
            }
            true
        }
        popupMenu.show()
    }
}
