package com.example.bachelor.signal

import com.example.bachelor.api.GrpcClient
import com.example.bachelor.signal.storage.TestInMemorySignalProtocolStore
import com.example.bachelor.smime.SmimeUtils
import com.google.protobuf.ByteString
import org.whispersystems.libsignal.SessionBuilder
import org.whispersystems.libsignal.SessionCipher
import org.whispersystems.libsignal.SignalProtocolAddress
import org.whispersystems.libsignal.ecc.Curve
import org.whispersystems.libsignal.protocol.SignalMessage
import org.whispersystems.libsignal.state.PreKeyBundle
import java.io.InputStream


class SessionGenerator {

    private val MOBILE_ADDRESS = SignalProtocolAddress("MOBILE", 1)
    private var DESKTOP_ADDRESS = SignalProtocolAddress("DESKTOP", 2)


    fun startCommunication(keystore: InputStream) {
        val signalProtocolStore = TestInMemorySignalProtocolStore()
        val sessionBuilder = SessionBuilder(signalProtocolStore, DESKTOP_ADDRESS)

        val ownPreKeyPair = Curve.generateKeyPair()
        val ownSignedPreKeyPair = Curve.generateKeyPair()
        val ownSignedPreKeySignature = Curve.calculateSignature(
            signalProtocolStore.identityKeyPair.privateKey,
            ownSignedPreKeyPair.publicKey.serialize()
        )

        val ownPreKeyBundle = PreKeyBundle(
            signalProtocolStore.localRegistrationId, 1,
            31337, ownPreKeyPair.publicKey, 22, ownSignedPreKeyPair.publicKey,
            ownSignedPreKeySignature, signalProtocolStore.identityKeyPair.publicKey
        )

        val desktopKeyBundle = GrpcClient.instance.exchangeKeybundles(ownPreKeyBundle)

        sessionBuilder.process(desktopKeyBundle)

        val message = "test"

        val sessionCipher = SessionCipher(signalProtocolStore, DESKTOP_ADDRESS)
        val outgoingMessage = sessionCipher.encrypt(message.toByteArray())

        val response = GrpcClient.instance.testDecrypt(ByteString.copyFrom(outgoingMessage.serialize()))

        val incomingMessage = SignalMessage(response.unencryptedMail.toByteArray())

        val incPlaintext = sessionCipher.decrypt(incomingMessage)

        val decrypted = SmimeUtils(keystore).decrypt(incPlaintext)
        print(String(decrypted!!))

    }

}