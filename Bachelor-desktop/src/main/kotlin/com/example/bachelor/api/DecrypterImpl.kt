package com.example.bachelor.api

import com.example.bachelor.*
import com.example.bachelor.signal.SessionGenerator
import com.google.protobuf.ByteString
import io.grpc.stub.StreamObserver
import org.bouncycastle.pkcs.PKCS10CertificationRequest
import org.whispersystems.libsignal.IdentityKey
import org.whispersystems.libsignal.ecc.Curve
import org.whispersystems.libsignal.protocol.PreKeySignalMessage
import org.whispersystems.libsignal.protocol.SignalMessage
import org.whispersystems.libsignal.state.PreKeyBundle
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import java.util.*


class DecrypterImpl : DecrypterGrpc.DecrypterImplBase() {

    companion object {
        var observer : StreamObserver<DecryptRequest>? = null
        val messages = LinkedList<DecryptRequest>()
    }

    override fun subscribeMails(responseObserver: StreamObserver<DecryptRequest>?): StreamObserver<DecryptResponse?> {
        observer = responseObserver

        return object : StreamObserver<DecryptResponse?> {
            override fun onNext(value: DecryptResponse?) {
                if (SessionGenerator.instance.signalProtocolStore.containsSession(SessionGenerator.instance.MOBILE_ADDRESS)) {
                    val message = SignalMessage(value?.unencryptedMail?.toByteArray())
                    println(String(SessionGenerator.instance.sessionCipher.decrypt(message)))
                } else {
                    val message = PreKeySignalMessage(value?.unencryptedMail?.toByteArray())
                    println(String(SessionGenerator.instance.decryptMessage(message)))
                }

                val message = messages.firstOrNull()
                if (message != null) {
                    val encryptedMessage = SessionGenerator.instance.sessionCipher.encrypt(message.encryptedMail.toByteArray())
                    val decryptRequest = DecryptRequest.newBuilder().setEncryptedMail(ByteString.copyFrom(encryptedMessage.serialize())).build()
                    messages.remove(message)
                    observer?.onNext(decryptRequest)
                }
            }

            override fun onError(t: Throwable?) {
                println("error on server")
                t?.printStackTrace()
            }

            override fun onCompleted() {
                println("Completed on Server")
            }

        }
    }

    override fun signPublicKey(request: SigningRequest?, responseObserver: StreamObserver<SigningResponse>?) {
        val preKeyPublicKey = PreKeySignalMessage(request?.publicKey?.toByteArray())
        val publicKeyBytes = SessionGenerator.instance.decryptMessage(preKeyPublicKey)
        val publicKey = KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(publicKeyBytes))
        val certificationRequest = request?.certificationRequest?.toByteArray()

        val signedCertificate = sign(PKCS10CertificationRequest(certificationRequest), publicKey)
        val encryptedMessage = SessionGenerator.sessionCipher.encrypt(signedCertificate?.encoded)

        responseObserver?.onNext(SigningResponse.newBuilder()
                .setX509Certificate(ByteString.copyFrom(encryptedMessage.serialize()))
                .build())
        responseObserver?.onCompleted()
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
}