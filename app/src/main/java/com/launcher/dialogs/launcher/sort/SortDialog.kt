package com.launcher.dialogs.launcher.sort

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import com.R
import com.databinding.DlgAppSortBinding
import com.launcher.LauncherActivity
import com.launcher.ext.click
import com.launcher.utils.Constants

class SortDialog(
    mContext: Context, private val launcherActivity: LauncherActivity
) : Dialog(
    mContext,
    R.style.DialogSlideUpAnim,
) {
    private lateinit var binding: DlgAppSortBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // no old title: Last Launcher use Activity class not AppCompatActivity so it show very old title
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = DlgAppSortBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.menuSortByName.click {
            launcherActivity.sortApps(Constants.SORT_BY_NAME)
            dismiss()
        }
        binding.menuSortByOpeningCounts.click {
            launcherActivity.sortApps(Constants.SORT_BY_OPENING_COUNTS)
            dismiss()
        }
        binding.menuSortByColor.click {
            launcherActivity.sortApps(Constants.SORT_BY_COLOR)
            dismiss()
        }
        binding.menuSortBySize.click {
            launcherActivity.sortApps(Constants.SORT_BY_SIZE)
            dismiss()
        }
        binding.menuSortByUpdateTime.click {
            launcherActivity.sortApps(Constants.SORT_BY_UPDATE_TIME)
            dismiss()
        }
        binding.menuSortByRecentUse.click {
            launcherActivity.sortApps(Constants.SORT_BY_RECENT_OPEN)
            dismiss()
        }
    }

}
