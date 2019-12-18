package com.example.bachelor.api

//import com.example.bachelor.DecryptResponse
import com.example.bachelor.DecryptRequest
import com.example.bachelor.DecryptResponse
import com.example.bachelor.DecrypterGrpc
import io.grpc.ManagedChannelBuilder
import io.grpc.stub.StreamObserver
import org.whispersystems.libsignal.state.PreKeyBundle


class GrpcClient(address: String) {

    val managedChannel = ManagedChannelBuilder.forTarget(address).usePlaintext().build()

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

        fun exchangeKeybundles(prekey: PreKeyBundle) {
//        val stub = DecrypterGrpc.newStub(managedChannel)


        }

}