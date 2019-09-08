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

    /**
     * Retrieve all decrypted values from the preferences. Any values that cannot be decrypted are not returned.
     */
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

    /**
     * Retrieves a decrypted String value from the preferences.
     *
     * @param key The name of the preference to retrieve
     * @param defaultValue Value to return if this preference does not exist.
     *
     * @return Returns the decrypted preference value if it exists, or defaultValue.
     *
     * @see android.content.SharedPreferences.getString
     */
    override fun getString(key: String, defaultValue: String?): String? {
        sharedPreferences.getString(keys[key], null)?.also { encryptedValue ->
            return decrypt(encryptedValue)
        }
        return defaultValue
    }

    /**
     * Retrieves a set of decrypted String values from the preferences.
     *
     * @param key The name of the preference to retrieve
     * @param defaultValue Value to return if this preference does not exist.
     *
     * @return Returns the decrypted preference value if it exists, or defaultValue.
     *
     * @see android.content.SharedPreferences.getStringSet
     */
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

    /**
     * Retrieves a decrypted int value from the preferences.
     *
     * @param key The name of the preference to retrieve
     * @param defaultValue Value to return if this preference does not exist.
     *
     * @return Returns the decrypted preference value if it exists, or defaultValue. Throws ClassCastException if there is a preference with this name that is not an int.
     *
     * @see android.content.SharedPreferences.getInt
     */
    override fun getInt(key: String, defaultValue: Int): Int {
        sharedPreferences.getString(keys[key], null)?.let { encryptedValue ->
            try {
                val decryptedValue = decrypt(encryptedValue)
                return decryptedValue.toInt()
            } catch (e: NumberFormatException) {
                throw ClassCastException(e.message)
            }
        }
        return defaultValue
    }

    /**
     * Retrieves a decrypted long value from the preferences.
     *
     * @param key The name of the preference to retrieve
     * @param defaultValue Value to return if this preference does not exist.
     *
     * @return Returns the decrypted preference value if it exists, or defaultValue. Throws ClassCastException if there is a preference with this name that is not a long.
     *
     * @see android.content.SharedPreferences.getLong
     */
    override fun getLong(key: String, defaultValue: Long): Long {
        sharedPreferences.getString(keys[key], null)?.let { encryptedValue ->
            try {
                val decryptedValue = decrypt(encryptedValue)
                return decryptedValue.toLong()
            } catch (e: NumberFormatException) {
                throw ClassCastException(e.message)
            }
        }
        return defaultValue
    }

    /**
     * Retrieves a decrypted float value from the preferences.
     *
     * @param key The name of the preference to retrieve
     * @param defaultValue Value to return if this preference does not exist.
     *
     * @return Returns the decrypted preference value if it exists, or defaultValue. Throws ClassCastException if there is a preference with this name that is not a float.
     *
     * @see android.content.SharedPreferences.getFloat
     */
    override fun getFloat(key: String, defaultValue: Float): Float {
        sharedPreferences.getString(keys[key], null)?.let { encryptedValue ->
            try {
                val decryptedValue = decrypt(encryptedValue)
                return decryptedValue.toFloat()
            } catch (e: NumberFormatException) {
                throw ClassCastException(e.message)
            }
        }
        return defaultValue
    }

    /**
     * Retrieves a decrypted boolean value from the preferences.
     *
     * @param key The name of the preference to retrieve
     * @param defaultValue Value to return if this preference does not exist.
     *
     * @return Returns the decrypted preference value if it exists, or defaultValue. Throws ClassCastException if there is a preference with this name that is not a boolean.
     *
     * @see android.content.SharedPreferences.getLong
     */
    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        sharedPreferences.getString(keys[key], null)?.let { encryptedValue ->
            val decryptedValue = decrypt(encryptedValue)
            return decryptedValue.toBoolean()
        }
        return defaultValue
    }

    /**
     * Checks whether the preferences contains a preference.
     *
     * @param key The name of the preference to check
     *
     * @return Returns true if the preference exists in the preferences, otherwise false.
     *
     * @see android.content.SharedPreferences.contains
     */
    override fun contains(key: String): Boolean {
        return sharedPreferences.contains(keys[key])
    }

    /**
     * Create a new Editor for these preferences, through which you can make modifications to the data in the preferences and atomically commit those changes back to the SharedPreferences object.
     *
     * @return Returns a new instance of the [SharedPreferences.Editor] interface, allowing you to modify the values in this SharedPreferences object.
     *
     * @see SharedPreferences.registerOnSharedPreferenceChangeListener
     */
    override fun edit(): SharedPreferences.Editor {
        return SecureEditor(keys, sharedPreferences.edit())
    }

    /**
     * Registers a callback to be invoked when a change happens to a preference.
     */
    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
    }

    /**
     * Unregisters a previous callback.
     *
     * @param listener The callback that should be unregistered.
     *
     * @see SharedPreferences.unregisterOnSharedPreferenceChangeListener
     */
    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
    }

    private fun decrypt(value: String): String {
        return CryptoUtil.decrypt(value, secretKey)
    }

    private inner class SecureEditor constructor(existingKeys: Map<String, String>, val wrappedEditor: SharedPreferences.Editor) : SharedPreferences.Editor {

        val tempKeys = existingKeys.toMutableMap()

        override fun putString(key: String, value: String?): SharedPreferences.Editor {
            if (!tempKeys.containsKey(key)) {
                tempKeys[key] = encrypt(key)
            }
            if (value != null) {
                wrappedEditor.putString(tempKeys[key], encrypt(value))
            } else {
                wrappedEditor.putString(tempKeys[key], null)
            }
            return this
        }

        override fun putStringSet(key: String, values: Set<String>?): SharedPreferences.Editor {
            if (!tempKeys.containsKey(key)) {
                tempKeys[key] = encrypt(key)
            }

            if (values != null) {
                val encryptedValues = mutableSetOf<String>()
                for (value in values) {
                    encryptedValues.add(encrypt(value))
                }
                wrappedEditor.putStringSet(tempKeys[key], encryptedValues)
            } else {
                wrappedEditor.putStringSet(tempKeys[key], null)
            }
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
