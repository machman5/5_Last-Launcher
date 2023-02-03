package com.launcher.dialogs.launcher.hidden

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import com.R
import com.databinding.DlgAppFontBinding
import com.databinding.DlgAppRemoveBinding
import com.launcher.LauncherActivity
import com.launcher.ext.click
import com.launcher.utils.Constants
import com.launcher.utils.DbUtils

class RemoveDialog(
    mContext: Context,
    private val title: String? = null,
    private val msg: String? = null,
    private val onClickRemove: ((Unit) -> Unit),
    private val onClickRun: ((Unit) -> Unit),
) : Dialog(
    mContext,
    R.style.DialogSlideUpAnim
) {
    private lateinit var binding: DlgAppRemoveBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // no old title: Last Launcher use Activity class not AppCompatActivity so it show very old title
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = DlgAppRemoveBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvClose.click {
            dismiss()
        }
        title?.let {
            binding.tvTitle.text = it
            binding.tvTitle.visibility = View.VISIBLE
        }
        msg?.let {
            binding.tvMsg.text = it
            binding.tvMsg.visibility = View.VISIBLE
        }

        binding.menuRemoveThis.click {
            onClickRemove.invoke(Unit)
            dismiss()
        }
        binding.menuRunThisApp.click {
            onClickRun.invoke(Unit)
            dismiss()
        }
    }

}
