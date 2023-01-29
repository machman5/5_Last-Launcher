package com.launcher.utils

import android.content.Context
import android.text.TextUtils
import android.util.Log
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

//ONLY included in DEBUG BUILD:
class CrashUtils(
    context: Context,
    crashReportSavePath: String
) : Thread.UncaughtExceptionHandler {

    companion object {
        private const val EXCEPTION_SUFFIX = "_exception"
        private const val CRASH_SUFFIX = "_crash"
        private const val FILE_EXTENSION = ".txt"
        private const val CRASH_REPORT_DIR = "crashReports"
        private const val TAG = "CrashUtils"
        private fun getStackTrace(e: Throwable): String {
            val result: Writer = StringWriter()
            val printWriter = PrintWriter(result)
            e.printStackTrace(printWriter)
            val crashLog = result.toString()
            printWriter.close()
            return crashLog
        }
    }

    private val exceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
    private val applicationContext: Context = context
    private val crashReportPath = crashReportSavePath

    init {
        if (Thread.getDefaultUncaughtExceptionHandler() !is CrashUtils) {
            Thread.setDefaultUncaughtExceptionHandler(this)
        }
    }

    override fun uncaughtException(
        thread: Thread,
        throwable: Throwable
    ) {
        saveCrashReport(throwable)
        exceptionHandler?.uncaughtException(thread, throwable)
    }

    private fun saveCrashReport(throwable: Throwable) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val filename = dateFormat.format(Date()) + CRASH_SUFFIX + FILE_EXTENSION
        writeToFile(crashReportPath, filename, getStackTrace(throwable))
    }

    private fun writeToFile(
        crashReportPath: String,
        filename: String,
        crashLog: String
    ) {
        var crashReportPath = crashReportPath
        if (TextUtils.isEmpty(crashReportPath)) {
            crashReportPath = defaultPath
        }
        val crashDir = File(crashReportPath)
        if (!crashDir.exists() || !crashDir.isDirectory) {
            crashReportPath = defaultPath
            Log.e(
                TAG, """
     Path provided doesn't exists : $crashDir
     Saving crash report at : $defaultPath
     """.trimIndent()
            )
        }
        val bufferedWriter: BufferedWriter
        try {
            bufferedWriter = BufferedWriter(
                FileWriter(
                    crashReportPath + File.separator + filename
                )
            )
            bufferedWriter.write(crashLog)
            bufferedWriter.flush()
            bufferedWriter.close()
            Log.d(TAG, "crash report saved in : $crashReportPath")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val defaultPath: String
        get() {
            val defaultPath = (applicationContext.getExternalFilesDir(null)!!.absolutePath
                    + File.separator + CRASH_REPORT_DIR)
            val file = File(defaultPath)
            file.mkdirs()
            return defaultPath
        }

    @Suppress("unused")
    fun logException(exception: Exception) {
        Thread {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val filename = dateFormat.format(Date()) + EXCEPTION_SUFFIX + FILE_EXTENSION
            writeToFile(crashReportPath, filename, getStackTrace(exception))
        }.start()
    }
}
