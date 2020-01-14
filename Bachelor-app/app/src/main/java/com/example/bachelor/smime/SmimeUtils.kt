package com.example.bachelor.smime

import android.security.keystore.KeyProperties
import org.bouncycastle.cms.CMSEnvelopedDataParser
import org.bouncycastle.cms.RecipientInformation
import org.bouncycastle.cms.jcajce.JceKeyTransEnvelopedRecipient
import java.security.SecureRandom
import java.security.Signature
import javax.crypto.Cipher


class SmimeUtils {

    val ENCRYPTION_ALG = KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1
    val SIGNATURE_ALG = KeyProperties.SIGNATURE_PADDING_RSA_PKCS1

//    fun decrypt(encryptedMessage: ByteArray): ByteArray {
//        val keypair = KeyTool().getKeyPair()
//        val cipher = Cipher.getInstance(ENCRYPTION_ALG)
//        cipher.init(Cipher.DECRYPT_MODE, keypair?.private)
//
//        return cipher.doFinal(encryptedMessage)
//    }

    fun decrypt(message: ByteArray): ByteArray? {
        val keypair = KeyTool().getKeyPair()
        val parser = CMSEnvelopedDataParser(message)

        val recInfo = getSingleRecipient(parser)
        val recipient = JceKeyTransEnvelopedRecipient(keypair?.private)

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


    fun encrypt(message: ByteArray): ByteArray {
        val keypair = KeyTool().getKeyPair()
        val cipher = Cipher.getInstance(ENCRYPTION_ALG)
        cipher.init(Cipher.ENCRYPT_MODE, keypair?.private)

        return cipher.doFinal(message)
    }

    fun sign(message: ByteArray): ByteArray {
        val keypair = KeyTool().getKeyPair()
        val signatureInstance = Signature.getInstance(SIGNATURE_ALG)
        signatureInstance.initSign(keypair?.private, SecureRandom())
        signatureInstance.update(message)
        return signatureInstance.sign()
    }

    // Would require other Public Keys
//    fun verify(publicKeyAlias: String, message: ByteArray): ByteArray {
//        val instance =
//            Signature.getInstance(SIGNATURE_ALG)
//        instance.initVerify(publicKey(alias))
//        instance.update(id.message.getBytes("UTF-8"))
//        return instance.verify(signature)
//    }
}