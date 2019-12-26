package com.example.bachelor.api

import com.example.bachelor.DecryptRequest
import com.example.bachelor.DecryptResponse
import com.example.bachelor.DecrypterGrpc
import com.example.bachelor.Keybundle
import com.example.bachelor.signal.SessionGenerator
import com.google.protobuf.ByteString
import io.grpc.stub.StreamObserver
import org.whispersystems.libsignal.IdentityKey
import org.whispersystems.libsignal.SessionCipher
import org.whispersystems.libsignal.ecc.Curve
import org.whispersystems.libsignal.ecc.DjbECPublicKey
import org.whispersystems.libsignal.ecc.ECPublicKey
import org.whispersystems.libsignal.protocol.CiphertextMessage
import org.whispersystems.libsignal.protocol.PreKeySignalMessage
import org.whispersystems.libsignal.protocol.SignalMessage
import org.whispersystems.libsignal.state.PreKeyBundle
import java.io.ByteArrayInputStream
import java.io.ObjectInputStream
import java.nio.charset.Charset

class DecrypterImpl : DecrypterGrpc.DecrypterImplBase() {
    override fun subscribeMails(responseObserver: StreamObserver<DecryptResponse?>?): StreamObserver<DecryptRequest?> {
        return object : StreamObserver<DecryptRequest?> {
            override fun onNext(value: DecryptRequest?) {
                println("onNext from Server")

                responseObserver?.onNext(DecryptResponse.getDefaultInstance())
            }

            override fun onError(t: Throwable) {
                println("on error")
                t.printStackTrace()
            }
            override fun onCompleted() {
                println("on completed")
            }
        }
    }

    override fun exchangeKeyBundle(request: Keybundle?, responseObserver: StreamObserver<Keybundle>?) {
        SessionGenerator.instance.mobileKeyBundle = PreKeyBundle(
                request?.registrationId!!,
                request.deviceId,
                request.preKeyId,
                Curve.decodePoint(request.preKeyPublic?.toByteArray(), 0),
                request.signedPreKeyId,
                Curve.decodePoint(request.signedPreKeyPublic.toByteArray(), 0),
                request.signedPreKeySignature.toByteArray(),
                IdentityKey(request.identityKey.toByteArray(), 0)
        )

        val desktopPreKeyBundle = SessionGenerator.instance.desktopKeyBundle

        val desktopKeyBundle = Keybundle.newBuilder()
                .setRegistrationId(desktopPreKeyBundle.registrationId)
                .setDeviceId(desktopPreKeyBundle.deviceId)
                .setPreKeyPublic(ByteString.copyFrom(desktopPreKeyBundle.preKey.serialize()))
                .setSignedPreKeyId(desktopPreKeyBundle.signedPreKeyId)
                .setSignedPreKeyPublic(ByteString.copyFrom(desktopPreKeyBundle.signedPreKey.serialize()))
                .setSignedPreKeySignature(ByteString.copyFrom(desktopPreKeyBundle.signedPreKeySignature))
                .setIdentityKey(ByteString.copyFrom(desktopPreKeyBundle.identityKey.publicKey.serialize()))
                .build()

        responseObserver?.onNext(desktopKeyBundle)
        responseObserver?.onCompleted()
    }

    override fun testGreet(request: DecryptRequest?, responseObserver: StreamObserver<DecryptResponse>?) {
        val incMessage = PreKeySignalMessage(request?.encryptedMail?.toByteArray())

        val plaintext = String(SessionGenerator.instance.decryptMessage(incMessage))

        val sessionCipher = SessionCipher(SessionGenerator.instance.signalProtocolStore, SessionGenerator.instance.MOBILE_ADDRESS)

        val outGoingMessage = sessionCipher.encrypt("test FROM DEsktop CLIENT".toByteArray(Charsets.UTF_8))

        // TODO: Do something with Plaintext

        responseObserver?.onNext(DecryptResponse.newBuilder().setUnencryptedMail(ByteString.copyFrom(outGoingMessage.serialize())).build())
        responseObserver?.onCompleted()
    }

}