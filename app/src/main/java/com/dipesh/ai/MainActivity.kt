package com.dipesh.ai

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.util.Locale

class MainActivity : Activity() {
    private lateinit var chat: TextView
    private lateinit var input: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 32, 24, 24)
            setBackgroundColor(0xFFF4F6FB.toInt())
        }
        root.addView(TextView(this).apply {
            text = "DIPESH AI"
            textSize = 28f
            gravity = Gravity.CENTER
            setTextColor(0xFF173B72.toInt())
        }, matchWidth())
        root.addView(TextView(this).apply {
            text = "आपका हिंदी वॉइस असिस्टेंट"
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 20)
        }, matchWidth())

        val scroll = ScrollView(this)
        chat = TextView(this).apply {
            textSize = 17f
            setTextColor(0xFF202532.toInt())
            setPadding(16, 16, 16, 16)
            text = "नमस्ते! मैं Dipesh AI हूँ।\nमाइक दबाकर हिंदी में बोलें या नीचे लिखें।\n\nध्यान दें: यह पहला डेमो है; AI सर्वर अभी कनेक्ट नहीं है।"
        }
        scroll.addView(chat)
        root.addView(scroll, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
        ))

        input = EditText(this).apply { hint = "अपना संदेश लिखें..." }
        root.addView(input, matchWidth())

        val actions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        actions.addView(Button(this).apply {
            text = "🎙 बोलें"
            setOnClickListener { startVoiceInput() }
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        actions.addView(Button(this).apply {
            text = "भेजें"
            setOnClickListener { sendMessage() }
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        root.addView(actions, matchWidth())
        setContentView(root)
    }

    private fun startVoiceInput() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 41)
            return
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "हिंदी में बोलें")
        }
        try {
            startActivityForResult(intent, 42)
        } catch (_: Exception) {
            Toast.makeText(this, "इस डिवाइस पर Speech Recognition उपलब्ध नहीं है।", Toast.LENGTH_LONG).show()
        }
    }

    @Deprecated("Uses the platform speech-recognition activity for broad Android compatibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 42 && resultCode == RESULT_OK) {
            val spoken = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                input.setText(spoken)
                input.setSelection(input.text.length)
            }
        }
    }

    private fun sendMessage() {
        val message = input.text.toString().trim()
        if (message.isEmpty()) return
        chat.append("\n\nआप: $message\nDipesh AI: यह शुरुआती डेमो है। AI जवाब देने की सुविधा अगले चरण में जोड़ी जाएगी।")
        input.text.clear()
    }

    private fun matchWidth() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
    )
}
