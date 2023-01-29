package com.launcher.utils

import android.content.Context
import android.content.Intent
import com.launcher.model.Shortcut

/**
 * This class manages Shortcut installed by user
 * most of the methods are now wrapper to Database
 */
class ShortcutUtils(context: Context?) {
    private var db: Database? = null

    init {
        if (null == db) {
            synchronized(ShortcutUtils::class.java) {
                if (null == db) {
                    db = Database(context)
                }
            }
        }
    }

    fun close() {
        db?.close()
    }

    @Throws(Throwable::class)
    @Suppress("unused")
    private fun checkDB() {
        if (db == null) {
            throw Throwable("Db is null")
        }
    }

    val allShortcuts: ArrayList<Shortcut>
        get() {
            return db?.allShortcuts ?: ArrayList()
        }

    /**
     * Add new shortcut
     *
     * @param shortcut instance of shortcut to be added
     */
    fun addShortcut(shortcut: Shortcut) {
        db?.insertShortcut(shortcut.name, shortcut.uri)
    }

    /**
     * remove the shortcuts
     *
     * @param shortcut to be removed
     */
    fun removeShortcut(shortcut: Shortcut) {
        db?.deleteShortcuts(shortcut.name)
    }

    /**
     * return true if shortcut is already install
     *
     * @param name uri of shortcut
     * @return true if already installed
     */
    fun isShortcutAlreadyAvailable(name: String): Boolean {
        return db?.shortcutsExists(name) ?: false
    }

    fun getShortcutCounts(): Int {
        return db?.shortcutsCounts ?: 0
    }

    fun isShortcutToApp(uri: String?): Boolean {
        try {
            val intent = Intent.parseUri(uri, 0)
            if (intent.categories != null && intent.categories.contains(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN == intent.action) {
                return true
            }
        } catch (e: Exception) {
            return false
        }
        return false
    }
}
