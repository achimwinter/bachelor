package com.example.bachelor.api

import com.example.bachelor.*
import com.example.bachelor.signal.SessionGenerator
import com.example.bachelor.smime.SmimeUtils
import com.google.protobuf.ByteString
import io.grpc.ManagedChannelBuilder
import io.grpc.stub.StreamObserver
import org.whispersystems.libsignal.IdentityKey
import org.whispersystems.libsignal.InvalidMessageException
import org.whispersystems.libsignal.ecc.Curve
import org.whispersystems.libsignal.protocol.SignalMessage
import org.whispersystems.libsignal.state.PreKeyBundle


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
    val observer = decryptStub.subscribeMails(object : StreamObserver<DecryptResponse> {
        override fun onNext(value: DecryptResponse?) {
            decryptMail(value)
            counter++
        }

        override fun onError(t: Throwable?) {
            println("onError on Client")
        }

        override fun onCompleted() {
            println("onCompleted on Client")
        }
    })


    fun startCommunication() {
        observer.onNext(DecryptRequest.newBuilder().setEncryptedMail(ByteString.copyFrom("Subscribing to Mails".toByteArray())).build())
    }

    private fun decryptMail(value: DecryptResponse?) {
        if (!value?.unencryptedMail!!.isEmpty) {
            val signalMessage = SignalMessage(value.unencryptedMail?.toByteArray())

            val encryptedMail = SessionGenerator.sessionCipher.decrypt(signalMessage)

            val decryptedMail = SmimeUtils().decrypt(encryptedMail)

            observer.onNext(DecryptRequest.newBuilder().setEncryptedMail(ByteString.copyFrom(decryptedMail)).build())
        }

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

    fun testDecrypt(byteString: ByteString): DecryptResponse {
        val stub = DecrypterGrpc.newBlockingStub(managedChannel)

        return stub.testGreet(DecryptRequest.newBuilder().setEncryptedMail(byteString).build())
    }

}