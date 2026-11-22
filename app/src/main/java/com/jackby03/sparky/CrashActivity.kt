package com.jackby03.sparky

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

/** Plain-views crash screen (no Compose / Hilt) so it can never fail the same way the app did. */
class CrashActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val trace = intent?.getStringExtra(CrashHandler.EXTRA_TRACE) ?: "No crash details available."
        val density = resources.displayMetrics.density
        fun dp(v: Int) = (v * density).toInt()

        val title = TextView(this).apply {
            text = "Sparky stopped unexpectedly\nبرنامه به‌طور ناگهانی متوقف شد"
            setTextColor(0xFFFF6B35.toInt())
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
        }

        val hint = TextView(this).apply {
            text = "Copy the report below and send it to the developer.\nگزارش زیر را کپی کنید و برای توسعه‌دهنده بفرستید."
            setTextColor(0xFFAAAAAA.toInt())
            textSize = 13f
            setPadding(0, dp(8), 0, dp(12))
        }

        val body = TextView(this).apply {
            text = trace
            setTextIsSelectable(true)
            setTextColor(0xFFCCCCCC.toInt())
            typeface = Typeface.MONOSPACE
            textSize = 11f
        }

        val scroll = ScrollView(this).apply {
            setBackgroundColor(0xFF111111.toInt())
            setPadding(dp(8), dp(8), dp(8), dp(8))
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)
            addView(body)
        }

        fun actionButton(label: String, onClick: () -> Unit) = Button(this).apply {
            text = label
            isAllCaps = false
            setOnClickListener { onClick() }
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(dp(4), dp(12), dp(4), 0)
            }
        }

        val buttons = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            addView(actionButton("Copy / کپی") {
                val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                cm.setPrimaryClip(ClipData.newPlainText("Sparky crash report", trace))
                Toast.makeText(this@CrashActivity, "Copied / کپی شد", Toast.LENGTH_SHORT).show()
            })
            addView(actionButton("Share / ارسال") {
                val send = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, trace)
                }
                startActivity(Intent.createChooser(send, "Sparky crash report"))
            })
            addView(actionButton("Close / بستن") { finishAndRemoveTask() })
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFF0A0A0A.toInt())
            setPadding(dp(16), dp(56), dp(16), dp(24))
            addView(title)
            addView(hint)
            addView(scroll)
            addView(buttons)
        }
        setContentView(root)
    }
}
