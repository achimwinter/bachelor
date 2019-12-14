package com.example.bachelor.signal

import org.whispersystems.libsignal.SignalProtocolAddress
import org.whispersystems.libsignal.state.SignalProtocolStore
import org.whispersystems.libsignal.util.KeyHelper
import java.io.File


class SessionGenerator {

    val MOBILE_ADRESS = SignalProtocolAddress("Mobile", 1)

    fun prepareSession() {
        val identityKeyPair = KeyHelper.generateIdentityKeyPair()
        val registrationId = KeyHelper.generateRegistrationId(false)
        val preKeys = KeyHelper.generatePreKeys(0, 100)
        val signedPreKey = KeyHelper.generateSignedPreKey(identityKeyPair, 5)

        val desktopStore: SignalProtocolStore = TestInMemorySignalProtocolStore()
    }

}