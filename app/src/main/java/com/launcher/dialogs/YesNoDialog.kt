package com.launcher.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.Window
import com.databinding.DlgYesNoBinding

class YesNoDialog(
    mContext: Context,
    private val title: String,
    private val msg: String,
    private val yes: String,
    private val no: String,
    private val onClickYes: ((Unit) -> Unit),
    private val onClickNo: ((Unit) -> Unit),
) : Dialog(mContext), View.OnClickListener {
    private lateinit var binding: DlgYesNoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // no old title: Last Launcher use Activity class not AppCompatActivity so it show very old title
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = DlgYesNoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvTitle.text = title
        binding.tvMsg.text = msg
        binding.tvYes.text = yes
        binding.tvNo.text = no

        binding.tvYes.setOnClickListener(this)
        binding.tvNo.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v) {
            binding.tvYes -> {
                onClickYes.invoke(Unit)
            }
            binding.tvNo -> {
                onClickNo.invoke(Unit)
            }
        }
        dismiss()
    }

}
