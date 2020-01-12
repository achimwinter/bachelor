package com.example.bachelor.signal

import com.example.bachelor.api.GrpcClient
import com.example.bachelor.signal.storage.TestInMemorySignalProtocolStore
import com.example.bachelor.smime.KeyTool
import org.whispersystems.libsignal.SessionBuilder
import org.whispersystems.libsignal.SessionCipher
import org.whispersystems.libsignal.SignalProtocolAddress
import org.whispersystems.libsignal.ecc.Curve
import org.whispersystems.libsignal.protocol.SignalMessage
import org.whispersystems.libsignal.state.PreKeyBundle
import java.io.ByteArrayInputStream
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.*


class SessionGenerator {


    fun startCommunication() {
        val ownPreKeyPair = Curve.generateKeyPair()
        val ownSignedPreKeyPair = Curve.generateKeyPair()
        val ownSignedPreKeySignature = Curve.calculateSignature(
            signalProtocolStore.identityKeyPair.privateKey,
            ownSignedPreKeyPair.publicKey.serialize()
        )

        val ownPreKeyBundle = PreKeyBundle(
            signalProtocolStore.localRegistrationId, 1,
            Random().nextInt(), ownPreKeyPair.publicKey, Random().nextInt(), ownSignedPreKeyPair.publicKey,
            ownSignedPreKeySignature, signalProtocolStore.identityKeyPair.publicKey
        )

        val desktopKeyBundle = GrpcClient.instance.exchangeKeybundles(ownPreKeyBundle)

        sessionBuilder.process(desktopKeyBundle)

        KeyTool().generateKeyPair()

        GrpcClient.instance.startCommunication()
    }

    companion object {
        private var DESKTOP_ADDRESS = SignalProtocolAddress("DESKTOP", 2)
        val signalProtocolStore = TestInMemorySignalProtocolStore()
        val sessionCipher = SessionCipher(signalProtocolStore, DESKTOP_ADDRESS)
        val sessionBuilder = SessionBuilder(signalProtocolStore, DESKTOP_ADDRESS)
    }

}