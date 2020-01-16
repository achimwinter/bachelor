package com.example.bachelor.smime

import org.bouncycastle.cert.jcajce.JcaCertStore
import org.bouncycastle.cms.CMSEnvelopedDataParser
import org.bouncycastle.cms.CMSProcessableByteArray
import org.bouncycastle.cms.CMSSignedDataGenerator
import org.bouncycastle.cms.RecipientInformation
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder
import org.bouncycastle.cms.jcajce.JceKeyTransEnvelopedRecipient
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder
import java.security.cert.Certificate
import java.security.cert.X509Certificate


class SmimeUtils {

    fun decrypt(message: ByteArray): ByteArray? {
        val keypair = KeyTool().getKeyPair()
        val parser = CMSEnvelopedDataParser(message)

        val recInfo = getSingleRecipient(parser)
        val recipient = JceKeyTransEnvelopedRecipient(keypair?.private)

        return recInfo?.getContent(recipient)
    }

    fun sign(message: ByteArray): ByteArray {
        val keyPair = KeyTool().getKeyPair()
        val keyStore = KeyTool().getKeyStoreInstance()
        val signedCert = keyStore.getCertificate("signed") as X509Certificate

        val certificateList = ArrayList<Certificate>()
        val cmsMessage = CMSProcessableByteArray(message)

        certificateList.add(signedCert)

        val certs = JcaCertStore(certificateList)

        val gen = CMSSignedDataGenerator()
        val sha512Signer =
            JcaContentSignerBuilder("SHA512withRSA").build(keyPair?.private)

        gen.addSignerInfoGenerator(
            JcaSignerInfoGeneratorBuilder(
                JcaDigestCalculatorProviderBuilder().build()
            )
                .build(sha512Signer, signedCert)
        )

        gen.addCertificates(certs)

        return gen.generate(cmsMessage, true).encoded
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