package com.launcher.dialogs.launcher.sort

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import com.R
import com.databinding.DlgAppFontBinding
import com.databinding.DlgAppSortBinding
import com.launcher.LauncherActivity
import com.launcher.utils.Constants
import com.launcher.utils.DbUtils

class SortDialog(
    mContext: Context,
    private val launcherActivity: LauncherActivity
) : Dialog(
    mContext,
    R.style.DialogSlideUpAnim,
), View.OnClickListener {
    private lateinit var binding: DlgAppSortBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // no old title: Last Launcher use Activity class not AppCompatActivity so it show very old title
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = DlgAppSortBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.menuSortByName.setOnClickListener(this)
        binding.menuSortByOpeningCounts.setOnClickListener(this)
        binding.menuSortByColor.setOnClickListener(this)
        binding.menuSortBySize.setOnClickListener(this)
        binding.menuSortByUpdateTime.setOnClickListener(this)
        binding.menuSortByRecentUse.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v) {
            binding.menuSortByName -> {
                launcherActivity.sortApps(Constants.SORT_BY_NAME)
            }
            binding.menuSortByOpeningCounts -> {
                launcherActivity.sortApps(Constants.SORT_BY_OPENING_COUNTS)
            }
            binding.menuSortByColor -> {
                launcherActivity.sortApps(Constants.SORT_BY_COLOR)
            }
            binding.menuSortBySize -> {
                launcherActivity.sortApps(Constants.SORT_BY_SIZE)
            }
            binding.menuSortByUpdateTime -> {
                launcherActivity.sortApps(Constants.SORT_BY_UPDATE_TIME)
            }
            binding.menuSortByRecentUse -> {
                launcherActivity.sortApps(Constants.SORT_BY_RECENT_OPEN)
            }
        }
        dismiss()
    }

}
