package com.example.bachelor

import com.example.bachelor.api.DecrypterImpl
import com.google.protobuf.ByteString
import io.grpc.Server
import io.grpc.ServerBuilder
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.security.Security
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage


class BachelorDesktop {

    private var server: Server? = null

    @Throws(IOException::class)
    private fun start() {

        Security.addProvider(BouncyCastleProvider())
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

    //     In real use-case, clicks in a user interface would add messages.
//     This is just for simulation purposes
    private fun sendMessages() {
        val timer = Timer()
        val task = object : TimerTask() {
            override fun run() = run {
                DecrypterImpl.observer?.onNext(null)
                DecrypterImpl.messages.add(MailRequest.newBuilder()
                        .setMethod(MailRequest.Method.DECRYPT)
                        .setMail(
                                ByteString.copyFrom(
                                        Files.readAllBytes(File("/Users/achim/certs/testEncrypted.txt").toPath())
                                )).build()
                )
                DecrypterImpl.messages.add(MailRequest.newBuilder()
                        .setMethod(MailRequest.Method.SIGN)
                        .setMail(ByteString.copyFrom(createMail()))
                        .build()

                )
                println("new messages added.")
            }

        }
        timer.schedule(task, 0, 20000)
    }

    private fun createMail(): ByteArray{
        val message = MimeBodyPart()
        val outputStream = ByteArrayOutputStream()

        message.setText("Hallo Herr Junker-Schilling")

        message.writeTo(outputStream)
        return outputStream.toByteArray()
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
