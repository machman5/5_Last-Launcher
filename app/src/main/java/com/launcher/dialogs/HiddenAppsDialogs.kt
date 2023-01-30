package com.launcher.dialogs

import android.app.Dialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.ListView
import android.widget.PopupMenu
import com.R
import com.launcher.adapters.UniversalAdapter
import com.launcher.model.Apps
import com.launcher.utils.DbUtils.theme
import java.util.*

class HiddenAppsDialogs(
    mContext: Context,
    private val mAppsList: List<Apps>
) : Dialog(
    mContext
) {
    private var hiddenApps = ArrayList<Apps>()

    override fun onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_hidden_apps)
        val listView = findViewById<ListView>(R.id.hidden_app_list)
        val adapter = UniversalAdapter(context, hiddenApps)
        listView.adapter = adapter
        adapter.setOnClickListener(object : UniversalAdapter.OnClickListener {
            override fun onClick(apps: Apps?, view: View) {
                apps?.apply {
                    confirmationAndRemove(
                        apps = this,
                        view = view
                    )
                }
            }

        })
    }

    private fun confirmationAndRemove(
        apps: Apps,
        view: View
    ) {
        val ctx: Context = if (theme == R.style.Wallpaper) {
            ContextThemeWrapper(
                /* base = */ context,
                /* themeResId = */ R.style.AppTheme
            )
        } else {
            ContextThemeWrapper(
                /* base = */ context,
                /* themeResId = */ theme
            )
        }
        val popupMenu = PopupMenu(/* context = */ ctx, /* anchor = */ view)
        popupMenu.menuInflater.inflate(R.menu.popup_remove, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
            if (menuItem.itemId == R.id.menuRemoveThis) {
                apps.setAppHidden(false)
                updateHiddenList()
            } else if (menuItem.itemId == R.id.menuRunThisApp) {
                if (!apps.isShortcut) {
                    apps.activityName?.let { name ->
                        val strings = name.split("/".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray()
                        val intent = Intent(Intent.ACTION_MAIN, null)
                        intent.setClassName(strings[0], strings[1])
                        intent.component = ComponentName(strings[0], strings[1])
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(intent)
                    }
                }
            }
            true
        }
        popupMenu.show()
    }

    fun updateHiddenList(): Int {
        synchronized(mAppsList) {
            for (apps in mAppsList) {
                if (apps.isHidden) {
                    hiddenApps.add(apps)
                }
            }
        }
        hiddenApps.sortWith { o1: Apps, o2: Apps ->
            o1.getAppName().compareTo(o2.getAppName(), ignoreCase = true)
        }
        return hiddenApps.size
    }
}
