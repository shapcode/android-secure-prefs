package com.shapcode.prefs

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties

import java.security.KeyStore

import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

internal object KeyStoreUtil {

    private const val ANDROID_KEYSTORE = "AndroidKeyStore"

    private val androidKeyStore: KeyStore by lazy {
        loadKeyStore()
    }

    private fun loadKeyStore(): KeyStore {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        return keyStore
    }

    @Throws(KeyStoreException::class)
    private fun hasKey(alias: String): Boolean {
        try {
            return androidKeyStore.containsAlias(alias)
        } catch (e: Exception) {
            throw KeyStoreException("Failed to get secret key for: $alias", e)
        }
    }

    @Throws(KeyStoreException::class)
    private fun getKey(alias: String): SecretKey {
        try {
            val secretKeyEntry = androidKeyStore.getEntry(alias, null) as KeyStore.SecretKeyEntry
            return secretKeyEntry.secretKey
        } catch (e: Exception) {
            throw KeyStoreException("Failed to get secret key for alias: $alias", e)
        }
    }

    @Throws(KeyStoreException::class)
    private fun createKey(alias: String, userAuthenticationRequired: Boolean): SecretKey {
        try {
            loadKeyStore()
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            keyGenerator.init(
                KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setUserAuthenticationRequired(userAuthenticationRequired)
                    .build()
            )
            return keyGenerator.generateKey()
        } catch (e: Exception) {
            throw KeyStoreException("Failed to create key for alias: $alias", e)
        }
    }

    @JvmStatic
    @Throws(KeyStoreException::class)
    fun getOrCreateKey(alias: String): SecretKey {
        if (hasKey(alias)) {
            return getKey(alias);
        } else {
            return createKey(alias, false)
        }
    }

    @Throws(KeyStoreException::class)
    fun deleteKey(alias: String) {
        try {
            androidKeyStore.deleteEntry(alias)
        } catch (e: Exception) {
            throw KeyStoreException("Failed to delete key for alias: $alias", e)
        }
    }

    class KeyStoreException(message: String, cause: Throwable) : Exception(message, cause)

}
