/*
 * Copyright 2015 The gRPC Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.bachelor

import com.google.protobuf.ByteString
import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.stub.StreamObserver
import java.io.IOException
import java.net.DatagramSocket
import java.net.Inet4Address
import java.net.InetAddress
import java.util.logging.Level
import java.util.logging.Logger


class BachelorDesktop {

    private var server: Server? = null

    @Throws(IOException::class)
    private fun start() {

       val ipAddress = getIpAdress()




        /* The port on which the server should run */
        val port = 50051
        server = ServerBuilder.forPort(port)
                //.addService(GreeterImpl())
                .build()
                .start()
        logger.log(Level.INFO, "Server started, listening on {0}", port)
        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down")
                this@BachelorDesktop.stop()
                System.err.println("*** server shut down")
            }
        })
    }

    private fun stop() {
        server?.shutdown()
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    @Throws(InterruptedException::class)
    private fun blockUntilShutdown() {
        server?.awaitTermination()
    }

    fun getIpAdress(): String {
        var ipAddress = ""
        DatagramSocket().use { socket ->
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002)
            ipAddress = socket.localAddress.hostAddress
        }

        if (ipAddress.isEmpty()) {
            // TODO: Something which extracts the needed Adress
        }

        return ipAddress
    }

    // This has to be done on the mobile phone
    internal class DecryptMailImpl: DecrypterGrpc.DecrypterImplBase() {

        override fun decryptMail(request: DecryptRequest?, responseObserver: StreamObserver<DecryptResponse>?) {
            super.decryptMail(request, responseObserver)
           // var reply = DecryptResponse.newBuilder().setUnencryptedMail(ByteString.copyFrom("Hello from Desktop".toByteArray()))

        }

    }

    /*
    internal class DecryptMailImpl : GreeterGrpc.GreeterImplBase() {

        override fun decryptMail(request: DecryptRequest?, responseObserver: StreamObserver<DecryptResponse>?) {
            super.decryptMail(request, responseObserver)
        }(req: HelloRequest, responseObserver: StreamObserver<HelloReply>) {
            val reply = HelloReply.newBuilder().setMessage("Hello ${req.name}").build()
            System.out.println("Got a greeting")
            responseObserver.onNext(reply)
            responseObserver.onCompleted()
        }
    }
    */


    companion object {
        private val logger = Logger.getLogger(BachelorDesktop::class.java.name)

        /**
         * Main launches the server from the command line.
         */
        @Throws(IOException::class, InterruptedException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            val server = BachelorDesktop()
            server.start()
            server.blockUntilShutdown()
        }
    }
}
