package com.example.bachelor.api

import android.util.Log
import com.example.bachelor.*
import io.grpc.ManagedChannelBuilder
import io.grpc.stub.StreamObserver


class GrpcClient {

    fun startCommunication(address: String) {

        val desktop = ManagedChannelBuilder.forTarget(address)
            .usePlaintext() // Bad, but mails are already encrypted by signal
            .build()

        val decryptStub = DecrypterGrpc.newBlockingStub(desktop)

//        val greeterStub = GreeterGrpc.newStub(desktop)


        val service = DecrypterGrpc.newStub(desktop)

        val observer = service.subscribeMails(object : StreamObserver<DecryptResponse> {
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

//        decryptStub.
//
//
//        val response = greeterStub.greeter(Greet.newBuilder().setMessage("test from client").build())
//
//        Log.w("api", response.message)
    }

//    private fun startDecrypting(stub: DecrypterGrpc.DecrypterBlockingStub) {
//        stub.
//    }

}