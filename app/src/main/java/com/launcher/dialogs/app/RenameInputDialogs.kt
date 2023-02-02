package com.launcher.dialogs.app

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.TextView.OnEditorActionListener
import com.R
import com.databinding.DlgRenameInputBinding
import com.launcher.LauncherActivity
import com.launcher.ext.showKeyboard
import com.launcher.utils.DbUtils.putAppName

class RenameInputDialogs(
    context: Context,
    private val appPackage: String,
    private val oldAppName: String,
    private val launcherActivity: LauncherActivity
) : Dialog(
    context,
    R.style.DialogSlideUpAnim
) {

    private lateinit var binding: DlgRenameInputBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = DlgRenameInputBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.etInput.let { et ->
            et.setText(oldAppName)
            et.setOnEditorActionListener(OnEditorActionListener { _, i, _ ->
                var handled = false
                if (i == EditorInfo.IME_ACTION_DONE) {
                    val temp = binding.etInput.text.toString()
                    if (temp.isNotEmpty()) {
                        putAppName(activityName = appPackage, value = temp)
                        //reflect this on screen immediately
                        launcherActivity.onAppRenamed(appPackage, temp)
                        cancel()
                    }
                    handled = true
                }
                return@OnEditorActionListener handled
            })
            et.isEnabled = true
            et.requestFocus()
        }

        window?.apply {
            this.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            this.setBackgroundDrawableResource(android.R.color.transparent)
        }

        binding.etInput.postDelayed(
            { binding.etInput.showKeyboard() }, 100
        )
    }

}
