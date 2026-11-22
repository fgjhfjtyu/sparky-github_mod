package com.jackby03.sparky

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.system.exitProcess

/**
 * Catches any uncaught exception, stores it in files/last_crash.txt and shows it on screen
 * (CrashActivity, separate process) so the report can be copied or shared.
 */
object CrashHandler {

    const val EXTRA_TRACE = "trace"
    private const val FILE_NAME = "last_crash.txt"
    private const val MAX_CHARS = 60_000

    fun install(context: Context) {
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val report = buildReport(thread, throwable)
                runCatching { File(context.filesDir, FILE_NAME).writeText(report) }
                val intent = Intent(context, CrashActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    putExtra(EXTRA_TRACE, report.take(MAX_CHARS))
                }
                context.startActivity(intent)
            } catch (_: Throwable) {
                // Never let the reporter itself throw.
            }
            Process.killProcess(Process.myPid())
            exitProcess(10)
        }
    }

    private fun buildReport(thread: Thread, throwable: Throwable): String {
        val stack = StringWriter().also { throwable.printStackTrace(PrintWriter(it)) }.toString()
        val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        return buildString {
            appendLine("Sparky crash report")
            appendLine("Time:   $time")
            appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
            appendLine("Android: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
            appendLine("Thread: ${thread.name}")
            appendLine()
            append(stack)
        }
    }
}
