package com.launcher.dialogs.launcher.hidden

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import com.databinding.DlgAppFontBinding
import com.databinding.DlgAppRemoveBinding
import com.launcher.LauncherActivity
import com.launcher.utils.Constants
import com.launcher.utils.DbUtils

class RemoveDialog(
    mContext: Context,
    private val onClickRemove: ((Unit) -> Unit),
    private val onClickRun: ((Unit) -> Unit),
) : Dialog(mContext), View.OnClickListener {
    private lateinit var binding: DlgAppRemoveBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // no old title: Last Launcher use Activity class not AppCompatActivity so it show very old title
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = DlgAppRemoveBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.menuRemoveThis.setOnClickListener(this)
        binding.menuRunThisApp.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v) {
            binding.menuRemoveThis -> {
                onClickRemove.invoke(Unit)
            }
            binding.menuRunThisApp -> {
                onClickRun.invoke(Unit)
            }
        }
        dismiss()
    }

}
