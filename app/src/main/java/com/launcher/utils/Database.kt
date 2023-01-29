package com.launcher.utils

import android.content.ContentValues
import android.content.Context
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.launcher.model.Shortcut

//This is new database based on SQLITE
// Why not ROOM: simply make this launcher lightweight, BTW This app database is too small :)
internal class Database  //private static final String TABLE_NAME_APPS = "apps";
/*private static final String APP_ACTIVITY = "activity";
    private static final String IS_SHORTCUTS="shortcut";
    private static final String APP_NAME = "name";
    private static final String APP_ORIGINAL_NAME = "original_name";
    private static final String APP_SIZE = "size";
    private static final String APP_COLOR = "color";
    private static final String APP_VISIBILITY = "visibility";
    private static final String APP_FROZEN = "frozen";
    private static final String APP_EXTERNAL_SOURCE_COLOR = "extrn_src_color";
    private static final String APP_GROUP_PREFIX = "gp_prefix";
    private static final String APP_CATEGORY = "category";
    private static final String APP_OPENING_COUNTS = "open_count";
    private static final String id="id";
    */

// private static final String CREATE_APPS_TABLE = "CREATE TABLE
    (context: Context?) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_SHORTCUTS_TABLE)
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_SHORTCUTS")
        onCreate(db)
    }

    fun insertShortcut(
        name: String?,
        uri: String?
    ) {
        val db = writableDatabase
        val cValues = ContentValues()
        cValues.put(SHORTCUT_NAME, name)
        cValues.put(SHORTCUT_URI, uri)
        db.insert(TABLE_NAME_SHORTCUTS, null, cValues)
        db.close()
    }

    fun deleteShortcuts(name: String) {
        val db = this.writableDatabase
        db.delete(TABLE_NAME_SHORTCUTS, "$SHORTCUT_NAME = ?", arrayOf(name))
        db.close()
    }

    val allShortcuts: ArrayList<Shortcut>
        get() {
            val db = this.writableDatabase
            val shortcuts = ArrayList<Shortcut>()
            val query = "SELECT uri,name FROM $TABLE_NAME_SHORTCUTS"
            val cursor = db.rawQuery(query, null)
            while (cursor.moveToNext()) {
                val shortcut = Shortcut()
                shortcut.name = cursor.getString(cursor.getColumnIndex(SHORTCUT_NAME))
                shortcut.uri = cursor.getString(cursor.getColumnIndex(SHORTCUT_URI))
                shortcuts.add(shortcut)
            }
            cursor.close()
            return shortcuts
        }
    val shortcutsCounts: Int
        get() {
            val db = this.writableDatabase
            val count = DatabaseUtils.queryNumEntries(db, TABLE_NAME_SHORTCUTS)
            db.close()
            return count.toInt()
        }

    fun shortcutsExists(name: String): Boolean {
        val db = this.readableDatabase
        val sql = "SELECT EXISTS (SELECT * FROM shortcuts WHERE uri='$name' LIMIT 1)"
        val cursor = db.rawQuery(sql, null)
        cursor.moveToFirst()
        return if (cursor.getInt(0) == 1) {
            cursor.close()
            true
        } else {
            cursor.close()
            false
        }
    }

    companion object {
        private const val DB_NAME = "launcher.db"
        private const val DB_VERSION = 1
        private const val TABLE_NAME_SHORTCUTS = "shortcuts"
        private const val SHORTCUT_URI = "uri"
        private const val SHORTCUT_NAME = "name"
        private const val CREATE_SHORTCUTS_TABLE =
            "CREATE TABLE shortcuts(id INTEGER  PRIMARY KEY AUTOINCREMENT, name TEXT, uri TEXT )"
    }
}
