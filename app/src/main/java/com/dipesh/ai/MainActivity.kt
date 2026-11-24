package com.dipesh.ai

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlin.concurrent.thread

class MainActivity : Activity(), TextToSpeech.OnInitListener {
    private lateinit var chat: TextView
    private lateinit var input: EditText
    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private val prefs by lazy { getSharedPreferences("dipesh_ai", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this, this)
        buildScreen()
    }

    private fun buildScreen() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 24, 20, 16)
            setBackgroundColor(0xFF101827.toInt())
        }
        root.addView(TextView(this).apply {
            text = "DIPESH AI"
            textSize = 27f
            gravity = Gravity.CENTER
            setTextColor(0xFF8BC5FF.toInt())
        }, matchWidth())
        root.addView(TextView(this).apply {
            text = "आपका हिंदी AI Assistant"
            textSize = 15f
            gravity = Gravity.CENTER
            setTextColor(0xFFD6E2F0.toInt())
            setPadding(0, 6, 0, 12)
        }, matchWidth())

        val top = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        top.addView(Button(this).apply {
            text = "⚙ AI सेटिंग"
            setOnClickListener { showSettings() }
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        top.addView(Button(this).apply {
            text = "🗣 जवाब सुनें"
            setOnClickListener {
                val latest = chat.text.toString().substringAfterLast("Dipesh AI: ", "")
                if (latest.isNotBlank()) speak(latest)
            }
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        root.addView(top, matchWidth())

        val scroll = ScrollView(this)
        chat = TextView(this).apply {
            textSize = 16f
            setTextColor(0xFFF2F5FA.toInt())
            setPadding(14, 14, 14, 14)
            text = "नमस्ते! मैं Dipesh AI हूँ।\nमाइक दबाकर बोलें या संदेश लिखें।\n\nऑनलाइन AI के लिए ⚙ AI सेटिंग में अपनी API key जोड़ें। बिना key के बेसिक ऑफलाइन जवाब उपलब्ध हैं।"
        }
        scroll.addView(chat)
        root.addView(scroll, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))

        input = EditText(this).apply {
            hint = "अपना संदेश लिखें..."
            setHintTextColor(0xFF9AA9BC.toInt())
            setTextColor(0xFFFFFFFF.toInt())
            setPadding(12, 8, 12, 8)
        }
        root.addView(input, matchWidth())

        val actions = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
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

    private fun showSettings() {
        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 8, 24, 4)
        }
        val endpoint = EditText(this).apply {
            hint = "API endpoint"
            setSingleLine(true)
            setText(prefs.getString("endpoint", "https://openrouter.ai/api/v1/chat/completions"))
        }
        val model = EditText(this).apply {
            hint = "Model name"
            setSingleLine(true)
            setText(prefs.getString("model", "openai/gpt-4o-mini"))
        }
        val key = EditText(this).apply {
            hint = "API key (आपकी निजी key)"
            setSingleLine(true)
            setText(prefs.getString("key", ""))
        }
        panel.addView(TextView(this).apply {
            text = "OpenAI-compatible API की जानकारी डालें। API key फोन में स्थानीय रूप से सेव होगी। इसे किसी के साथ साझा न करें।"
            textSize = 13f
        })
        panel.addView(endpoint)
        panel.addView(model)
        panel.addView(key)
        AlertDialog.Builder(this)
            .setTitle("AI कनेक्शन सेटिंग")
            .setView(panel)
            .setNegativeButton("रद्द", null)
            .setPositiveButton("सेव") { _, _ ->
                prefs.edit()
                    .putString("endpoint", endpoint.text.toString().trim())
                    .putString("model", model.text.toString().trim())
                    .putString("key", key.text.toString().trim())
                    .apply()
                Toast.makeText(this, "AI सेटिंग सेव हो गई।", Toast.LENGTH_SHORT).show()
            }.show()
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

    @Deprecated("Platform speech-recognition activity")
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
        chat.append("\n\nआप: " + message + "\nDipesh AI: जवाब तैयार हो रहा है…")
        input.text.clear()
        val key = prefs.getString("key", "").orEmpty()
        if (key.isBlank()) {
            showAnswer(offlineReply(message))
            return
        }
        thread {
            val answer = try {
                requestCloudAnswer(message, key)
            } catch (e: Exception) {
                "ऑनलाइन AI से कनेक्शन नहीं हो पाया (" + (e.message ?: "network error") + ")। इंटरनेट/API सेटिंग जाँचें।\n\nऑफलाइन मदद: " + offlineReply(message)
            }
            runOnUiThread { showAnswer(answer) }
        }
    }

    private fun requestCloudAnswer(message: String, key: String): String {
        val endpoint = prefs.getString("endpoint", "").orEmpty()
        val model = prefs.getString("model", "").orEmpty()
        require(endpoint.startsWith("https://")) { "HTTPS endpoint आवश्यक है" }
        require(model.isNotBlank()) { "Model name खाली है" }
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 20000
            readTimeout = 30000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Authorization", "Bearer " + key)
        }
        try {
            val body = JSONObject()
                .put("model", model)
                .put("messages", JSONArray()
                    .put(JSONObject().put("role", "system").put("content", "You are Dipesh AI, a helpful assistant. Reply in Hindi unless the user uses another language. Be accurate and concise."))
                    .put(JSONObject().put("role", "user").put("content", message)))
                .put("temperature", 0.7)
            connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val response = stream.bufferedReader().use { it.readText() }
            if (code !in 200..299) throw IllegalStateException("API error " + code)
            return JSONObject(response).getJSONArray("choices")
                .getJSONObject(0).getJSONObject("message").getString("content").trim()
        } finally {
            connection.disconnect()
        }
    }

    private fun offlineReply(message: String): String {
        val q = message.lowercase(Locale.ROOT)
        return when {
            listOf("नमस्ते", "हेलो", "hello", "hi").any { q.contains(it) } ->
                "नमस्ते! मैं Dipesh AI हूँ। आप मुझसे सवाल पूछ सकते हैं।"
            q.contains("समय") || q.contains("time") ->
                "फोन के स्टेटस बार में वर्तमान स्थानीय समय देखें।"
            q.contains("नाम") || q.contains("who are you") ->
                "मैं Dipesh AI हूँ, आपका हिंदी Android Assistant।"
            q.contains("मदद") || q.contains("help") ->
                "मैं अभी बेसिक ऑफलाइन जवाब दे सकता हूँ। अधिक सवालों के लिए AI सेटिंग में HTTPS API endpoint, model और अपनी API key सेव करें।"
            else ->
                "अभी इंटरनेट/API key उपलब्ध नहीं है, इसलिए मैं इस सवाल का पूरा AI उत्तर नहीं दे सकता। ⚙ AI सेटिंग में API key जोड़ें या बाद में फिर कोशिश करें।"
        }
    }

    private fun showAnswer(answer: String) {
        chat.append("\n" + answer)
    }

    private fun speak(text: String) {
        if (ttsReady) tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "dipesh-ai-answer")
        else Toast.makeText(this, "Voice तैयार हो रही है।", Toast.LENGTH_SHORT).show()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            ttsReady = true
            tts?.language = Locale("hi", "IN")
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }

    private fun matchWidth() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
    )
}
