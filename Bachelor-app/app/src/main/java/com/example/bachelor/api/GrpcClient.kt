package com.example.bachelor.api

import android.util.Log
import com.example.bachelor.Greet
import com.example.bachelor.GreeterGrpc
import io.grpc.ManagedChannelBuilder


class GrpcClient {

    fun startCommunication(address: String) {

        val desktop = ManagedChannelBuilder.forTarget(address)
            .usePlaintext() // Bad, but mails are already encrypted by signal
            .build()


        val stub = GreeterGrpc.newBlockingStub(desktop)

        val response = stub.greeter(Greet.newBuilder().setMessage("test from client").build())

        Log.w("api", response.message)
    }

}