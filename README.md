# Android Secure Preferences

[![Actions Status](https://github.com/shapcode/android-secure-prefs/workflows/Android%20CI/badge.svg)](https://github.com/shapcode/android-secure-prefs/actions) ![https://img.shields.io/maven-central/v/com.shapcode/android-secure-prefs](https://img.shields.io/maven-central/v/com.shapcode/android-secure-prefs)

SecurePreferences is a simple wrapper for Android's SharedPreferences which adds encryption to the keys and values. The
Android KeyStore is used to generate and store the SecretKey.

Android API 23+ is required.

### Download
Available on Maven Central at coordinates `com.shapcode:android-secure-prefs:1.0.0`.

#### Gradle:

```implementation 'com.shapcode:android-secure-prefs:1.0.0'```
