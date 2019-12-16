package com.example.bachelor.api

import com.example.bachelor.DecryptRequest
import com.example.bachelor.DecryptResponse
import com.example.bachelor.DecrypterGrpc
import io.grpc.stub.StreamObserver

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
}