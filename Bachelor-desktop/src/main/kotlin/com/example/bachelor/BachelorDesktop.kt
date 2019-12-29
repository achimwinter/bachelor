package com.example.bachelor

import com.example.bachelor.api.DecrypterImpl
import com.google.protobuf.ByteString
import io.grpc.Server
import io.grpc.ServerBuilder
import java.io.IOException
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger


class BachelorDesktop {

    private var server: Server? = null

    @Throws(IOException::class)
    private fun start() {

        QRCode().generateQRCode()
        sendMessages()

        val port = 50051
        server = ServerBuilder.forPort(port)
                .addService(DecrypterImpl())
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

    private fun sendMessages() {
        val timer = Timer()
        val task = object: TimerTask() {
            override fun run() = run {
                var counter = 0
                DecrypterImpl.observers.forEach{
                    it?.onNext(null)
                }
                DecrypterImpl.messages.add(DecryptRequest.newBuilder().setEncryptedMail(ByteString.copyFrom("Test Message from Server ${++counter}".toByteArray())).build())
                println("new message added.")
            }
        }
        timer.schedule(task, 0, 20000)
    }


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
