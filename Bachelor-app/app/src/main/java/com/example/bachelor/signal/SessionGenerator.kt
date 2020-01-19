package com.example.bachelor.signal

import com.example.bachelor.api.GrpcClient
import com.example.bachelor.signal.storage.TestInMemorySignalProtocolStore
import com.example.bachelor.smime.KeyTool
import org.whispersystems.libsignal.SessionBuilder
import org.whispersystems.libsignal.SessionCipher
import org.whispersystems.libsignal.SignalProtocolAddress
import org.whispersystems.libsignal.ecc.Curve
import org.whispersystems.libsignal.state.PreKeyBundle
import java.security.SecureRandom


class SessionGenerator {

    fun startCommunication(qrCode: String) {
        serverAddress = qrCode.split("|")[0]
        val scannedFingerprint = qrCode.split("|")[1]

        val ownPreKeyPair = Curve.generateKeyPair()
        val ownSignedPreKeyPair = Curve.generateKeyPair()
        val ownSignedPreKeySignature = Curve.calculateSignature(
            signalProtocolStore.identityKeyPair.privateKey,
            ownSignedPreKeyPair.publicKey.serialize()
        )

        val ownPreKeyBundle = PreKeyBundle(
            signalProtocolStore.localRegistrationId,
            1,
            SecureRandom().nextInt(),
            ownPreKeyPair.publicKey,
            SecureRandom().nextInt(),
            ownSignedPreKeyPair.publicKey,
            ownSignedPreKeySignature,
            signalProtocolStore.identityKeyPair.publicKey
        )

        val desktopKeyBundle = GrpcClient.instance.exchangeKeybundles(ownPreKeyBundle)
        if (desktopKeyBundle.identityKey.fingerprint != scannedFingerprint) {
            return
        }

        sessionBuilder.process(desktopKeyBundle)

        KeyTool().generateKeyPair()

        GrpcClient.instance.startCommunication()
    }

    companion object {
        private var DESKTOP_ADDRESS = SignalProtocolAddress("DESKTOP", 2)
        val signalProtocolStore = TestInMemorySignalProtocolStore()
        val sessionCipher = SessionCipher(signalProtocolStore, DESKTOP_ADDRESS)
        val sessionBuilder = SessionBuilder(signalProtocolStore, DESKTOP_ADDRESS)
        lateinit var serverAddress: String
    }

}