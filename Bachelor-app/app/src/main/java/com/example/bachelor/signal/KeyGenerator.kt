package com.example.bachelor.signal

import android.content.Context
import de.adorsys.android.securestoragelibrary.SecurePreferences
import org.whispersystems.libsignal.IdentityKeyPair
import org.whispersystems.libsignal.util.KeyHelper
import java.nio.charset.Charset

class KeyGenerator(context: Context) {


    // TODO: Only do this on install time https://github.com/signalapp/libsignal-protocol-java

    init {
        var identityKeyPair = KeyHelper.generateIdentityKeyPair()
        var registrationId = KeyHelper.generateRegistrationId(false)
        var preKeys = KeyHelper.generatePreKeys(0, 100) // check startNumber. how does this affect... things...
        var signedPreKey = KeyHelper.generateSignedPreKey(identityKeyPair, 0) // check signedPreKeyId, what does this number mean

        saveKeyPair(context, identityKeyPair)
        saveRegistrationId(context, registrationId)
    }

    fun saveKeyPair(context: Context, identityKeyPair: IdentityKeyPair) {
        val serializedPrivateKey = identityKeyPair.privateKey.serialize().toString(Charset.defaultCharset())
        val serializedPublicKey = identityKeyPair.publicKey.serialize().toString(Charset.defaultCharset())

        SecurePreferences.setValue(context, "serializedPrivateKey", serializedPrivateKey)
        SecurePreferences.setValue(context, "serializedPublicKey", serializedPublicKey)
    }

    fun saveRegistrationId(context: Context, registrationId: Int) {
        SecurePreferences.setValue(context, "registrationId", registrationId)
    }

// Store identityKeyPair somewhere durable and safe.
// Store registrationId somewhere durable and safe.

// Store preKeys in PreKeyStore.
// Store signed prekey in SignedPreKeyStore.
}