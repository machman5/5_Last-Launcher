package com.launcher.dialogs.launcher.font

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import com.R
import com.databinding.DlgAppFontBinding
import com.launcher.LauncherActivity
import com.launcher.ext.click
import com.launcher.utils.Constants
import com.launcher.utils.DbUtils

/**
 * this the launcher setting Dialog
 */
class FontDialog(
    mContext: Context,
    private val launcherActivity: LauncherActivity
) : Dialog(
    mContext,
    R.style.DialogSlideUpAnim,
) {
    private lateinit var binding: DlgAppFontBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // no old title: Last Launcher use Activity class not AppCompatActivity so it show very old title
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = DlgAppFontBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.menuChooseFonts.click {
            setFonts()
        }
        binding.menuDefaultFont.click {
            if (DbUtils.isFontExists) {
                DbUtils.removeFont()
                launcherActivity.setFont()
                launcherActivity.loadApps()
                cancel()
            }
            dismiss()
        }
    }

    private fun setFonts() {
        cancel()
        val intentSetFonts = Intent(Intent.ACTION_GET_CONTENT)
        intentSetFonts.addCategory(Intent.CATEGORY_OPENABLE)
        //intentSetFonts.setType("application/x-font-ttf");
        // intentSetFonts.setType("file/plain");
        intentSetFonts.type = "*/*"
        val intent = Intent.createChooser(intentSetFonts, "Choose Fonts")
        launcherActivity.startActivityForResult(intent, Constants.FONTS_REQUEST)
    }

}
