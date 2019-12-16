package com.example.bachelor.api

import com.example.bachelor.DecryptRequest
import com.example.bachelor.DecryptResponse
import com.example.bachelor.DecrypterGrpc
import io.grpc.stub.StreamObserver

class DeryptServiceImpl : DecrypterGrpc.DecrypterImplBase() {
    override fun subscribeMails(responseObserver: StreamObserver<DecryptResponse?>?): StreamObserver<DecryptRequest?> {
        return object : StreamObserver<DecryptRequest?> {
            override fun onNext(value: DecryptRequest?) {
                println()
            }

            override fun onError(t: Throwable) {}
            override fun onCompleted() {}
        }
    }
}
