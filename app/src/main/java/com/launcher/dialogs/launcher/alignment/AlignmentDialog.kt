package com.launcher.dialogs.launcher.alignment

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.Window
import com.databinding.DlgAppFontBinding
import com.databinding.DlgGlobalAlignmentBinding
import com.launcher.LauncherActivity
import com.launcher.utils.Constants
import com.launcher.utils.DbUtils

class AlignmentDialog(
    mContext: Context,
    private val launcherActivity: LauncherActivity
) : Dialog(mContext), View.OnClickListener {
    private lateinit var binding: DlgGlobalAlignmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // no old title: Last Launcher use Activity class not AppCompatActivity so it show very old title
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = DlgGlobalAlignmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.menuCenter.setOnClickListener(this)
        binding.menuEnd.setOnClickListener(this)
        binding.menuStart.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v) {
            binding.menuCenter -> {
                launcherActivity.setFlowLayoutAlignment(Gravity.CENTER or Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL)
            }
            binding.menuEnd -> {
                launcherActivity.setFlowLayoutAlignment(Gravity.END or Gravity.CENTER_VERTICAL)
            }
            binding.menuStart -> {
                launcherActivity.setFlowLayoutAlignment(Gravity.START or Gravity.CENTER_VERTICAL)
            }
        }
        dismiss()
    }
}
