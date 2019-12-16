package com.example.bachelor

import com.google.zxing.BarcodeFormat
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.QRCodeWriter
import java.awt.FlowLayout
import java.io.ByteArrayOutputStream
import java.net.DatagramSocket
import java.net.Inet4Address
import java.net.InetAddress
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JLabel

class QRCode {

    private val WIDTH: Int = 400
    private val HEIGHT: Int = 400

    fun generateQRCode() {
        val qrCodeWriter = QRCodeWriter()
        val bitMatrix = qrCodeWriter.encode(getIpAdress() + ":" + 50051, BarcodeFormat.QR_CODE, WIDTH, HEIGHT)
        val pngOutputStream = ByteArrayOutputStream()
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream)
        displayQRCode(pngOutputStream.toByteArray())
    }

    private fun displayQRCode(imgArray: ByteArray) {
        val icon = ImageIcon(imgArray)
        val frame = JFrame()
        frame.layout = FlowLayout()
        frame.setSize(WIDTH, HEIGHT)
        val lbl = JLabel()
        lbl.icon = icon
        frame.add(lbl)
        frame.isVisible = true
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    }

    private fun getIpAdress(): String {
        var ipAddress = ""
        DatagramSocket().use { socket ->
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002)
            ipAddress = socket.localAddress.hostAddress
        }

        if (ipAddress.isBlank()) {
            ipAddress = Inet4Address.getLocalHost().hostAddress
        }

        return ipAddress
    }

}