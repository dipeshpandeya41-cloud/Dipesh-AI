package com.dipesh.ai

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Encrypts the Gemini credential with a non-exportable Android Keystore AES key.
 * Only ciphertext and the GCM IV are stored in app preferences.
 */
class SecureKeyStore(context: Context) {
    private val prefs = context.getSharedPreferences("dipesh_secure_secrets", Context.MODE_PRIVATE)

    fun save(value: String) {
        if (value.isBlank()) {
            clear()
            return
        }
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val encrypted = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
        prefs.edit()
            .putString(KEY_CIPHERTEXT, Base64.encodeToString(encrypted, Base64.NO_WRAP))
            .putString(KEY_IV, Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
            .commit()
    }

    fun read(): String? = try {
        val ciphertext = prefs.getString(KEY_CIPHERTEXT, null) ?: return null
        val iv = prefs.getString(KEY_IV, null) ?: return null
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(
            Cipher.DECRYPT_MODE,
            getOrCreateKey(),
            GCMParameterSpec(128, Base64.decode(iv, Base64.NO_WRAP))
        )
        String(cipher.doFinal(Base64.decode(ciphertext, Base64.NO_WRAP)), Charsets.UTF_8)
    } catch (_: Exception) {
        null
    }

    fun clear() {
        prefs.edit().remove(KEY_CIPHERTEXT).remove(KEY_IV).commit()
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build()
        )
        return generator.generateKey()
    }

    private companion object {
        const val KEY_ALIAS = "dipesh_ai_gemini_api_key"
        const val KEY_CIPHERTEXT = "gemini_ciphertext"
        const val KEY_IV = "gemini_iv"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
    }
}
