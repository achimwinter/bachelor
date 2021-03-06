package com.example.bachelor.api

import android.util.Log
import com.example.bachelor.*
import com.example.bachelor.signal.SessionGenerator
import com.example.bachelor.smime.SmimeUtils
import com.google.protobuf.ByteString
import io.grpc.ManagedChannelBuilder
import io.grpc.stub.StreamObserver
import org.whispersystems.libsignal.IdentityKey
import org.whispersystems.libsignal.ecc.Curve
import org.whispersystems.libsignal.protocol.SignalMessage
import org.whispersystems.libsignal.state.PreKeyBundle


class GrpcClient {

    companion object {
        val instance = GrpcClient()
    }

    private val managedChannel = ManagedChannelBuilder
        .forTarget(SessionGenerator.serverAddress)
        .usePlaintext()
        .build()


    val decryptStub = DecrypterGrpc.newStub(managedChannel)
    val observer = decryptStub.subscribeMails(object : StreamObserver<MailRequest> {
        override fun onNext(value: MailRequest?) {
            decryptMail(value)

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

        observer.onNext(
            MailResponse.newBuilder()
                .setMail(ByteString.copyFrom(handshakeMessage.serialize()))
                .build()
        )
    }

    private fun decryptMail(value: MailRequest?) {
        if (!value?.mail?.isEmpty!!) {
            val incomingSignalMessage = SignalMessage(value.mail?.toByteArray())

            val mail = SessionGenerator.sessionCipher.decrypt(incomingSignalMessage)

            val editetMail = when (value.method) {
                MailRequest.Method.SIGN -> SmimeUtils().sign(mail)
                MailRequest.Method.DECRYPT -> SmimeUtils().decrypt(mail)
                else -> {
                    Log.ERROR
                    ByteArray(-1)
                }
            }

            val outgoingSignalMessage = SessionGenerator.sessionCipher.encrypt(editetMail)

            observer.onNext(
                MailResponse.newBuilder()
                    .setMail(ByteString.copyFrom(outgoingSignalMessage.serialize()))
                    .build()
            )
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

    fun signCertificate(
        certificateSigningRequest: ByteArray?,
        encryptedPublicKey: ByteArray
    ): SigningResponse {
        val stub = DecrypterGrpc.newBlockingStub(managedChannel)

        return stub.signPublicKey(
            SigningRequest.newBuilder()
                .setCertificationRequest(ByteString.copyFrom(certificateSigningRequest))
                .setPublicKey(ByteString.copyFrom(encryptedPublicKey))
                .build()
        )
    }

}