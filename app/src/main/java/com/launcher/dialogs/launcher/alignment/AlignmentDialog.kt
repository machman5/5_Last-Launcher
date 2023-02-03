package com.launcher.dialogs.launcher.alignment

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.Window
import com.R
import com.databinding.DlgGlobalAlignmentBinding
import com.launcher.LauncherActivity
import com.launcher.ext.click

class AlignmentDialog(
    mContext: Context,
    private val launcherActivity: LauncherActivity
) : Dialog(
    mContext,
    R.style.DialogSlideUpAnim
) {
    private lateinit var binding: DlgGlobalAlignmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // no old title: Last Launcher use Activity class not AppCompatActivity so it show very old title
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = DlgGlobalAlignmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvClose.click {
            dismiss()
        }
        binding.menuCenter.click {
            launcherActivity.setFlowLayoutAlignment(Gravity.CENTER or Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL)
            dismiss()
        }
        binding.menuEnd.click {
            launcherActivity.setFlowLayoutAlignment(Gravity.END or Gravity.CENTER_VERTICAL)
            dismiss()
        }
        binding.menuStart.click {
            launcherActivity.setFlowLayoutAlignment(Gravity.START or Gravity.CENTER_VERTICAL)
            dismiss()
        }
    }
}
