package com.launcher.utils

import android.content.Context
import android.content.SharedPreferences
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.io.ObjectInputStream

// utility to handle shared prefs
internal class SpUtils private constructor() {

    companion object {
        @Volatile
        private var mInstance: SpUtils? = null
        val instance: SpUtils
            get() {
                if (null == mInstance) {
                    synchronized(SpUtils::class.java) {
                        if (null == mInstance) {
                            mInstance = SpUtils()
                        }
                    }
                }
                return mInstance!!
            }
    }

    private var mPref: SharedPreferences? = null

    fun init(context: Context) {
        if (mPref == null) {
//            mPref = PreferenceManager.getDefaultSharedPreferences(context)
            mPref =
                context.getSharedPreferences(SpUtils::class.java.simpleName, Context.MODE_PRIVATE)
        }
    }

    fun putString(key: String?, value: String?) {
        if (mPref != null) {
            mPref?.apply {
                val editor = this.edit()
                editor.putString(key, value)
                editor.apply()
            }
        } else {
            throw RuntimeException("First Initialize context")
        }
    }

    @Suppress("unused")
    fun putLong(key: String?, value: Long) {
        if (mPref != null) {
            mPref?.apply {
                val editor = this.edit()
                editor.putLong(key, value)
                editor.apply()
            }
        } else throw RuntimeException("First Initialize context")
    }

    fun putInt(key: String?, value: Int) {
        if (mPref != null) {
            mPref?.apply {
                val editor = this.edit()
                editor.putInt(key, value)
                editor.apply()
            }
        } else throw RuntimeException("First Initialize context")
    }

    fun putIntCommit(key: String?, value: Int) {
        if (mPref != null) {
            mPref?.apply {
                val editor = this.edit()
                editor.putInt(key, value)
//            editor.commit()
                editor.apply()
            }
        } else throw RuntimeException("First Initialize context")
    }

    fun putBoolean(key: String?, value: Boolean) {
        if (mPref != null) {
            mPref?.apply {
                val editor = this.edit()
                editor.putBoolean(key, value)
                editor.apply()
            }
        } else throw RuntimeException("First Initialize context")
    }

    @Suppress("unused")
    fun getBoolean(key: String?): Boolean {
        return if (mPref != null) {
            mPref?.getBoolean(key, false) ?: false
        } else throw RuntimeException("First Initialize context")
    }

    fun getBoolean(key: String?, def: Boolean): Boolean {
        return if (mPref != null) {
            mPref?.getBoolean(key, def) ?: false
        } else throw RuntimeException("First Initialize context")
    }

    fun getString(key: String?): String? {
        return if (mPref != null) {
            mPref?.getString(key, "")
        } else throw RuntimeException("First Initialize context")
    }

    fun getString(key: String?, def: String?): String? {
        return if (mPref != null) {
            mPref?.getString(key, def)
        } else throw RuntimeException("First Initialize context")
    }

    @Suppress("unused")
    fun getLong(key: String?): Long {
        return if (mPref != null) {
            mPref?.getLong(key, 0) ?: 0
        } else throw RuntimeException("First Initialize context")
    }

    @Suppress("unused")
    fun getLong(key: String?, defInt: Int): Long {
        return if (mPref != null) {
            mPref?.getLong(key, defInt.toLong()) ?: defInt.toLong()
        } else throw RuntimeException("First Initialize context")
    }

    @Suppress("unused")
    fun getInt(key: String?): Int {
        return if (mPref != null) {
            mPref?.getInt(key, 0) ?: 0
        } else throw RuntimeException("First Initialize context")
    }

    fun getInt(key: String?, defInt: Int): Int {
        return if (mPref != null) {
            mPref?.getInt(key, defInt) ?: defInt
        } else throw RuntimeException("First Initialize context")
    }

    operator fun contains(key: String?): Boolean {
        return if (mPref != null) {
            mPref?.contains(key) ?: false
        } else throw RuntimeException("First Initialize context")
    }

    fun remove(key: String?) {
        if (mPref != null) {
            mPref?.apply {
                val editor = this.edit()
                editor.remove(key)
                editor.apply()
            }
        } else throw RuntimeException("First Initialize context")
    }

    fun clear() {
        if (mPref != null) {
            mPref?.apply {
                val editor = this.edit()
                editor.clear()
//            editor.commit()
                editor.apply()
            }
        } else throw RuntimeException("First Initialize context")
    }

    //stub
    fun loadSharedPreferencesFromFile(inputS: InputStream?): Boolean {
        var res = false
        var input: ObjectInputStream? = null
        try {
            input = ObjectInputStream(inputS)
            clear()
            val entries = input.readObject() as Map<String, *>
            for ((key, value) in entries) {
                when (value) {
                    is Boolean -> putBoolean(key, value)
                    is Int -> putInt(
                        key, value
                    )
                    is Long -> putLong(key, value)
                    is String -> putString(key, value)
                }
            }
            res = true
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        } finally {
            try {
                input?.close()
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
        }
        return res
    }

    val all: Map<String, *>
        get() = mPref!!.all
}
