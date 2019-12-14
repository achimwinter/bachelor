package com.example.bachelor

import io.grpc.Server
import io.grpc.ServerBuilder
import java.io.IOException
import java.util.logging.Level
import java.util.logging.Logger


class BachelorDesktop {

    private var server: Server? = null

    @Throws(IOException::class)
    private fun start() {

        QRCode().generateQRCode()

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

//    // This has to be done on the mobile phone
//    internal class DecryptMailImpl: DecrypterGrpc.DecrypterImplBase() {
//
//        override fun decryptMail(request: DecryptRequest?, responseObserver: StreamObserver<DecryptResponse>?) {
//            super.decryptMail(request, responseObserver)
//           // var reply = DecryptResponse.newBuilder().setUnencryptedMail(ByteString.copyFrom("Hello from Desktop".toByteArray()))
//
//        }
//
//    }

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
