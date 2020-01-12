package com.example.bachelor.api

import com.example.bachelor.*
import com.example.bachelor.signal.SessionGenerator
import com.example.bachelor.smime.SmimeUtils
import com.google.protobuf.ByteString
import io.grpc.ManagedChannelBuilder
import io.grpc.stub.StreamObserver
import org.bouncycastle.pkcs.PKCS10CertificationRequest
import org.whispersystems.libsignal.IdentityKey
import org.whispersystems.libsignal.InvalidMessageException
import org.whispersystems.libsignal.ecc.Curve
import org.whispersystems.libsignal.protocol.PreKeySignalMessage
import org.whispersystems.libsignal.protocol.SignalMessage
import org.whispersystems.libsignal.state.PreKeyBundle
import java.security.PublicKey


class GrpcClient {

    companion object {
        val instance = GrpcClient()
        var counter = 0
    }

    private val managedChannel = ManagedChannelBuilder
//        .forTarget(MainActivity.tvresult?.text.toString())
        .forTarget("192.168.2.117:50051")
        .usePlaintext()
        .build()


    val decryptStub = DecrypterGrpc.newStub(managedChannel)
    val observer = decryptStub.subscribeMails(object : StreamObserver<DecryptRequest> {
        override fun onNext(value: DecryptRequest?) {
            decryptMail(value)
            counter++
        }

        override fun onError(t: Throwable?) {
            t?.printStackTrace()
            println("onError on Client")
        }

        override fun onCompleted() {
            println("onCompleted on Client")
        }
    })


    fun startCommunication() {
        val handshakeMessage = SessionGenerator.sessionCipher.encrypt("INIT".toByteArray())

        observer.onNext(DecryptResponse.newBuilder()
            .setUnencryptedMail(ByteString.copyFrom(handshakeMessage.serialize()))
            .build())
    }

    private fun decryptMail(value: DecryptRequest?) {
        if (!value?.encryptedMail?.isEmpty!!) {
            val incomingSignalMessage = SignalMessage(value.encryptedMail?.toByteArray())

            val encryptedMail = SessionGenerator.sessionCipher.decrypt(incomingSignalMessage)

            val decryptedMail = SmimeUtils().decrypt(encryptedMail)

            val outgoingSignalMessage = SessionGenerator.sessionCipher.encrypt(decryptedMail)

            observer.onNext(DecryptResponse.newBuilder()
                .setUnencryptedMail(ByteString.copyFrom(outgoingSignalMessage.serialize()))
                .build())
        }
        observer.onNext(null)

    }


    fun exchangeKeybundles(ownPreKeyBundle: PreKeyBundle): PreKeyBundle {
        val stub = DecrypterGrpc.newBlockingStub(managedChannel)

        val ownSerializedKeybundle = Keybundle.newBuilder()
            .setRegistrationId(ownPreKeyBundle.registrationId)
            .setDeviceId(ownPreKeyBundle.deviceId)
            .setPreKeyPublic(ByteString.copyFrom(ownPreKeyBundle.preKey.serialize()))
            .setSignedPreKeyId(ownPreKeyBundle.signedPreKeyId)
            .setSignedPreKeyPublic(ByteString.copyFrom(ownPreKeyBundle.signedPreKey.serialize()))
            .setSignedPreKeySignature(ByteString.copyFrom(ownPreKeyBundle.signedPreKeySignature))
            .setIdentityKey(ByteString.copyFrom(ownPreKeyBundle.identityKey.publicKey.serialize()))
            .build()

        val responseKeyBundle = stub.exchangeKeyBundle(ownSerializedKeybundle)

        return PreKeyBundle(
            responseKeyBundle.registrationId,
            responseKeyBundle.deviceId,
            responseKeyBundle.preKeyId,
            Curve.decodePoint(responseKeyBundle.preKeyPublic.toByteArray(), 0),
            responseKeyBundle.signedPreKeyId,
            Curve.decodePoint(responseKeyBundle.signedPreKeyPublic.toByteArray(), 0),
            responseKeyBundle.signedPreKeySignature.toByteArray(),
            IdentityKey(responseKeyBundle.identityKey.toByteArray(), 0)
        )
    }

    fun signCertificate(certificateSigningRequest: ByteArray?, encryptedPublicKey: ByteArray): SigningResponse {
        val stub = DecrypterGrpc.newBlockingStub(managedChannel)

        return stub.signPublicKey(SigningRequest.newBuilder()
            .setCertificationRequest(ByteString.copyFrom(certificateSigningRequest))
            .setPublicKey(ByteString.copyFrom(encryptedPublicKey))
            .build())
    }

}