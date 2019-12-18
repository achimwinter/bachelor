package com.example.bachelor.signal

import android.content.Context
import com.example.bachelor.signal.storage.TestInMemorySignalProtocolStore
import de.adorsys.android.securestoragelibrary.SecurePreferences
import org.whispersystems.libsignal.IdentityKeyPair
import org.whispersystems.libsignal.util.KeyHelper
import java.nio.charset.Charset

class KeyGenerator(context: Context) {

    val identityKeyPair = KeyHelper.generateIdentityKeyPair()
    val registrationId = KeyHelper.generateRegistrationId(false)
    val preKeys = KeyHelper.generatePreKeys(0, 100) // check startNumber. how does this affect... things...
    val signedPreKey = KeyHelper.generateSignedPreKey(identityKeyPair, 0)  // check signedPreKeyId, what does this number mean

    init {
        // TODO: preKeys and signedPreKeys store in inMemoryKeyStore

        saveKeyPair(context, identityKeyPair)
        saveRegistrationId(context, registrationId)
    }

    fun getPreKeyBundle(signalProtocolStore: TestInMemorySignalProtocolStore) {

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