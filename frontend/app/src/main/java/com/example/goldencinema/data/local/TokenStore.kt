package com.example.goldencinema

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import org.json.JSONObject

object TokenStore {
    private const val PREFS_NAME = "gc_secure_prefs"
    private const val KEY_TOKEN = "jwt_token"

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        prefs = try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    fun save(token: String) {
        prefs?.edit()?.putString(KEY_TOKEN, token)?.apply()
    }

    fun get(): String? = prefs?.getString(KEY_TOKEN, null)

    fun clear() {
        prefs?.edit()?.remove(KEY_TOKEN)?.apply()
    }

    fun getUserRole(): String? {
        val token = get() ?: return null
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return null
            val bytes = Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_PADDING)
            JSONObject(String(bytes, Charsets.UTF_8)).optString("role").takeIf { it.isNotEmpty() }
        } catch (e: Exception) {
            null
        }
    }

    fun isTokenValid(): Boolean {
        val token = get() ?: return false
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return false
            val payloadBytes = Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_PADDING)
            val payload = JSONObject(String(payloadBytes, Charsets.UTF_8))
            val exp = payload.getLong("exp")
            exp * 1000L > System.currentTimeMillis()
        } catch (e: Exception) {
            false
        }
    }
}
