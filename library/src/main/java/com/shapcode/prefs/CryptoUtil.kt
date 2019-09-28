/*
 *  Android Secure Preferences
 *  https://github.com/shapcode/android-secure-prefs
 *
 *  Copyright (C) 2019 Justin Shapcott
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.shapcode.prefs

import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.*
import javax.crypto.spec.GCMParameterSpec

internal object CryptoUtil {

    private const val LOG_MESSAGE_FAILED_TO_ENCRYPT = "Failed to encrypt."
    private const val LOG_MESSAGE_FAILED_TO_DECRYPT = "Failed to decrypt."

    private const val TRANSFORM_AES_GCM = "AES/GCM/NoPadding"

    private const val CHARSET_NAME = "UTF-8"

    private const val SIZE_IV_GCM = 12

    @Throws(CryptoException::class)
    fun encrypt(value: String, secretKey: SecretKey): String {
        try {
            val encrypted = encrypt(value.toByteArray(charset(CHARSET_NAME)), secretKey)
            return encode(encrypted)
        } catch (e: UnsupportedEncodingException) {
            throw CryptoException(LOG_MESSAGE_FAILED_TO_ENCRYPT, e)
        }
    }

    @Throws(CryptoException::class)
    fun encrypt(value: ByteArray, secretKey: SecretKey): ByteArray {
        try {
            val iv: ByteArray
            val encryptCipher = Cipher.getInstance(TRANSFORM_AES_GCM)
            encryptCipher.init(Cipher.ENCRYPT_MODE, secretKey)
            iv = encryptCipher.iv
            return encrypt(value, iv, encryptCipher)
        } catch (e: NoSuchAlgorithmException) {
            throw CryptoException(LOG_MESSAGE_FAILED_TO_ENCRYPT, e)
        } catch (e: NoSuchPaddingException) {
            throw CryptoException(LOG_MESSAGE_FAILED_TO_ENCRYPT, e)
        } catch (e: InvalidKeyException) {
            throw CryptoException(LOG_MESSAGE_FAILED_TO_ENCRYPT, e)
        }

    }

    @Throws(CryptoException::class)
    fun encrypt(value: String, iv: ByteArray, encryptCipher: Cipher): ByteArray {
        try {
            return encrypt(value.toByteArray(charset(CHARSET_NAME)), iv, encryptCipher)
        } catch (e: UnsupportedEncodingException) {
            throw CryptoException(LOG_MESSAGE_FAILED_TO_ENCRYPT, e)
        }

    }

    @Throws(CryptoException::class)
    fun encrypt(value: ByteArray, iv: ByteArray, encryptCipher: Cipher): ByteArray {
        try {
            val baos = ByteArrayOutputStream()
            baos.write(iv)
            baos.write(encryptCipher.doFinal(value))
            return baos.toByteArray()
        } catch (e: IOException) {
            throw CryptoException(LOG_MESSAGE_FAILED_TO_ENCRYPT, e)
        } catch (e: IllegalBlockSizeException) {
            throw CryptoException(LOG_MESSAGE_FAILED_TO_ENCRYPT, e)
        } catch (e: BadPaddingException) {
            throw CryptoException(LOG_MESSAGE_FAILED_TO_ENCRYPT, e)
        }

    }

    @Throws(CryptoException::class)
    fun decrypt(value: String, secretKey: SecretKey): String {
        try {
            val decoded = decode(value)
            val decrypted = decrypt(decoded, secretKey)
            return String(decrypted, charset(CHARSET_NAME))
        } catch (e: UnsupportedEncodingException) {
            throw CryptoException(LOG_MESSAGE_FAILED_TO_ENCRYPT, e)
        }

    }

    @Throws(CryptoException::class)
    fun decrypt(value: ByteArray, secretKey: SecretKey): ByteArray {
        try {
            val ivSize = SIZE_IV_GCM
            val iv = ByteArray(ivSize)
            System.arraycopy(value, 0, iv, 0, ivSize)
            val decryptCipher = Cipher.getInstance(TRANSFORM_AES_GCM)
            decryptCipher.init(Cipher.DECRYPT_MODE, secretKey, getParameterSpec(iv))
            return decrypt(value, iv, decryptCipher)
        } catch (e: NoSuchAlgorithmException) {
            throw CryptoException(LOG_MESSAGE_FAILED_TO_DECRYPT, e)
        } catch (e: NoSuchPaddingException) {
            throw CryptoException(LOG_MESSAGE_FAILED_TO_DECRYPT, e)
        } catch (e: InvalidAlgorithmParameterException) {
            throw CryptoException(LOG_MESSAGE_FAILED_TO_DECRYPT, e)
        } catch (e: InvalidKeyException) {
            throw CryptoException(LOG_MESSAGE_FAILED_TO_DECRYPT, e)
        }

    }

    @Throws(CryptoException::class)
    fun decrypt(value: ByteArray, iv: ByteArray, decryptCipher: Cipher): ByteArray {
        try {
            return decryptCipher.doFinal(value, iv.size, value.size - iv.size)
        } catch (e: IllegalBlockSizeException) {
            throw CryptoException(LOG_MESSAGE_FAILED_TO_DECRYPT, e)
        } catch (e: BadPaddingException) {
            throw CryptoException(LOG_MESSAGE_FAILED_TO_DECRYPT, e)
        }

    }

    private fun getParameterSpec(iv: ByteArray): AlgorithmParameterSpec {
        return GCMParameterSpec(128, iv)
    }

    private fun encode(value: ByteArray): String {
        return Base64.encodeToString(value, Base64.NO_PADDING or Base64.NO_WRAP)
    }

    private fun decode(value: String): ByteArray {
        return Base64.decode(value, Base64.NO_PADDING or Base64.NO_WRAP)
    }

    class CryptoException(message: String, cause: Throwable) : Exception(message, cause)

}