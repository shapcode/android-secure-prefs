package com.shapcode.prefs

import android.content.Context
import android.content.SharedPreferences

/**
 * SecurePreferences implements the SharedPreferences interface while adding encryption to the keys and values.
 */
class SecurePreferences(context: Context, name: String) : SharedPreferences {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE)
    private val secretKey = KeyStoreUtil.getOrCreateKey(name)
    private var keys = loadAllKeys()

    private fun loadAllKeys(): MutableMap<String, String> {
        val keys = mutableMapOf<String, String>()
        sharedPreferences.all.forEach { entry ->
            keys[decrypt(entry.key)] = entry.key
        }
        return keys
    }

    override fun getAll(): Map<String, String> {
        val encryptedMap = sharedPreferences.all
        val decryptedMap = mutableMapOf<String, String>()
        for (entry in encryptedMap.entries) {
            try {
                decryptedMap[decrypt(entry.key)] = decrypt(entry.value.toString())
            } catch (e: Exception) {
                // Ignore unencrypted key/value pairs
            }
        }
        return decryptedMap
    }

    override fun getString(key: String, defaultValue: String?): String? {
        sharedPreferences.getString(keys[key], null)?.also { encryptedValue ->
            return decrypt(encryptedValue)
        }
        return defaultValue
    }

    override fun getStringSet(key: String, defaultValues: Set<String>?): Set<String>? {
        val encryptedSet = sharedPreferences.getStringSet(keys[key], null) ?: return defaultValues
        val decryptedSet = mutableSetOf<String>()
        for (encryptedValue in encryptedSet) {
            decrypt(encryptedValue).let { decryptedValue ->
                decryptedSet.add(decryptedValue)
            }
        }
        return decryptedSet
    }

    override fun getInt(key: String, defaultValue: Int): Int {
        sharedPreferences.getString(keys[key], null)?.let { encryptedValue ->
            try {
                val decryptedValue = decrypt(encryptedValue)
                return Integer.parseInt(decryptedValue)
            } catch (e: NumberFormatException) {
                throw ClassCastException(e.message)
            }
        }
        return defaultValue
    }

    override fun getLong(key: String, defaultValue: Long): Long {
        sharedPreferences.getString(keys[key], null)?.let { encryptedValue ->
            try {
                val decryptedValue = decrypt(encryptedValue)
                return java.lang.Long.parseLong(decryptedValue)
            } catch (e: NumberFormatException) {
                throw ClassCastException(e.message)
            }
        }
        return defaultValue
    }

    override fun getFloat(key: String, defaultValue: Float): Float {
        sharedPreferences.getString(keys[key], null)?.let { encryptedValue ->
            try {
                val decryptedValue = decrypt(encryptedValue)
                return java.lang.Float.parseFloat(decryptedValue)
            } catch (e: NumberFormatException) {
                throw ClassCastException(e.message)
            }
        }
        return defaultValue
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        sharedPreferences.getString(keys[key], null)?.let { encryptedValue ->
            try {
                val decryptedValue = decrypt(encryptedValue)
                return java.lang.Boolean.parseBoolean(decryptedValue)
            } catch (e: NumberFormatException) {
                throw ClassCastException(e.message)
            }
        }
        return defaultValue
    }

    override fun contains(key: String): Boolean {
        return sharedPreferences.contains(keys[key])
    }

    override fun edit(): SharedPreferences.Editor {
        return SecureEditor(keys, sharedPreferences.edit())
    }

    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
    }

    private fun decrypt(value: String): String {
        return CryptoUtil.decrypt(value, secretKey)
    }

    private inner class SecureEditor constructor(existingKeys: Map<String, String>, val wrappedEditor: SharedPreferences.Editor) : SharedPreferences.Editor {

        val tempKeys = existingKeys.toMutableMap()

        override fun putString(key: String, value: String): SharedPreferences.Editor {
            if (!tempKeys.containsKey(key)) {
                tempKeys[key] = encrypt(key)
            }
            wrappedEditor.putString(tempKeys[key], encrypt(value))
            return this
        }

        override fun putStringSet(key: String, values: Set<String>): SharedPreferences.Editor {

            val encryptedValues = mutableSetOf<String>()
            for (value in values) {
                encryptedValues.add(encrypt(value))
            }

            if (!tempKeys.containsKey(key)) {
                tempKeys[key] = encrypt(key)
            }
            wrappedEditor.putStringSet(tempKeys[key], encryptedValues)
            return this
        }

        override fun putInt(key: String, value: Int): SharedPreferences.Editor {
            if (!tempKeys.containsKey(key)) {
                tempKeys[key] = encrypt(key)
            }
            wrappedEditor.putString(tempKeys[key], encrypt(value.toString()))
            return this
        }

        override fun putLong(key: String, value: Long): SharedPreferences.Editor {
            if (!tempKeys.containsKey(key)) {
                tempKeys[key] = encrypt(key)
            }
            wrappedEditor.putString(tempKeys[key], encrypt(value.toString()))
            return this
        }

        override fun putFloat(key: String, value: Float): SharedPreferences.Editor {
            if (!tempKeys.containsKey(key)) {
                tempKeys[key] = encrypt(key)
            }
            wrappedEditor.putString(tempKeys[key], encrypt(value.toString()))
            return this
        }

        override fun putBoolean(key: String, value: Boolean): SharedPreferences.Editor {
            if (!tempKeys.containsKey(key)) {
                tempKeys[key] = encrypt(key)
            }
            wrappedEditor.putString(tempKeys[key], encrypt(value.toString()))
            return this
        }

        override fun remove(key: String): SharedPreferences.Editor {
            if (tempKeys.containsKey(key)) {
                wrappedEditor.remove(tempKeys[key])
            }
            return this
        }

        override fun clear(): SharedPreferences.Editor {
            wrappedEditor.clear()
            return this
        }

        override fun commit(): Boolean {
            if (wrappedEditor.commit()) {
                keys = tempKeys
                return true
            }
            return false
        }

        override fun apply() {
            keys = tempKeys
            wrappedEditor.apply()
        }

        private fun encrypt(value: String): String {
            return CryptoUtil.encrypt(value, secretKey)
        }

    }
}
