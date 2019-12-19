package com.example.bachelor.api

import com.example.bachelor.DecryptRequest
import com.example.bachelor.DecryptResponse
import com.example.bachelor.DecrypterGrpc
import com.example.bachelor.Keybundle
import com.example.bachelor.signal.SessionGenerator
import com.google.protobuf.ByteString
import io.grpc.stub.StreamObserver
import org.whispersystems.libsignal.IdentityKey
import org.whispersystems.libsignal.ecc.ECPublicKey
import org.whispersystems.libsignal.state.PreKeyBundle
import java.io.ByteArrayInputStream
import java.io.ObjectInputStream

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
        SessionGenerator().desktopKeyBundle = PreKeyBundle(
                request?.registrationId!!,
                request.deviceId,
                request.preKeyId,
                deserializeECPreKey(request.preKeyPublic),
                request.signedPreKeyId,
                deserializeECPreKey(request.signedPreKeyPublic),
                request.signedPreKeySignature.toByteArray(),
                deserializeIdentityKey(request.identityKey)
        )

        val desktopPreKeyBundle = SessionGenerator().desktopKeyBundle

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

        println(request.toString())

        super.testGreet(request, responseObserver)
    }

    private fun deserializeECPreKey(byteString: ByteString): ECPublicKey {
        val bis = ByteArrayInputStream(byteString.toByteArray())
        val ois = ObjectInputStream(bis)
        return ois.readObject() as ECPublicKey
    }

    private fun deserializeIdentityKey(byteString: ByteString): IdentityKey {
        val bis = ByteArrayInputStream(byteString.toByteArray())
        val ois = ObjectInputStream(bis)
        return ois.readObject() as IdentityKey
    }
}