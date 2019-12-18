package com.example.bachelor.api

import com.example.bachelor.*
import com.google.protobuf.ByteString
import io.grpc.ManagedChannelBuilder
import io.grpc.stub.StreamObserver
import org.whispersystems.libsignal.IdentityKey
import org.whispersystems.libsignal.ecc.ECPublicKey
import org.whispersystems.libsignal.state.PreKeyBundle
import java.io.ByteArrayInputStream
import java.io.ObjectInputStream


class GrpcClient {

     val managedChannel = ManagedChannelBuilder.forTarget(MainActivity.tvresult.toString()).usePlaintext().build()

    fun startCommunication() {

        DecryptResponse.newBuilder().clearUnencryptedMail()

        val decryptStub = DecrypterGrpc.newStub(managedChannel)

        val observer = decryptStub.subscribeMails(object : StreamObserver<DecryptResponse> {
            override fun onNext(value: DecryptResponse?) {
                println("onNext on Client")
            }

            override fun onError(t: Throwable?) {
                println("onError on Client")
            }

            override fun onCompleted() {
                println("onCompleted on Client")
            }

        })

        observer.onNext(DecryptRequest.getDefaultInstance())
        observer.onCompleted()
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
            deserializeECPreKey(responseKeyBundle.preKeyPublic),
            responseKeyBundle.signedPreKeyId,
            deserializeECPreKey(responseKeyBundle.signedPreKeyPublic),
            responseKeyBundle.signedPreKeySignature.toByteArray(),
            deserializeIdentityKey(responseKeyBundle.identityKey)
        )
    }

    //TODO :: Use generics maybe...

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