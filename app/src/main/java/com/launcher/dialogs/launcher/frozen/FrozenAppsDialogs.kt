package com.launcher.dialogs.launcher.frozen

import android.app.Dialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import com.databinding.DlgFrozenAppsBinding
import com.databinding.DlgHiddenAppsBinding
import com.launcher.adapters.UniversalAdapter
import com.launcher.dialogs.launcher.hidden.RemoveDialog
import com.launcher.model.Apps

class FrozenAppsDialogs(
    mContext: Context, private val mAppsList: List<Apps>
) : Dialog(
    mContext
) {
    private lateinit var binding: DlgFrozenAppsBinding
    private var frozenApps = ArrayList<Apps>()
    private var adapter: UniversalAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = DlgFrozenAppsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = UniversalAdapter(context, frozenApps)
        binding.lvHiddenApp.adapter = adapter

        adapter?.setOnClickListener(object : UniversalAdapter.OnClickListener {
            override fun onClick(apps: Apps?, view: View) {
                apps?.let { a ->
                    confirmationAndRemove(
                        apps = a
                    )
                }
            }
        })
    }

    private fun confirmationAndRemove(
        apps: Apps
    ) {
        val dialogs = RemoveDialog(mContext = context, onClickRemove = {
            apps.setFreeze(false)
            updateFrozenList()
            adapter?.notifyDataSetChanged()
            if (frozenApps.isEmpty()) {
                dismiss()
            }
        }, onClickRun = {
            if (!apps.isShortcut) {
                dismiss()
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
        })
        dialogs.show()
        val window = dialogs.window
        if (window != null) {
            window.setGravity(Gravity.TOP)
            window.setBackgroundDrawableResource(android.R.color.transparent)
            window.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
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
