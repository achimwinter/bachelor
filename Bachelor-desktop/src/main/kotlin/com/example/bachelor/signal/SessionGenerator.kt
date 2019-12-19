package com.example.bachelor.signal

import org.whispersystems.libsignal.SessionBuilder
import org.whispersystems.libsignal.SignalProtocolAddress
import org.whispersystems.libsignal.ecc.Curve
import org.whispersystems.libsignal.state.PreKeyBundle
import org.whispersystems.libsignal.state.SignalProtocolStore
import org.whispersystems.libsignal.util.KeyHelper


class SessionGenerator {

    private val MOBILE_ADDRESS = SignalProtocolAddress("MOBILE", 1)
    private var DESKTOP_ADDRESS = SignalProtocolAddress("DESKTOP", 2)

    var desktopKeyBundle = getKeyBundle()
    var mobileKeyBundle : PreKeyBundle? = null


    fun getKeyBundle(): PreKeyBundle {
        val identityKeyPair = KeyHelper.generateIdentityKeyPair()
        val registrationId = KeyHelper.generateRegistrationId(false)
        val preKeys = KeyHelper.generatePreKeys(0, 100)
        val signedPreKey = KeyHelper.generateSignedPreKey(identityKeyPair, 5)

        val signalProtocolStore: SignalProtocolStore = TestInMemorySignalProtocolStore()

        val sessionBuilder = SessionBuilder(signalProtocolStore, MOBILE_ADDRESS)

        val desktopPreKeyPair = Curve.generateKeyPair()
        val desktopSignedPreKeyPair = Curve.generateKeyPair()
        val desktopSignedPreKeySignature = Curve.calculateSignature(
                signalProtocolStore.identityKeyPair.privateKey,
                desktopPreKeyPair.publicKey.serialize()
        )

        return PreKeyBundle(registrationId, 2, 31338, desktopPreKeyPair.publicKey,
                12, desktopSignedPreKeyPair.publicKey, desktopSignedPreKeySignature,
                signalProtocolStore.identityKeyPair.publicKey)
    }

}