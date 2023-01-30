package com.launcher.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import com.R
import com.launcher.LauncherActivity
import com.launcher.utils.DbUtils.putAppName

class RenameInputDialogs(
    context: Context,
    private val appPackage: String,
    private val oldAppName: String,
    private val launcherActivity: LauncherActivity
) : Dialog(
    context
), OnEditorActionListener {

    private var etInput: EditText? = null

    override fun onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_rename_input)
        etInput = findViewById(R.id.ed_input)
        etInput?.let { et ->
            et.setText(oldAppName)
            et.setOnEditorActionListener(this)
            et.isEnabled = true
            et.requestFocus()
        }

        window?.apply {
            this.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            this.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    override fun onEditorAction(
        tv: TextView,
        i: Int,
        keyEvent: KeyEvent
    ): Boolean {
        var handled = false
        if (i == EditorInfo.IME_ACTION_DONE) {
            val temp = etInput?.text.toString()
            if (temp.isNotEmpty()) {
                putAppName(activityName = appPackage, value = temp)
                //reflect this on screen immediately
                launcherActivity.onAppRenamed(appPackage, temp)
                cancel()
            }
            handled = true
        }
        return handled
    }
}
