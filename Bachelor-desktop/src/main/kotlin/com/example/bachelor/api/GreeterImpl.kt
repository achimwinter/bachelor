package com.example.bachelor.api

import com.example.bachelor.Greet
import com.example.bachelor.GreeterGrpc
import io.grpc.stub.StreamObserver

class GreeterImpl : GreeterGrpc.GreeterImplBase() {

    override fun greeter(request: Greet?, responseObserver: StreamObserver<Greet>?) {
        println("Message from Client: " + request?.message)

        val reply = Greet.newBuilder().setMessage("Hello Client im your Server").build()

        responseObserver?.onNext(reply)
        responseObserver?.onCompleted()
    }
}