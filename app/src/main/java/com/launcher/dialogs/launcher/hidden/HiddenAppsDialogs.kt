package com.launcher.dialogs.launcher.hidden

import android.app.Dialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import com.databinding.DlgHiddenAppsBinding
import com.launcher.adapters.UniversalAdapter
import com.launcher.model.Apps

class HiddenAppsDialogs(
    mContext: Context,
    private val mAppsList: List<Apps>
) : Dialog(
    mContext
) {
    private lateinit var binding: DlgHiddenAppsBinding
    private var hiddenApps = ArrayList<Apps>()
    private var adapter: UniversalAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = DlgHiddenAppsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = UniversalAdapter(context, hiddenApps)
        binding.lvHiddenApp.adapter = adapter
        adapter?.setOnClickListener(object : UniversalAdapter.OnClickListener {
            override fun onClick(apps: Apps?, view: View) {
                apps?.apply {
                    confirmationAndRemove(
                        apps = this
                    )
                }
            }
        })
    }

    private fun confirmationAndRemove(
        apps: Apps
    ) {
        val dialogs = RemoveDialog(
            mContext = context,
            onClickRemove = {
                apps.setAppHidden(false)
                updateHiddenList()
                adapter?.notifyDataSetChanged()
                if (hiddenApps.isEmpty()) {
                    dismiss()
                }
            },
            onClickRun = {
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
        )
        dialogs.show()
        val window = dialogs.window
        if (window != null) {
            window.setGravity(Gravity.TOP)
            window.setBackgroundDrawableResource(android.R.color.transparent)
            window.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    fun updateHiddenList(): Int {
        synchronized(mAppsList) {
            hiddenApps.clear()
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
