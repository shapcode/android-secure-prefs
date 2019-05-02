package com.shapcode.prefs

import android.support.test.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import java.lang.ClassCastException
import kotlin.random.Random

class SecurePreferencesTest {

    @After fun tearDown() {
        KeyStoreUtil.deleteKey("test")
    }

    @Test fun testDefaults() {
        val securePreferences = SecurePreferences(InstrumentationRegistry.getContext(), "test")
        assertEquals("Default", securePreferences.getString("String", "Default"))
        assertEquals(setOf<String>(), securePreferences.getStringSet("String", setOf()))
        assertEquals(1, securePreferences.getInt("Integer", 1))
        assertEquals(1L, securePreferences.getLong("Long", 1L))
        assertEquals(1.0f, securePreferences.getFloat("Float", 1.0f))
        assertEquals(true, securePreferences.getBoolean("Boolean", true))
        securePreferences.edit().clear().commit()
    }

    @Test fun testStringsOperations() {

        val testData = mutableMapOf<String, String>()

        for (it in 1..10) {
            testData[randomString(Random.nextInt(255))] = randomString(Random.nextInt(255))
        }

        val securePreferences = SecurePreferences(InstrumentationRegistry.getContext(), "test")
        val editor = securePreferences.edit()

        testData.entries.forEach { entry ->
            editor.putString(entry.key, entry.value)
        }

        testData.entries.forEach { entry ->
            editor.putString(entry.key, entry.value)
        }

        editor.commit()

        testData.entries.forEach { entry ->
            assertEquals(entry.value, securePreferences.getString(entry.key, null))
        }

        val loadedPreferences = SecurePreferences(InstrumentationRegistry.getContext(), "test")

        testData.entries.forEach { entry ->
            assertEquals(entry.value, loadedPreferences.getString(entry.key, null))
        }

        securePreferences.edit().clear().commit()
    }

    @Test fun testStringSetOperations() {

        val testData = mutableMapOf<String, Set<String>>()

        for (i in 1..10) {
            val stringSet = mutableSetOf<String>()
            for (s in 1..10) {
                stringSet.add(randomString(Random.nextInt(255)))
            }
            testData[randomString(Random.nextInt(255))] = stringSet
        }

        val securePreferences = SecurePreferences(InstrumentationRegistry.getContext(), "test")
        val editor = securePreferences.edit()

        testData.entries.forEach { entry ->
            editor.putStringSet(entry.key, entry.value)
        }

        editor.commit()

        testData.entries.forEach { entry ->
            assertEquals(entry.value, securePreferences.getStringSet(entry.key, null))
        }

        val loadedPreferences = SecurePreferences(InstrumentationRegistry.getContext(), "test")

        testData.entries.forEach { entry ->
            assertEquals(entry.value, loadedPreferences.getStringSet(entry.key, null))
        }

        securePreferences.edit().clear().commit()
    }

    @Test fun testIntOperations() {

        val testData = mutableMapOf<String, Int>()

        for (it in 1..10) {
            testData[randomString(Random.nextInt(255))] = Random.nextInt()
        }

        val securePreferences = SecurePreferences(InstrumentationRegistry.getContext(), "test")
        val editor = securePreferences.edit()

        testData.entries.forEach { entry ->
            editor.putInt(entry.key, entry.value)
        }

        testData.entries.forEach { entry ->
            editor.putInt(entry.key, entry.value)
        }

        editor.commit()

        assertEquals(10, securePreferences.all.count())

        testData.entries.forEach { entry ->
            assertEquals(entry.value, securePreferences.getInt(entry.key, 0))
        }

        val loadedPreferences = SecurePreferences(InstrumentationRegistry.getContext(), "test")

        testData.entries.forEach { entry ->
            assertEquals(entry.value, loadedPreferences.getInt(entry.key, 0))
        }

        securePreferences.edit().clear().commit()
    }

    @Test fun testLongOperations() {

        val testData = mutableMapOf<String, Long>()

        for (it in 1..10) {
            testData[randomString(Random.nextInt(255))] = Random.nextLong()
        }

        val securePreferences = SecurePreferences(InstrumentationRegistry.getContext(), "test")
        val editor = securePreferences.edit()

        testData.entries.forEach { entry ->
            editor.putLong(entry.key, entry.value)
        }

        editor.commit()

        testData.entries.forEach { entry ->
            assertEquals(entry.value, securePreferences.getLong(entry.key, 0))
        }

        val loadedPreferences = SecurePreferences(InstrumentationRegistry.getContext(), "test")

        testData.entries.forEach { entry ->
            assertEquals(entry.value, loadedPreferences.getLong(entry.key, 0))
        }

        securePreferences.edit().clear().commit()
    }

    @Test fun testFloatOperations() {

        val testData = mutableMapOf<String, Float>()

        for (it in 1..10) {
            testData[randomString(Random.nextInt(255))] = Random.nextFloat()
        }

        val securePreferences = SecurePreferences(InstrumentationRegistry.getContext(), "test")
        val editor = securePreferences.edit()

        testData.entries.forEach { entry ->
            editor.putFloat(entry.key, entry.value)
        }

        editor.commit()

        testData.entries.forEach { entry ->
            assertEquals(entry.value, securePreferences.getFloat(entry.key, 0f))
        }

        val loadedPreferences = SecurePreferences(InstrumentationRegistry.getContext(), "test")

        testData.entries.forEach { entry ->
            assertEquals(entry.value, loadedPreferences.getFloat(entry.key, 0f))
        }

        securePreferences.edit().clear().commit()
    }

    @Test fun testBooleanOperations() {

        val testData = mutableMapOf<String, Boolean>()

        for (it in 1..10) {
            testData[randomString(Random.nextInt(255))] = Random.nextBoolean()
        }

        val securePreferences = SecurePreferences(InstrumentationRegistry.getContext(), "test")
        val editor = securePreferences.edit()

        testData.entries.forEach { entry ->
            editor.putBoolean(entry.key, entry.value)
        }

        editor.commit()

        testData.entries.forEach { entry ->
            assertEquals(entry.value, securePreferences.getBoolean(entry.key, false))
        }

        val loadedPreferences = SecurePreferences(InstrumentationRegistry.getContext(), "test")

        testData.entries.forEach { entry ->
            assertEquals(entry.value, loadedPreferences.getBoolean(entry.key, false))
        }

        securePreferences.edit().clear().commit()
    }


    private val randomStringChars : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    private fun randomString(stringLength: Int): String {

        return (1..stringLength)
            .map { Random.nextInt(0, randomStringChars.size) }
            .map(randomStringChars::get)
            .joinToString("")
    }

}