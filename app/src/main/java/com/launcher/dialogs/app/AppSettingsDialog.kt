package com.launcher.dialogs.app

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.Window
import com.databinding.DlgAppSettingsBinding
import com.launcher.LauncherActivity

/**
 * this the launcher setting Dialog
 */
class AppSettingsDialog(
    mContext: Context,
    private val launcherActivity: LauncherActivity
) : Dialog(mContext), View.OnClickListener {
    private lateinit var binding: DlgAppSettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // no old title: Last Launcher use Activity class not AppCompatActivity so it show very old title
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = DlgAppSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.settingsFonts.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view) {
            binding.settingsFonts -> {

            }
        }
    }
}
