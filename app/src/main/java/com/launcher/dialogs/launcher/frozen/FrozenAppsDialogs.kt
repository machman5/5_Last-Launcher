package com.launcher.dialogs.launcher.frozen

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
import com.databinding.DlgHiddenAppsBinding
import com.launcher.adapters.UniversalAdapter
import com.launcher.model.Apps
import com.launcher.utils.DbUtils.theme

class FrozenAppsDialogs(
    mContext: Context,
    private val mAppsList: List<Apps>
) : Dialog(
    mContext
) {
    private lateinit var binding: DlgHiddenAppsBinding
    private var frozenApps = ArrayList<Apps>()
    private var adapter: UniversalAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = DlgHiddenAppsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = UniversalAdapter(context, frozenApps)
        binding.lvHiddenApp.adapter = adapter

        adapter?.setOnClickListener(object : UniversalAdapter.OnClickListener {
            override fun onClick(apps: Apps?, view: View) {
                apps?.let { a ->
                    confirmationAndRemove(
                        apps = a,
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
                adapter?.notifyDataSetChanged()
                if (frozenApps.isEmpty()) {
                    dismiss()
                }
            }
            true
        }
        popupMenu.show()
    }

    fun updateFrozenList(): Int {
        synchronized(mAppsList) {
            // only show frozen app
            frozenApps.clear()
            for (apps in mAppsList) {
                if (apps.isSizeFrozen) {
                    frozenApps.add(apps)
                }
            }
        }
        return frozenApps.size
    }
}
