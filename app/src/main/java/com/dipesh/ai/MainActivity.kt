package com.dipesh.ai

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
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
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlin.concurrent.thread

class MainActivity : Activity(), TextToSpeech.OnInitListener {
    private lateinit var chat: TextView
    private lateinit var input: EditText
    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var receiverRegistered = false
    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val bytes = intent?.getByteArrayExtra("image")
            if (bytes != null) {
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                if (bitmap != null) analyzeImage(bitmap, "इस स्क्रीनशॉट में क्या दिखाई दे रहा है? निजी जानकारी को दोहराने से बचें और उपयोगकर्ता के प्रश्न का उत्तर दें।")
                else toast("स्क्रीनशॉट पढ़ा नहीं जा सका।")
            } else toast(intent?.getStringExtra("error") ?: "स्क्रीन कैप्चर असफल रहा।")
        }
    }
    private val prefs by lazy { getSharedPreferences("dipesh_ai", MODE_PRIVATE) }
    private val secureKeys by lazy { SecureKeyStore(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this, this)
        val legacyKey = prefs.getString("key", "").orEmpty()
        if (legacyKey.isNotBlank() && secureKeys.read().isNullOrBlank()) secureKeys.save(legacyKey)
        prefs.edit().remove("key").apply()
        buildScreen()
        chat.text = prefs.getString("history", chat.text.toString())
        val filter = IntentFilter(ScreenCaptureService.ACTION_RESULT)
        if (Build.VERSION.SDK_INT >= 33) registerReceiver(screenReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        else @Suppress("DEPRECATION") registerReceiver(screenReceiver, filter)
        receiverRegistered = true
    }

    private fun buildScreen() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 20, 16, 12)
            setBackgroundColor(0xFF101827.toInt())
        }
        root.addView(TextView(this).apply {
            text = "DIPESH AI"
            textSize = 27f
            gravity = Gravity.CENTER
            setTextColor(0xFF8BC5FF.toInt())
        }, matchWidth())
        root.addView(TextView(this).apply {
            text = "हिंदी AI • Voice • Camera • Phone Tools"
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(0xFFD6E2F0.toInt())
            setPadding(0, 5, 0, 10)
        }, matchWidth())

        val settingsRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        settingsRow.addView(Button(this).apply {
            text = "⚙ AI/API सेटिंग"
            setOnClickListener { showSettings() }
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        settingsRow.addView(Button(this).apply {
            text = "🔊 जवाब सुनें"
            setOnClickListener {
                val latest = chat.text.toString().substringAfterLast("Dipesh AI: ", "")
                if (latest.isNotBlank()) speak(latest)
            }
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        root.addView(settingsRow, matchWidth())

        val tools = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        tools.addView(Button(this).apply {
            text = "📷 कैमरा"
            setOnClickListener { openCamera() }
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        tools.addView(Button(this).apply {
            text = "🖼 फोटो चुनें"
            setOnClickListener { choosePhoto() }
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        tools.addView(Button(this).apply {
            text = "📁 फ़ाइल"
            setOnClickListener { openTextFile() }
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        root.addView(tools, matchWidth())

        val phoneTools = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        phoneTools.addView(Button(this).apply {
            text = "⚙ फोन सेटिंग"
            setOnClickListener { launchSafely(Intent(android.provider.Settings.ACTION_SETTINGS)) }
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        phoneTools.addView(Button(this).apply {
            text = "📞 डायलर"
            setOnClickListener { launchSafely(Intent(Intent.ACTION_DIAL)) }
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        phoneTools.addView(Button(this).apply {
            text = "🖥 स्क्रीन समझें"
            setOnClickListener { requestScreenCapture() }
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        phoneTools.addView(Button(this).apply {
            text = "🧹 चैट साफ़"
            setOnClickListener {
                chat.text = "नमस्ते! मैं Dipesh AI हूँ।"
                saveHistory()
            }
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        phoneTools.addView(Button(this).apply {
            text = "🤖 Automation"
            setOnClickListener { showAutomationControls() }
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        phoneTools.addView(Button(this).apply {
            text = "⌨ Termux"
            setOnClickListener { openTermux() }
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        root.addView(phoneTools, matchWidth())

        val scroll = ScrollView(this)
        chat = TextView(this).apply {
            textSize = 16f
            setTextColor(0xFFF2F5FA.toInt())
            setPadding(12, 12, 12, 12)
            text = "नमस्ते! मैं Dipesh AI हूँ।\nमाइक से बोलें, संदेश लिखें या कैमरा/गैलरी से फोटो चुनकर उसके बारे में पूछें।\n\nGemini API key जोड़ने पर ऑनलाइन चैट और फोटो पहचान काम करेगी।"
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
            setPadding(20, 8, 20, 4)
        }
        val model = EditText(this).apply {
            hint = "Gemini model"
            setSingleLine(true)
            setText(prefs.getString("model", "gemini-2.5-flash"))
        }
        val key = EditText(this).apply {
            hint = "अपनी Gemini API key डालें"
            setSingleLine(true)
            setText(secureKeys.read().orEmpty())
        }
        panel.addView(TextView(this).apply {
            text = "Free Tier के लिए Google AI Studio से अपनी API key बनाएँ। यह ऐप में आपके फोन पर सेव होगी। Free quota सीमित है; Google इसकी शर्तें बदल सकता है।"
            textSize = 13f
        })
        val speechLanguage = EditText(this).apply {
            hint = "Voice / speech language (e.g. hi-IN, en-US)"
            setSingleLine(true)
            setText(prefs.getString("speech_language", "hi-IN"))
        }
        panel.addView(model)
        panel.addView(key)
        panel.addView(speechLanguage)
        panel.addView(Button(this).apply {
            text = "अगली installed voice चुनें"
            setOnClickListener { cycleVoice() }
        })
        panel.addView(Button(this).apply {
            text = "Google AI Studio खोलें"
            setOnClickListener { launchSafely(Intent(Intent.ACTION_VIEW, Uri.parse("https://aistudio.google.com/apikey"))) }
        })
        AlertDialog.Builder(this)
            .setTitle("Gemini AI सेटिंग")
            .setView(panel)
            .setNegativeButton("रद्द", null)
            .setPositiveButton("सेव") { _, _ ->
                prefs.edit().putString("model", model.text.toString().trim())
                    .putString("speech_language", speechLanguage.text.toString().trim().ifBlank { "hi-IN" }).apply()
                secureKeys.save(key.text.toString().trim())
                tts?.language = Locale.forLanguageTag(speechLanguage.text.toString().trim().ifBlank { "hi-IN" })
                Toast.makeText(this@MainActivity, "सेटिंग सेव हो गई।", Toast.LENGTH_SHORT).show()
            }.show()
    }

    private fun showAutomationControls() {
        val choices = arrayOf(
            "Accessibility अनुमति चालू करें",
            "Back दबाएँ",
            "Home दबाएँ",
            "Notifications खोलें",
            "Quick Settings खोलें"
        )
        AlertDialog.Builder(this)
            .setTitle("Phone Automation")
            .setMessage("यह सेवा केवल तुम्हारे चुने हुए सिस्टम शॉर्टकट चलाती है। यह अपने आप स्क्रीन नहीं पढ़ती, टैप नहीं करती और कोई संदेश नहीं भेजती।")
            .setItems(choices) { _, which ->
                if (which == 0) {
                    launchSafely(Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS))
                } else {
                    val action = when (which) {
                        1 -> "back"
                        2 -> "home"
                        3 -> "notifications"
                        else -> "quick_settings"
                    }
                    if (!DipeshAccessibilityService.runUserAction(action)) {
                        toast("पहले Accessibility अनुमति चालू करें।")
                    }
                }
            }
            .setNegativeButton("बंद", null)
            .show()
    }

    private fun openTermux() {
        val launch = packageManager.getLaunchIntentForPackage("com.termux")
        if (launch != null) launchSafely(launch)
        else toast("Termux install नहीं है। पहले F-Droid से Termux install करें।")
    }

    private fun openTextFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/*"
        }
        try { startActivityForResult(intent, 46) }
        catch (_: Exception) { toast("File picker उपलब्ध नहीं है।") }
    }

    private fun cycleVoice() {
        val voices = tts?.voices?.sortedBy { it.name }.orEmpty()
        if (voices.isEmpty()) {
            toast("इस फोन में कोई TTS voice उपलब्ध नहीं है।")
            return
        }
        val current = prefs.getString("tts_voice_name", null)
        val nextIndex = ((voices.indexOfFirst { it.name == current } + 1) % voices.size)
        val next = voices[nextIndex]
        tts?.voice = next
        prefs.edit().putString("tts_voice_name", next.name).apply()
        toast("Voice: " + next.name + " • " + next.locale.displayName)
    }

    private fun requestScreenCapture() {
        try {
            val manager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            startActivityForResult(manager.createScreenCaptureIntent(), 45)
        } catch (_: Exception) { toast("इस Android डिवाइस पर स्क्रीन कैप्चर उपलब्ध नहीं है।") }
    }

    private fun startVoiceInput() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 41)
            return
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            val language = prefs.getString("speech_language", "hi-IN").orEmpty()
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "बोलें / Speak")
        }
        try { startActivityForResult(intent, 42) }
        catch (_: Exception) { toast("इस डिवाइस पर Speech Recognition उपलब्ध नहीं है।") }
    }

    private fun openCamera() {
        try { startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE), 43) }
        catch (_: Exception) { toast("कैमरा ऐप उपलब्ध नहीं है।") }
    }

    private fun choosePhoto() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "image/*" }
        startActivityForResult(Intent.createChooser(intent, "फोटो चुनें"), 44)
    }

    @Deprecated("Platform activity result API")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK || data == null) return
        when (requestCode) {
            45 -> {
                val serviceIntent = Intent(this, ScreenCaptureService::class.java)
                    .putExtra("resultCode", resultCode).putExtra("data", data)
                try {
                    if (Build.VERSION.SDK_INT >= 26) startForegroundService(serviceIntent)
                    else startService(serviceIntent)
                    toast("स्क्रीन कैप्चर हो रहा है…")
                } catch (_: Exception) { toast("स्क्रीन कैप्चर सेवा शुरू नहीं हुई।") }
            }
            42 -> {
                val spoken = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
                if (!spoken.isNullOrBlank()) { input.setText(spoken); input.setSelection(input.text.length) }
            }
            43 -> {
                val bitmap = data.extras?.get("data") as? Bitmap
                if (bitmap != null) analyzeImage(bitmap, "कैमरे से ली गई फोटो का वर्णन करें।")
                else toast("कैमरा फोटो नहीं मिली।")
            }
            44 -> {
                val uri = data.data
                if (uri != null) {
                    try {
                        val bitmap = contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
                        if (bitmap != null) analyzeImage(bitmap, "इस फोटो में क्या दिखाई दे रहा है? विस्तार से बताइए।")
                        else toast("फोटो पढ़ी नहीं जा सकी।")
                    } catch (_: Exception) { toast("फोटो खोलने में समस्या हुई।") }
                }
            }
            46 -> {
                val uri = data.data
                if (uri != null) {
                    try { contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
                    catch (_: Exception) { }
                    try {
                        val contents = contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                        if (contents.isNullOrBlank()) toast("फ़ाइल खाली है या पढ़ी नहीं जा सकती।")
                        else {
                            input.setText(contents.take(50000))
                            input.setSelection(input.text.length)
                            toast("फ़ाइल पढ़ी गई। भेजने से पहले सामग्री जाँच लें।")
                        }
                    } catch (_: Exception) { toast("सिर्फ़ readable text/CSV फ़ाइलें अभी समर्थित हैं।") }
                }
            }
        }
    }

    private fun analyzeImage(bitmap: Bitmap, prompt: String) {
        val key = secureKeys.read().orEmpty()
        if (key.isBlank()) { toast("पहले AI सेटिंग में अपनी Gemini API key डालें।"); return }
        chat.append("\n\nआप: [फोटो/स्क्रीन भेजी गई]\nDipesh AI: विश्लेषण कर रहा हूँ…"); saveHistory()
        thread {
            val answer = try { requestGemini(prompt, key, bitmap) }
            catch (e: Exception) { "फोटो समझने में समस्या: " + (e.message ?: "network error") }
            runOnUiThread { showAnswer(answer) }
        }
    }

    private fun sendMessage() {
        val message = input.text.toString().trim()
        if (message.isEmpty()) return
        chat.append("\n\nआप: " + message + "\nDipesh AI: जवाब तैयार हो रहा है…"); saveHistory()
        input.text.clear()
        val key = prefs.getString("key", "").orEmpty()
        if (key.isBlank()) { showAnswer(offlineReply(message)); return }
        thread {
            val answer = try { requestGemini(message, key, null) }
            catch (e: Exception) {
                "ऑनलाइन AI से कनेक्शन नहीं हो पाया (" + (e.message ?: "network error") + ")।\nऑफलाइन मदद: " + offlineReply(message)
            }
            runOnUiThread { showAnswer(answer) }
        }
    }

    private fun requestGemini(prompt: String, key: String, bitmap: Bitmap?): String {
        val model = prefs.getString("model", "gemini-2.5-flash").orEmpty()
        require(model.matches(Regex("[A-Za-z0-9._-]+"))) { "Model name सही नहीं है" }
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/" +
            model + ":generateContent"
        val parts = JSONArray().put(JSONObject().put("text", prompt))
        if (bitmap != null) {
            val scaled = scaleBitmap(bitmap)
            val bytes = ByteArrayOutputStream().use { out ->
                scaled.compress(Bitmap.CompressFormat.JPEG, 82, out)
                out.toByteArray()
            }
            val encoded = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
            parts.put(JSONObject().put("inline_data", JSONObject()
                .put("mime_type", "image/jpeg").put("data", encoded)))
        }
        val body = JSONObject().put("contents", JSONArray().put(JSONObject().put("parts", parts)))
            .put("generationConfig", JSONObject().put("temperature", 0.6).put("maxOutputTokens", 1200))
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 20000
            readTimeout = 45000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("x-goog-api-key", key)
        }
        try {
            connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val response = stream.bufferedReader().use { it.readText() }
            if (code !in 200..299) throw IllegalStateException("Gemini API error " + code + ": " + response.take(240))
            val candidates = JSONObject(response).getJSONArray("candidates")
            val contentParts = candidates.getJSONObject(0).getJSONObject("content").getJSONArray("parts")
            return (0 until contentParts.length()).mapNotNull { i ->
                contentParts.getJSONObject(i).optString("text").takeIf { it.isNotBlank() }
            }.joinToString("\n").ifBlank { "AI से खाली जवाब मिला।" }
        } finally { connection.disconnect() }
    }

    private fun scaleBitmap(source: Bitmap): Bitmap {
        val maxSide = 1024
        val scale = minOf(1f, maxSide.toFloat() / maxOf(source.width, source.height))
        return if (scale < 1f) Bitmap.createScaledBitmap(source,
            (source.width * scale).toInt().coerceAtLeast(1),
            (source.height * scale).toInt().coerceAtLeast(1), true) else source
    }

    private fun offlineReply(message: String): String {
        val q = message.lowercase(Locale.ROOT)
        return when {
            listOf("नमस्ते", "हेलो", "hello", "hi").any { q.contains(it) } ->
                "नमस्ते! मैं Dipesh AI हूँ। आप मुझसे सवाल पूछ सकते हैं।"
            q.contains("नाम") || q.contains("who are you") ->
                "मैं Dipesh AI हूँ, आपका हिंदी Android Assistant।"
            q.contains("मदद") || q.contains("help") ->
                "मैं ऑफलाइन बेसिक जवाब दे सकता हूँ। पूरी AI चैट और फोटो पहचान के लिए Gemini API key जोड़ें।"
            else -> "अभी API key उपलब्ध नहीं है। AI सेटिंग खोलकर अपनी Gemini key जोड़ें; बेसिक ऑफलाइन सहायता चालू है।"
        }
    }

    private fun launchSafely(intent: Intent) {
        try { startActivity(intent) } catch (_: Exception) { toast("यह सुविधा इस फोन पर उपलब्ध नहीं है।") }
    }

    private fun saveHistory() {
        prefs.edit().putString("history", chat.text.toString().takeLast(18000)).apply()
    }
    private fun showAnswer(answer: String) { chat.append("\n" + answer); saveHistory() }
    private fun speak(text: String) {
        if (ttsReady) tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "dipesh-ai-answer")
        else toast("Voice तैयार हो रही है।")
    }
    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            ttsReady = true
            val language = prefs.getString("speech_language", "hi-IN").orEmpty()
            tts?.language = Locale.forLanguageTag(language)
            val savedVoice = prefs.getString("tts_voice_name", null)
            if (savedVoice != null) tts?.voices?.firstOrNull { it.name == savedVoice }?.let { tts?.voice = it }
        }
    }
    override fun onDestroy() {
        if (receiverRegistered) try { unregisterReceiver(screenReceiver) } catch (_: Exception) {}
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
    private fun matchWidth() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
    )
}
