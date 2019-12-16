package com.example.bachelor.api

import android.util.Log
import com.example.bachelor.Greet
import com.example.bachelor.GreeterGrpc
import io.grpc.ManagedChannelBuilder


class GrpcClient {

    fun startCommunication(host: String, port: Int) {

        val desktop = ManagedChannelBuilder.forAddress(host, port)
            .usePlaintext() // Bad, but mails are already encrypted by signal
            .build()


        val communication = GreeterGrpc.newBlockingStub(desktop)

        val reponse = communication.greeter(Greet.newBuilder().setMessage("test client").build())

        Log.w("api", reponse.message)
    }

}