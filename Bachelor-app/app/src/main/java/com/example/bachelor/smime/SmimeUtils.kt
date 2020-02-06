package com.example.bachelor.smime

import org.spongycastle.cert.jcajce.JcaCertStore
import org.spongycastle.cms.CMSEnvelopedDataParser
import org.spongycastle.cms.RecipientInformation
import org.spongycastle.cms.jcajce.JcaSimpleSignerInfoGeneratorBuilder
import org.spongycastle.cms.jcajce.JceKeyTransEnvelopedRecipient
import org.spongycastle.mail.smime.SMIMESignedGenerator
import org.spongycastle.util.encoders.Hex
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.security.cert.Certificate
import java.security.cert.X509Certificate
import javax.mail.internet.MimeBodyPart

class SmimeUtils {

    fun decrypt(message: ByteArray): ByteArray? {
        val keypair = KeyTool().getKeyPair()
        val parser = CMSEnvelopedDataParser(message)

        val recInfo = getSingleRecipient(parser)
        val recipient = JceKeyTransEnvelopedRecipient(keypair?.private)


        return recInfo?.getContent(recipient)
    }

    fun sign(message: ByteArray): ByteArray {
        val inputStream = ByteArrayInputStream(message)
        val messageBody = MimeBodyPart(inputStream)

        val keyPair = KeyTool().getKeyPair()
        val keyStore = KeyTool().getKeyStoreInstance()
        val signedCert = keyStore.getCertificate("signed") as X509Certificate

        val certificateList = ArrayList<Certificate>()
        certificateList.add(signedCert)

        val certs = JcaCertStore(certificateList)

        val gen = SMIMESignedGenerator()

        gen.addSignerInfoGenerator(
            JcaSimpleSignerInfoGeneratorBuilder()
                .build("SHA512withRSA", keyPair?.private, signedCert)
        )

        gen.addCertificates(certs)

        val outputStream = ByteArrayOutputStream()
        val multipart = gen.generate(messageBody)

        multipart.writeTo(outputStream)

        return outputStream.toByteArray().plus(("|" + multipart.contentType).toByteArray())
    }


    private fun getSingleRecipient(parser: CMSEnvelopedDataParser): RecipientInformation? {
        val recInfos: Collection<*> = parser.recipientInfos.recipients
        val recipientIterator = recInfos.iterator()
        if (!recipientIterator.hasNext()) {
            throw RuntimeException("Could not find recipient")
        }
        return recipientIterator.next() as RecipientInformation?
    }
}