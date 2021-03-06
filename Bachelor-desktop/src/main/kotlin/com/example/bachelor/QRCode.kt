package com.example.bachelor

import com.example.bachelor.signal.SessionGenerator
import com.google.zxing.BarcodeFormat
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.QRCodeWriter
import java.awt.FlowLayout
import java.io.ByteArrayOutputStream
import java.net.DatagramSocket
import java.net.InetAddress
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JLabel

class QRCode {

    private val WIDTH: Int = 400
    private val HEIGHT: Int = 400

    // In a real Application the ip Adress wouldnt be needed here. The QR-Code should just prove that the identity key matches
    fun generateQRCode() {
        val qrCodeWriter = QRCodeWriter()
        val bitMatrix = qrCodeWriter.encode(
                getIpAdress() + ":" + 50051 +
                        "|" + SessionGenerator.instance.getIdentityKey().fingerprint,
                BarcodeFormat.QR_CODE, WIDTH, HEIGHT)
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
            socket.close()
        }

        if (ipAddress.isBlank() || ipAddress == "0.0.0.0") {
            ipAddress = InetAddress.getLocalHost().hostAddress
        }

        return ipAddress
    }

}