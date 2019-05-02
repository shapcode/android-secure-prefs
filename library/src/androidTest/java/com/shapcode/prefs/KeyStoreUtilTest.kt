package com.shapcode.prefs

import org.junit.Assert.assertNotNull
import org.junit.Test

class KeyStoreUtilTest {

    @Test fun createOrGetKey() {
        val key = KeyStoreUtil.getOrCreateKey("test")
        assertNotNull(key)
    }

    @Test fun deleteKey() {
        KeyStoreUtil.deleteKey("test")
    }

}