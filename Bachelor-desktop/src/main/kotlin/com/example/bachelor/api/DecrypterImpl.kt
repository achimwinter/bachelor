package com.example.bachelor.api

import com.example.bachelor.*
import com.example.bachelor.signal.SessionGenerator
import com.google.protobuf.ByteString
import io.grpc.stub.StreamObserver
import org.bouncycastle.pkcs.PKCS10CertificationRequest
import org.whispersystems.libsignal.IdentityKey
import org.whispersystems.libsignal.ecc.Curve
import org.whispersystems.libsignal.protocol.PreKeySignalMessage
import org.whispersystems.libsignal.protocol.SignalMessage
import org.whispersystems.libsignal.state.PreKeyBundle
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart
import javax.mail.util.ByteArrayDataSource


class DecrypterImpl : DecrypterGrpc.DecrypterImplBase() {

    companion object {
        var observer: StreamObserver<MailRequest>? = null
        val messages = LinkedList<MailRequest>()
        var mailSend = AtomicBoolean(false)
    }

    override fun subscribeMails(responseObserver: StreamObserver<MailRequest>?): StreamObserver<MailResponse?> {
        observer = responseObserver

        return object : StreamObserver<MailResponse?> {
            override fun onNext(value: MailResponse?) {
                if (!value?.mail?.isEmpty!!) {
                    if (SessionGenerator.instance.signalProtocolStore.containsSession(SessionGenerator.instance.MOBILE_ADDRESS)) {
                        val message = SignalMessage(value.mail?.toByteArray())
                        val plaintext = SessionGenerator.instance.sessionCipher.decrypt(message)
                        if (String(plaintext) != "Das ist eine Testmail") {
                            if (!mailSend.get()) {
                                sendMail(plaintext)
                                mailSend.set(true)
                            }

                        } else
                            println(String(plaintext))
                    } else {
                        val message = PreKeySignalMessage(value.mail?.toByteArray())
                        println(String(SessionGenerator.instance.decryptMessage(message)))
                    }
                }

                val message = messages.firstOrNull()
                if (message != null) {
                    val encryptedMessage = SessionGenerator.instance.sessionCipher.encrypt(message.mail.toByteArray())

                    val mailRequest = MailRequest.newBuilder()
                            .setMail(ByteString.copyFrom(encryptedMessage.serialize()))
                            .setMethod(message.method)
                            .build()
                    messages.remove(message)
                    observer?.onNext(mailRequest)
                }
            }


            override fun onError(t: Throwable?) {
                println("error on server")
                t?.printStackTrace()
            }

            override fun onCompleted() {
                println("Completed on Server")
            }

        }
    }

    override fun signPublicKey(request: SigningRequest?, responseObserver: StreamObserver<SigningResponse>?) {
        val preKeyPublicKey = PreKeySignalMessage(request?.publicKey?.toByteArray())
        val publicKeyBytes = SessionGenerator.instance.decryptMessage(preKeyPublicKey)
        val publicKey = KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(publicKeyBytes))
        val certificationRequest = request?.certificationRequest?.toByteArray()

        val signedCertificate = sign(PKCS10CertificationRequest(certificationRequest), publicKey)
        val encryptedMessage = SessionGenerator.sessionCipher.encrypt(signedCertificate?.encoded)

        responseObserver?.onNext(SigningResponse.newBuilder()
                .setX509Certificate(ByteString.copyFrom(encryptedMessage.serialize()))
                .build())
        responseObserver?.onCompleted()
    }

    override fun exchangeKeyBundle(request: Keybundle?, responseObserver: StreamObserver<Keybundle>?) {
        SessionGenerator.instance.mobileKeyBundle = PreKeyBundle(
                request?.registrationId!!,
                request.deviceId,
                request.preKeyId,
                Curve.decodePoint(request.preKeyPublic?.toByteArray(), 0),
                request.signedPreKeyId,
                Curve.decodePoint(request.signedPreKeyPublic.toByteArray(), 0),
                request.signedPreKeySignature.toByteArray(),
                IdentityKey(request.identityKey.toByteArray(), 0)
        )

        val desktopPreKeyBundle = SessionGenerator.instance.desktopKeyBundle

        val desktopKeyBundle = Keybundle.newBuilder()
                .setRegistrationId(desktopPreKeyBundle.registrationId)
                .setDeviceId(desktopPreKeyBundle.deviceId)
                .setPreKeyPublic(ByteString.copyFrom(desktopPreKeyBundle.preKey.serialize()))
                .setSignedPreKeyId(desktopPreKeyBundle.signedPreKeyId)
                .setSignedPreKeyPublic(ByteString.copyFrom(desktopPreKeyBundle.signedPreKey.serialize()))
                .setSignedPreKeySignature(ByteString.copyFrom(desktopPreKeyBundle.signedPreKeySignature))
                .setIdentityKey(ByteString.copyFrom(desktopPreKeyBundle.identityKey.publicKey.serialize()))
                .build()

        responseObserver?.onNext(desktopKeyBundle)
        responseObserver?.onCompleted()
    }

    private fun sendMail(mailMultiPart: ByteArray) {
        //        val to = "klaus.junker-schilling@fhws.de"
        val to = "acw@adorsys.de"
        val from = "bachelorhsm@gmail.com"
        val host = "smtp.gmail.com"


        val parts = String(mailMultiPart).split("|")
        val body = parts[0]

        val properties = System.getProperties()
        properties.setProperty("mail.smtp.host", host)
        properties.setProperty("mail.smtp/port", "465")
        properties.setProperty("mail.smtp.ssl.enable", "true")
        properties.setProperty("mail.smtp.auth", "true")
        val authenticator = object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(from, "bach4test")
            }
        }
        val session = Session.getInstance(properties, authenticator)

        val multipartMessage = MimeMultipart(ByteArrayDataSource(body, parts[1]))

        val message = MimeMessage(session)
        message.setHeader("Content-Type", "text/html; charset=\"UTF-8\"")
        message.setFrom(InternetAddress(from))
        message.setRecipient(Message.RecipientType.TO, InternetAddress(to))
        message.setSubject("signed message", "UTF-8")
        message.setContent(multipartMessage)

        println("sending mail")
        Transport.send(message)
    }
}