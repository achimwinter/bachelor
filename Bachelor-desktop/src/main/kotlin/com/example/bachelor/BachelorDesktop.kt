package com.example.bachelor

import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.QRCodeWriter
import io.grpc.Server
import io.grpc.ServerBuilder
import java.awt.FlowLayout
import java.awt.Graphics
import java.awt.Image
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.net.DatagramSocket
import java.net.Inet4Address
import java.net.InetAddress
import java.util.logging.Level
import java.util.logging.Logger
import javax.imageio.ImageIO
import javax.swing.ImageIcon
import javax.swing.JComponent
import javax.swing.JFrame
import javax.swing.JLabel


class BachelorDesktop {

    private var server: Server? = null

    @Throws(IOException::class)
    private fun start() {

       val ipAddress = getIpAdress()

        displayQRCode(generateQRCode(ipAddress, 300, 300)!!)


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

    private fun displayQRCode(imgArray: ByteArray) {
        val icon = ImageIcon(imgArray)
        val frame = JFrame()
        frame.layout = FlowLayout()
        frame.setSize(200, 300)
        val lbl = JLabel()
        lbl.icon = icon
        frame.add(lbl)
        frame.isVisible = true
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    }

    fun getIpAdress(): String {
        var ipAddress = ""
        DatagramSocket().use { socket ->
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002)
            ipAddress = socket.localAddress.hostAddress
        }

        if (ipAddress.isEmpty()) {
            // TODO: Something which extracts the needed address from the multiple
        }

        return ipAddress
    }

    @Throws(WriterException::class, IOException::class)
    private fun generateQRCode(ipAddress: String, width: Int, height: Int): ByteArray? {
        val qrCodeWriter = QRCodeWriter()
        val bitMatrix = qrCodeWriter.encode(ipAddress, BarcodeFormat.QR_CODE, width, height)
        val pngOutputStream = ByteArrayOutputStream()
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream)
        return pngOutputStream.toByteArray()
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
