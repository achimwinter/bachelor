package com.example.bachelor.signal

import org.whispersystems.libsignal.IdentityKey
import org.whispersystems.libsignal.SessionBuilder
import org.whispersystems.libsignal.SessionCipher
import org.whispersystems.libsignal.SignalProtocolAddress
import org.whispersystems.libsignal.ecc.Curve
import org.whispersystems.libsignal.protocol.PreKeySignalMessage
import org.whispersystems.libsignal.state.PreKeyBundle
import org.whispersystems.libsignal.state.PreKeyRecord
import org.whispersystems.libsignal.state.SignedPreKeyRecord
import kotlin.random.Random


class SessionGenerator {

    companion object {
        val instance = SessionGenerator()
        val sessionCipher = SessionCipher(instance.signalProtocolStore, instance.MOBILE_ADDRESS)
    }

    val MOBILE_ADDRESS = SignalProtocolAddress("MOBILE", 1)

    val signalProtocolStore = TestInMemorySignalProtocolStore()
    val desktopPreKeyPair = Curve.generateKeyPair()
    val desktopSignedPreKeyPair = Curve.generateKeyPair()
    val desktopSignedPreKeySignature = Curve.calculateSignature(
            signalProtocolStore.identityKeyPair.privateKey,
            desktopSignedPreKeyPair.publicKey.serialize()
    )

    val desktopKeyBundle = generateKeyBundle()
    var mobileKeyBundle: PreKeyBundle? = null

    val sessionCipher = SessionCipher(signalProtocolStore, MOBILE_ADDRESS)

    fun getIdentityKey(): IdentityKey {
        return signalProtocolStore.identityKeyPair.publicKey
    }

    private fun generateKeyBundle(): PreKeyBundle {
        return PreKeyBundle(
                signalProtocolStore.localRegistrationId, 1,
                Random.nextInt(), desktopPreKeyPair.publicKey, Random.nextInt(), desktopSignedPreKeyPair.publicKey,
                desktopSignedPreKeySignature, signalProtocolStore.identityKeyPair.publicKey
        )
    }

    fun decryptMessage(message: PreKeySignalMessage): ByteArray {
        signalProtocolStore.storePreKey(message.preKeyId.get(), PreKeyRecord(desktopKeyBundle.preKeyId, desktopPreKeyPair))
        signalProtocolStore.storeSignedPreKey(message.signedPreKeyId, SignedPreKeyRecord(message.signedPreKeyId, System.currentTimeMillis(), desktopSignedPreKeyPair, desktopSignedPreKeySignature))

        val sessionBuilder = SessionBuilder(signalProtocolStore, MOBILE_ADDRESS)
        sessionBuilder.process(mobileKeyBundle)

        return sessionCipher.decrypt(message)
    }


}