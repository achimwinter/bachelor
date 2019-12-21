package com.example.bachelor.signal

import org.whispersystems.libsignal.IdentityKeyPair
import org.whispersystems.libsignal.SessionBuilder
import org.whispersystems.libsignal.SessionCipher
import org.whispersystems.libsignal.SignalProtocolAddress
import org.whispersystems.libsignal.ecc.Curve
import org.whispersystems.libsignal.protocol.PreKeySignalMessage
import org.whispersystems.libsignal.state.PreKeyBundle
import org.whispersystems.libsignal.state.PreKeyRecord
import org.whispersystems.libsignal.state.SignedPreKeyRecord
import org.whispersystems.libsignal.util.KeyHelper


class SessionGenerator {

    companion object {
        val instance = SessionGenerator()
    }

    val MOBILE_ADDRESS = SignalProtocolAddress("MOBILE", 1)
    var DESKTOP_ADDRESS = SignalProtocolAddress("DESKTOP", 2)

    val signalProtocolStore = TestInMemorySignalProtocolStore(KeyHelper.generateIdentityKeyPair(), 1)
    val desktopPreKeyPair = Curve.generateKeyPair()
    val desktopSignedPreKeyPair = Curve.generateKeyPair()
    val desktopSignedPreKeySignature = Curve.calculateSignature(
            signalProtocolStore.identityKeyPair.privateKey,
            desktopSignedPreKeyPair.publicKey.serialize()
    )

    val desktopKeyBundle = generateKeyBundle()
    var mobileKeyBundle : PreKeyBundle? = null

    fun generateKeyBundle(): PreKeyBundle {
        return PreKeyBundle(
                signalProtocolStore.localRegistrationId, 1,
                31337, desktopPreKeyPair.publicKey, 22, desktopSignedPreKeyPair.publicKey,
                desktopSignedPreKeySignature, signalProtocolStore.identityKeyPair.publicKey
        )
    }

    fun decryptMessage(message: PreKeySignalMessage): ByteArray {
        signalProtocolStore.storePreKey(31337, PreKeyRecord( desktopKeyBundle.preKeyId, desktopPreKeyPair ))
        signalProtocolStore.storeSignedPreKey(message.signedPreKeyId, SignedPreKeyRecord(message.signedPreKeyId, System.currentTimeMillis(), desktopSignedPreKeyPair, desktopSignedPreKeySignature))

        val sessionBuilder = SessionBuilder(signalProtocolStore, MOBILE_ADDRESS)
        sessionBuilder.process(mobileKeyBundle)

        val sessionCipher = SessionCipher(signalProtocolStore, MOBILE_ADDRESS)

        return sessionCipher.decrypt(message)
    }

}
//Curve.verifySignature(bundle.getIdentityKey().getPublicKey(),
//bundle.getSignedPreKey().serialize(),
//bundle.getSignedPreKeySignature())