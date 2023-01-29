package com.launcher.model

import android.view.View
import com.launcher.utils.DbUtils
import com.launcher.utils.DbUtils.freezeAppSize
import com.launcher.utils.DbUtils.hideApp
import com.launcher.utils.DbUtils.putAppSize
import com.launcher.utils.DbUtils.setCategories
import com.launcher.utils.DbUtils.setOpeningCounts
import com.launcher.utils.Utils.Companion.hash
import com.launcher.views.textview.AppTextView

// a model class that hold everything related to an app
class Apps(// tell whether this is a shortcut or not if this is shortcut then activity field wll holds the Uri not an activity
    val isShortcut: Boolean,
    activity: String, // app name to shown on screen
    private var appName: String, // a text view or a subclass of TextView
    val textView: AppTextView, // app text color
    private var color: Int, // app text size
    private var size: Int,
    isAppHidden: Boolean,
    isSizeFrozen: Boolean,
    openingCounts: Int, // last updated date and time
    var updateTime: Int
) {
    // App activity name format package.name/package.name.ClassName
    // For eg. com.example.app_name/com.example.app_name.MainActivity
    // For eg  io.github.subhamtyagi.lastlauncher/io.github.subhamtyagi.lastlauncher.LauncherActivity
    // if this is a shortcut then this field represent a unique URI string
    var activityName: String? = null

    // is app text size frozen
    var isSizeFrozen = false
        private set

    // is app hidden from home screen
    var isHidden = false
        private set

    //store how many time this app is opened by user
    // save this to DB. So launcher can sort the app based on this
    // in theory this is a tracking count which store how many time user opened this apps
    // Only locally and privately saved to user device
    // and btw this launcher doesn't have INTERNET PERMISSION
    var openingCounts: Int
        private set

    // This field is use for grouping the app: not in use
    @Suppress("unused")
    var groupPrefix: String? = null
        set(groupPrefix) {
            field = groupPrefix
            activityName?.let {
                setCategories(activityName = it, categories = groupPrefix)
            }
        }

    // app belongs to this categories,,: not in use
    @Suppress("unused")
    var categories: String? = null
        set(categories) {
            field = categories
            activityName?.let {
                setCategories(activityName = it, categories = categories)
            }
        }
    var recentUsedWeight = 0

    /**
     * isShortcut    tell whether this shortcut or not
     * activity      activity path if this is shortcut then it will hold a unique strings
     * appName       App name
     * tv            a text view corresponding to App
     * color         Text color
     * size          Text Size
     * isAppHidden   boolean to tell 'is app hide
     * isSizeFrozen  is app size to freeze
     * openingCounts how many time apps was opened before this addition
     * updateTime    update time of this app since epoch (use for sorting)
     */
    init {
        if (isShortcut) {
            activityName = hash(activity).toString()
            textView.uri = activity
            // textView.setUniqueCode(this.activity);
        } else {
            activityName = activity
            //textView.setUniqueCode(String.valueOf(Utils.hash(activity)));
        }
        textView.text = appName
        textView.tag = activityName
        textView.isShortcut = isShortcut

        // if color is not -1 then set this to color
        // else not set the color default theme text color will handle the color
        if (color != DbUtils.NULL_TEXT_COLOR) textView.setTextColor(color)
        this.openingCounts = openingCounts
        setSize(size)
        setAppHidden(isAppHidden)
        setFreeze(isSizeFrozen)
    }

    fun setAppHidden(appHidden: Boolean) {
        isHidden = appHidden
        textView.visibility = if (appHidden) View.GONE else View.VISIBLE
        activityName?.let {
            hideApp(activityName = it, value = appHidden)
        }
    }

    fun getSize(): Int {
        return size
    }

    fun setSize(size: Int) {
        this.size = size
        textView.textSize = size.toFloat()
        activityName?.let {
            putAppSize(activityName = it, size = size)
        }
    }

    fun setFreeze(freezeSize: Boolean) {
        isSizeFrozen = freezeSize
        activityName?.let {
            freezeAppSize(activityName = it, value = freezeSize)
        }
    }

    fun getAppName(): String {
        return appName
    }

    fun setAppName(appName: String) {
        this.appName = appName
        textView.text = appName
    }

    fun getColor(): Int {
        return color
    }

    @Suppress("unused")
    fun setColor(color: Int) {
        this.color = color
        if (color != DbUtils.NULL_TEXT_COLOR) textView.setTextColor(color)
    }

    fun increaseOpeningCounts() {
        openingCounts++
        activityName?.let {
            setOpeningCounts(activityName = it, count = openingCounts)
        }
    }
}
