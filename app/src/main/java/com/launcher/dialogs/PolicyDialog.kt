package com.launcher.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.Window
import com.R
import com.databinding.DlgPolicyBinding
import com.databinding.DlgYesNoBinding
import com.launcher.ext.click

class PolicyDialog(
    mContext: Context,
    private val onClick: OnClick? = null,
) : Dialog(
    mContext,
    R.style.DialogSlideUpAnim,
) {
    private lateinit var binding: DlgPolicyBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // no old title: Last Launcher use Activity class not AppCompatActivity so it show very old title
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = DlgPolicyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvTitle.text = "Welcome to ${context.getString(R.string.app_name)}"

        binding.fl.click {
            onClick?.onYes()
            dismiss()
        }
    }

    interface OnClick {
        fun onYes()
    }
}
