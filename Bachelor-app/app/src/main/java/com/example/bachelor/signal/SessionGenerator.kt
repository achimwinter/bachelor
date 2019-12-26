package com.example.bachelor.signal

import android.content.Context
import android.se.omapi.Session
import android.util.Log
import com.example.bachelor.api.GrpcClient
import com.example.bachelor.signal.storage.TestInMemorySignalProtocolStore
import com.google.protobuf.ByteString
import de.adorsys.android.securestoragelibrary.SecurePreferences
import org.whispersystems.libsignal.*
import org.whispersystems.libsignal.NoSessionException
import org.whispersystems.libsignal.ecc.Curve
import org.whispersystems.libsignal.ecc.ECKeyPair
import org.whispersystems.libsignal.protocol.PreKeySignalMessage
import org.whispersystems.libsignal.protocol.SignalMessage
import org.whispersystems.libsignal.protocol.SignalProtos
import org.whispersystems.libsignal.state.PreKeyBundle
import org.whispersystems.libsignal.state.PreKeyRecord
import org.whispersystems.libsignal.state.SignalProtocolStore
import org.whispersystems.libsignal.state.SignedPreKeyRecord
import org.whispersystems.libsignal.state.impl.InMemorySignalProtocolStore


class SessionGenerator {

    private val MOBILE_ADDRESS = SignalProtocolAddress("MOBILE", 1)
    private var DESKTOP_ADDRESS = SignalProtocolAddress("DESKTOP", 2)


    fun startCommunication() {
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

        val response = GrpcClient().testDecrypt(ByteString.copyFrom(outgoingMessage.serialize()))

        val incomingMessage = SignalMessage(response.unencryptedMail.toByteArray())

        val incPlaintext = sessionCipher.decrypt(incomingMessage)

        print(incPlaintext)

    }


    fun testSessionBuilder() {
        var aliceStore: SignalProtocolStore = TestInMemorySignalProtocolStore()
        var aliceSessionBuilder = SessionBuilder(aliceStore, DESKTOP_ADDRESS)

        val bobStore: SignalProtocolStore = TestInMemorySignalProtocolStore()
        var bobPreKeyPair = Curve.generateKeyPair()
        var bobSignedPreKeyPair =
            Curve.generateKeyPair()
        var bobSignedPreKeySignature =
            Curve.calculateSignature(
                bobStore.identityKeyPair.privateKey,
                bobSignedPreKeyPair.publicKey.serialize()
            )


        var bobPreKey = PreKeyBundle(
            bobStore.localRegistrationId, 1,
            31337, bobPreKeyPair.publicKey,
            22, bobSignedPreKeyPair.publicKey,
            bobSignedPreKeySignature,
            bobStore.identityKeyPair.publicKey
        )

        aliceSessionBuilder.process(bobPreKey)

        val originalMessage = "Hello World through Signal Protocol"
        var aliceSessionCipher = SessionCipher(aliceStore, DESKTOP_ADDRESS)
        var outgoingMessage =
            aliceSessionCipher.encrypt(originalMessage.toByteArray())

        val incomingMessage = PreKeySignalMessage(outgoingMessage.serialize())
        bobStore.storePreKey(31337, PreKeyRecord(bobPreKey.preKeyId, bobPreKeyPair))
        bobStore.storeSignedPreKey(
            22,
            SignedPreKeyRecord(
                22,
                System.currentTimeMillis(),
                bobSignedPreKeyPair,
                bobSignedPreKeySignature
            )
        )

        val bobSessionCipher = SessionCipher(bobStore, MOBILE_ADDRESS)

        var plaintext = bobSessionCipher.decrypt(incomingMessage)

        val bobOutgoingMessage = bobSessionCipher.encrypt(originalMessage.toByteArray())

        val alicePlaintext: ByteArray =
            aliceSessionCipher.decrypt(SignalMessage(bobOutgoingMessage.serialize()))

        runInteraction(aliceStore, bobStore)

        aliceStore = TestInMemorySignalProtocolStore()
        aliceSessionBuilder = SessionBuilder(aliceStore, DESKTOP_ADDRESS)
        aliceSessionCipher = SessionCipher(aliceStore, DESKTOP_ADDRESS)

        bobPreKeyPair = Curve.generateKeyPair()
        bobSignedPreKeyPair = Curve.generateKeyPair()
        bobSignedPreKeySignature = Curve.calculateSignature(
            bobStore.identityKeyPair.privateKey,
            bobSignedPreKeyPair.publicKey.serialize()
        )
        bobPreKey = PreKeyBundle(
            bobStore.localRegistrationId,
            1, 31338, bobPreKeyPair.publicKey,
            23, bobSignedPreKeyPair.publicKey, bobSignedPreKeySignature,
            bobStore.identityKeyPair.publicKey
        )

        bobStore.storePreKey(31338, PreKeyRecord(bobPreKey.preKeyId, bobPreKeyPair))
        bobStore.storeSignedPreKey(
            23,
            SignedPreKeyRecord(
                23,
                System.currentTimeMillis(),
                bobSignedPreKeyPair,
                bobSignedPreKeySignature
            )
        )
        aliceSessionBuilder.process(bobPreKey)


        outgoingMessage = aliceSessionCipher.encrypt(originalMessage.toByteArray())

        try {
            plaintext = bobSessionCipher.decrypt(PreKeySignalMessage(outgoingMessage.serialize()))
            throw AssertionError("shouldn't be trusted!")
        } catch (uie: UntrustedIdentityException) {
            bobStore.saveIdentity(MOBILE_ADDRESS, PreKeySignalMessage(outgoingMessage.serialize()).getIdentityKey())
        }

        val plaintext2 = bobSessionCipher.decrypt( PreKeySignalMessage(outgoingMessage.serialize()))

        bobPreKey = PreKeyBundle(bobStore.localRegistrationId, 1,
        31337, Curve.generateKeyPair().publicKey,
        23, bobSignedPreKeyPair.publicKey, bobSignedPreKeySignature,
        aliceStore.getIdentityKeyPair().publicKey)

        try {
            aliceSessionBuilder.process(bobPreKey);
            throw AssertionError("shoulnd't be trusted!")
        } catch (uie: UntrustedIdentityException) {
            Log.w("good", "Test good, Not trusted prekey")
            // good
        }
    }

    @Throws(
        DuplicateMessageException::class,
        LegacyMessageException::class,
        InvalidMessageException::class,
        NoSessionException::class,
        UntrustedIdentityException::class
    )
    private fun runInteraction(
        aliceStore: SignalProtocolStore,
        bobStore: SignalProtocolStore
    ) {
        val aliceSessionCipher = SessionCipher(aliceStore, DESKTOP_ADDRESS)
        val bobSessionCipher = SessionCipher(bobStore, MOBILE_ADDRESS)
        val originalMessage = "smert ze smert"


        val aliceMessage =
            aliceSessionCipher.encrypt(originalMessage.toByteArray())
        var plaintext = bobSessionCipher.decrypt(
            SignalMessage(aliceMessage.serialize())
        )
        val bobMessage = bobSessionCipher.encrypt(originalMessage.toByteArray())
        plaintext = aliceSessionCipher.decrypt(
            SignalMessage(bobMessage.serialize())
        )
        for (i in 0..9) {
            val loopingMessage =
                "What do we mean by saying that existence precedes essence? " +
                        "We mean that man first of all exists, encounters himself, " +
                        "surges up in the world--and defines himself aftward. " + i
            val aliceLoopingMessage =
                aliceSessionCipher.encrypt(loopingMessage.toByteArray())
            val loopingPlaintext = bobSessionCipher.decrypt(
                SignalMessage(aliceLoopingMessage.serialize())
            )
        }
    }





    // TODO: Do something with the session
    fun generateSession(context: Context) {

        val bobStore: SignalProtocolStore = TestInMemorySignalProtocolStore()
        val bobPreKeyPair: ECKeyPair = Curve.generateKeyPair()
        val bobSignedPreKeyPair: ECKeyPair = Curve.generateKeyPair()
        val bobSignedPreKeySignature: ByteArray = Curve.calculateSignature(
            bobStore.identityKeyPair.privateKey,
            bobSignedPreKeyPair.publicKey.serialize()
        )

        val privateKey = SecurePreferences.getStringValue(context, "serializedPrivateKey", "")
        val publicKey = SecurePreferences.getStringValue(context, "serializedPublicKey", "")
        val registrationId = SecurePreferences.getStringValue(context, "registrationId", "")!!.toInt()

        val identityKeyPair = IdentityKeyPair((privateKey + publicKey).toByteArray(Charsets.UTF_8))

        val store = InMemorySignalProtocolStore(identityKeyPair, registrationId)

        val sessionBuilder = SessionBuilder(store, MOBILE_ADDRESS)

        sessionBuilder.process(generatePreKeyBundle(bobStore))

        val sessionCipher = SessionCipher(store, MOBILE_ADDRESS)
        val message = sessionCipher.encrypt("Hello World".toByteArray(Charsets.UTF_8))

        message.serialize()
    }

    fun generatePreKeyBundle(bobKeyStore: SignalProtocolStore): PreKeyBundle {
        val bobPreKeyPair = Curve.generateKeyPair()

        return PreKeyBundle(bobKeyStore.localRegistrationId, 1, 31337,
            bobPreKeyPair.publicKey, 0, null,
            null, bobKeyStore.identityKeyPair.publicKey)
    }









}