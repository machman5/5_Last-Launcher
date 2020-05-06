/*
 * Last Launcher
 * Copyright (C) 2019 Shubham Tyagi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.subhamtyagi.lastlauncher.model;

import android.view.View;

import io.github.subhamtyagi.lastlauncher.utils.DbUtils;
import io.github.subhamtyagi.lastlauncher.utils.Utils;
import io.github.subhamtyagi.lastlauncher.views.textview.AppTextView;


// a model class that hold everything related to an app
public class Apps {

    // app activity name format package.name/package.name.ClassName
    // for eg. com.example.appname/com.example.appname.MainActivity
    // for eg  io.github.subhamtyagi.lastlauncher/io.github.subhamtyagi.lastlauncher.LauncherActivity
    // if this shortcut than activity represent the URI string
    final private String activity;

    // app name to shown on screen
    private String appName;
    // a text view or a subclass
    private AppTextView textView;
    // app color
    private int color;
    // app size
    private int size;
    // is app size frozen
    private boolean isSizeFrozen;
    // is app hidden from home screen
    private boolean isAppHidden;

    //store how many time this app is opened by user
    // save this to DB. So launcher can sort the app based on it uses:
    // (take precaution while saving this: encrypt this it contains sensitive info)
    // in theory this is a tracking count which store how many time user opens this apps
    // nothing will send to anywhere only locally and privately saved to user device and btw this
    // launcher doesn't have internet permission
    private int openingCounts;

    // This field is use for grouping the app
    private String groupPrefix;

    // app belongs to this categories
    private String categories;

    // tell whether this is a shortcut or not if this shortcut then activity field hold the Uri not activity
    private boolean isShortcut;


    /**
     * @param isShortcut    tell whether this shortcut or not
     * @param activity      activity path if this is shortcut then it will hold a unique strings
     * @param appName       App name
     * @param tv            a text view corresponding to App
     * @param color         Text color
     * @param size          Text Size
     * @param isAppHidden   boolean to tell 'is app hide
     * @param isSizeFrozen  is app size to freeze
     * @param openingCounts how many time apps was opened before this addition
     */
    public Apps(boolean isShortcut, String activity, String appName, AppTextView tv, int color, int size, boolean isAppHidden, boolean isSizeFrozen, int openingCounts) {

        this.isShortcut = isShortcut;
        this.textView = tv;
        this.appName = appName;

        this.color = color;
        this.size = size;

        if (isShortcut) {
            this.activity = String.valueOf(Utils.hash(activity));
            textView.setUri(activity);
            // textView.setUniqueCode(this.activity);
        } else {
            this.activity = activity;
            //textView.setUniqueCode(String.valueOf(Utils.hash(activity)));
        }

        textView.setText(this.appName);
        textView.setTag(this.activity);
        textView.setShortcut(this.isShortcut);

        // if color is not -1 then set this to color
        // else not set the color default theme text color will handle the color
        if (color != DbUtils.NULL_TEXT_COLOR)
            textView.setTextColor(color);

        this.openingCounts = openingCounts;

        setSize(size);
        setAppHidden(isAppHidden);
        setFreeze(isSizeFrozen);

    }

    public boolean isShortcut() {
        return isShortcut;
    }

    public boolean isSizeFrozen() {
        return isSizeFrozen;
    }

    public boolean isHidden() {
        return isAppHidden;
    }

    public void setAppHidden(boolean appHidden) {
        this.isAppHidden = appHidden;
        textView.setVisibility(appHidden ? View.GONE : View.VISIBLE);
        DbUtils.hideApp(activity, appHidden);
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
        textView.setTextSize(size);
        DbUtils.putAppSize(activity, size);
    }

    public void setFreeze(boolean freezeSize) {
        this.isSizeFrozen = freezeSize;
        DbUtils.freezeAppSize(activity, freezeSize);
    }


    public String getActivityName() {
        return activity;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
        textView.setText(appName);
    }

    public AppTextView getTextView() {
        return textView;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
        if (color != DbUtils.NULL_TEXT_COLOR)
            textView.setTextColor(color);
    }


    public int getOpeningCounts() {
        return openingCounts;
    }

    public void setOpeningCounts(int openingCounts) {
        this.openingCounts = openingCounts;
        DbUtils.setOpeningCounts(this.activity, openingCounts);
    }

    public void increaseOpeningCounts() {
        this.openingCounts++;
        DbUtils.setOpeningCounts(this.activity, openingCounts);
    }

    public String getGroupPrefix() {
        return groupPrefix;
    }

    public void setGroupPrefix(String groupPrefix) {
        this.groupPrefix = groupPrefix;
        DbUtils.setCategories(this.activity, groupPrefix);
    }

    public String getCategories() {
        return categories;
    }

    public void setCategories(String categories) {
        this.categories = categories;
        DbUtils.setCategories(this.activity, categories);
    }
}
