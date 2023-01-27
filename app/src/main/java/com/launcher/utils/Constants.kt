package com.launcher.utils

object Constants {
    const val COLOR_SNIFFER_REQUEST = 154
    const val DEFAULT_COLOR_FOR_APPS = "default_color_for_apps"

    //various sorting constant
    //why constant? Why not enums for this ?
    // may be lack from Shared Preference DB
    const val SORT_BY_NAME = 1
    const val SORT_BY_SIZE = 2
    const val SORT_BY_COLOR = 3
    const val SORT_BY_OPENING_COUNTS = 4
    @Suppress("unused")
    const val SORT_BY_CUSTOM = 5
    const val SORT_BY_UPDATE_TIME = 6
    const val SORT_BY_RECENT_OPEN = 7
    const val RESTORE_REQUEST = 125
    const val FONTS_REQUEST = 126
    const val BACKUP_REQUEST = 128
    const val DEFAULT_MAX_TEXT_SIZE = 10
    const val DEFAULT_MIN_TEXT_SIZE = -10
    const val MAX_PADDING_LEFT = 99
    const val MAX_PADDING_RIGHT = 99
    const val MAX_PADDING_TOP = 999
    const val MAX_PADDING_BOTTOM = 999
    const val MIN_PADDING = 0

    //TODO: Dynamic height
    var dynamicHeight = 20

    @JvmField
    val DEFAULT_TEXT_SIZE_NORMAL_APPS = dynamicHeight

    @JvmField
    val DEFAULT_TEXT_SIZE_OFTEN_APPS = dynamicHeight * 9 / 5
    const val MAX_TEXT_SIZE_FOR_APPS = 90
    const val MIN_TEXT_SIZE_FOR_APPS = 14
}
