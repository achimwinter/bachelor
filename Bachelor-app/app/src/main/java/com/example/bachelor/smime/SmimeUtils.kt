package com.example.bachelor.smime

import org.bouncycastle.cms.CMSEnvelopedDataParser
import org.bouncycastle.cms.RecipientInformation
import org.bouncycastle.cms.jcajce.JceKeyTransEnvelopedRecipient
import java.io.InputStream
import java.security.KeyStore
import java.security.PrivateKey


class SmimeUtils(ins: InputStream) {

    val keyStore = KeyStore.getInstance("PKCS12", "BC")
    val privateKey = getPrivateKey(ins)

    private fun getPrivateKey(ins: InputStream): PrivateKey {
        keyStore.load(ins, "adorsys".toCharArray())
        return (keyStore.getKey("acw", "adorsys".toCharArray())) as PrivateKey
    }

    fun decrypt(message: ByteArray): ByteArray? {
        val parser = CMSEnvelopedDataParser(message)

        val recInfo = getSingleRecipient(parser)
        val recipient = JceKeyTransEnvelopedRecipient(privateKey)

        return recInfo?.getContent(recipient)
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