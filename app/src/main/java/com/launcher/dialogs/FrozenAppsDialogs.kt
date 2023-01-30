package com.launcher.dialogs

import android.app.Dialog
import android.content.Context
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

class FrozenAppsDialogs(
    mContext: Context,
    private val mAppsList: List<Apps>
) : Dialog(
    mContext
) {
    private var frozenApps = ArrayList<Apps>()

    override fun onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_hidden_apps)
        val listView = findViewById<ListView>(R.id.hidden_app_list)
        val adapter = UniversalAdapter(context, frozenApps)
        listView.adapter = adapter

        adapter.setOnClickListener(object : UniversalAdapter.OnClickListener {
            override fun onClick(apps: Apps?, view: View) {
                apps?.let { a ->
                    confirmationAndRemove(
                        a,
                        view
                    )
                }
            }
        })
    }

    private fun confirmationAndRemove(
        apps: Apps,
        view: View
    ) {
        val ctx: Context = if (theme == R.style.Wallpaper) ContextThemeWrapper(
            context,
            R.style.AppTheme
        ) else ContextThemeWrapper(
            context, theme
        )
        val popupMenu = PopupMenu(ctx, view)
        popupMenu.menuInflater.inflate(R.menu.popup_remove, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
            if (menuItem.itemId == R.id.menuRemoveThis) {
                apps.setFreeze(false)
                updateFrozenList()
            }
            true
        }
        popupMenu.show()
    }

    fun updateFrozenList(): Int {
        synchronized(mAppsList) {
            // only show frozen app
            for (apps in mAppsList) {
                if (apps.isSizeFrozen) {
                    frozenApps.add(apps)
                }
            }
        }
        return frozenApps.size
    }
}
