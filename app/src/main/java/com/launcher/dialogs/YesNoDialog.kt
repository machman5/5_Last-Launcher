package com.launcher.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.Window
import com.R
import com.databinding.DlgYesNoBinding
import com.launcher.ext.click

class YesNoDialog(
    mContext: Context,
    private val title: String,
    private val msg: String,
    private val yes: String,
    private val no: String,
    private val onClickYes: ((Unit) -> Unit),
    private val onClickNo: ((Unit) -> Unit),
) : Dialog(
    mContext,
    R.style.DialogSlideUpAnim,
) {
    private lateinit var binding: DlgYesNoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // no old title: Last Launcher use Activity class not AppCompatActivity so it show very old title
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = DlgYesNoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvClose.click {
            dismiss()
        }
        binding.tvTitle.text = title
        binding.tvMsg.text = msg
        binding.tvYes.text = yes
        binding.tvNo.text = no

        binding.tvYes.click {
            onClickYes.invoke(Unit)
            dismiss()
        }
        binding.tvNo.click {
            onClickNo.invoke(Unit)
            dismiss()
        }
    }

}
