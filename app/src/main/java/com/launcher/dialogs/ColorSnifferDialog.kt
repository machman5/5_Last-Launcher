package com.launcher.dialogs

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.TextView
import com.R
import com.launcher.LauncherActivity
import com.launcher.utils.Constants
import com.launcher.utils.DbUtils.externalSourceColor
import com.launcher.utils.DbUtils.isExternalSourceColor

// color sniffer dialog
// may be removed from stable version
//Color sniffer developer halted the development of that app
class ColorSnifferDialog internal constructor(
    private val mContext: Context,
    private val launcherActivity: LauncherActivity
) : Dialog(
    mContext
), View.OnClickListener {
    private var onOffSwitch: TextView? = null
    private var mStartColorSniffer: TextView? = null
    private var change = false

    override fun onClick(view: View) {
        when (view.id) {
            R.id.text_color_sniffer_on_off -> {
                onOffColorSnifferCustomisation()
            }
            R.id.color_sniffer_clipboard -> {
                launcherActivity.clipboardData()
            }
            R.id.color_sniffer_external_app -> {
                if (isExternalSourceColor) {
                    startColorSnifferApp()
                }
            }
        }
    }

    private fun onOffColorSnifferCustomisation() {
        val b = isExternalSourceColor
        externalSourceColor(!b)
        onOffSwitch?.setText(if (!b) R.string.on else R.string.off)
        change = !change
        mStartColorSniffer?.visibility = if (!b) View.VISIBLE else View.GONE
    }

    //TODO: uri update, data schema
    private fun startColorSnifferApp() {
        //check app android compat currently colorSniffer api=19 and this app api=14
        try {
            val intent = Intent("android.intent.action.MAIN")
            //is this correct call Rui Zhao?
            intent.component = ComponentName("ryey.colorsniffer", "ryey.colorsniffer.FormActivity")
            //currently default color is only provided by Theme:
            //Is it required to send default colors of apps : YES
            // is it required/ to send theme related data for better experience : ask for color sniffer developer
            // 2121= dummy value
            intent.putExtra(Constants.DEFAULT_COLOR_FOR_APPS, 2121)
            launcherActivity.startActivityForResult(intent, Constants.COLOR_SNIFFER_REQUEST)
            // for activity result see LauncherActivity line 509
            cancel()
        } catch (e: ActivityNotFoundException) {
            //this will never happen because this option is only shown after app is installed
            val uri = Uri.parse("market://details?id=ryey.colorsniffer")
            val i = Intent(Intent.ACTION_VIEW, uri)
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(i)
        }
    }

    override fun onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dlg_color_sniffer_settings)
        findViewById<View>(R.id.text_color_sniffer_on_off).setOnClickListener(this)
        findViewById<View>(R.id.color_sniffer_clipboard).setOnClickListener(this)
        mStartColorSniffer = findViewById(R.id.color_sniffer_external_app)
        mStartColorSniffer?.setOnClickListener(this)
        onOffSwitch = findViewById(R.id.switch_color_sniffer_on_off)
        val onOff = isExternalSourceColor
        onOffSwitch?.setText(if (onOff) R.string.on else R.string.off)
        mStartColorSniffer?.visibility = if (onOff) View.VISIBLE else View.GONE
    }

    override fun onStop() {
        super.onStop()
        //else Window decor leak happen
        if (change) {
            launcherActivity.recreate()
        }
    }
}
